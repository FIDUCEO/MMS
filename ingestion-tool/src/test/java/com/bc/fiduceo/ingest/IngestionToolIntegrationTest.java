/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

import com.bc.fiduceo.TestData;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.tool.ToolContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("ConstantConditions")
@RunWith(DbAndIOTestRunner.class)
public class IngestionToolIntegrationTest {

    private File configDir;
    private GeometryFactory geometryFactory;
    private Storage storage;

    @Before
    public void setUp() throws SQLException, IOException {
        final File testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create testGroupInputProduct directory: " + configDir.getAbsolutePath());
        }

        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        storage = Storage.create(TestUtil.getdatasourceMongoDb(), geometryFactory);

        TestUtil.writeMmdWriterConfig(configDir);
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemConfig(configDir);
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testIngest_notInputParameter() throws ParseException, IOException, SQLException {
        final String[] args = new String[0];
        final boolean errorOutputExpected = true;

        callMainAndValidateSystemOutput(args, errorOutputExpected);
    }

    @Test
    public void testIngest_help() throws ParseException, IOException, SQLException {
        final boolean errorOutputExpected = false;
        String[] args = new String[]{"-h"};
        callMainAndValidateSystemOutput(args, errorOutputExpected);

        args = new String[]{"--help"};
        callMainAndValidateSystemOutput(args, errorOutputExpected);
    }

