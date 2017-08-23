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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConditionFactoryTest {

    private ConditionFactory conditionFactory;

    @Before
    public void setUp() {
        conditionFactory = ConditionFactory.get();
    }

    @Test
    public void testGet() throws JDOMException, IOException {
        final String XML = "<time-delta>" +
                "  <time-delta-seconds>" +
                "    198" +
                "  </time-delta-seconds>" +
                "</time-delta>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = conditionFactory.getCondition(element);
        assertNotNull(condition);
        assertTrue(condition instanceof TimeDeltaCondition);
    }

    @Test
    public void testGet_invalidName() throws JDOMException, IOException {
        final String XML = "<dunnowhattodo>" +
                "</dunnowhattodo>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            conditionFactory.getCondition(element);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
