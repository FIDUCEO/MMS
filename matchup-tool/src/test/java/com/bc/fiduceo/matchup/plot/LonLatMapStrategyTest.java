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

package com.bc.fiduceo.matchup.plot;

import com.bc.fiduceo.core.SamplingPoint;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LonLatMapStrategyTest {

    private LonLatMapStrategy strategy;

    @Before
    public void setUp() {
        strategy = new LonLatMapStrategy(800, 400);
    }

    @Test
    public void testMap() {
        SamplingPoint samplingPoint = new SamplingPoint(-34.4, 12.66, 77745387L);
        PlotPoint point = strategy.map(samplingPoint);
        assertNotNull(point);
        assertEquals(323, point.getX());
        assertEquals(171, point.getY());

        samplingPoint = new SamplingPoint(66.886, -42.66, 77745387L);
        point = strategy.map(samplingPoint);
        assertNotNull(point);
        assertEquals(548, point.getX());
        assertEquals(294, point.getY());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testInterfaceImplemented() {
         assertTrue(strategy instanceof MapStrategy);
    }
}
