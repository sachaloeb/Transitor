/*
 * This class extends the AbstractCalculator class to calculate time for walking.
 */
package com.project12.Backend.TimeCalculators;

public class WalkTimeCalculator extends AbstractCalculator {

    // The walking speed in kilometers per hour
    private static final double WALK_SPEED = 5;

    /*
     * Retrieves the walking speed.
     *
     * @return The walking speed in kilometers per hour.
     */
    @Override
    protected double getTransportSpeed() {
        return WALK_SPEED;
    }
}
