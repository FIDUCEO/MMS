/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.matchup.cloud;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AMSUBCloudScreeningTest {

    private AMSUBCloudScreening screening;

    @Before
    public void setUp() {
        screening = new AMSUBCloudScreening();
    }

    @Test
    public void testRun_DeltaT() {
        final double[] ch1 = {262.8, 274.3};
        final double[] ch2 = {263.3, 272.9};
        final double[] satZenith = {0.0, 0.0};

        final boolean[] cloudFlag = screening.run(ch1, ch2, satZenith);
        assertEquals(2, cloudFlag.length);
        assertFalse(cloudFlag[0]);
        assertTrue(cloudFlag[1]);
    }

    @Test
    public void testRun_ch1Threshold() {
        final double[] ch1 = {262.8, 240.0};
        final double[] ch2 = {10000.0, 10000.0};    // make sure that ch1 always is smaller than ch2
        final double[] satZenith = {0.58, 6.05};

        final boolean[] cloudFlag = screening.run(ch1, ch2, satZenith);
        assertEquals(2, cloudFlag.length);
        assertFalse(cloudFlag[0]);
        assertTrue(cloudFlag[1]);
    }

    @Test
    public void testGetThreshold() {
        assertEquals(240.0, AMSUBCloudScreening.getThreshold(0.0), 1e-8);
        assertEquals(240.1, AMSUBCloudScreening.getThreshold(0.2), 1e-8);
        assertEquals(240.1, AMSUBCloudScreening.getThreshold(7.15), 1e-8);
        assertEquals(239.9, AMSUBCloudScreening.getThreshold(8.25), 1e-8);
        assertEquals(239.9, AMSUBCloudScreening.getThreshold(9.35), 1e-8);
        assertEquals(239.8, AMSUBCloudScreening.getThreshold(10.45), 1e-8);
        assertEquals(239.8, AMSUBCloudScreening.getThreshold(11.55), 1e-8);
        assertEquals(239.7, AMSUBCloudScreening.getThreshold(12.65), 1e-8);
        assertEquals(239.7, AMSUBCloudScreening.getThreshold(13.75), 1e-8);
        assertEquals(239.6, AMSUBCloudScreening.getThreshold(14.85), 1e-8);
        assertEquals(239.6, AMSUBCloudScreening.getThreshold(15.95), 1e-8);
        assertEquals(239.5, AMSUBCloudScreening.getThreshold(17.05), 1e-8);
        assertEquals(239.4, AMSUBCloudScreening.getThreshold(18.15), 1e-8);
        assertEquals(239.3, AMSUBCloudScreening.getThreshold(19.25), 1e-8);
        assertEquals(239.2, AMSUBCloudScreening.getThreshold(20.35), 1e-8);
        assertEquals(239.2, AMSUBCloudScreening.getThreshold(21.45), 1e-8);
        assertEquals(239.1, AMSUBCloudScreening.getThreshold(22.55), 1e-8);
        assertEquals(239.0, AMSUBCloudScreening.getThreshold(23.65), 1e-8);
        assertEquals(238.8, AMSUBCloudScreening.getThreshold(24.75), 1e-8);
        assertEquals(238.7, AMSUBCloudScreening.getThreshold(25.85), 1e-8);
        assertEquals(238.6, AMSUBCloudScreening.getThreshold(26.95), 1e-8);
        assertEquals(238.5, AMSUBCloudScreening.getThreshold(28.05), 1e-8);
        assertEquals(238.3, AMSUBCloudScreening.getThreshold(29.15), 1e-8);
        assertEquals(238.2, AMSUBCloudScreening.getThreshold(30.25), 1e-8);
        assertEquals(238.0, AMSUBCloudScreening.getThreshold(31.35), 1e-8);
        assertEquals(237.8, AMSUBCloudScreening.getThreshold(32.45), 1e-8);
        assertEquals(237.6, AMSUBCloudScreening.getThreshold(33.55), 1e-8);
        assertEquals(237.4, AMSUBCloudScreening.getThreshold(34.65), 1e-8);
        assertEquals(237.2, AMSUBCloudScreening.getThreshold(35.75), 1e-8);
        assertEquals(237.0, AMSUBCloudScreening.getThreshold(36.85), 1e-8);
        assertEquals(236.7, AMSUBCloudScreening.getThreshold(37.95), 1e-8);
        assertEquals(236.6, AMSUBCloudScreening.getThreshold(39.05), 1e-8);
        assertEquals(236.4, AMSUBCloudScreening.getThreshold(40.15), 1e-8);
        assertEquals(236.1, AMSUBCloudScreening.getThreshold(41.25), 1e-8);
        assertEquals(235.8, AMSUBCloudScreening.getThreshold(42.35), 1e-8);
        assertEquals(235.5, AMSUBCloudScreening.getThreshold(43.45), 1e-8);
        assertEquals(235.2, AMSUBCloudScreening.getThreshold(44.55), 1e-8);
        assertEquals(234.9, AMSUBCloudScreening.getThreshold(45.65), 1e-8);
        assertEquals(234.4, AMSUBCloudScreening.getThreshold(46.75), 1e-8);
        assertEquals(233.9, AMSUBCloudScreening.getThreshold(47.85), 1e-8);
        assertEquals(233.3, AMSUBCloudScreening.getThreshold(48.95), 1e-8);
    }

    @Test
    public void testGetThreshold_outOfRange() {
        try {
            AMSUBCloudScreening.getThreshold(52.6);
            fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException expected){
        }
    }
}
