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
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BorderDistanceConditionPluginTest {

    private BorderDistanceConditionPlugin plugin;

    @Before
    public void setUp() {
        plugin = new BorderDistanceConditionPlugin();
    }

    @Test
    public void testGetConditionName() {
        assertEquals("border-point_distance", plugin.getConditionName());
    }

    @Test
    public void testCreateCondition() throws JDOMException, IOException {
        final String XML = "<border-point_distance>" +
                "  <primary>" +
                "    <nx>4</nx>" +
                "    <ny>8</ny>" +
                "  </primary>" +
                "</border-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
        assertTrue(condition instanceof BorderDistanceCondition);
    }

    @Test
    public void testParseConfiguration_primary() throws JDOMException, IOException {
        final String XML = "<border-point_distance>" +
                "  <primary>" +
                "    <nx>3</nx>" +
                "    <ny>4</ny>" +
                "  </primary>" +
                "</border-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        final BorderDistanceCondition.Configuration configuration = plugin.parseConfiguration(element);
        assertTrue(configuration.usePrimary);
        assertEquals(3, configuration.primary_x);
        assertEquals(4, configuration.primary_y);

        assertFalse(configuration.useSecondary);
    }

    @Test
    public void testParseConfiguration_secondary() throws JDOMException, IOException {
        final String XML = "<border-point_distance>" +
                "  <secondary>" +
                "    <nx>5</nx>" +
                "    <ny>6</ny>" +
                "  </secondary>" +
                "</border-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        final BorderDistanceCondition.Configuration configuration = plugin.parseConfiguration(element);
        assertTrue(configuration.useSecondary);
        assertEquals(5, configuration.secondary_x);
        assertEquals(6, configuration.secondary_y);

        assertFalse(configuration.usePrimary);
    }

    @Test
    public void testParseConfiguration_both() throws JDOMException, IOException {
        final String XML = "<border-point_distance>" +
                "  <primary>" +
                "    <nx>7</nx>" +
                "    <ny>8</ny>" +
                "  </primary>" +
                "  <secondary>" +
                "    <nx>9</nx>" +
                "    <ny>10</ny>" +
                "  </secondary>" +
                "</border-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        final BorderDistanceCondition.Configuration configuration = plugin.parseConfiguration(element);
        assertTrue(configuration.usePrimary);
        assertEquals(7, configuration.primary_x);
        assertEquals(8, configuration.primary_y);

        assertTrue(configuration.useSecondary);
        assertEquals(9, configuration.secondary_x);
        assertEquals(10, configuration.secondary_y);
    }

    @Test
    public void testParseConfiguration_none() throws JDOMException, IOException {
        final String XML = "<border-point_distance>" +
                "</border-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        final BorderDistanceCondition.Configuration configuration = plugin.parseConfiguration(element);
        assertFalse(configuration.usePrimary);
        assertFalse(configuration.useSecondary);
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
        }
    }

    @Test
    public void testParseConfiguration_primary_missingXTag() throws JDOMException, IOException {
        final String XML = "<border-point_distance>" +
                "  <primary>" +
                "    <ny>4</ny>" +
                "  </primary>" +
                "</border-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testParseConfiguration_secondary_missingYTag() throws JDOMException, IOException {
        final String XML = "<border-point_distance>" +
                "  <secondary>" +
                "    <nx>5</nx>" +
                "  </secondary>" +
                "</border-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
