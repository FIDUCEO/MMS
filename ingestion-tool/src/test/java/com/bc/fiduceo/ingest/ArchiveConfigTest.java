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

package com.bc.fiduceo.ingest;


import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ArchiveConfigTest {

    @Test
    public void testParseAndGet_rootPath() {
        final String rootPathXML = "<archive>" +
                "    <root-path>" +
                "        /usr/local/data/fiduceo" +
                "    </root-path>" +
                "</archive>";

        final ArchiveConfig config = ArchiveConfig.parse(rootPathXML);
        assertNotNull(config);
        assertEquals("/usr/local/data/fiduceo", config.getRootPath());
    }

    @Test
    public void testParseAndGet_invalidTag() {
        final String rootPathXML = "<file-system>" +
                "</file-system>";

        try {
            ArchiveConfig.parse(rootPathXML);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testParseAndGet_missingRootPath() {
        final String rootPathXML = "<archive>" +
                "</archive>";

        try {
            ArchiveConfig.parse(rootPathXML);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testParseAndGet_emptyRootPath() {
        final String rootPathXML = "<archive>" +
                "    <root-path>" +
                "    </root-path>" +
                "</archive>";

        try {
            ArchiveConfig.parse(rootPathXML);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testParseAndGet_ruleWithStandardElements() {
        final String rootPathXML = "<archive>" +
                "    <root-path>" +
                "        /usr/local/data/fiduceo" +
                "    </root-path>" +
                "    <rule sensors = \"gammel, dansk\">" +
                "        SENSOR/VERSION/YEAR" +
                "    </rule>" +
                "</archive>";

        final ArchiveConfig config = ArchiveConfig.parse(rootPathXML);

        final Map<String, PathElement[]> rules = config.getRules();
        assertNotNull(rules);

        PathElement[] elements = rules.get("dansk");
        assertEquals(3, elements.length);
        assertEquals("SENSOR", elements[0].getName());

        elements = rules.get("gammel");
        assertEquals(3, elements.length);
        assertEquals("VERSION", elements[1].getName());
        assertEquals("YEAR", elements[2].getName());
    }

    @Test
    public void testParseAndGet_ruleWithStandardElements_missingSensors() {
        final String rootPathXML = "<archive>" +
                "    <root-path>" +
                "        /usr/local/data/fiduceo" +
                "    </root-path>" +
                "    <rule>" +
                "        SENSOR/VERSION/YEAR" +
                "    </rule>" +
                "</archive>";

        try {
            ArchiveConfig.parse(rootPathXML);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}