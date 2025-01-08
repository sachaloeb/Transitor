/*
 * This class is responsible for reading data from the database and extracting coordinates.
 * It provides methods to search for coordinates based on a given postcode and to extract latitude and longitude from a string.
 */
package com.project12.Backend;

import com.project12.Frontend.ErrorMessage;

import java.util.List;

public class DataReader {

    // Reference to the database connection
    private final DBConnectionSingleton dbConnection = DBConnectionSingleton.getDbConnection();

    /*
     * Searches for coordinates based on the given postcode.
     * If the postcode is found, returns an array containing latitude and longitude.
     * If the postcode is not found, shows an error message and returns an empty array.
     *
     * @param postCode The postcode to search for.
     * @return An array containing latitude and longitude if found, otherwise an empty array.
     */
    public String[] search(String postCode) {
        // Query the database for coordinates associated with the given postcode
        List<String> coords = dbConnection.readFromDB(
                "SELECT DISTINCT Latitude, Longitude FROM postalcodelatlong WHERE Postcode = '" + postCode + "';"
        );

        // If no coordinates are found for the postcode, show an error message
        if (coords.isEmpty()) {
            ErrorMessage.window("Error!", "Postcode", "The postcode is not in our database! Please try a different one.");
            return new String[]{"", ""};
        }

        // If multiple instances of the same postcode are found, show an error message
        if (coords.size() == 1) {
            String[] coordinates = coords.get(0).split(" ");
            return coordinates;
        }

        ErrorMessage.window("Error!", "Postcode", "Multiple instances of the same postcode found!");
        return new String[]{};
    }

    /*
     * Extracts latitude and longitude from a string in the format "{latitude: value, longitude: value}".
     *
     * @param coordinates The string containing latitude and longitude.
     * @return An array containing latitude and longitude extracted from the string.
     */
    public static String[] extractCoordinates(String coordinates) {
        // Remove unnecessary characters from the string
        coordinates = coordinates.substring(1, coordinates.length() - 1);

        // Split the string into key-value pairs
        String[] keyValuePairs = coordinates.split(",");

        // Initialize latitude and longitude strings
        String latitude = "";
        String longitude = "";

        // Iterate through key-value pairs to extract latitude and longitude
        for (String pair : keyValuePairs) {
            // Split each pair into key and value
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                // Remove quotes from key and value
                key = key.replaceAll("\"", "").trim();
                value = value.replaceAll("\"", "").trim();

                // Check if the key is latitude or longitude
                if (key.equals("latitude")) {
                    latitude = value;
                } else if (key.equals("longitude")) {
                    longitude = value;
                }
            }
        }

        // Return latitude and longitude in a string array
        return new String[]{latitude, longitude};
    }
}
