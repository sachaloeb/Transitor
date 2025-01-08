/*
 * Class to calculate the distance between two geographic coordinates using the Haversine formula.
 */
package com.project12.Backend;

public class DistanceCalculator {

    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Convert degrees to radians
    private static double toRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }

    /**
     * Calculate the distance between two coordinates using the Haversine formula.
     *
     * @param coordinatesStart The starting coordinates.
     * @param coordinatesEnd   The ending coordinates.
     * @return The distance between the two coordinates in kilometers.
     */
    public static double calculateDistance(Coordinates coordinatesStart, Coordinates coordinatesEnd) {
        // Convert coordinates to radians
        double[] radStart = new double[]{toRadians(coordinatesStart.getLat()), toRadians(coordinatesStart.getLon())};
        double[] radEnd = new double[]{toRadians(coordinatesEnd.getLat()), toRadians(coordinatesEnd.getLon())};

        // Haversine formula
        double deltaLat = radEnd[0] - radStart[0];
        double deltaLon = radEnd[1] - radStart[1];
        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(radStart[0]) * Math.cos(radEnd[0])
                * Math.pow(Math.sin(deltaLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        return distance;
    }
    public static double getEarthRadiusKm() {
        return EARTH_RADIUS_KM;
    }
}
