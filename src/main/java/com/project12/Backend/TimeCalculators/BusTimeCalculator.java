/*
 * This file defines a class for calculating time specifically for bus transport.
 */
package com.project12.Backend.TimeCalculators;

public class BusTimeCalculator extends AbstractCalculator{
    
    // Speed of a bus in kilometers per hour
    private static final double BUS_SPEED = 15;
    
    /*
     * Overrides the method from the abstract superclass to provide the bus speed.
     *
     * @return The speed of the bus in kilometers per hour.
     */
    @Override
    protected double getTransportSpeed() {
        return BUS_SPEED;
    }
}
