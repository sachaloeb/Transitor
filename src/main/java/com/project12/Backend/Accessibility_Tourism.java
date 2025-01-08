package com.project12.Backend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Accessibility_Tourism {

    private static final double DISTANCE_METERS = 900; // Distance in meters
    private static final BigDecimal MAX_TOURISM_SCORE = new BigDecimal("490.8"); // Maximum possible tourism score

    public String getScore(String postCode) {
        List<String> postcodes = Arrays.asList(postCode); // example postcodes
        String result = "";
        try {
            Map<String, BigDecimal> categoryScores = calculateTotalAccessibilityScore(postcodes);
            BigDecimal totalTourismScore = BigDecimal.ZERO;
            DecimalFormat df = new DecimalFormat("#.##");

            // Display individual category scores
            for (Map.Entry<String, BigDecimal> entry : categoryScores.entrySet()) {
                String category = capitalizeFirstLetter(entry.getKey());
                BigDecimal score = entry.getValue();
                totalTourismScore = totalTourismScore.add(score);
                System.out.println(category + " Score: " + df.format(score));
                result += category + " : " + df.format(score) + "\n";
            }

            // Display total tourism score
            System.out.println("Total Tourism Score: " + df.format(totalTourismScore) + " out of 490.8");
            result += "Total Tourism Score: " + df.format(totalTourismScore) + " out of 490.8" + "\n";

            // Calculate and display the tourism score as a percentage of 490.8
            BigDecimal percentageTourismScore = totalTourismScore.divide(MAX_TOURISM_SCORE, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            System.out.println("Percentage Tourism Score: " + df.format(percentageTourismScore) + "%");
            result += "Percentage Tourism Score: " + df.format(percentageTourismScore) + "%";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static Coordinates getCoordinates(String postcode) throws SQLException {
        String query = "SELECT Latitude, Longitude FROM postalcodelatlong WHERE postcode = ?";
        try (Connection conn = DBConnectionSingleton.getDbConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, postcode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double latitude = rs.getDouble("Latitude");
                    double longitude = rs.getDouble("Longitude");
                    return new Coordinates(latitude, longitude);
                }
            }
        }
        return null;
    }

    private static double[] calculateBoundingCoordinates(double lat, double lon, double distanceMeters) {
        double distanceKm = distanceMeters / 1000.0;

        Coordinates start = new Coordinates(lat, lon);

        // Calculate north, south, east, and west coordinates
        Coordinates north = moveCoordinate(start, distanceKm, 0);
        Coordinates south = moveCoordinate(start, -distanceKm, 0);
        Coordinates east = moveCoordinate(start, 0, distanceKm);
        Coordinates west = moveCoordinate(start, 0, -distanceKm);

        // Calculate min and max latitudes and longitudes
        double minLat = Math.min(north.getLat(), south.getLat());
        double maxLat = Math.max(north.getLat(), south.getLat());
        double minLon = Math.min(east.getLon(), west.getLon());
        double maxLon = Math.max(east.getLon(), west.getLon());

        return new double[]{minLat, maxLat, minLon, maxLon};
    }

    private static Coordinates moveCoordinate(Coordinates start, double dLat, double dLon) {
        double earthRadiusKm = DistanceCalculator.getEarthRadiusKm(); // Use the method from DistanceCalculator
        double newLat = start.getLat() + (dLat / earthRadiusKm) * (180 / Math.PI);
        double newLon = start.getLon() + (dLon / earthRadiusKm) * (180 / Math.PI) / Math.cos(start.getLat() * Math.PI / 180);
        return new Coordinates(newLat, newLon);
    }

    private static Map<String, BigDecimal> calculateAccessibilityScore(double minLat, double maxLat, double minLon, double maxLon) throws SQLException {
        String query = "SELECT category, COUNT(*) as count FROM tourism_new WHERE latitude BETWEEN ? AND ? AND longitude BETWEEN ? AND ? GROUP BY category";
        try (Connection conn = DBConnectionSingleton.getDbConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, minLat);
            stmt.setDouble(2, maxLat);
            stmt.setDouble(3, minLon);
            stmt.setDouble(4, maxLon);
            ResultSet rs = stmt.executeQuery();

            Map<String, BigDecimal> categoryScores = new HashMap<>();
            while (rs.next()) {
                String category = rs.getString("category");
                int count = rs.getInt("count");
                BigDecimal weight = BigDecimal.valueOf(getCategoryWeight(category));
                BigDecimal weightedCount = weight.multiply(BigDecimal.valueOf(count));
                categoryScores.put(category, weightedCount);
            }
            return categoryScores;
        }
    }

    private static double getCategoryWeight(String category) {
        switch (category) {
            case "Information and Scenic Spots":
                return 2.0;
            case "Accommodation":
                return 1.8;
            case "Art and Culture":
                return 1.9;
            case "Attractions":
                return 1.7;
            default:
                return 0.5;
        }
    }

    private static Map<String, BigDecimal> calculateTotalAccessibilityScore(List<String> postcodes) throws SQLException {
        Map<String, BigDecimal> totalCategoryScores = new HashMap<>();
        for (String postcode : postcodes) {
            Coordinates coordinates = getCoordinates(postcode);
            if (coordinates != null) {
                double lat = coordinates.getLat();
                double lon = coordinates.getLon();

                double[] boundingCoordinates = calculateBoundingCoordinates(lat, lon, DISTANCE_METERS);
                double minLat = boundingCoordinates[0];
                double maxLat = boundingCoordinates[1];
                double minLon = boundingCoordinates[2];
                double maxLon = boundingCoordinates[3];

                Map<String, BigDecimal> categoryScores = calculateAccessibilityScore(minLat, maxLat, minLon, maxLon);
                for (Map.Entry<String, BigDecimal> entry : categoryScores.entrySet()) {
                    String category = entry.getKey();
                    BigDecimal score = entry.getValue();
                    totalCategoryScores.put(category, totalCategoryScores.getOrDefault(category, BigDecimal.ZERO).add(score));
                }
            }
        }
        return totalCategoryScores;
    }

    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
