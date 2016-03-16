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

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

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
    public void testCreate() throws IOException {
        final MmdWriter mmdWriter = new MmdWriter();

        final File mmdFile = new File(testDir, "test_mmd.nc");
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

        try {
            mmdWriter.create(mmdFile, dimemsions, variablePrototypes, 2346);
        } finally {
            mmdWriter.close();
        }

        assertTrue(mmdFile.isFile());

        NetcdfFile mmd = null;
        try {
            mmd = NetcdfFile.open(mmdFile.getPath());

            assertGlobalAttribute("title", "FIDUCEO multi-sensor match-up dataset (MMD)", mmd);
            assertGlobalAttribute("institution", "Brockmann Consult GmbH", mmd);
            assertGlobalAttribute("contact", "Tom Block (tom.block@brockmann-consult.de)", mmd);
            assertGlobalAttribute("license", "This dataset is released for use under CC-BY licence and was developed in the EC FIDUCEO project \"Fidelity and Uncertainty in Climate Data Records from Earth Observations\". Grant Agreement: 638822.", mmd);
            assertGlobalDateAttribute("creation_date", TimeUtils.createNow(), mmd);

            assertDimension("avhrr-n11_nx", 5, mmd);
            assertDimension("avhrr-n11_ny", 7, mmd);
            assertDimension("avhrr-n12_nx", 3, mmd);
            assertDimension("avhrr-n12_ny", 5, mmd);
            assertDimension("matchup_count", 2346, mmd);

            Variable variable = mmd.findVariable("avhrr-n11_ch3b");
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

        } finally {
            if (mmd != null) {
                mmd.close();
            }
        }
    }

    private void assertCorrectDimensions(Variable variable, int z, int y, int x) {
        assertEquals(z, variable.getDimension(0).getLength());
        assertEquals(y, variable.getDimension(1).getLength());
        assertEquals(x, variable.getDimension(2).getLength());
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
