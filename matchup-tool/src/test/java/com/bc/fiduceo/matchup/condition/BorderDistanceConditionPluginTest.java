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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.matchup.SampleSet;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.*;

import java.io.IOException;
import java.util.List;

public class BorderDistanceConditionPluginTest {

    private BorderDistanceConditionPlugin plugin;

    @Before
    public void setUp() {
        plugin = new BorderDistanceConditionPlugin();
    }

    @Test
    public void testGetConditionName() {
        assertEquals("border-distance", plugin.getConditionName());
    }

    @Test
    public void testCreateCondition() throws JDOMException, IOException {
        final String XML = "<border-distance>" +
                           "  <primary>" +
                           "    <nx>4</nx>" +
                           "    <ny>8</ny>" +
                           "  </primary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
        assertTrue(condition instanceof BorderDistanceCondition);
    }

    @Test
    public void testParseConfiguration_primary() throws JDOMException, IOException {
        final String XML = "<border-distance>" +
                           "  <primary>" +
                           "    <nx>3</nx>" +
                           "    <ny>4</ny>" +
                           "  </primary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        final List<BorderDistanceCondition.Configuration> configurations = plugin.parseConfiguration(element);
        assertEquals(1, configurations.size());
        final BorderDistanceCondition.Configuration configuration = configurations.get(0);
        assertTrue(configuration.usePrimary);
        assertEquals(3, configuration.primary_x);
        assertEquals(4, configuration.primary_y);

        assertFalse(configuration.useSecondary);
    }

