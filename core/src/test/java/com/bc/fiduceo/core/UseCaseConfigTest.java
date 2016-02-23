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

package com.bc.fiduceo.core;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.Assert.*;

public class UseCaseConfigTest {

    @Test
    public void testLoad__useCaseName() {
        final String useCaseXml = "<use-case-config name=\"use-case 17\"></use-case-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final UseCaseConfig useCaseConfig = UseCaseConfig.load(inputStream);
        assertNotNull(useCaseConfig);
        assertEquals("use-case 17", useCaseConfig.getName());
    }

    @Test
    public void testLoad__oneSensor() {
        final String useCaseXml = "<use-case-config name=\"use-case 18\">" +
                "  <sensors>" +
                "    <sensor>" +
                "      <name>amsub-n16</name>" +
                "    </sensor>" +
                "  </sensors>" +
                "</use-case-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final UseCaseConfig useCaseConfig = UseCaseConfig.load(inputStream);
        assertEquals("use-case 18", useCaseConfig.getName());
        final List<Sensor> sensors = useCaseConfig.getSensors();
        assertEquals(1, sensors.size());
        assertEquals("amsub-n16", sensors.get(0).getName());
    }

    @Test
    public void testLoad__twoSensors() {
        final String useCaseXml = "<use-case-config name=\"use-case 19\">" +
                "  <sensors>" +
                "    <sensor>" +
                "      <name>amsub-n16</name>" +
                "    </sensor>" +
                "    <sensor>" +
                "      <name>mhs-n18</name>" +
                "    </sensor>" +
                "  </sensors>" +
                "</use-case-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final UseCaseConfig useCaseConfig = UseCaseConfig.load(inputStream);
        assertEquals("use-case 19", useCaseConfig.getName());
        final List<Sensor> sensors = useCaseConfig.getSensors();
        assertEquals(2, sensors.size());
        assertEquals("mhs-n18", sensors.get(1).getName());
    }

    @Test
    public void testLoad__oneSensor_primary() {
        final String useCaseXml = "<use-case-config name=\"use-case 19\">" +
                "  <sensors>" +
                "    <sensor>" +
                "      <name>amsub-n20</name>" +
                "      <primary>true</primary>" +
                "    </sensor>" +
                "  </sensors>" +
                "</use-case-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final UseCaseConfig useCaseConfig = UseCaseConfig.load(inputStream);
        assertEquals("use-case 19", useCaseConfig.getName());
        final List<Sensor> sensors = useCaseConfig.getSensors();
        assertEquals(1, sensors.size());
        assertEquals("amsub-n20", sensors.get(0).getName());
        assertTrue(sensors.get(0).isPrimary());
    }

    @Test
    public void testLoad__timeDelta() {
        final String useCaseXml = "<use-case-config name=\"use-case 20\">" +
                "  <time-delta>300</time-delta>" +
                "</use-case-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final UseCaseConfig useCaseConfig = UseCaseConfig.load(inputStream);
        assertEquals("use-case 20", useCaseConfig.getName());
        assertEquals(300, useCaseConfig.getTimeDelta());
    }
}
