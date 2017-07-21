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
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TimeDeltaConditionPluginTest {

    private TimeDeltaConditionPlugin plugin;

    @Before
    public void setUp() {
        plugin = new TimeDeltaConditionPlugin();
    }

    @Test
    public void testGetConditionName() {
        assertEquals("time-delta", plugin.getConditionName());
    }

    @Test
    public void testCreateCondition() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <time-delta-seconds>" +
                "    198" +
                "  </time-delta-seconds>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
        assertThat(condition.getClass(), is(equalTo(TimeDeltaCondition.class)));
        final TimeDeltaCondition tdCondition = (TimeDeltaCondition) condition;
        assertEquals(198000, tdCondition.getMaxTimeDeltaInMillis());
        assertArrayEquals(new String[]{SampleSet.getOnlyOneSecondaryKey()}, tdCondition.getSecondarySensorNames());
    }

    @Test
    public void testCreateCondition_withOptionalSecondarySensorName() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <time-delta-seconds names=\"secSenName\">" +
                "    298" +
                "  </time-delta-seconds>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
        assertThat(condition.getClass(), is(equalTo(TimeDeltaCondition.class)));
        final TimeDeltaCondition tdCondition = (TimeDeltaCondition) condition;
        assertNotNull(condition);
        assertEquals(298000, tdCondition.getMaxTimeDeltaInMillis());
        assertArrayEquals(new String[]{"secSenName"}, tdCondition.getSecondarySensorNames());
    }

    @Test
    public void testCreateCondition_withOneSecondarySensorNameAndPrimaryCheckFalse_notAllowed() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <time-delta-seconds names=\"secSenName\" primaryCheck=\"false\">" +
                "    298" +
                "  </time-delta-seconds>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            // The default of TimeDeltaCondition.secondaryCheck is false.
            // So the deactivation of primaryCheck without activation of secondaryCheck makes no sense.
            plugin.createCondition(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("At least primaryCheck or secondaryCheck mut be true.", expected.getMessage());
        }
    }

    @Test
    public void testCreateCondition_withOneSecondarySensorNameAndSecondaryCheckTrue_notAllowed() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <time-delta-seconds names=\"secSenName,  \" primaryCheck=\"false\" secondaryCheck=\"true\">" +
                "    298" +
                "  </time-delta-seconds>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.createCondition(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("If secondaryCheck is true at least two secondary sensor names are needed.", expected.getMessage());
            assertThat(expected.getClass(), is(equalTo(RuntimeException.class)));
        }
    }

    @Test
    public void testCreateCondition_multipleConditions() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <time-delta-seconds names=\"name1\">" +
                "    3" +
                "  </time-delta-seconds>" +
                "  <time-delta-seconds names=\"name2\">" +
                "    4" +
                "  </time-delta-seconds>" +
                "  <time-delta-seconds names=\"name3\">" +
                "    5" +
                "  </time-delta-seconds>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
        assertThat(condition, is(not(instanceOf(TimeDeltaCondition.class))));

        final SampleSet expected = createValidSampleSet();
        final MatchupSet matchupSet = new MatchupSet();
        matchupSet.getSampleSets().add(createSampleSetWithInvalidSample("name1"));
        matchupSet.getSampleSets().add(expected);
        matchupSet.getSampleSets().add(createSampleSetWithInvalidSample("name2"));
        matchupSet.getSampleSets().add(createSampleSetWithInvalidSample("name3"));

        assertEquals(4, matchupSet.getNumObservations());

        condition.apply(matchupSet, null);

        assertEquals(1, matchupSet.getNumObservations());
        assertSame(expected, matchupSet.getSampleSets().get(0));
    }

    @Test
    public void testCreateCondition_primaryCondition_combinedWithSecondaryCondition() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <time-delta-seconds names=\"a, b, c\"" +
                "         primaryCheck=\"true\"" +
                "         secondaryCheck=\"false\">" +
                "    3" +
                "  </time-delta-seconds>" +
                "  <time-delta-seconds names=\"b, c\"" +
                "         primaryCheck=\"false\"" +
                "         secondaryCheck=\"true\">" +
                "    4" +
                "  </time-delta-seconds>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
        assertThat(condition, is(not(instanceOf(TimeDeltaCondition.class))));

        SampleSet validSampleSet = createSampleSet(10000).secondary("a", 13000).secondary("b", 7000).secondary("c", 10500).build();
        SampleSet time_b_c_is_invalid = createSampleSet(10000).secondary("a", 13000).secondary("b", 7000).secondary("c", 12000).build();
        SampleSet time_primary_a_is_invalid = createSampleSet(10000).secondary("a", 13001).secondary("b", 7000).secondary("c", 10500).build();
        SampleSet time_primary_b_is_invalid = createSampleSet(10000).secondary("a", 13000).secondary("b", 6999).secondary("c", 10500).build();
        SampleSet time_primary_c_is_invalid = createSampleSet(10000).secondary("a", 13000).secondary("b", 7000).secondary("c", 6999).build();
        final MatchupSet matchupSet = new MatchupSet();
        matchupSet.getSampleSets().add(validSampleSet);
        matchupSet.getSampleSets().add(time_b_c_is_invalid);
        matchupSet.getSampleSets().add(time_primary_a_is_invalid);
        matchupSet.getSampleSets().add(time_primary_b_is_invalid);
        matchupSet.getSampleSets().add(time_primary_c_is_invalid);

        assertEquals(5, matchupSet.getNumObservations());

        condition.apply(matchupSet, null);

        assertEquals(1, matchupSet.getNumObservations());
        assertSame(validSampleSet, matchupSet.getSampleSets().get(0));
    }

    @Test
    public void testCreateCondition_sameCondition_threeSensorNames() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <time-delta-seconds names=\"name1, name2, name3\">" +
                "    5" +
                "  </time-delta-seconds>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
        assertThat(condition.getClass(), is(equalTo(TimeDeltaCondition.class)));
        final TimeDeltaCondition tdCondition = (TimeDeltaCondition) condition;
        assertNotNull(condition);
        assertEquals(5000, tdCondition.getMaxTimeDeltaInMillis());
        assertArrayEquals(new String[]{"name1", "name2", "name3"}, tdCondition.getSecondarySensorNames());

        final SampleSet expected = createValidSampleSet();
        final MatchupSet matchupSet = new MatchupSet();
        matchupSet.getSampleSets().add(createSampleSetWithInvalidSample("name1"));
        matchupSet.getSampleSets().add(expected);
        matchupSet.getSampleSets().add(createSampleSetWithInvalidSample("name2"));
        matchupSet.getSampleSets().add(createSampleSetWithInvalidSample("name3"));

        assertEquals(4, matchupSet.getNumObservations());

        condition.apply(matchupSet, null);

        assertEquals(1, matchupSet.getNumObservations());
        assertSame(expected, matchupSet.getSampleSets().get(0));
    }

    @Test
    public void testCreateCondition_invalidTag() throws JDOMException, IOException {
        final String XML = "<time-difference>" +
                "  <time-delta-seconds>" +
                "    198" +
                "  </time-delta-seconds>" +
                "</time-difference>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.createCondition(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Illegal XML Element. Tagname 'time-delta' expected.", expected.getMessage());
        }
    }

    @Test
    public void testCreateCondition_invalidInnerTag() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <clock>" +
                "    198" +
                "  </clock>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.createCondition(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("At least one child element 'time-delta-seconds' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreateCondition_dontAllowConcurrentNoSecondaryNameModeAndSecondaryNameMode() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <time-delta-seconds>" +
                "    12" +
                "  </time-delta-seconds>" +
                "  <time-delta-seconds names=\"secName\">" +
                "    13" +
                "  </time-delta-seconds>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.createCondition(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("It is not allowed to define time delta conditions with and without secondary sensor names concurrently.", expected.getMessage());
        }
    }

    @Test
    public void testCreateCondition_allowOnlyOneConditionInNoSecondaryNameMode() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <time-delta-seconds>" +
                "    12" +
                "  </time-delta-seconds>" +
                "  <time-delta-seconds>" +
                "    13" +
                "  </time-delta-seconds>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.createCondition(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("In the mode 'no secondary sensor names' it is not allowed to define a TimeDeltaCondition twice.", expected.getMessage());
        }
    }

    private SampleSet createSampleSetWithInvalidSample(String secondarySensorName) {
        final SampleSet invalidSampleSet_secName1 = createValidSampleSet();
        final int milliSeconds = 10000;
        final Sample invalid = new Sample(0, 0, 0, 0, milliSeconds);
        invalidSampleSet_secName1.setSecondary(secondarySensorName, invalid);
        return invalidSampleSet_secName1;
    }

    private SampleSet createValidSampleSet() {
        final int millisecondsPrime = 0;
        final int milliSeconds1 = 3000;
        final int milliSeconds2 = 4000;
        final int milliSeconds3 = 5000;

        final Sample primary = new Sample(0, 0, 0, 0, millisecondsPrime);
        final Sample validSec1 = new Sample(0, 0, 0, 0, milliSeconds1);
        final Sample validSec2 = new Sample(0, 0, 0, 0, milliSeconds2);
        final Sample validSec3 = new Sample(0, 0, 0, 0, milliSeconds3);

        final SampleSet validSampleSet = new SampleSet();
        validSampleSet.setPrimary(primary);
        validSampleSet.setSecondary("name1", validSec1);
        validSampleSet.setSecondary("name2", validSec2);
        validSampleSet.setSecondary("name3", validSec3);
        return validSampleSet;
    }

    private SampleSetBuilder createSampleSet(long i) {
        return new SampleSetBuilder(i);
    }

    static class SampleSetBuilder {

        private final SampleSet sampleSet;

        SampleSetBuilder(long primaryTime) {
            sampleSet = new SampleSet();
            sampleSet.setPrimary(new Sample(0, 0, 0, 0, primaryTime));
        }

        public SampleSetBuilder secondary(String sensorName, long t) {
            sampleSet.setSecondary(sensorName, new Sample(0, 0, 0, 0, t));
            return this;
        }

        SampleSet build() {
            return sampleSet;
        }
    }
}
