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

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NonOverlappingCollectorTest {

    private final String SEC_NAME = "secName";

    private NonOverlappingCollector primaryCollector;
    private NonOverlappingCollector secondaryCollector;
    private NonOverlappingCollector namedSecondaryCollector;

    @Before
    public void setUp() {
        primaryCollector = new NonOverlappingCollector(3, 5, true);
        secondaryCollector = new NonOverlappingCollector(5, 7, false);
        namedSecondaryCollector = new NonOverlappingCollector(5, 7, false, SEC_NAME);
    }

    @Test
    public void testCreateAndGet_emptySet() {
        List<SampleSet> nonOverlappingList = primaryCollector.get();
        assertNotNull(nonOverlappingList);
        assertEquals(0, nonOverlappingList.size());

        nonOverlappingList = secondaryCollector.get();
        assertNotNull(nonOverlappingList);
        assertEquals(0, nonOverlappingList.size());
    }

    @Test
    public void testAddAndGet_oneSample_primary() {
        final SampleSet sampleSet = createSampleSet(23, 197, 1, 1);

        primaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = primaryCollector.get();
        assertEquals(1, sampleSets.size());
    }

    @Test
    public void testAddAndGet_oneSample_secondary() {
        final SampleSet sampleSet = createSampleSet(2, 2, 107, 1812);

        secondaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = secondaryCollector.get();
        assertEquals(1, sampleSets.size());
    }

    @Test
    public void testAddAndGet_oneSample_secondaryWithName() {
        final SampleSet sampleSet = createSampleSet(2, 2, 107, 1812, SEC_NAME);

        namedSecondaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = namedSecondaryCollector.get();
        assertEquals(1, sampleSets.size());
    }

    @Test
    public void testAddAndGet_twoSamples_nonOverlapping_primary() {
        SampleSet sampleSet = createSampleSet(23, 197, 1, 1);
        primaryCollector.add(sampleSet);

        sampleSet = createSampleSet(223, 1197, 1, 1);
        primaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = primaryCollector.get();
        assertEquals(2, sampleSets.size());
    }

    @Test
    public void testAddAndGet_twoSamples_nonOverlapping_secondary() {
        SampleSet sampleSet = createSampleSet(0, 0, 107, 1812);
        secondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(0, 0, 407, 2212);
        secondaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = secondaryCollector.get();
        assertEquals(2, sampleSets.size());
    }


    @Test
    public void testAddAndGet_twoSamples_nonOverlapping_namedSecondary() {
        SampleSet sampleSet = createSampleSet(0, 0, 107, 1812, SEC_NAME);
        namedSecondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(0, 0, 407, 2212, SEC_NAME);
        namedSecondaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = namedSecondaryCollector.get();
        assertEquals(2, sampleSets.size());
    }

    @Test
    public void testAddAndGet_twoSamples_overlapping_primary() {
        SampleSet sampleSet = createSampleSet(23, 197, 1000, 1);
        primaryCollector.add(sampleSet);

        sampleSet = createSampleSet(22, 199, 1, 10000);
        primaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = primaryCollector.get();
        assertEquals(1, sampleSets.size());
        assertEquals(23, sampleSets.get(0).getPrimary().x);
        assertEquals(197, sampleSets.get(0).getPrimary().y);
    }

    @Test
    public void testAddAndGet_twoSamples_overlapping_secondary() {
        SampleSet sampleSet = createSampleSet(0, 10000, 107, 1812);
        secondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(10000, 0, 105, 1814);
        secondaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = secondaryCollector.get();
        assertEquals(1, sampleSets.size());
        assertEquals(107, sampleSets.get(0).getSecondary(SampleSet.getOnlyOneSecondaryKey()).x);
        assertEquals(1812, sampleSets.get(0).getSecondary(SampleSet.getOnlyOneSecondaryKey()).y);
    }

    @Test
    public void testAddAndGet_twoSamples_overlapping_namedSecondary() {
        SampleSet sampleSet = createSampleSet(0, 10000, 107, 1812, SEC_NAME);
        namedSecondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(10000, 0, 105, 1814, SEC_NAME);
        namedSecondaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = namedSecondaryCollector.get();
        assertEquals(1, sampleSets.size());
        assertEquals(107, sampleSets.get(0).getSecondary(SEC_NAME).x);
        assertEquals(1812, sampleSets.get(0).getSecondary(SEC_NAME).y);
    }

    @Test
    public void testAddAndGet_manySamples_mixed_primary() {
        SampleSet sampleSet = createSampleSet(23, 197, 1000, 1);  // <- keep
        primaryCollector.add(sampleSet);

        sampleSet = createSampleSet(22, 199, 1, 10000);
        primaryCollector.add(sampleSet);

        sampleSet = createSampleSet(140, 2807, 10000, 1);   // <- keep
        primaryCollector.add(sampleSet);

        sampleSet = createSampleSet(24, 196, 1000, 10000);
        primaryCollector.add(sampleSet);

        sampleSet = createSampleSet(139, 2808, 10000, 10000);
        primaryCollector.add(sampleSet);

        sampleSet = createSampleSet(488, 1943, 10000, 10000);  // <- keep
        primaryCollector.add(sampleSet);


        final List<SampleSet> sampleSets = primaryCollector.get();
        assertEquals(3, sampleSets.size());

        assertEquals(23, sampleSets.get(0).getPrimary().x);
        assertEquals(197, sampleSets.get(0).getPrimary().y);

        assertEquals(140, sampleSets.get(1).getPrimary().x);
        assertEquals(2807, sampleSets.get(1).getPrimary().y);

        assertEquals(488, sampleSets.get(2).getPrimary().x);
        assertEquals(1943, sampleSets.get(2).getPrimary().y);
    }

    @Test
    public void testAddAndGet_manySamples_mixed_secondary() {
        SampleSet sampleSet = createSampleSet(0, 10000, 107, 1812); // <- keep
        secondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(1000, 10000, 1107, 11812);   // <- keep
        secondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(10000, 0, 105, 1814);
        secondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(10000, 10100, 107, 1813);
        secondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(10000, 1110, 106, 1812);
        secondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(1000, 10000, 1105, 11813);
        secondaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = secondaryCollector.get();
        assertEquals(2, sampleSets.size());

        assertEquals(107, sampleSets.get(0).getSecondary(SampleSet.getOnlyOneSecondaryKey()).x);
        assertEquals(1812, sampleSets.get(0).getSecondary(SampleSet.getOnlyOneSecondaryKey()).y);

        assertEquals(1107, sampleSets.get(1).getSecondary(SampleSet.getOnlyOneSecondaryKey()).x);
        assertEquals(11812, sampleSets.get(1).getSecondary(SampleSet.getOnlyOneSecondaryKey()).y);
    }

    @Test
    public void testAddAndGet_manySamples_mixed_namedSecondary() {
        SampleSet sampleSet = createSampleSet(0, 10000, 107, 1812, SEC_NAME); // <- keep
        namedSecondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(1000, 10000, 1107, 11812, SEC_NAME);   // <- keep
        namedSecondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(10000, 0, 105, 1814, SEC_NAME);
        namedSecondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(10000, 10100, 107, 1813, SEC_NAME);
        namedSecondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(10000, 1110, 106, 1812, SEC_NAME);
        namedSecondaryCollector.add(sampleSet);

        sampleSet = createSampleSet(1000, 10000, 1105, 11813, SEC_NAME);
        namedSecondaryCollector.add(sampleSet);

        final List<SampleSet> sampleSets = namedSecondaryCollector.get();
        assertEquals(2, sampleSets.size());

        assertEquals(107, sampleSets.get(0).getSecondary(SEC_NAME).x);
        assertEquals(1812, sampleSets.get(0).getSecondary(SEC_NAME).y);

        assertEquals(1107, sampleSets.get(1).getSecondary(SEC_NAME).x);
        assertEquals(11812, sampleSets.get(1).getSecondary(SEC_NAME).y);
    }

    @Test
    public void testGetSample_primary() {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(1, 2, 3, 4, 5));

        final Sample sample = primaryCollector.getSample(sampleSet);
        assertNotNull(sample);
        assertEquals(1, sample.x);
        assertEquals(2, sample.y);
    }

    @Test
    public void testGetSample_secondary() {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), new Sample(2, 3, 4, 5, 6));

        final Sample sample = secondaryCollector.getSample(sampleSet);
        assertNotNull(sample);
        assertEquals(2, sample.x);
        assertEquals(3, sample.y);
    }

    @Test
    public void testGetSample_namedSecondary() {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setSecondary(SEC_NAME, new Sample(2, 3, 4, 5, 6));

        final Sample sample = namedSecondaryCollector.getSample(sampleSet);
        assertNotNull(sample);
        assertEquals(2, sample.x);
        assertEquals(3, sample.y);
    }

    @Test
    public void testAreOverlapping() {
        final Sample reference = createSample(100, 2000);

        // check with primary - we only care about width and height in this method. W=3, H=5 tb 2016-11-23

        // check along x-axis
        Sample sample = createSample(97, 2000);
        assertFalse(primaryCollector.areOverlapping(reference, sample));
        sample = createSample(98, 2000);
        assertTrue(primaryCollector.areOverlapping(reference, sample));
        sample = createSample(99, 2000);
        assertTrue(primaryCollector.areOverlapping(reference, sample));
        sample = createSample(100, 2000);
        assertTrue(primaryCollector.areOverlapping(reference, sample));
        sample = createSample(101, 2000);
        assertTrue(primaryCollector.areOverlapping(reference, sample));
        sample = createSample(102, 2000);
        assertTrue(primaryCollector.areOverlapping(reference, sample));
        sample = createSample(103, 2000);
        assertFalse(primaryCollector.areOverlapping(reference, sample));

        // check along x-axis
        createSample(100, 1995);
        assertFalse(primaryCollector.areOverlapping(reference, sample));
        sample = createSample(100, 1996);
        assertTrue(primaryCollector.areOverlapping(reference, sample));
        sample = createSample(100, 2000);
        assertTrue(primaryCollector.areOverlapping(reference, sample));
        sample = createSample(100, 2004);
        assertTrue(primaryCollector.areOverlapping(reference, sample));
        sample = createSample(100, 2005);
        assertFalse(primaryCollector.areOverlapping(reference, sample));
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        return createSampleSet(primaryX, primaryY, secondaryX, secondaryY, SampleSet.getOnlyOneSecondaryKey());
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY, String secondaryName) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(createSample(primaryX, primaryY));
        sampleSet.setSecondary(secondaryName, createSample(secondaryX, secondaryY));

        return sampleSet;
    }

    private Sample createSample(int x, int y) {
        return new Sample(x, y, -22.5, 18.98, 111027);
    }
}
