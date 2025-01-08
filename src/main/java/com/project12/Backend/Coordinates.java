/*
 * This class represents geographical coordinates with latitude and longitude.
 */
package com.project12.Backend;

public class Coordinates {

    // Latitude of the coordinates
    public double lat;

    // Longitude of the coordinates
    public double lon;

    /*
     * Constructs a Coordinates object with the given latitude and longitude.
     *
     * @param lat The latitude.
     * @param lon The longitude.
     */
    public Coordinates(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /*
     * Sets new latitude and longitude for the coordinates.
     *
     * @param lat The new latitude.
     * @param lon The new longitude.
     */
    public void setCoordinates(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /*
     * Retrieves the latitude of the coordinates.
     *
     * @return The latitude.
     */
    public double getLat() {
        return this.lat;
    }

    /*
     * Retrieves the longitude of the coordinates.
     *
     * @return The longitude.
     */
    public double getLon() {
        return this.lon;
    }
}
