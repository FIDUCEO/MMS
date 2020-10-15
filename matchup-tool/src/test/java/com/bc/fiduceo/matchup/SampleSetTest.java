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

import com.bc.fiduceo.core.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SampleSetTest {

    private SampleSet sampleSet;

    @Before
    public void setUp() {
        SampleSet.resetKey_UseThisMethodInUnitLevelTestsOnly();
        sampleSet = new SampleSet();
    }

    @After
    public void tearDown() {
        SampleSet.resetKey_UseThisMethodInUnitLevelTestsOnly();
    }

    @Test
    public void testSetPrimary() {
        final Sample sample = new Sample(6, 7, 8, 9, 10L);

        sampleSet.setPrimary(sample);
        final Sample result = sampleSet.getPrimary();
        assertNotNull(result);
        assertEquals(sample.getLat(), result.getLat(), 1e-8);
    }

    @Test
    public void testSetSecondary() {
        final Sample sample = new Sample(6, 7, 8, 9, 10L);

        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), sample);
        final Sample result = sampleSet.getSecondary(SampleSet.getOnlyOneSecondaryKey());
        assertNotNull(result);
        assertEquals(sample.getX(), result.getX());
    }

    @Test
    public void testOnlyOneSecondaryKey_defaultValue() {
        assertEquals("0000", SampleSet.getOnlyOneSecondaryKey());
    }

    @Test
    public void testOnlyOneSecondaryKey_setToOtherValue() {
        SampleSet.setOnlyOneSecondaryKey("otherKey");
        assertEquals("otherKey", SampleSet.getOnlyOneSecondaryKey());
    }

    @Test
    public void testOnlyOneSecondaryKey_ExceptionIfSetTwice() {
        try {
            SampleSet.setOnlyOneSecondaryKey("firstOtherKey");
            SampleSet.setOnlyOneSecondaryKey("secondeOtherKey");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Set the property \"ONLY_ONE_SECONDARY\" twice is not allowed.", expected.getMessage());
        }
    }

    @Test
    public void testClone() {
        final Sample primarySample = new Sample(6, 7, 8, 9, 10L);
        sampleSet.setPrimary(primarySample);

        final Sample hansSample = new Sample(7, 8, 9, 10, 11L);
        sampleSet.setSecondary("Hans", hansSample);

        final Sample heleneSample = new Sample(8, 9, 10, 11, 12L);
        sampleSet.setSecondary("Helene", heleneSample);

        final SampleSet clone = sampleSet.clone();
        final Sample primary = clone.getPrimary();
        assertEquals(6, primary.getX());
        assertEquals(7, primary.getY());
        assertEquals(8.0, primary.getLon(), 1e-8);
        assertEquals(9.0, primary.getLat(), 1e-8);
        assertEquals(10L, primary.getTime());

        final Sample hans = clone.getSecondary("Hans");
        assertEquals(7, hans.getX());
        assertEquals(8, hans.getY());
        assertEquals(9.0, hans.getLon(), 1e-8);
        assertEquals(10.0, hans.getLat(), 1e-8);
        assertEquals(11L, hans.getTime());

        final Sample helene = clone.getSecondary("Helene");
        assertEquals(8, helene.getX());
        assertEquals(9, helene.getY());
        assertEquals(10.0, helene.getLon(), 1e-8);
        assertEquals(11.0, helene.getLat(), 1e-8);
        assertEquals(12L, helene.getTime());
    }
}