    @Test
    public void testParseConfiguration_OnlyOnePrimaryAllowed() throws JDOMException, IOException {
        //preparation
        final String XML = "<border-distance>" +
                           "  <primary>" +
                           "  </primary>" +
                           "  <primary>" +
                           "  </primary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            //execution
            plugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            //verification
            assertThat(expected.getMessage(), is(equalTo("Illegal XML Element. Tag name 'primary'. Only one 'primary' definition allowed.")));
        }
    }

    @Test
    public void testParseConfiguration_secondary() throws JDOMException, IOException {
        final String XML = "<border-distance>" +
                           "  <secondary>" +
                           "    <nx>5</nx>" +
                           "    <ny>6</ny>" +
                           "  </secondary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        final List<BorderDistanceCondition.Configuration> configurations = plugin.parseConfiguration(element);
        assertEquals(1, configurations.size());
        final BorderDistanceCondition.Configuration configuration = configurations.get(0);
        assertTrue(configuration.useSecondary);
        assertEquals(SampleSet.getOnlyOneSecondaryKey(), configuration.secondaryName);
        assertEquals(5, configuration.secondary_x);
        assertEquals(6, configuration.secondary_y);

        assertFalse(configuration.usePrimary);
    }

    @Test
    public void testParseConfiguration_twoSecondriesWithoutNamesAttribute() throws JDOMException, IOException {
        //preparation
        final String XML = "<border-distance>" +
                           "  <secondary>" +
                           "    <nx>5</nx>" +
                           "    <ny>6</ny>" +
                           "  </secondary>" +
                           "  <secondary>" +
                           "    <nx>7</nx>" +
                           "    <ny>8</ny>" +
                           "  </secondary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            //execution
            plugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            //verification
            assertThat(expected.getMessage(), is(equalTo("Forbidden to define two unnamed 'secondary' tags.")));
        }
    }

    @Test
    public void testParseConfiguration_twoSecondariesWithDifferentNames() throws Exception {
        final String XML = "<border-distance>" +
                           "  <secondary names=\"nameA\">" +
                           "    <nx>5</nx>" +
                           "    <ny>6</ny>" +
                           "  </secondary>" +
                           "  <secondary names=\"nameB\">" +
                           "    <nx>7</nx>" +
                           "    <ny>8</ny>" +
                           "  </secondary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        final List<BorderDistanceCondition.Configuration> configurations = plugin.parseConfiguration(element);
        assertEquals(2, configurations.size());

        final BorderDistanceCondition.Configuration configuration1 = configurations.get(0);
        assertTrue(configuration1.useSecondary);
        assertThat(configuration1.secondaryName, is(equalTo("nameA")));
        assertEquals(5, configuration1.secondary_x);
        assertEquals(6, configuration1.secondary_y);

        final BorderDistanceCondition.Configuration configuration2 = configurations.get(1);
        assertTrue(configuration2.useSecondary);
        assertThat(configuration2.secondaryName, is(equalTo("nameB")));
        assertEquals(7, configuration2.secondary_x);
        assertEquals(8, configuration2.secondary_y);

        assertFalse(configuration2.usePrimary);
    }

    @Test
    public void testParseConfiguration_NotAllowedToUseASecondaryNameTwice() throws Exception {
        final String XML = "<border-distance>" +
                           "  <secondary names=\"nameA,nameB,nameC\">" +
                           "    <nx>5</nx>" +
                           "    <ny>6</ny>" +
                           "  </secondary>" +
                           "  <secondary names=\"nameB\">" +
                           "    <nx>12</nx>" +
                           "    <ny>14</ny>" +
                           "  </secondary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), is(equalTo("It is not allowed to use a secondary name twice.")));
        }
    }

    @Test
    public void testParseConfiguration_oneSecondariesWithTwoNames() throws Exception {
        final String XML = "<border-distance>" +
                           "  <secondary names=\"nameA,nameB\">" +
                           "    <nx>5</nx>" +
                           "    <ny>6</ny>" +
                           "  </secondary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        final List<BorderDistanceCondition.Configuration> configurations = plugin.parseConfiguration(element);
        assertEquals(2, configurations.size());

        final BorderDistanceCondition.Configuration configuration1 = configurations.get(0);
        assertTrue(configuration1.useSecondary);
        assertThat(configuration1.secondaryName, is(equalTo("nameA")));
        assertEquals(5, configuration1.secondary_x);
        assertEquals(6, configuration1.secondary_y);

        final BorderDistanceCondition.Configuration configuration2 = configurations.get(1);
        assertTrue(configuration2.useSecondary);
        assertThat(configuration2.secondaryName, is(equalTo("nameB")));
        assertEquals(5, configuration2.secondary_x);
        assertEquals(6, configuration2.secondary_y);

        assertFalse(configuration2.usePrimary);
    }

    @Test
    public void testParseConfiguration_mixingOfNamedSecondaryAndUnnamedSecondariesNotAllowed() throws Exception {
        final String XML = "<border-distance>" +
                           "  <secondary>" + // only one secondary case
                           "    <nx>5</nx>" +
                           "    <ny>6</ny>" +
                           "  </secondary>" +
                           "  <secondary names=\"nameA\">" + // multiple secondary case
                           "    <nx>5</nx>" +
                           "    <ny>6</ny>" +
                           "  </secondary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), is(equalTo("It is not allowed to mix 'secondary' tags with and without 'names' attribute.")));
        }
    }

    @Test
    public void testParseConfiguration_both() throws JDOMException, IOException {
        final String XML = "<border-distance>" +
                           "  <primary>" +
                           "    <nx>7</nx>" +
                           "    <ny>8</ny>" +
                           "  </primary>" +
                           "  <secondary>" +
                           "    <nx>9</nx>" +
                           "    <ny>10</ny>" +
                           "  </secondary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        final List<BorderDistanceCondition.Configuration> configurations = plugin.parseConfiguration(element);
        assertEquals(2, configurations.size());

        final BorderDistanceCondition.Configuration configuration = configurations.get(0);
        assertTrue(configuration.usePrimary);
        assertEquals(7, configuration.primary_x);
        assertEquals(8, configuration.primary_y);

        final BorderDistanceCondition.Configuration configuration2 = configurations.get(1);
        assertTrue(configuration2.useSecondary);
        assertEquals(SampleSet.getOnlyOneSecondaryKey(), configuration2.secondaryName);
        assertEquals(9, configuration2.secondary_x);
        assertEquals(10, configuration2.secondary_y);
    }

    @Test
    public void testParseConfiguration_none() throws JDOMException, IOException {
        final String XML = "<border-distance>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        final List<BorderDistanceCondition.Configuration> configurations = plugin.parseConfiguration(element);
        assertEquals(0, configurations.size());
    }

    @Test
    public void testParseConfiguration_invalidTag() throws JDOMException, IOException {
        final String XML = "<frontier>" +
                           "</frontier>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), containsString("'border-distance'"));
            assertThat(expected.getMessage(), containsString("expected"));
        }
    }

    @Test
    public void testParseConfiguration_primary_missingXTag() throws JDOMException, IOException {
        final String XML = "<border-distance>" +
                           "  <primary>" +
                           "    <ny>4</ny>" +
                           "  </primary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), containsString("'nx'"));
            assertThat(expected.getMessage(), containsString("expected"));
        }
    }

    @Test
    public void testParseConfiguration_secondary_missingYTag() throws JDOMException, IOException {
        final String XML = "<border-distance>" +
                           "  <secondary>" +
                           "    <nx>5</nx>" +
                           "  </secondary>" +
                           "</border-distance>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), containsString("'ny'"));
            assertThat(expected.getMessage(), containsString("expected"));
        }
    }
}
