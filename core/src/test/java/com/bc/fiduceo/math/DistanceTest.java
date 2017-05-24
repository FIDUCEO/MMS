package com.bc.fiduceo.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DistanceTest {

    @Test
    public void testComputeDistanceInKm() {
        double distance = Distance.computeSphericalDistanceKm(0.0, 10.0, 0.0, 10.0);
        assertEquals(0.0, distance, 1e-8);

        distance = Distance.computeSphericalDistanceKm(0.5, 10.0, 0.0, 10.0);
        assertEquals(54.75278190659841, distance, 1e-8);

        distance = Distance.computeSphericalDistanceKm(0.0, 10.0, 0.5, 10.0);
        assertEquals(54.75278190659841, distance, 1e-8);

        distance = Distance.computeSphericalDistanceKm(22.0, 10.0, 22.5, 9.8);
        assertEquals(59.11236878616983, distance, 1e-8);
    }
}
