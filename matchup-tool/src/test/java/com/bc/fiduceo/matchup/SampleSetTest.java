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

package com.bc.fiduceo.matchup;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SampleSetTest {

    private SampleSet sampleSet;

    @Before
    public void setUp() throws Exception {
        sampleSet = new SampleSet();
    }

    @Test
    public void testSetPrimary() {
        final Sample sample = new Sample(6, 7, 8, 9, 10L);

        sampleSet.setPrimary(sample);
        final Sample result = sampleSet.getPrimary();
        assertNotNull(result);
        assertEquals(sample.lat, result.lat, 1e-8);
    }

    @Test
    public void testSetSecondary() {
        final Sample sample = new Sample(6, 7, 8, 9, 10L);

        sampleSet.setSecondary(SampleSet.ONLY_ONE_SECONDARY, sample);
        final Sample result = sampleSet.getSecondary(SampleSet.ONLY_ONE_SECONDARY);
        assertNotNull(result);
        assertEquals(sample.x, result.x);
    }
}
