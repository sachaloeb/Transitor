/*
 * This class provides functionality for calculating time based on the distance and selected vehicle.
 */
package com.project12.Backend.TimeCalculators;

public class TimeCalculator {

    // The calculator instance to compute time based on the selected vehicle
    private final AbstractCalculator calculator;

    // The distance for which time is being calculated
    private final double distance;

    /*
     * Constructs a TimeCalculator object with the specified distance and vehicle type.
     *
     * @param distance The distance for which time needs to be calculated.
     * @param vehicle The type of vehicle for which time is being calculated (e.g., "Walking", "Bus").
     */
    public TimeCalculator(double distance, String vehicle) {
        calculator = switch (vehicle) {
            case "Walk" -> new WalkTimeCalculator();
            case "Bus" -> new BusTimeCalculator();
            default -> new WalkTimeCalculator();
        };

        this.distance = distance;
    }

    /*
     * Calculates the time taken for the given distance and vehicle type.
     *
     * @return A formatted string representing the time taken in hours, minutes, and seconds.
     */
    public String timeCalculatorString() {
        double timedouble = this.calculator.calculateTime(distance);
        String timeString = timedoubleToString(timedouble);
        //System.out.println("Time taken for " + distance + " kilometers: " + timeString);
        return timeString;
    }

    /*
     * Calculates the time taken for the given distance and vehicle type.
     *
     * @return The time taken in seconds as a double value.
     */
    public double timeCalculatorDouble() {
        double timedouble = this.calculator.calculateTime(distance);
        return timedouble;
    }

    /*
     * Converts a double value representing time in seconds to a formatted string (HH:MM:SS).
     *
     * @param timedouble The time in seconds as a double value.
     * @return A formatted string representing the time in hours, minutes, and seconds.
     */
    private static String timedoubleToString(double timedouble){
        int hours = (int) timedouble / 3600;
        int remainingSeconds = (int) timedouble % 3600;
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
