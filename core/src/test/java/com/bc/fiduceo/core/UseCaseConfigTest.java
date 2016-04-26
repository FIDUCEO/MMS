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

import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.*;

import org.jdom.JDOMException;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UseCaseConfigTest {

    private UseCaseConfig useCaseConfig;

    @Before
    public void setUp() throws JDOMException, IOException {
        useCaseConfig = UseCaseConfig.load(new ByteArrayInputStream("<use-case-config name=\"testName\"/>".getBytes()));
    }

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
        assertEquals("amsub-n16", sensors.get(0).getName());
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
                                  "  <conditions>" +
                                  "    <time-delta>" +
                                  "      <time-delta-seconds>300</time-delta-seconds>" +
                                  "    </time-delta>" +
                                  "  </conditions>" +
                                  "</use-case-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final UseCaseConfig useCaseConfig = UseCaseConfig.load(inputStream);
        assertEquals("use-case 20", useCaseConfig.getName());
        assertEquals(300, useCaseConfig.getTimeDeltaSeconds());
    }

    @Test
    public void testLoad__outputPath() {
        final String useCaseXml = "<use-case-config name=\"use-case 20\">" +
                                  "  <output-path>file/system/path</output-path>" +
                                  "</use-case-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final UseCaseConfig useCaseConfig = UseCaseConfig.load(inputStream);
        assertEquals("file/system/path", useCaseConfig.getOutputPath());
    }

    @Test
    public void testLoad__dimensionList() {
        final String useCaseXml = "<use-case-config name=\"use-case 20\">" +
                                  "  <dimensions>" +
                                  "    <dimension name=\"avhrr-n08\">" +
                                  "      <nx>7</nx>" +
                                  "      <ny>8</ny>" +
                                  "    </dimension>" +
                                  "    <dimension name=\"avhrr-n09\">" +
                                  "      <nx>9</nx>" +
                                  "      <ny>10</ny>" +
                                  "    </dimension>" +
                                  "  </dimensions>" +
                                  "</use-case-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final UseCaseConfig useCaseConfig = UseCaseConfig.load(inputStream);
        final List<Dimension> dimensions = useCaseConfig.getDimensions();
        assertNotNull(dimensions);
        assertEquals(2, dimensions.size());

        Dimension dimension = dimensions.get(0);
        assertEquals("avhrr-n08", dimension.getName());
        assertEquals(7, dimension.getNx());
        assertEquals(8, dimension.getNy());

        dimension = dimensions.get(1);
        assertEquals("avhrr-n09", dimension.getName());
        assertEquals(9, dimension.getNx());
        assertEquals(10, dimension.getNy());
    }

    @Test
    public void testStore() throws IOException {

        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder
                    .build("test_use_case")
                    .withSensors(Arrays.asList(
                                new Sensor("first"),
                                new Sensor("second")))
                    .withDimensions(Arrays.asList(
                                new Dimension("first", 11, 15),
                                new Dimension("second", 3, 5)))
                    .withTimeDeltaSeconds(12345)
                    .withOutputPath("wherever/you/want/it")
                    .withMaxPixelDistanceKm(14.8f)
                    .createConfig();


        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        useCaseConfig.store(outputStream);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<use-case-config name=\"test_use_case\">");
        pw.println("  <sensors>");
        pw.println("    <sensor>");
        pw.println("      <name>first</name>");
        pw.println("      <primary>false</primary>");
        pw.println("    </sensor>");
        pw.println("    <sensor>");
        pw.println("      <name>second</name>");
        pw.println("      <primary>false</primary>");
        pw.println("    </sensor>");
        pw.println("  </sensors>");
        pw.println("  <dimensions>");
        pw.println("    <dimension name=\"first\">");
        pw.println("      <nx>11</nx>");
        pw.println("      <ny>15</ny>");
        pw.println("    </dimension>");
        pw.println("    <dimension name=\"second\">");
        pw.println("      <nx>3</nx>");
        pw.println("      <ny>5</ny>");
        pw.println("    </dimension>");
        pw.println("  </dimensions>");
        pw.println("  <conditions>");
        pw.println("    <time-delta>");
        pw.println("      <time-delta-seconds>12345</time-delta-seconds>");
        pw.println("    </time-delta>");
        pw.println("    <spherical-distance>");
        pw.println("      <max-pixel-distance-km>14.8</max-pixel-distance-km>");
        pw.println("    </spherical-distance>");
        pw.println("  </conditions>");
        pw.println("  <output-path>wherever/you/want/it</output-path>");
        pw.println("</use-case-config>");
        pw.flush();

        assertThat(sw.toString(), equalToIgnoringWhiteSpace(outputStream.toString()));
    }

    @Test
    public void testGetPrimarySensor_emptySensorList() {
        final Sensor sensor = useCaseConfig.getPrimarySensor();
        assertNull(sensor);
    }

    @Test
    public void testGetPrimarySensor_noPrimarySensorInList() {
        final List<Sensor> sensorList = new ArrayList<>();
        sensorList.add(new Sensor("first"));
        sensorList.add(new Sensor("second"));
        useCaseConfig.setSensors(sensorList);

        final Sensor sensor = useCaseConfig.getPrimarySensor();
        assertNull(sensor);
    }

    @Test
    public void testGetPrimarySensor_primarySensorInList() {
        final List<Sensor> sensorList = new ArrayList<>();
        sensorList.add(new Sensor("first"));

        final Sensor second = new Sensor("second");
        second.setPrimary(true);
        sensorList.add(second);
        useCaseConfig.setSensors(sensorList);

        final Sensor sensor = useCaseConfig.getPrimarySensor();
        assertNotNull(sensor);
        assertEquals("second", sensor.getName());
    }

    @Test
    public void testGetAdditionalSensor() {
        final List<Sensor> sensorList = new ArrayList<>();
        sensorList.add(new Sensor("first"));

        final Sensor second = new Sensor();
        second.setName("second");
        second.setPrimary(true);
        sensorList.add(second);
        useCaseConfig.setSensors(sensorList);

        final List<Sensor> additionalSensorList = useCaseConfig.getAdditionalSensors();
        assertEquals(1, additionalSensorList.size());
        assertEquals("first", additionalSensorList.get(0).getName());
    }

    @Test
    public void testGetAdditionalSensor_noAdditionalSensor() {
        final List<Sensor> sensorList = new ArrayList<>();

        final Sensor primary = new Sensor("primary");
        primary.setPrimary(true);
        sensorList.add(primary);
        useCaseConfig.setSensors(sensorList);

        final List<Sensor> additionalSensorList = useCaseConfig.getAdditionalSensors();
        assertEquals(0, additionalSensorList.size());
    }

    @Test
    public void testSetGetName() {
        useCaseConfig.setName("well-done");
        assertEquals("well-done", useCaseConfig.getName());
    }

    @Test
    public void testSetGetTimeDelta() {
        useCaseConfig.setTimeDeltaSeconds(4067);
        assertEquals(4067, useCaseConfig.getTimeDeltaSeconds());
    }

    @Test
    public void testSetGetOutputPath() {
        final String path = "/some/where/on/disk";

        useCaseConfig.setOutputPath(path);
        assertEquals(path, useCaseConfig.getOutputPath());
    }

    @Test
    public void testGetDimensionFor() {
        final List<Sensor> sensorList = new ArrayList<>();
        sensorList.add(new Sensor("first"));
        sensorList.add(new Sensor("second"));
        useCaseConfig.setSensors(sensorList);

        final List<Dimension> dimensionsList = new ArrayList<>();
        dimensionsList.add(new Dimension("first", 1, 2));
        dimensionsList.add(new Dimension("second", 3, 4));
        useCaseConfig.setDimensions(dimensionsList);

        final Dimension second = useCaseConfig.getDimensionFor("second");
        assertNotNull(second);
        assertEquals(3, second.getNx());
        assertEquals(4, second.getNy());
    }

    @Test
    public void testGetDimensionFor_notAvailableSensorDimension() {
        final List<Sensor> sensorList = new ArrayList<>();
        sensorList.add(new Sensor("first"));
        sensorList.add(new Sensor("second"));
        useCaseConfig.setSensors(sensorList);

        final List<Dimension> dimensionsList = new ArrayList<>();
        dimensionsList.add(new Dimension("second", 3, 4));
        useCaseConfig.setDimensions(dimensionsList);

        try {
            useCaseConfig.getDimensionFor("first");
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testConstruction() {
        final List<Sensor> additionalSensors = useCaseConfig.getAdditionalSensors();
        assertNotNull(additionalSensors);
        assertEquals(0, additionalSensors.size());

        final List<Dimension> dimensions = useCaseConfig.getDimensions();
        assertNotNull(dimensions);
        assertEquals(0, dimensions.size());

        assertEquals(-1, useCaseConfig.getTimeDeltaSeconds());
    }
}
