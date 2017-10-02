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

import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UniqueSamplesConditionPluginTest {

    private UniqueSamplesConditionPlugin plugin;

    @Before
    public void setUp() {
        plugin = new UniqueSamplesConditionPlugin();
    }

    @Test
    public void testGetConditionName() {
        assertEquals("unique-samples", plugin.getConditionName());
    }

    @Test
    public void testCreateCondition() throws JDOMException, IOException {
        final String XML = "<unique-samples>" +
                "   <reference-sensor>amsu-b</reference-sensor>" +
                "   <associated-sensor>gtmba-sst</associated-sensor>" +
                "</unique-samples>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertTrue(condition instanceof UniqueSamplesCondition);
    }

    @Test
    public void testParseConfig() throws JDOMException, IOException {
        final String XML = "<unique-samples>" +
                "   <reference-sensor>amsu-b</reference-sensor>" +
                "   <associated-sensor>gtmba-sst</associated-sensor>" +
                "</unique-samples>";
        final Element element = TestUtil.createDomElement(XML);

        final UniqueSamplesCondition.Configuration config = UniqueSamplesConditionPlugin.parseConfig(element);
        assertEquals("amsu-b", config.referenceSensorKey);
        assertEquals("gtmba-sst", config.associatedSensorKey);
    }

    @Test
    public void testParseConfig_wrongTagName() throws JDOMException, IOException {
        final String XML = "<some_samples>" +
                "   <reference-sensor>amsu-b</reference-sensor>" +
                "   <associated-sensor>gtmba-sst</associated-sensor>" +
                "</some_samples>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            UniqueSamplesConditionPlugin.parseConfig(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testParseConfig_missingReferenceSensorTag() throws JDOMException, IOException {
        final String XML = "<unique-samples>" +
                "   <associated-sensor>gtmba-sst</associated-sensor>" +
                "</unique-samples>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            UniqueSamplesConditionPlugin.parseConfig(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testParseConfig_missingAssociatedSensorTag() throws JDOMException, IOException {
        final String XML = "<unique-samples>" +
                "   <reference-sensor>amsu-b</reference-sensor>" +
                "</unique-samples>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            UniqueSamplesConditionPlugin.parseConfig(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testParseConfig_emptyReferenceSensorTag() throws JDOMException, IOException {
        final String XML = "<unique-samples>" +
                "   <reference-sensor></reference-sensor>" +
                "   <associated-sensor>gtmba-sst</associated-sensor>" +
                "</unique-samples>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            UniqueSamplesConditionPlugin.parseConfig(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testParseConfig_emptyAssociatedSensorTag() throws JDOMException, IOException {
        final String XML = "<unique-samples>" +
                "   <reference-sensor>schlumpf</reference-sensor>" +
                "   <associated-sensor></associated-sensor>" +
                "</unique-samples>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            UniqueSamplesConditionPlugin.parseConfig(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
