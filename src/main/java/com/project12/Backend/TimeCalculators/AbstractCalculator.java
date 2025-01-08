/*
 * This file defines an abstract class for calculating time based on distance and transport speed.
 */
package com.project12.Backend.TimeCalculators;

public abstract class AbstractCalculator {

    /*
     * Abstract method to be implemented by subclasses to get the transport speed.
     */
    protected abstract double getTransportSpeed();

    /*
     * Method to calculate time based on distance and transport speed.
     *
     * @param distance The distance to be traveled.
     * @return The calculated time in seconds.
     */
    public double calculateTime(double distance) {
        double time = 0;
        double speed = getTransportSpeed();
        try {
            time = (double) Math.round(3600 * (distance / speed));
        } catch (ArithmeticException e) {
            System.out.println(e);
        }
        return time;
    }
}
