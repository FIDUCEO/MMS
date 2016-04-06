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

package com.bc.fiduceo.matchup.writer;

import static org.junit.Assert.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(IOTestRunner.class)
public class MmdWriter_IO_Test {

    private File testDir;

    @Before
    public void setUp() throws Exception {
        testDir = TestUtil.createTestDirectory();
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testCreate() throws IOException, InvalidRangeException {
        final MmdWriter mmdWriter = new MmdWriter(10000);
        final List<Dimension> dimemsions = new ArrayList<>();
        dimemsions.add(new Dimension("avhrr-n11", 5, 7));
        dimemsions.add(new Dimension("avhrr-n12", 3, 5));

        final List<VariablePrototype> variablePrototypes = new ArrayList<>();
        VariablePrototype variablePrototype = new VariablePrototype();
        variablePrototype.setTargetVariableName("avhrr-n11_ch3b");
        variablePrototype.setDimensionNames("matchup_count avhrr-n11_ny avhrr-n11_nx");
        variablePrototype.setDataType("short");
        variablePrototype.setAttributes(new ArrayList<>());
        variablePrototypes.add(variablePrototype);

        variablePrototype = new VariablePrototype();
        variablePrototype.setTargetVariableName("avhrr-n12_ch4");
        variablePrototype.setDimensionNames("matchup_count avhrr-n12_ny avhrr-n12_nx");
        variablePrototype.setDataType("int");
        variablePrototype.setAttributes(new ArrayList<>());
        variablePrototypes.add(variablePrototype);

        variablePrototype = new VariablePrototype();
        variablePrototype.setTargetVariableName("avhrr-n12_cloud_mask");
        variablePrototype.setDimensionNames("matchup_count avhrr-n12_ny avhrr-n12_nx");
        variablePrototype.setDataType("byte");
        variablePrototype.setAttributes(new ArrayList<>());
        variablePrototypes.add(variablePrototype);

        variablePrototype = new VariablePrototype();
        variablePrototype.setTargetVariableName("avhrr-n12_dtime");
        variablePrototype.setDimensionNames("matchup_count avhrr-n12_ny avhrr-n12_nx");
        variablePrototype.setDataType("float");
        variablePrototype.setAttributes(new ArrayList<>());
        variablePrototypes.add(variablePrototype);

        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        useCaseConfig.setName("useCaseName");
        useCaseConfig.setDimensions(dimemsions);

        final Sensor primarySensor = new Sensor("avhrr-n11");
        primarySensor.setPrimary(true);
        final Sensor secondarySensor = new Sensor("avhrr-n12");
        useCaseConfig.setSensors(Arrays.asList(primarySensor, secondarySensor));

        final Path mmdFile = Paths.get(testDir.toURI()).resolve("test_mmd.nc");

        try {
            mmdWriter.initializeNetcdfFile(mmdFile, useCaseConfig, variablePrototypes, 2346);
        } finally {
            mmdWriter.close();
        }

        assertTrue(Files.isRegularFile(mmdFile));

        NetcdfFile mmd = null;
        try {
            mmd = NetcdfFile.open(mmdFile.toString());

            assertGlobalAttribute("title", "FIDUCEO multi-sensor match-up dataset (MMD)", mmd);
            assertGlobalAttribute("institution", "Brockmann Consult GmbH", mmd);
            assertGlobalAttribute("contact", "Tom Block (tom.block@brockmann-consult.de)", mmd);
            assertGlobalAttribute("license", "This dataset is released for use under CC-BY licence and was developed in the EC FIDUCEO project \"Fidelity and Uncertainty in Climate Data Records from Earth Observations\". Grant Agreement: 638822.", mmd);
            assertGlobalDateAttribute("creation_date", TimeUtils.createNow(), mmd);

            final Attribute comment = mmd.findGlobalAttribute("comment");
            assertNotNull(comment);
            assertEquals(DataType.STRING, comment.getDataType());
            assertEquals(
                        "The MMD file is created based on the use case configuration documented in the attribute 'use-case-configuration'.",
                        comment.getStringValue()
            );

            final Attribute useCaseConfigAttr = mmd.findGlobalAttribute("use-case-configuration");
            assertNotNull(useCaseConfigAttr);
            assertEquals(DataType.STRING, useCaseConfigAttr.getDataType());
            assertEquals(
                        "<use-case-config name=\"useCaseName\">\n" +
                        "  <sensors class=\"java.util.Arrays$ArrayList\">\n" +
                        "    <a class=\"sensor-array\">\n" +
                        "      <sensor>\n" +
                        "        <name>avhrr-n11</name>\n" +
                        "        <primary>true</primary>\n" +
                        "      </sensor>\n" +
                        "      <sensor>\n" +
                        "        <name>avhrr-n12</name>\n" +
                        "        <primary>false</primary>\n" +
                        "      </sensor>\n" +
                        "    </a>\n" +
                        "  </sensors>\n" +
                        "  <dimensions>\n" +
                        "    <dimension name=\"avhrr-n11\">\n" +
                        "      <nx>5</nx>\n" +
                        "      <ny>7</ny>\n" +
                        "    </dimension>\n" +
                        "    <dimension name=\"avhrr-n12\">\n" +
                        "      <nx>3</nx>\n" +
                        "      <ny>5</ny>\n" +
                        "    </dimension>\n" +
                        "  </dimensions>\n" +
                        "  <time-delta-seconds>-1</time-delta-seconds>\n" +
                        "  <max-pixel-distance-km>-1.0</max-pixel-distance-km>\n" +
                        "</use-case-config>",
                        useCaseConfigAttr.getStringValue()
            );

            assertDimension("avhrr-n11_nx", 5, mmd);
            assertDimension("avhrr-n11_ny", 7, mmd);
            assertDimension("avhrr-n12_nx", 3, mmd);
            assertDimension("avhrr-n12_ny", 5, mmd);
            assertDimension("matchup_count", 2346, mmd);

            Variable variable;
            Attribute att;

            variable = mmd.findVariable("avhrr-n11_ch3b");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346, 7, 5);
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = mmd.findVariable("avhrr-n12_ch4");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346, 5, 3);
            assertEquals(DataType.INT, variable.getDataType());

            variable = mmd.findVariable("avhrr-n12_cloud_mask");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346, 5, 3);
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = mmd.findVariable("avhrr-n12_dtime");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346, 5, 3);
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = mmd.findVariable("avhrr-n11_x");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346);
            assertEquals(1, variable.getAttributes().size());
            att = variable.findAttribute("description");
            assertNotNull(att);
            assertEquals("pixel original x location in satellite raster", att.getStringValue());

            variable = mmd.findVariable("avhrr-n12_x");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346);
            assertEquals(1, variable.getAttributes().size());
            att = variable.findAttribute("description");
            assertNotNull(att);
            assertEquals("pixel original x location in satellite raster", att.getStringValue());

            variable = mmd.findVariable("avhrr-n11_y");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346);
            assertEquals(1, variable.getAttributes().size());
            att = variable.findAttribute("description");
            assertNotNull(att);
            assertEquals("pixel original y location in satellite raster", att.getStringValue());

            variable = mmd.findVariable("avhrr-n12_y");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346);
            assertEquals(1, variable.getAttributes().size());
            att = variable.findAttribute("description");
            assertNotNull(att);
            assertEquals("pixel original y location in satellite raster", att.getStringValue());

            variable = mmd.findVariable("avhrr-n11_file_name");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346, 128);
            assertEquals(1, variable.getAttributes().size());
            att = variable.findAttribute("description");
            assertNotNull(att);
            assertEquals("file name of the original data file", att.getStringValue());

            variable = mmd.findVariable("avhrr-n12_file_name");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346, 128);
            assertEquals(1, variable.getAttributes().size());
            att = variable.findAttribute("description");
            assertNotNull(att);
            assertEquals("file name of the original data file", att.getStringValue());

            variable = mmd.findVariable("avhrr-n11_acquisition_time");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346, 7, 5);
            assertEquals(3, variable.getAttributes().size());
            att = variable.findAttribute("description");
            assertNotNull(att);
            assertEquals("acquisition time of original pixel", att.getStringValue());
            att = variable.findAttribute("unit");
            assertNotNull(att);
            assertEquals("seconds since 1970-01-01", att.getStringValue());
            att = variable.findAttribute("_FillValue");
            assertNotNull(att);
            assertEquals("-2147483648", att.getStringValue());

            variable = mmd.findVariable("avhrr-n12_acquisition_time");
            assertNotNull(variable);
            assertCorrectDimensions(variable, 2346, 5, 3);
            assertEquals(3, variable.getAttributes().size());
            att = variable.findAttribute("description");
            assertNotNull(att);
            assertEquals("acquisition time of original pixel", att.getStringValue());
            att = variable.findAttribute("unit");
            assertNotNull(att);
            assertEquals("seconds since 1970-01-01", att.getStringValue());
            att = variable.findAttribute("_FillValue");
            assertNotNull(att);
            assertEquals("-2147483648", att.getStringValue());

            final List<Variable> variables = mmd.getVariables();
            assertEquals(12, variables.size());
        } finally {
            if (mmd != null) {
                mmd.close();
            }
        }
    }

    private void assertCorrectDimensions(Variable variable, int... dims) {
        final List<ucar.nc2.Dimension> dimensions = variable.getDimensions();
        assertEquals(dims.length, dimensions.size());
        for (int i = 0; i < dims.length; i++) {
            int dim = dims[i];
            assertEquals("wrong dimension at idx " + i, dim, dimensions.get(i).getLength());
        }
    }

    private void assertDimension(String name, int expected, NetcdfFile mmd) {
        final ucar.nc2.Dimension ncDimension = mmd.findDimension(name);
        assertNotNull(ncDimension);
        assertEquals(expected, ncDimension.getLength());
    }

    private void assertGlobalDateAttribute(String name, Date expected, NetcdfFile mmd) {
        final Attribute creation_date = mmd.findGlobalAttribute(name);
        assertNotNull(creation_date);
        final String dateStringValue = creation_date.getStringValue();
        final Date actual = TimeUtils.parse(dateStringValue, "yyyy-MM-dd HH:mm:ss");
        TestUtil.assertWithinLastMinute(expected, actual);
    }

    private void assertGlobalAttribute(String name, String value, NetcdfFile mmd) {
        Attribute globalAttribute = mmd.findGlobalAttribute(name);
        assertNotNull(globalAttribute);
        assertEquals(value, globalAttribute.getStringValue());
    }
}
