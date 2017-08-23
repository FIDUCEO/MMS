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

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.core.Sample;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SwathOrderedSampleComparatorTest {

    private SwathOrderedSamplesComparator comparator;

    @Before
    public void setUp() {
        comparator = new SwathOrderedSamplesComparator();
    }

    @Test
    public void testCompare_same() {
        final Sample sample_1 = new Sample(12, 34, Double.NaN, Double.NaN, Integer.MAX_VALUE);
        final Sample sample_2 = new Sample(12, 34, Double.NaN, Double.NaN, Integer.MAX_VALUE);

        assertEquals(0, comparator.compare(sample_1, sample_2));
    }

    @Test
    public void testCompare_scanLine() {
        final Sample sample_1 = new Sample(12, 34, Double.NaN, Double.NaN, Integer.MAX_VALUE);
        final Sample sample_2 = new Sample(12, 36, Double.NaN, Double.NaN, Integer.MAX_VALUE);

        assertEquals(-1, comparator.compare(sample_1, sample_2));
        assertEquals(1, comparator.compare(sample_2, sample_1));
    }

    @Test
    public void testCompare_sameScanLine() {
        final Sample sample_1 = new Sample(12, 34, Double.NaN, Double.NaN, Integer.MAX_VALUE);
        final Sample sample_2 = new Sample(16, 34, Double.NaN, Double.NaN, Integer.MAX_VALUE);

        assertEquals(-1, comparator.compare(sample_1, sample_2));
        assertEquals(1, comparator.compare(sample_2, sample_1));
    }
}
