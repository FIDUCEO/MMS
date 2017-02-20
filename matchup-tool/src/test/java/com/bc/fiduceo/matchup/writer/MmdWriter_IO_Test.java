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

import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.*;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.core.UseCaseConfigBuilder;
import com.bc.fiduceo.matchup.Delegator_MatchupTool;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.netcdf3.N3iosp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
    private MmdWriterConfig writerConfig;

    @Before
    public void setUp() throws Exception {
        testDir = TestUtil.createTestDirectory();
        writerConfig = new MmdWriterConfig();
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testCreate() throws IOException, InvalidRangeException {
        final MmdWriterNC3 mmdWriter = new MmdWriterNC3(writerConfig);

        final List<IOVariable> ioVariables = new ArrayList<>();
        WindowReadingIOVariable ioVariable;

        ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setTargetVariableName("avhrr-n11_ch3b");
        ioVariable.setDimensionNames("matchup_count avhrr-n11_ny avhrr-n11_nx");
        ioVariable.setDataType("short");
        ioVariable.setAttributes(new ArrayList<>());
        ioVariables.add(ioVariable);

        ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setTargetVariableName("avhrr-n12_ch4");
        ioVariable.setDimensionNames("matchup_count avhrr-n12_ny avhrr-n12_nx");
        ioVariable.setDataType("int");
        ioVariable.setAttributes(new ArrayList<>());
        ioVariables.add(ioVariable);

        ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setTargetVariableName("avhrr-n12_cloud_mask");
        ioVariable.setDimensionNames("matchup_count avhrr-n12_ny avhrr-n12_nx");
        ioVariable.setDataType("byte");
        ioVariable.setAttributes(new ArrayList<>());
        ioVariables.add(ioVariable);

        ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setTargetVariableName("avhrr-n12_dtime");
        ioVariable.setDimensionNames("matchup_count avhrr-n12_ny avhrr-n12_nx");
        ioVariable.setDataType("float");
        ioVariable.setAttributes(new ArrayList<>());
        ioVariables.add(ioVariable);

        final Sensor primarySensor = new Sensor("avhrr-n11");
        primarySensor.setPrimary(true);

        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder
                    .build("useCaseName")
                    .withDimensions(Arrays.asList(
                                new Dimension("avhrr-n11", 5, 7),
                                new Dimension("avhrr-n12", 3, 5)))
                    .withSensors(Arrays.asList(
                                primarySensor,
                                new Sensor("avhrr-n12")))
                    .createConfig();

        final Path mmdFile = Paths.get(testDir.toURI()).resolve("test_mmd.nc");

        try {
            mmdWriter.initializeNetcdfFile(mmdFile, useCaseConfig, ioVariables, 2346);
        } finally {
            mmdWriter.close();
        }

        assertTrue(Files.isRegularFile(mmdFile));

        NetcdfFile mmd = null;
        try {
            mmd = NetcdfFile.open(mmdFile.toString());

            assertEquals(8, mmd.getGlobalAttributes().size());

            assertGlobalAttribute("title", "FIDUCEO multi-sensor match-up dataset (MMD)", mmd);
            assertGlobalAttribute("institution", "Brockmann Consult GmbH", mmd);
            assertGlobalAttribute("contact", "Tom Block (tom.block@brockmann-consult.de)", mmd);
            assertGlobalAttribute("license", "This dataset is released for use under CC-BY licence and was developed in the EC FIDUCEO project \"Fidelity and Uncertainty in Climate Data Records from Earth Observations\". Grant Agreement: 638822.", mmd);
            assertGlobalDateAttribute("creation_date", TimeUtils.createNow(), mmd);
            assertGlobalAttribute("software_version", FiduceoConstants.VERSION, mmd);

            final Attribute comment = mmd.findGlobalAttribute("comment");
            assertNotNull(comment);
            assertEquals(DataType.STRING, comment.getDataType());
            assertEquals("This MMD file is created based on the use case configuration documented in the attribute 'use-case-configuration'.",
                    comment.getStringValue()
            );

            final Attribute useCaseConfigAttr = mmd.findGlobalAttribute("use-case-configuration");
            assertNotNull(useCaseConfigAttr);
            assertEquals(DataType.STRING, useCaseConfigAttr.getDataType());

            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<use-case-config name=\"useCaseName\">");
            pw.println("  <dimensions>");
            pw.println("    <dimension name=\"avhrr-n11\">");
            pw.println("      <nx>5</nx>");
            pw.println("      <ny>7</ny>");
            pw.println("    </dimension>");
            pw.println("    <dimension name=\"avhrr-n12\">");
            pw.println("      <nx>3</nx>");
            pw.println("      <ny>5</ny>");
            pw.println("    </dimension>");
            pw.println("  </dimensions>");
            pw.println("  <sensors>");
            pw.println("    <sensor>");
            pw.println("      <name>avhrr-n11</name>");
            pw.println("      <primary>true</primary>");
            pw.println("    </sensor>");
            pw.println("    <sensor>");
            pw.println("      <name>avhrr-n12</name>");
            pw.println("      <primary>false</primary>");
            pw.println("    </sensor>");
            pw.println("  </sensors>");
            pw.println("</use-case-config>");
            pw.flush();

            assertThat(sw.toString(), equalToIgnoringWhiteSpace(useCaseConfigAttr.getStringValue()));


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
            att = variable.findAttribute("_FillValue");
            assertNotNull(att);
            assertEquals(N3iosp.NC_FILL_FLOAT, att.getNumericValue().floatValue(), 1e-8);

            final List<Variable> variables = mmd.getVariables();
            assertEquals(4, variables.size());
        } finally {
            if (mmd != null) {
                mmd.close();
            }
        }
    }

    @Test
    public void testWrite_usecase02_AVHRR_NC3() throws IOException, InvalidRangeException {
        final MmdWriter mmdWriter = new MmdWriterNC3(writerConfig);
        execute_usecase_02(mmdWriter);
    }

    @Test
    public void testWrite_usecase02_AVHRR_NC4() throws IOException, InvalidRangeException {
        final MmdWriter mmdWriter = new MmdWriterNC4(writerConfig);
        execute_usecase_02(mmdWriter);
    }

    private static MatchupCollection createMatchupCollection_AVHRR(File testDataDirectory) {
        final MatchupCollection matchupCollection = new MatchupCollection();
        final MatchupSet matchupSet = new MatchupSet();
        final String primaryPath = TestUtil.assembleFileSystemPath(new String[]{testDataDirectory.getAbsolutePath(), "avhrr-n10", "v01.2", "1989", "05", "01", "19890501225800-ESACCI-L1C-AVHRR10_G-fv01.0.nc"}, false);
        matchupSet.setPrimaryObservationPath(Paths.get(primaryPath));
        final String secondaryPath = TestUtil.assembleFileSystemPath(new String[]{testDataDirectory.getAbsolutePath(), "avhrr-n11", "v01.2", "1989", "05", "02", "19890502001800-ESACCI-L1C-AVHRR11_G-fv01.0.nc"}, false);
        matchupSet.setSecondaryObservationPath(Paths.get(secondaryPath));
        for (int i = 0; i < 8; i++) {
            final SampleSet sampleSet = new SampleSet();
            sampleSet.setPrimary(new Sample(0, 8981 + i, 34.726, -67.245, 610071188));
            sampleSet.setSecondary(new Sample(408, 819 + i, 34.793, -67.246, 610071904));
            matchupSet.getSampleSets().add(sampleSet);
        }
        matchupCollection.add(matchupSet);
        return matchupCollection;
    }

    private void execute_usecase_02(MmdWriter mmdWriter) throws IOException, InvalidRangeException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();

        final MatchupCollection matchupCollection = createMatchupCollection_AVHRR(testDataDirectory);

        final ToolContext context = new ToolContext();
        final UseCaseConfig useCaseConfig = createUseCaseConfig_AVHRR();
        context.setUseCaseConfig(useCaseConfig);
        context.setStartDate(TimeUtils.parseDOYBeginOfDay("1989-122"));
        context.setEndDate(TimeUtils.parseDOYEndOfDay("1989-123"));

        final ReaderFactory readerFactory = ReaderFactory.get(context.getGeometryFactory());
        context.setReaderFactory(readerFactory);
        final IOVariablesList ioVariablesList = new IOVariablesList(readerFactory);

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();

        Delegator_MatchupTool.createIOVariablesPerSensor(ioVariablesList, matchupCollection, useCaseConfig, variablesConfiguration);
        mmdWriter.writeMMD(matchupCollection, context, ioVariablesList);

        NetcdfFile netcdfFile = null;
        try {
            netcdfFile = NetcdfFile.open(testDir.getAbsolutePath() + File.separator + "mmd02_avhrr-n10_avhrr-n11_1989-122_1989-123.nc");

            NCTestUtils.assertVectorVariable("avhrr-n10_x", 0, 0.0, netcdfFile);
            NCTestUtils.assertVectorVariable("avhrr-n10_y", 1, 8982.0, netcdfFile);
            NCTestUtils.assertStringVariable("avhrr-n10_file_name", 2, "19890501225800-ESACCI-L1C-AVHRR10_G-fv01.0.nc", netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_acquisition_time", 0, 0, 3, -1.0, netcdfFile);

            NCTestUtils.assertVectorVariable("avhrr-n11_x", 4, 408.0, netcdfFile);
            NCTestUtils.assertVectorVariable("avhrr-n11_y", 5, 824.0, netcdfFile);
            NCTestUtils.assertStringVariable("avhrr-n11_file_name", 6, "19890502001800-ESACCI-L1C-AVHRR11_G-fv01.0.nc", netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_acquisition_time", 1, 0, 7, 610071907.0, netcdfFile);

            NCTestUtils.assert3DVariable("avhrr-n11_lat", 2, 0, 0, -67.18399810791016, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_lon", 3, 0, 1, -32768.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_dtime", 4, 0, 2, N3iosp.NC_FILL_FLOAT, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_ch1", 0, 1, 3, 0.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_ch2", 1, 1, 4, 0.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_ch3b", 2, 1, 5, -2968.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_ch4", 3, 1, 6, -32768.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_ch5", 4, 1, 7, -32768.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_cloud_mask", 0, 2, 0, 7.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_cloud_probability", 1, 2, 1, -128.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_ict_temp", 3, 2, 3, -32768.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_l1b_line_number", 4, 2, 4, -32768.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_qual_flags", 0, 3, 5, 0.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_relative_azimuth_angle", 1, 3, 6, -32768.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_satellite_zenith_angle", 2, 3, 7, 6957.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n11_solar_zenith_angle", 3, 3, 0, -32768.0, netcdfFile);

            NCTestUtils.assert3DVariable("avhrr-n10_lat", 4, 3, 1, -67.66300201416016, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_lon", 0, 4, 2, -32768.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_dtime", 1, 4, 3, N3iosp.NC_FILL_FLOAT, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_ch1", 2, 4, 4, 0.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_ch2", 3, 4, 5, 46.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_ch3b", 4, 4, 6, -1197.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_ch4", 0, 0, 7, -32768.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_cloud_mask", 1, 0, 0, -128.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_cloud_probability", 1, 0, 1, -128.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_ict_temp", 2, 0, 2, 2052.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_l1b_line_number", 3, 0, 3, 8983.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_qual_flags", 4, 0, 4, 0.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_relative_azimuth_angle", 0, 1, 5, -32768.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_satellite_zenith_angle", 1, 1, 6, -32768.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_satellite_zenith_angle", 2, 1, 6, 6844.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_satellite_zenith_angle", 3, 1, 6, 6797.0, netcdfFile);
            NCTestUtils.assert3DVariable("avhrr-n10_solar_zenith_angle", 4, 1, 7, 12181.0, netcdfFile);
        } finally {
            if (netcdfFile != null) {
                netcdfFile.close();
            }
        }
    }

    private UseCaseConfig createUseCaseConfig_AVHRR() throws IOException {
        final Sensor primary = new Sensor("avhrr-n10");
        primary.setPrimary(true);

        return UseCaseConfigBuilder
                    .build("mmd02")
                    .withSensors(Arrays.asList(
                                primary,
                                new Sensor("avhrr-n11")))
                    .withDimensions(Arrays.asList(
                                new Dimension("avhrr-n10", 5, 5),
                                new Dimension("avhrr-n11", 5, 5)))
                    .withOutputPath(testDir.getAbsolutePath())
                    .createConfig();
    }

    private void assertCorrectDimensions(Variable variable, int z, int y, int x) {
        assertEquals(z, variable.getDimension(0).getLength());
        assertEquals(y, variable.getDimension(1).getLength());
        assertEquals(x, variable.getDimension(2).getLength());
    }

    private void assertDimension(String name, int expected, NetcdfFile mmd) {
        final int dimensionLength = NetCDFUtils.getDimensionLength(name, mmd);
        assertEquals(expected, dimensionLength);
    }

    private void assertGlobalDateAttribute(String name, Date expected, NetcdfFile mmd) {
        final Attribute creation_date = mmd.findGlobalAttribute(name);
        assertNotNull(creation_date);
        final String dateStringValue = creation_date.getStringValue();
        final Date actual = TimeUtils.parse(dateStringValue, "yyyy-MM-dd HH:mm:ss");
        TestUtil.assertWithinLastMinute(expected, actual);
    }

    private void assertGlobalAttribute(String name, String value, NetcdfFile mmd) {
        final Attribute globalAttribute = mmd.findGlobalAttribute(name);
        assertNotNull(globalAttribute);
        assertEquals(value, globalAttribute.getStringValue());
    }
}
