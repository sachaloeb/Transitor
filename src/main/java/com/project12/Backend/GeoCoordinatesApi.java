/*
 * Class to interact with the Postal Code API to retrieve latitude and longitude coordinates based on postcode input.
 */
package com.project12.Backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeoCoordinatesApi {

    /**
     * Retrieves latitude and longitude coordinates for a given postcode using the Postal Code API.
     *
     * @param inputPostcode The postcode for which coordinates are to be retrieved.
     * @return A JSON string containing the latitude and longitude coordinates.
     */
    public static String getCoordinates(String inputPostcode) {
        String responseMessage = "";
        try {
            String endpoint = "https://postal-code-api-phi.vercel.app/latlng/";
            URL url = new URL(endpoint + inputPostcode);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");

            int status = con.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                responseMessage = response.toString();
            } catch (IOException e) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    responseMessage = response.toString();
                }
            }
            // Check for rate limit exceeded error
            if (status == 429) {
                responseMessage = "{\"error\": \"Rate limit exceeded. Try again later.\"}";
            }
        } catch (Exception e) {
            responseMessage = "{\"error\": \"" + e.getMessage() + "\"}";
        }
        return responseMessage;
    }

    public static void main(String[] args) {
        // Example usage
        String s = getCoordinates("6229EN");
        System.out.println(s);
    }
}
