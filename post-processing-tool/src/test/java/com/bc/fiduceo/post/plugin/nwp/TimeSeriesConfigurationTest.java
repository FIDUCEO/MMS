/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.nwp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TimeSeriesConfigurationTest {

    private TimeSeriesConfiguration config;

    @Before
    public void setUp() {
        config = new TimeSeriesConfiguration();
    }

    @Test
    public void testSetGetAnalysisSteps() {
        config.setAnalysisSteps(19);
        assertEquals(19, config.getAnalysisSteps());
    }

    @Test
    public void testSetGetForecastSteps() {
        config.setForecastSteps(31);
        assertEquals(31, config.getForecastSteps());
    }

    @Test
    public void testSetGetTimeVariableName() {
        final String timeVariableName = "clock";

        config.setTimeVariableName(timeVariableName);
        assertEquals(timeVariableName, config.getTimeVariableName());
    }

    @Test
    public void testSetGetLongitudeVariableName() {
        final String variableName = "longi-tuhude";

        config.setLongitudeVariableName(variableName);
        assertEquals(variableName, config.getLongitudeVariableName());
    }

    @Test
    public void testSetGetLatitudeVariableName() {
        final String variableName = "lat-popat";

        config.setLatitudeVariableName(variableName);
        assertEquals(variableName, config.getLatitudeVariableName());
    }

    @Test
    public void testDefaultValues() {
        assertEquals(17, config.getAnalysisSteps());
        assertEquals(33, config.getForecastSteps());
        assertNull(config.getTimeVariableName());
        assertNull(config.getLongitudeVariableName());
        assertNull(config.getLatitudeVariableName());
    }
}
