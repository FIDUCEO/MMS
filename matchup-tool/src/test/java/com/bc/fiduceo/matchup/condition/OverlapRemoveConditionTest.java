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

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.esa.snap.core.util.StopWatch;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OverlapRemoveConditionTest {

    private OverlapRemoveCondition primaryCondition;
    private OverlapRemoveCondition secondaryCondition;
    private MatchupSet matchupSet;
    private ConditionEngineContext context;

    @Before
    public void setUp() {
        primaryCondition = new OverlapRemoveCondition(true);
        secondaryCondition = new OverlapRemoveCondition(false);
        matchupSet = new MatchupSet();

        context = new ConditionEngineContext();
        context.setPrimarySize(new Dimension("bla", 3, 5));
        context.setSecondarySize(new Dimension("bla", 7, 9));
    }

    @Test
    public void testRemove_emptyMatchupSet() {
        primaryCondition.apply(matchupSet, context);
        assertEquals(0, matchupSet.getNumObservations());

        secondaryCondition.apply(matchupSet, new ConditionEngineContext());
        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testRemove_oneMatchup_primary() {
        addSampleSet(108, 346, 3567, 12056);

        primaryCondition.apply(matchupSet, context);

        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testRemove_oneMatchup_secondary() {
        addSampleSet(22, 105, 1944, 22055);

        secondaryCondition.apply(matchupSet, context);

        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testRemove_twoMatchups_primary_nonOverlapping() {
        addSampleSet(108, 346, 3567, 12056);
        addSampleSet(208, 446, 3567, 12056);

        primaryCondition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
    }

    @Test
    public void testRemove_twoMatchups_secondary_nonOverlapping() {
        addSampleSet(108, 346, 3567, 12056);
        addSampleSet(108, 346, 3577, 12106);

        secondaryCondition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
    }

    @Test
    public void testRemove_twoMatchups_primary_overlapping() {
        addSampleSet(108, 346, 3567, 12056);
        addSampleSet(110, 344, 3567, 12056);

        primaryCondition.apply(matchupSet, context);

        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testRemove_manyMatchups_secondary_mixed() {
        addSampleSet(108, 346, 3567, 12056); // <- keep
        addSampleSet(108, 346, 3565, 12056);
        addSampleSet(108, 346, 4000, 12106); // <- keep
        addSampleSet(108, 346, 5000, 12106); // <- keep
        addSampleSet(108, 346, 3568, 12056);
        addSampleSet(108, 346, 5002, 12106);
        addSampleSet(108, 346, 4003, 12106);

        secondaryCondition.apply(matchupSet, context);

        assertEquals(3, matchupSet.getNumObservations());
    }

    @Test
    @Ignore
    public void testRemove_performanceTest_mimick_AATSR() {
        int numSamples = 1000000;
        for (int i = 0; i < numSamples; i++) {
            final int primaryX = (int) (512 * Math.random());
            final int primaryY = (int) (40000 * Math.random());
            final int secondaryX = (int) (512 * Math.random());
            final int secondaryY = (int) (40000 * Math.random());

            addSampleSet(primaryX, primaryY, secondaryX, secondaryY);
        }

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        primaryCondition.apply(matchupSet, context);

        stopWatch.stop();
        System.out.println("time = " + stopWatch.getTimeDiffString());
    }

    @Test
    public void testGetDimension_primary() {
        context.setPrimarySize(new Dimension("prime", 7, 9));

        final Dimension dimension = primaryCondition.getDimension(context);
        assertNotNull(dimension);
        assertEquals(7, dimension.getNx());
        assertEquals(9, dimension.getNy());
    }

    @Test
    public void testGetDimension_secondary() {
        context.setSecondarySize(new Dimension("sec", 5, 3));

        final Dimension dimension = secondaryCondition.getDimension(context);
        assertNotNull(dimension);
        assertEquals(5, dimension.getNx());
        assertEquals(3, dimension.getNy());
    }

    @Test
    public void testGetReferenceFormElement_primary() throws JDOMException, IOException {
        final String XML = "<overlap-remove>" +
                "    <reference>PRIMARY</reference>" +
                "</overlap-remove>";
        final Element element = TestUtil.createDomElement(XML);

        boolean primary = secondaryCondition.getReferenceFromElement(element);
        assertTrue(primary);
    }

    @Test
    public void testGetReferenceFormElement_secondary() throws JDOMException, IOException {
        final String XML = "<overlap-remove>" +
                "    <reference>SECONDARY</reference>" +
                "</overlap-remove>";
        final Element element = TestUtil.createDomElement(XML);

        boolean primary = secondaryCondition.getReferenceFromElement(element);
        assertFalse(primary);
    }

    @Test
    public void testGetReferenceFormElement_invalid() throws JDOMException, IOException {
        final String XML = "<overlap-remove>" +
                "    <reference>rubbish</reference>" +
                "</overlap-remove>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            secondaryCondition.getReferenceFromElement(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    private void addSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(primaryX, primaryY, -22.5, 18.98, 111027));
        sampleSet.setSecondary(new Sample(secondaryX, secondaryY, -23.5, 19.98, 121027));
        sampleSets.add(sampleSet);
    }
}
