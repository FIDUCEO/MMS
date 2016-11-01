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

package com.bc.fiduceo.ingest;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PathContextTest {

    @Test
    public void testCreateAndGet_defaults() {
        final PathContext pathContext = new PathContext("coolSensor", "version_1", 1998, 6, 22);

        PathElement pathElement = new PathElement("SENSOR", null);
        String pathSegment = pathContext.getSegment(pathElement);
        assertEquals("coolSensor", pathSegment);

        pathElement = new PathElement("VERSION", null);
        pathSegment = pathContext.getSegment(pathElement);
        assertEquals("version_1", pathSegment);

        pathElement = new PathElement("YEAR", null);
        pathSegment = pathContext.getSegment(pathElement);
        assertEquals("1998", pathSegment);

        pathElement = new PathElement("MONTH", null);
        pathSegment = pathContext.getSegment(pathElement);
        assertEquals("06", pathSegment);

        pathElement = new PathElement("DAY", null);
        pathSegment = pathContext.getSegment(pathElement);
        assertEquals("22", pathSegment);
    }

    @Test
    public void testCreateAndGet_specialSegments() {
        final PathContext pathContext = new PathContext("coolSensor", "version_1", 1998, 6, 22);

        final PathElement pathElement = new PathElement("PREFIX", "subPath");
        final String pathSegment = pathContext.getSegment(pathElement);
        assertEquals("subPath", pathSegment);
    }
}
