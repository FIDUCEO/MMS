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

package com.bc.fiduceo.db;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QueryParameterTest {

    private QueryParameter parameter;

    @Before
    public void setUp() {
        parameter = new QueryParameter();
    }

    @Test
    public void testConstruction() {
        assertNull(parameter.getStartTime());
        assertNull(parameter.getStopTime());
        assertNull(parameter.getSensorName());
        assertNull(parameter.getVersion());
        assertNull(parameter.getPath());

        assertEquals(-1, parameter.getPageSize());
        assertEquals(-1, parameter.getOffset());
    }

    @Test
    public void testSetGetStartTime() {
        final Date startTime = new Date(100000000L);

        parameter.setStartTime(startTime);
        assertEquals(startTime.getTime(), parameter.getStartTime().getTime());
    }

    @Test
    public void testSetGetStopTime() {
        final Date stopTime = new Date(100200000L);

        parameter.setStopTime(stopTime);
        assertEquals(stopTime.getTime(), parameter.getStopTime().getTime());
    }

    @Test
    public void testSetGetSensorName() {
        final String sensor_name_1 = "what a name";
        final String sensor_name_2 = "sen-sor";

        parameter.setSensorName(sensor_name_1);
        assertEquals(sensor_name_1, parameter.getSensorName());

        parameter.setSensorName(sensor_name_2);
        assertEquals(sensor_name_2, parameter.getSensorName());
    }

    @Test
    public void testSetGetVersion() {
        final String version_1 = "v1.0";
        final String version_2 = "v2.0";

        parameter.setVersion(version_1);
        assertEquals(version_1, parameter.getVersion());

        parameter.setVersion(version_2);
        assertEquals(version_2, parameter.getVersion());
    }

    @Test
    public void testSetGetPath() {
        final String path_1 = "v1.0";
        final String path_2 = "v2.0";

        parameter.setPath(path_1);
        assertEquals(path_1, parameter.getPath());

        parameter.setPath(path_2);
        assertEquals(path_2, parameter.getPath());
    }

    @Test
    public void testSetGetPageSize() {
        final int size_1= 58;
        final int size_2= 108;

        parameter.setPageSize(size_1);
        assertEquals(size_1, parameter.getPageSize());

        parameter.setPageSize(size_2);
        assertEquals(size_2, parameter.getPageSize());
    }

    @Test
    public void testSetGetOffset() {
        final int offset_1= 34567;
        final int offset_2= 100099746;

        parameter.setOffset(offset_1);
        assertEquals(offset_1, parameter.getOffset());

        parameter.setOffset(offset_2);
        assertEquals(offset_2, parameter.getOffset());
    }
}
