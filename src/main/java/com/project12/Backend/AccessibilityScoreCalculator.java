package com.project12.Backend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class AccessibilityScoreCalculator {

    private static final double DISTANCE_METERS = 900; // Distance in meters
    private static final BigDecimal MAX_SCORE = new BigDecimal("1824.5"); // Maximum possible score

    public String getTotalScore(String postCode) {
        String postcode = postCode;
        String result = "";
        try {
            Coordinates coordinates = getCoordinates(postcode);
            if (coordinates != null) {
                double lat = coordinates.getLat();
                double lon = coordinates.getLon();

                double[] boundingCoordinates = calculateBoundingCoordinates(lat, lon, DISTANCE_METERS);
                double minLat = boundingCoordinates[0];
                double maxLat = boundingCoordinates[1];
                double minLon = boundingCoordinates[2];
                double maxLon = boundingCoordinates[3];

                Map<String, BigDecimal> categoryScores = calculateCategoryScores(minLat, maxLat, minLon, maxLon);
                BigDecimal totalScore = BigDecimal.ZERO;

                DecimalFormat df = new DecimalFormat("#.##");

                // Display individual category scores
                for (Map.Entry<String, BigDecimal> entry : categoryScores.entrySet()) {
                    String category = capitalizeFirstLetter(entry.getKey());
                    BigDecimal score = entry.getValue();
                    totalScore = totalScore.add(score);
                    System.out.println(category + " Score: " + df.format(score));
                    result += category + " : " + df.format(score) + "\n";
                }

                // Display total accessibility score
                System.out.println("Total Accessibility Score: " + df.format(totalScore) + " out of 1824.5");
                result += "Total Accessibility Score: " + df.format(totalScore) + " out of 1824.5" + "\n";

                // Calculate and display the final accessibility score as a percentage of 1824.5
                BigDecimal percentageScore = totalScore.divide(MAX_SCORE, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                System.out.println("Percentage Accessibility Score : " + df.format(percentageScore) + "%");
                result += "Percentage Accessibility Score : " + df.format(percentageScore) + "%";
            } else {
                System.out.println("The following postcode is not in our database: " + postcode);
            }
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

    private static Map<String, BigDecimal> calculateCategoryScores(double minLat, double maxLat, double minLon, double maxLon) throws SQLException {
        Map<String, BigDecimal> categoryScores = new HashMap<>();

        String amenityQuery = "SELECT amenity, COUNT(*) as count FROM Amenity WHERE latitude BETWEEN ? AND ? AND longitude BETWEEN ? AND ? GROUP BY amenity";
        try (Connection conn = DBConnectionSingleton.getDbConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(amenityQuery)) {
            stmt.setDouble(1, minLat);
            stmt.setDouble(2, maxLat);
            stmt.setDouble(3, minLon);
            stmt.setDouble(4, maxLon);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String amenityType = rs.getString("amenity");
                int count = rs.getInt("count");
                BigDecimal weight = BigDecimal.valueOf(getAmenityWeight(amenityType));
                BigDecimal weightedCount = weight.multiply(BigDecimal.valueOf(count));
                categoryScores.put(amenityType, weightedCount);
            }
        }

        String shopQuery = "SELECT shop, COUNT(*) as count FROM Shops WHERE latitude BETWEEN ? AND ? AND longitude BETWEEN ? AND ? GROUP BY shop";
        try (Connection conn = DBConnectionSingleton.getDbConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(shopQuery)) {
            stmt.setDouble(1, minLat);
            stmt.setDouble(2, maxLat);
            stmt.setDouble(3, minLon);
            stmt.setDouble(4, maxLon);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String shopType = rs.getString("shop");
                int count = rs.getInt("count");
                BigDecimal weight = BigDecimal.valueOf(getShopWeight(shopType));
                BigDecimal weightedCount = weight.multiply(BigDecimal.valueOf(count));
                categoryScores.put(shopType, categoryScores.getOrDefault(shopType, BigDecimal.ZERO).add(weightedCount));
            }
        }

        return categoryScores;
    }

    private static double getAmenityWeight(String amenityType) {
        switch (amenityType) {
            case "miscellaneous":
                return 0.5;
            case "parking":
                return 0.7;
            case "utilities":
                return 1.0;
            case "waste":
                return 0.8;
            case "food":
                return 1.2;
            case "shopping":
                return 1.1;
            case "religion":
                return 0.6;
            case "entertainment":
                return 1.3;
            case "finance":
                return 0.9;
            case "health":
                return 1.5;
            case "government":
                return 1.0;
            case "education":
                return 1.4;
            default:
                return 0.0;
        }
    }

    private static double getShopWeight(String shopType) {
        switch (shopType) {
            case "18+":
                return 0.8;
            case "pastries":
                return 1.0;
            case "shopping":
                return 1.1;
            case "groceries":
                return 1.2;
            case "self-care":
                return 1.0;
            case "utilities":
                return 1.0;
            case "miscellaneous":
                return 0.5;
            case "food":
                return 1.2;
            case "entertainment":
                return 1.3;
            case "health":
                return 1.5;
            default:
                return 0.0;
        }
    }

    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