    @Test
    public void testIngest_errorStopDateBeforeStartDate() throws ParseException, IOException, SQLException {
        final String[] args = new String[]{
                    "-c", configDir.getAbsolutePath(), "-s", "iasi-mb", "-v", "v0-0N",
                    "-start", "2001-02-02",
                    "-end", "2001-01-01"
        };
        final CommandLineParser parser = new PosixParser();
        final CommandLine commandLine = parser.parse(IngestionTool.getOptions(), args);

        final IngestionTool ingestionTool = new IngestionTool();
        try {
            ingestionTool.run(commandLine);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getClass().getTypeName(), is(equalTo("java.lang.RuntimeException")));
            assertThat(expected.getMessage(), is(equalTo("End date before start date")));
        }
    }

    @Test
    public void testInitializeContext() throws IOException, SQLException {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn("1979-231");
        when(commandLine.getOptionValue("end")).thenReturn("1979-238");

        final ToolContext toolContext = IngestionTool.initializeContext(commandLine, Paths.get(configDir.getAbsolutePath()));
        assertNotNull(toolContext);
        assertEquals(303868800000L, toolContext.getStartDate().getTime());
        assertEquals(304473600000L, toolContext.getEndDate().getTime());

        final GeometryFactory geometryFactory = toolContext.getGeometryFactory();
        assertNotNull(geometryFactory);

        final Storage storage = toolContext.getStorage();
        assertNotNull(storage);
        assertTrue(storage.isInitialized());

        final SystemConfig systemConfig = toolContext.getSystemConfig();
        assertEquals("S2", systemConfig.getGeometryLibraryType());

        final UseCaseConfig useCaseConfig = toolContext.getUseCaseConfig();
        assertNull(useCaseConfig);  // IngestionTool does not use this tb 2017-07-18

        final ReaderFactory readerFactory = toolContext.getReaderFactory();
        assertNotNull(readerFactory);
    }

    @Test
    public void testIngest_AVHRR_GAC_NOAA17_v012() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-n17", "-start", "2007-091", "-end", "2007-093", "-v", "1.01"};

        try {
            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc", satelliteObservations);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 3, 34, 54, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 5, 28, 48, 0, observation.getStopTime());
            assertEquals("avhrr-n17", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "1.01", "2007", "04", "01", "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("1.01", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);
            assertEquals(TestData.AVHRR_GAC_N17_GEOMETRIES_v012[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.AVHRR_GAC_N17_GEOMETRIES_v012[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 3, 34, 54, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 4, 31, 51, 0, timeAxes[0].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N17_AXIS_GEOMETRIES_v012[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(2007, 4, 1, 4, 31, 51, 0, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 5, 28, 48, 0, timeAxes[1].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N17_AXIS_GEOMETRIES_v012[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_AVHRR_GAC_NOAA18_v012() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-n18", "-start", "2007-090", "-end", "2007-092", "-v", "1.02"};

        try {
            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc", satelliteObservations);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 4, 12, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 9, 46, 11, 0, observation.getStopTime());
            assertEquals("avhrr-n18", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n18", "1.02", "2007", "04", "01", "20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("1.02", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);
            assertEquals(TestData.AVHRR_GAC_N18_GEOMETRIES_v012[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.AVHRR_GAC_N18_GEOMETRIES_v012[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 4, 12, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 55, 11, 500, timeAxes[0].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N18_AXIS_GEOMETRIES_v012[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 55, 11, 500, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 9, 46, 11, 0, timeAxes[1].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N18_AXIS_GEOMETRIES_v012[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_AVHRR_GAC_NOAA10_v013() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-n10", "-start", "1988-078", "-end", "1988-078", "-v", "v01.3"};

        try {
            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("19880318000900-ESACCI-L1C-AVHRR10_G-fv01.0.nc", satelliteObservations);
            TestUtil.assertCorrectUTCDate(1988, 3, 18, 0, 9, 17, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(1988, 3, 18, 2, 3, 15, 0, observation.getStopTime());
            assertEquals("avhrr-n10", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n10", "v01.3", "1988", "03", "18", "19880318000900-ESACCI-L1C-AVHRR10_G-fv01.0.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v01.3", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);
            assertEquals(TestData.AVHRR_GAC_N10_GEOMETRIES_v013[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.AVHRR_GAC_N10_GEOMETRIES_v013[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(1988, 3, 18, 0, 9, 17, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(1988, 3, 18, 1, 6, 16, 0, timeAxes[0].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N10_AXIS_GEOMETRIES_v013[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(1988, 3, 18, 1, 6, 16, 0, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(1988, 3, 18, 2, 3, 15, 0, timeAxes[1].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N10_AXIS_GEOMETRIES_v013[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_AVHRR_GAC_NOAA11_v013() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-n11", "-start", "1991-129", "-end", "1991-129", "-v", "v01.3"};

        try {
            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("19910509075100-ESACCI-L1C-AVHRR11_G-fv01.0.nc", satelliteObservations);
            TestUtil.assertCorrectUTCDate(1991, 5, 9, 7, 51, 46, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(1991, 5, 9, 9, 45, 41, 0, observation.getStopTime());
            assertEquals("avhrr-n11", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n11", "v01.3", "1991", "05", "09", "19910509075100-ESACCI-L1C-AVHRR11_G-fv01.0.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v01.3", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);
            assertEquals(TestData.AVHRR_GAC_N11_GEOMETRIES_v013[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.AVHRR_GAC_N11_GEOMETRIES_v013[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(1991, 5, 9, 7, 51, 46, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(1991, 5, 9, 8, 48, 43, 500, timeAxes[0].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N11_AXIS_GEOMETRIES_v013[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(1991, 5, 9, 8, 48, 43, 500, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(1991, 5, 9, 9, 45, 41, 0, timeAxes[1].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N11_AXIS_GEOMETRIES_v013[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_AMSUB_NOAA15() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "amsub-n15", "-start", "2007-233", "-end", "2007-235", "-v", "v1.0"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(3, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5", satelliteObservations);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 16, 40, 37, 120, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 18, 24, 53, 119, observation.getStopTime());
            assertEquals("amsub-n15", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsub-n15", "v1.0", "2007", "08", "22", "L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v1.0", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);
            assertEquals(TestData.AMSUB_N15_GEOMETRIES[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.AMSUB_N15_GEOMETRIES[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 16, 40, 37, 120, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 17, 32, 45, 119, timeAxes[0].getEndTime());
            assertEquals(TestData.AMSUB_N15_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(2007, 8, 22, 17, 32, 45, 119, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 18, 24, 53, 119, timeAxes[1].getEndTime());
            assertEquals(TestData.AMSUB_N15_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_AMSUB_NOAA15_twice() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "amsub-n15", "-start", "2007-233", "-end", "2007-235", "-v", "v1.0"};

        try {
            IngestionToolMain.main(args);
            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(3, satelliteObservations.size());
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_MHS_NOAA18() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "mhs-n18", "-start", "2007-233", "-end", "2007-235", "-v", "v1.0"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(3, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("NSS.MHSX.NN.D07234.S1151.E1337.B1162021.GC.h5", satelliteObservations);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 11, 51, 27, 277, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 13, 37, 32, 610, observation.getStopTime());
            assertEquals("mhs-n18", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mhs-n18", "v1.0", "2007", "08", "22", "NSS.MHSX.NN.D07234.S1151.E1337.B1162021.GC.h5"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v1.0", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);
            assertEquals(TestData.MHS_N18_GEOMETRIES[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.MHS_N18_GEOMETRIES[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 11, 51, 27, 277, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 12, 44, 29, 943, timeAxes[0].getEndTime());
            assertEquals(TestData.MHS_N18_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(2007, 8, 22, 12, 44, 29, 943, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 13, 37, 32, 610, timeAxes[1].getEndTime());
            assertEquals(TestData.MHS_N18_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_HIRS_TIROSN() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "hirs-tn", "-start", "1979-286", "-end", "1979-288", "-v", "1.0"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc", satelliteObservations);

            TestUtil.assertCorrectUTCDate(1979, 10, 14, 16, 23, 59, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 18, 7, 33, 0, observation.getStopTime());
            assertEquals("hirs-tn", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-tn", "1.0", "1979", "10", "14", "NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("1.0", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            assertEquals(TestData.HIRS_TN_GEOMETRIES[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.HIRS_TN_GEOMETRIES[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 16, 23, 59, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 17, 15, 46, 0, timeAxes[0].getEndTime());
            assertEquals(TestData.HIRS_TN_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(1979, 10, 14, 17, 15, 46, 0, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 18, 7, 33, 0, timeAxes[1].getEndTime());
            assertEquals(TestData.HIRS_TN_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_HIRS_NOAA10() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "hirs-n10", "-start", "1989-076", "-end", "1989-077", "-v", "1.0"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("NSS.HIRX.NG.D89076.S0608.E0802.B1296162.WI.nc", satelliteObservations);

            TestUtil.assertCorrectUTCDate(1989, 3, 17, 6, 12, 16, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 8, 2, 2, 0, observation.getStopTime());
            assertEquals("hirs-n10", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-n10", "1.0", "1989", "03", "17", "NSS.HIRX.NG.D89076.S0608.E0802.B1296162.WI.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("1.0", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            assertEquals(TestData.HIRS_N10_GEOMETRIES[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.HIRS_N10_GEOMETRIES[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 6, 12, 16, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 7, 7, 9, 0, timeAxes[0].getEndTime());
            assertEquals(TestData.HIRS_N10_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(1989, 3, 17, 7, 7, 9, 0, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 8, 2, 2, 0, timeAxes[1].getEndTime());
            assertEquals(TestData.HIRS_N10_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_HIRS_METOPA() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "hirs-ma", "-start", "2011-234", "-end", "2011-236", "-v", "1.0"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc", satelliteObservations);

            TestUtil.assertCorrectUTCDate(2011, 8, 23, 16, 41, 52, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 18, 22, 40, 0, observation.getStopTime());
            assertEquals("hirs-ma", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-ma", "1.0", "2011", "08", "23", "190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("1.0", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            assertEquals(TestData.HIRS_MA_GEOMETRIES[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.HIRS_MA_GEOMETRIES[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 16, 41, 52, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 17, 32, 16, 0, timeAxes[0].getEndTime());
            assertEquals(TestData.HIRS_MA_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(2011, 8, 23, 17, 32, 16, 0, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 18, 22, 40, 0, timeAxes[1].getEndTime());
            assertEquals(TestData.HIRS_MA_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_ATSR1() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "atsr-e1", "-start", "1993-217", "-end", "1993-217", "-v", "v3"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("AT1_TOA_1PURAL19930805_210030_000000004015_00085_10751_0000.E1", satelliteObservations);

            TestUtil.assertCorrectUTCDate(1993, 8, 5, 21, 0, 30, 240, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 22, 41, 8, 490, observation.getStopTime());
            assertEquals("atsr-e1", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"atsr-e1", "v3", "1993", "08", "05", "AT1_TOA_1PURAL19930805_210030_000000004015_00085_10751_0000.E1"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v3", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof Polygon);

            assertEquals(TestData.ATSR1_GEOMETRY, geometryFactory.format(geoBounds));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(1, timeAxes.length);
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 21, 0, 30, 240, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 22, 41, 8, 490, timeAxes[0].getEndTime());
            assertEquals(TestData.ATSR1_AXIS_GEOMETRY, geometryFactory.format(timeAxes[0].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_ATSR2() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "atsr-e2", "-start", "1998-114", "-end", "1998-114", "-v", "v3"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.E2", satelliteObservations);

            TestUtil.assertCorrectUTCDate(1998, 4, 24, 5, 57, 54, 720, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 7, 38, 32, 970, observation.getStopTime());
            assertEquals("atsr-e2", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"atsr-e2", "v3", "1998", "04", "24", "AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.E2"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v3", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof Polygon);

            assertEquals(TestData.ATSR2_GEOMETRY, geometryFactory.format(geoBounds));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(1, timeAxes.length);
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 5, 57, 54, 720, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 7, 38, 32, 970, timeAxes[0].getEndTime());
            assertEquals(TestData.ATSR2_AXIS_GEOMETRY, geometryFactory.format(timeAxes[0].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_AATSR() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "aatsr-en", "-start", "2006-046", "-end", "2006-046", "-v", "v3"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("ATS_TOA_1PUUPA20060215_070852_000065272045_00120_20715_4282.N1", satelliteObservations);

            TestUtil.assertCorrectUTCDate(2006, 2, 15, 7, 8, 52, 812, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 8, 57, 40, 662, observation.getStopTime());
            assertEquals("aatsr-en", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"aatsr-en", "v3", "2006", "02", "15", "ATS_TOA_1PUUPA20060215_070852_000065272045_00120_20715_4282.N1"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v3", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof Polygon);

            assertEquals(TestData.AATSR_GEOMETRY, geometryFactory.format(geoBounds));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(1, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 7, 8, 52, 812, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 8, 57, 40, 662, timeAxes[0].getEndTime());
            assertEquals(TestData.AATSR_AXIS_GEOMETRY, geometryFactory.format(timeAxes[0].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_AMSRE() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "amsre-aq", "-start", "2005-048", "-end", "2005-048", "-v", "v12"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf", satelliteObservations);

            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 36, 6, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 56, 0, observation.getStopTime());
            assertEquals("amsre-aq", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsre-aq", "v12", "2005", "02", "17", "AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.DESCENDING, observation.getNodeType());
            assertEquals("v12", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof Polygon);

            assertEquals(TestData.AMSRE_GEOMETRY, geometryFactory.format(geoBounds));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(1, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 36, 6, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 56, 0, timeAxes[0].getEndTime());
            assertEquals(TestData.AMSRE_AXIS_GEOMETRY, geometryFactory.format(timeAxes[0].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_SSMT2() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "ssmt2-f14", "-start", "2001-165", "-end", "2001-165", "-v", "v01"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("F14200106141229.nc", satelliteObservations);
            TestUtil.assertCorrectUTCDate(2001, 6, 14, 12, 29, 4, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2001, 6, 14, 14, 10, 58, 0, observation.getStopTime());

            assertEquals("ssmt2-f14", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"ssmt2-f14", "v01", "2001", "06", "14", "F14200106141229.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.ASCENDING, observation.getNodeType());
            assertEquals("v01", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof Polygon);
            assertEquals(TestData.SSMT2_GEOMETRY, geometryFactory.format(geoBounds));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(1, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2001, 6, 14, 12, 29, 4, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2001, 6, 14, 14, 10, 58, 0, timeAxes[0].getEndTime());
            assertEquals(TestData.SSMT2_AXIS_GEOMETRY, geometryFactory.format(timeAxes[0].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_insitu_SST_Drifter_v33() throws SQLException, IOException, ParseException {
        // @todo 2 tb/tb we have to supply dates here - which are not used during ingestion - rethink this 2016-11-03
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "drifter-sst", "-start", "2001-165", "-end", "2001-165", "-v", "v03.3"};

        try {
            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(4, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("insitu_0_WMOID_51993_20040402_20060207.nc", satelliteObservations);
            TestUtil.assertCorrectUTCDate(2004, 4, 2, 18, 43, 47, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2006, 2, 7, 5, 17, 59, 0, observation.getStopTime());

            assertEquals("drifter-sst", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "drifter-sst", "v03.3", "insitu_0_WMOID_51993_20040402_20060207.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v03.3", observation.getVersion());

            assertNull(observation.getGeoBounds());
            assertNull(observation.getTimeAxes());
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_insitu_SST_Drifter_v40() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "drifter-sst", "-start", "1996-245", "-end", "1996-248", "-v", "v04.0"};

        try {
            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("insitu_0_WMOID_42531_19960904_19960909.nc", satelliteObservations);
            TestUtil.assertCorrectUTCDate(1996, 9, 4, 22, 0, 0, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(1996, 9, 9, 13, 19, 47, 0, observation.getStopTime());

            assertEquals("drifter-sst", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "drifter-sst", "v04.0", "insitu_0_WMOID_42531_19960904_19960909.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v04.0", observation.getVersion());

            assertNull(observation.getGeoBounds());
            assertNull(observation.getTimeAxes());
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_IASI_MA() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "iasi-ma", "-start", "2016-001", "-end", "2016-001", "-v", "v3-6N"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat", satelliteObservations);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 12, 47, 54, 870, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 14, 26, 58, 414, observation.getStopTime());

            assertEquals("iasi-ma", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"iasi-ma", "v3-6N", "2016", "01", "IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v3-6N", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);
            assertEquals(TestData.IASI_MA_GEOMETRIES[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.IASI_MA_GEOMETRIES[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 12, 47, 54, 870, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 13, 37, 26, 642, timeAxes[0].getEndTime());
            assertEquals(TestData.IASI_MA_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(2016, 1, 1, 13, 37, 26, 642, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 14, 26, 58, 414, timeAxes[1].getEndTime());
            assertEquals(TestData.IASI_MA_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_MYD06_AQUA() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "myd06-aq", "-start", "2009-133", "-end", "2009-133", "-v", "v006"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("MYD06_L2.A2009133.1035.006.2014062050327.hdf", satelliteObservations);
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 35, 0, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 40, 0, 0, observation.getStopTime());

            assertEquals("myd06-aq", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"myd06-aq", "v006", "2009", "133", "MYD06_L2.A2009133.1035.006.2014062050327.hdf"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v006", observation.getVersion());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof Polygon);

            assertEquals(TestData.MYD06_AQUA_GEOMETRY, geometryFactory.format(geoBounds));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(1, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 35, 0, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 40, 0, 0, timeAxes[0].getEndTime());
            assertEquals(TestData.MYD06_AQUA_AXIS_GEOMETRY, geometryFactory.format(timeAxes[0].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_OceanRain_Insitu() throws SQLException, IOException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "ocean-rain-sst", "-start", "2011-133", "-end", "2011-134", "-v", "v1.0"};

        try {
            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = getSatelliteObservation("OceanRAIN_allships_2010-2017_SST.ascii", satelliteObservations);
            TestUtil.assertCorrectUTCDate(2010, 6, 10, 21, 0, 0, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2016, 9, 16, 22, 59, 0, 0, observation.getStopTime());

            assertEquals("ocean-rain-sst", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ocean-rain-sst", "v1.0", "OceanRAIN_allships_2010-2017_SST.ascii"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
            assertEquals("v1.0", observation.getVersion());

            assertNull(observation.getGeoBounds());
            assertNull(observation.getTimeAxes());
        } finally {
            storage.clear();
            storage.close();
        }
    }

    private void callMainAndValidateSystemOutput(String[] args, boolean errorOutputExpected) throws ParseException, IOException, SQLException {
        final ByteArrayOutputStream expected = new ByteArrayOutputStream();
        new IngestionTool().printUsageTo(expected);

        final PrintStream _err = System.err;
        final PrintStream _out = System.out;
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ByteArrayOutputStream err = new ByteArrayOutputStream();
            final PrintStream psO = new PrintStream(out);
            final PrintStream psE = new PrintStream(err);
            System.setOut(psO);
            System.setErr(psE);

            IngestionToolMain.main(args);

            psO.flush();
            psE.flush();
            if (errorOutputExpected) {
                assertThat(out.toString(), is(equalTo("")));
                assertThat(err.toString(), is(equalTo(expected.toString())));
            } else {
                assertThat(out.toString(), is(equalTo(expected.toString())));
                assertThat(err.toString(), is(equalTo("")));
            }
        } finally {
            System.setErr(_err);
            System.setOut(_out);
        }
    }

    private SatelliteObservation getSatelliteObservation(String name, List<SatelliteObservation> satelliteObservations) {
        for (final SatelliteObservation observation : satelliteObservations) {
            if (observation.getDataFilePath().endsWith(name)) {
                return observation;
            }
        }
        fail("requested observation not in database: " + name);
        return null;
    }
}
