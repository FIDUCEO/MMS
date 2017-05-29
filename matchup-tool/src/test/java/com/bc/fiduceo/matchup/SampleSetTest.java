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

import static org.junit.Assert.*;

import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.*;

public class SampleSetTest {

    private SampleSet sampleSet;

    @Before
    public void setUp() throws Exception {
        SampleSet.resetKey_UseThisMethodInUnitLevelTestsOnly();
        sampleSet = new SampleSet();
    }

    @After
    public void tearDown() throws Exception {
        SampleSet.resetKey_UseThisMethodInUnitLevelTestsOnly();
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

        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), sample);
        final Sample result = sampleSet.getSecondary(SampleSet.getOnlyOneSecondaryKey());
        assertNotNull(result);
        assertEquals(sample.x, result.x);
    }

    @Test
    public void testOnlyOneSecondaryKey_defaultValue() throws Exception {
        assertEquals("0000", SampleSet.getOnlyOneSecondaryKey());
    }

    @Test
    public void testOnlyOneSecondaryKey_setToOtherValue() throws Exception {
        SampleSet.setOnlyOneSecondaryKey("otherKey");
        assertEquals("otherKey", SampleSet.getOnlyOneSecondaryKey());
    }

    @Test
    public void testOnlyOneSecondaryKey_ExceptionIfSetTwice() throws Exception {
        try {
            SampleSet.setOnlyOneSecondaryKey("firstOtherKey");
            SampleSet.setOnlyOneSecondaryKey("secondeOtherKey");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Set the property \"ONLY_ONE_SECONDARY\" twice is not allowed.", expected.getMessage());
        } catch (Exception e) {
            fail("RuntimeException expected");
        }
    }
}
