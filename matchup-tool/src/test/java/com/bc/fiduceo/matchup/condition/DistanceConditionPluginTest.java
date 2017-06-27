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
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.matchup.SampleSet;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.*;

import java.io.IOException;

public class DistanceConditionPluginTest {

    private DistanceConditionPlugin plugin;

    @Before
    public void setUp() {
        plugin = new DistanceConditionPlugin();
    }

    @Test
    public void testGetConditionName() {
        assertEquals("spherical-point_distance", plugin.getConditionName());
    }

    @Test
    public void testCreateCondition() throws JDOMException, IOException {
        final String XML = "<spherical-point_distance>" +
                           "  <max-pixel-point_distance-km>" +
                           "    4.5" +
                           "  </max-pixel-point_distance-km>" +
                           "</spherical-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
        assertThat(condition, is(instanceOf(DistanceCondition.class)));
        final DistanceCondition distanceCondition = (DistanceCondition) condition;
        assertEquals(4.5, distanceCondition.getMaxDistanceInKm(), 1e-250);
        assertEquals(SampleSet.getOnlyOneSecondaryKey(), distanceCondition.getSecondarySensorName());
    }

    @Test
    public void testCreateCondition_withOptionalSecondarySensorName() throws JDOMException, IOException {
        final String XML = "<spherical-point_distance>" +
                           "  <max-pixel-point_distance-km names=\"secSenName\">" +
                           "    4.5" +
                           "  </max-pixel-point_distance-km>" +
                           "</spherical-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
        assertThat(condition, is(instanceOf(DistanceCondition.class)));
        final DistanceCondition distanceCondition = (DistanceCondition) condition;
        assertEquals(4.5, distanceCondition.getMaxDistanceInKm(), 1e-250);
        assertEquals("secSenName", distanceCondition.getSecondarySensorName());
    }

    @Test
    public void testCreateCondition_invalidTag() throws JDOMException, IOException {
        final String XML = "<point_distance>" +
                           "  <max-pixel-point_distance-km>" +
                           "    4.5" +
                           "  </max-pixel-point_distance-km>" +
                           "</point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.createCondition(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Illegal XML Element. Tagname 'spherical-point_distance' expected.", expected.getMessage());
        }
    }

    @Test
    public void testCreateCondition_invalidInnerTag() throws JDOMException, IOException {
        final String XML = "<spherical-point_distance>" +
                           "  <the_delta>" +
                           "    4.5" +
                           "  </the_delta>" +
                           "</spherical-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.createCondition(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("At least one child element 'max-pixel-point_distance-km' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreateCondition_emptySecondarySensorNameTag() throws JDOMException, IOException {
        final String XML = "<spherical-point_distance>" +
                           "  <max-pixel-point_distance-km names=\"\">" +
                           "    4.5" +
                           "  </max-pixel-point_distance-km>" +
                           "</spherical-point_distance>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
        assertThat(condition, is(instanceOf(DistanceCondition.class)));
        final DistanceCondition distanceCondition = (DistanceCondition) condition;
        assertEquals(4.5, distanceCondition.getMaxDistanceInKm(), 1e-250);
        assertEquals(SampleSet.getOnlyOneSecondaryKey(), distanceCondition.getSecondarySensorName());

    }
}
