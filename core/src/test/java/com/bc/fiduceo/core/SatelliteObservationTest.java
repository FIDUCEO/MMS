/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

package com.bc.fiduceo.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SatelliteObservationTest {

    private SatelliteObservation observation;

    @Before
    public void setUp() {
        observation = new SatelliteObservation();
    }

    @Test
    public void testConstructor() {
        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals(-1, observation.getId());
    }

    @Test
    public void testSetGetVersion() {
        final String version_1 = "1.0";
        final String version_2 = "2.0";

        observation.setVersion(version_1);
        assertEquals(version_1, observation.getVersion());

        observation.setVersion(version_2);
        assertEquals(version_2, observation.getVersion());
    }
}
