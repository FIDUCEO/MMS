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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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

        final TimeDeltaCondition condition = plugin.createCondition(element);
        assertNotNull(condition);
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
        }
    }
}
