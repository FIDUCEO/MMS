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
import com.bc.fiduceo.db.DatabaseConfig;
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
        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setDataSource(TestUtil.getDataSource_MongoDb());
        storage = Storage.create(databaseConfig, geometryFactory);
        //storage = Storage.create(TestUtil.getDataSource_Postgres(), geometryFactory);

        TestUtil.writeMmdWriterConfig(configDir);
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        //TestUtil.writeDatabaseProperties_Postgres(configDir);
        TestUtil.writeSystemConfig(configDir);
    }

    @After
    public void tearDown() throws SQLException {
        if (storage != null) {
            storage.clear();
            storage.close();
            storage = null;
        }
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testIngest_notInputParameter() throws ParseException {
        final String[] args = new String[0];
        final boolean errorOutputExpected = true;

        callMainAndValidateSystemOutput(args, errorOutputExpected);
    }

    @Test
    public void testIngest_help() throws ParseException {
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
                "-start", "2001-004",
                "-end", "2001-001"
        };
        final CommandLineParser parser = new PosixParser();
        final CommandLine commandLine = parser.parse(IngestionTool.getOptions(), args);

        final IngestionTool ingestionTool = new IngestionTool();
        try {
            ingestionTool.run(commandLine);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("java.lang.RuntimeException", expected.getClass().getTypeName());
            assertEquals("End date before start date", expected.getMessage());
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
    public void testIngest_AVHRR_GAC_NOAA10_v013() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-n10", "-start", "1988-078", "-end", "1988-078", "-v", "v01.3"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("19880318000900-ESACCI-L1C-AVHRR10_G-fv01.0.nc", satelliteObservations);
        TestUtil.assertCorrectUTCDate(1988, 3, 18, 0, 9, 17, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(1988, 3, 18, 2, 3, 15, 0, observation.getStopTime());
        assertEquals("avhrr-n10", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n10", "v01.3", "1988", "03", "18", "19880318000900-ESACCI-L1C-AVHRR10_G-fv01.0.nc"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v01.3", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertTrue(geoBounds instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoBounds;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());
        assertEquals(TestData.AVHRR_GAC_N10_GEOMETRIES_v013[0], geometryFactory.format(polygons.get(0)));
        assertEquals(TestData.AVHRR_GAC_N10_GEOMETRIES_v013[1], geometryFactory.format(polygons.get(1)));

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        assertEquals(2, timeAxes.length);
        TestUtil.assertCorrectUTCDate(1988, 3, 18, 0, 9, 17, 0, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(1988, 3, 18, 1, 6, 16, 0, timeAxes[0].getEndTime());
        assertEquals(TestData.AVHRR_GAC_N10_AXIS_GEOMETRIES_v013[0], geometryFactory.format(timeAxes[0].getGeometry()));

        TestUtil.assertCorrectUTCDate(1988, 3, 18, 1, 6, 16, 0, timeAxes[1].getStartTime());
        TestUtil.assertCorrectUTCDate(1988, 3, 18, 2, 3, 15, 0, timeAxes[1].getEndTime());
        assertEquals(TestData.AVHRR_GAC_N10_AXIS_GEOMETRIES_v013[1], geometryFactory.format(timeAxes[1].getGeometry()));
    }

    @Test
    public void testIngest_AVHRR_GAC_NOAA11_v013() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-n11", "-start", "1991-129", "-end", "1991-129", "-v", "v01.3"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("19910509075100-ESACCI-L1C-AVHRR11_G-fv01.0.nc", satelliteObservations);
        TestUtil.assertCorrectUTCDate(1991, 5, 9, 7, 51, 46, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(1991, 5, 9, 9, 45, 41, 0, observation.getStopTime());
        assertEquals("avhrr-n11", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n11", "v01.3", "1991", "05", "09", "19910509075100-ESACCI-L1C-AVHRR11_G-fv01.0.nc"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v01.3", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertTrue(geoBounds instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoBounds;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());
        assertEquals(TestData.AVHRR_GAC_N11_GEOMETRIES_v013[0], geometryFactory.format(polygons.get(0)));
        assertEquals(TestData.AVHRR_GAC_N11_GEOMETRIES_v013[1], geometryFactory.format(polygons.get(1)));

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        assertEquals(2, timeAxes.length);
        TestUtil.assertCorrectUTCDate(1991, 5, 9, 7, 51, 46, 0, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(1991, 5, 9, 8, 48, 43, 500, timeAxes[0].getEndTime());
        assertEquals(TestData.AVHRR_GAC_N11_AXIS_GEOMETRIES_v013[0], geometryFactory.format(timeAxes[0].getGeometry()));

        TestUtil.assertCorrectUTCDate(1991, 5, 9, 8, 48, 43, 500, timeAxes[1].getStartTime());
        TestUtil.assertCorrectUTCDate(1991, 5, 9, 9, 45, 41, 0, timeAxes[1].getEndTime());
        assertEquals(TestData.AVHRR_GAC_N11_AXIS_GEOMETRIES_v013[1], geometryFactory.format(timeAxes[1].getGeometry()));
    }

    @Test
    public void testIngest_AVHRR_GAC_NOAA17_v014() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-n17", "-start", "2009-298", "-end", "2009-298", "-v", "v01.4-cspp"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("20091025080600-ESACCI-L1C-AVHRR17_G-fv01.0.nc", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2009, 10, 25, 8, 7, 39, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2009, 10, 25, 10, 0, 39, 0, observation.getStopTime());
        assertEquals("avhrr-n17", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "v01.4-cspp", "2009", "10", "25", "20091025080600-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v01.4-cspp", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertTrue(geoBounds instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoBounds;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());
        assertEquals(TestData.AVHRR_GAC_N17_GEOMETRIES_v014_CSPP[0], geometryFactory.format(polygons.get(0)));
        assertEquals(TestData.AVHRR_GAC_N17_GEOMETRIES_v014_CSPP[1], geometryFactory.format(polygons.get(1)));

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        assertEquals(2, timeAxes.length);
        TestUtil.assertCorrectUTCDate(2009, 10, 25, 8, 7, 39, 0, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(2009, 10, 25, 9, 4, 9, 0, timeAxes[0].getEndTime());
        assertEquals(TestData.AVHRR_GAC_N17_AXIS_GEOMETRIES_v014_CSPP[0], geometryFactory.format(timeAxes[0].getGeometry()));

        TestUtil.assertCorrectUTCDate(2009, 10, 25, 9, 4, 9, 0, timeAxes[1].getStartTime());
        TestUtil.assertCorrectUTCDate(2009, 10, 25, 10, 0, 39, 0, timeAxes[1].getEndTime());
        assertEquals(TestData.AVHRR_GAC_N17_AXIS_GEOMETRIES_v014_CSPP[1], geometryFactory.format(timeAxes[1].getGeometry()));
    }

    @Test
    public void testIngest_AMSUB_NOAA15() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "amsub-n15", "-start", "2007-233", "-end", "2007-235", "-v", "v1.0"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(3, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2007, 8, 22, 16, 40, 37, 120, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2007, 8, 22, 18, 24, 53, 119, observation.getStopTime());
        assertEquals("amsub-n15", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"amsub-n15", "v1.0", "2007", "08", "22", "L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v1.0", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertTrue(geoBounds instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoBounds;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());
        assertEquals(TestData.AMSUB_N15_GEOMETRIES[0], geometryFactory.format(polygons.get(0)));
        assertEquals(TestData.AMSUB_N15_GEOMETRIES[1], geometryFactory.format(polygons.get(1)));

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        assertEquals(2, timeAxes.length);
        TestUtil.assertCorrectUTCDate(2007, 8, 22, 16, 40, 37, 120, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(2007, 8, 22, 17, 32, 45, 119, timeAxes[0].getEndTime());
        assertEquals(TestData.AMSUB_N15_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

        TestUtil.assertCorrectUTCDate(2007, 8, 22, 17, 32, 45, 119, timeAxes[1].getStartTime());
        TestUtil.assertCorrectUTCDate(2007, 8, 22, 18, 24, 53, 119, timeAxes[1].getEndTime());
        assertEquals(TestData.AMSUB_N15_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
    }

    @Test
    public void testIngest_AMSUB_NOAA15_twice() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "amsub-n15", "-start", "2007-233", "-end", "2007-235", "-v", "v1.0"};

        IngestionToolMain.main(args);
        IngestionToolMain.main(args);

        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(3, satelliteObservations.size());
    }

    @Test
    public void testIngest_MHS_NOAA18() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "mhs-n18", "-start", "2007-233", "-end", "2007-235", "-v", "v1.0"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(3, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("NSS.MHSX.NN.D07234.S1151.E1337.B1162021.GC.h5", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2007, 8, 22, 11, 51, 27, 277, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2007, 8, 22, 13, 37, 32, 610, observation.getStopTime());
        assertEquals("mhs-n18", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"mhs-n18", "v1.0", "2007", "08", "22", "NSS.MHSX.NN.D07234.S1151.E1337.B1162021.GC.h5"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v1.0", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertTrue(geoBounds instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoBounds;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());

        assertEquals(TestData.MHS_N18_GEOMETRIES[0], geometryFactory.format(polygons.get(0)));
        assertEquals(TestData.MHS_N18_GEOMETRIES[1], geometryFactory.format(polygons.get(1)));

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        assertEquals(2, timeAxes.length);
        TestUtil.assertCorrectUTCDate(2007, 8, 22, 11, 51, 27, 277, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(2007, 8, 22, 12, 44, 29, 943, timeAxes[0].getEndTime());
        assertEquals(TestData.MHS_N18_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

        TestUtil.assertCorrectUTCDate(2007, 8, 22, 12, 44, 29, 943, timeAxes[1].getStartTime());
        TestUtil.assertCorrectUTCDate(2007, 8, 22, 13, 37, 32, 610, timeAxes[1].getEndTime());
        assertEquals(TestData.MHS_N18_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
    }

    @Test
    public void testIngest_HIRS_TIROSN() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "hirs-tn", "-start", "1979-286", "-end", "1979-288", "-v", "1.0"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc", satelliteObservations);

        TestUtil.assertCorrectUTCDate(1979, 10, 14, 16, 23, 59, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(1979, 10, 14, 18, 7, 33, 0, observation.getStopTime());
        assertEquals("hirs-tn", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"hirs-tn", "1.0", "1979", "10", "14", "NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("1.0", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertTrue(geoBounds instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoBounds;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());

        assertEquals(TestData.HIRS_TN_GEOMETRIES[0], geometryFactory.format(polygons.get(0)));
        assertEquals(TestData.HIRS_TN_GEOMETRIES[1], geometryFactory.format(polygons.get(1)));

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        assertEquals(2, timeAxes.length);
        TestUtil.assertCorrectUTCDate(1979, 10, 14, 16, 23, 59, 0, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(1979, 10, 14, 17, 15, 46, 0, timeAxes[0].getEndTime());
        assertEquals(TestData.HIRS_TN_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

        TestUtil.assertCorrectUTCDate(1979, 10, 14, 17, 15, 46, 0, timeAxes[1].getStartTime());
        TestUtil.assertCorrectUTCDate(1979, 10, 14, 18, 7, 33, 0, timeAxes[1].getEndTime());
        assertEquals(TestData.HIRS_TN_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
    }

    @Test
    public void testIngest_HIRS_NOAA10() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "hirs-n10", "-start", "1989-076", "-end", "1989-077", "-v", "1.0"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("NSS.HIRX.NG.D89076.S0608.E0802.B1296162.WI.nc", satelliteObservations);

        TestUtil.assertCorrectUTCDate(1989, 3, 17, 6, 12, 16, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(1989, 3, 17, 8, 2, 2, 0, observation.getStopTime());
        assertEquals("hirs-n10", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"hirs-n10", "1.0", "1989", "03", "17", "NSS.HIRX.NG.D89076.S0608.E0802.B1296162.WI.nc"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("1.0", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertTrue(geoBounds instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoBounds;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());

        assertEquals(TestData.HIRS_N10_GEOMETRIES[0], geometryFactory.format(polygons.get(0)));
        assertEquals(TestData.HIRS_N10_GEOMETRIES[1], geometryFactory.format(polygons.get(1)));

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        assertEquals(2, timeAxes.length);
        TestUtil.assertCorrectUTCDate(1989, 3, 17, 6, 12, 16, 0, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(1989, 3, 17, 7, 7, 9, 0, timeAxes[0].getEndTime());
        assertEquals(TestData.HIRS_N10_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

        TestUtil.assertCorrectUTCDate(1989, 3, 17, 7, 7, 9, 0, timeAxes[1].getStartTime());
        TestUtil.assertCorrectUTCDate(1989, 3, 17, 8, 2, 2, 0, timeAxes[1].getEndTime());
        assertEquals(TestData.HIRS_N10_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
    }

    @Test
    public void testIngest_HIRS_METOPA() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "hirs-ma", "-start", "2011-234", "-end", "2011-236", "-v", "1.0"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc", satelliteObservations);

        TestUtil.assertCorrectUTCDate(2011, 8, 23, 16, 41, 52, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2011, 8, 23, 18, 22, 40, 0, observation.getStopTime());
        assertEquals("hirs-ma", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"hirs-ma", "1.0", "2011", "08", "23", "190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("1.0", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertTrue(geoBounds instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoBounds;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());

        assertEquals(TestData.HIRS_MA_GEOMETRIES[0], geometryFactory.format(polygons.get(0)));
        assertEquals(TestData.HIRS_MA_GEOMETRIES[1], geometryFactory.format(polygons.get(1)));

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        assertEquals(2, timeAxes.length);
        TestUtil.assertCorrectUTCDate(2011, 8, 23, 16, 41, 52, 0, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(2011, 8, 23, 17, 32, 16, 0, timeAxes[0].getEndTime());
        assertEquals(TestData.HIRS_MA_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

        TestUtil.assertCorrectUTCDate(2011, 8, 23, 17, 32, 16, 0, timeAxes[1].getStartTime());
        TestUtil.assertCorrectUTCDate(2011, 8, 23, 18, 22, 40, 0, timeAxes[1].getEndTime());
        assertEquals(TestData.HIRS_MA_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
    }

    @Test
    public void testIngest_ATSR1() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "atsr-e1", "-start", "1993-217", "-end", "1993-217", "-v", "v3"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("AT1_TOA_1PURAL19930805_210030_000000004015_00085_10751_0000.E1", satelliteObservations);

        TestUtil.assertCorrectUTCDate(1993, 8, 5, 21, 0, 30, 240, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(1993, 8, 5, 22, 41, 8, 490, observation.getStopTime());
        assertEquals("atsr-e1", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"atsr-e1", "v3", "1993", "08", "05", "AT1_TOA_1PURAL19930805_210030_000000004015_00085_10751_0000.E1"}, false);
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
    }

    @Test
    public void testIngest_ATSR2() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "atsr-e2", "-start", "1998-114", "-end", "1998-114", "-v", "v3"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.E2", satelliteObservations);

        TestUtil.assertCorrectUTCDate(1998, 4, 24, 5, 57, 54, 720, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(1998, 4, 24, 7, 38, 32, 970, observation.getStopTime());
        assertEquals("atsr-e2", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"atsr-e2", "v3", "1998", "04", "24", "AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.E2"}, false);
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
    }

    @Test
    public void testIngest_AATSR() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "aatsr-en", "-start", "2006-046", "-end", "2006-046", "-v", "v3"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("ATS_TOA_1PUUPA20060215_070852_000065272045_00120_20715_4282.N1", satelliteObservations);

        TestUtil.assertCorrectUTCDate(2006, 2, 15, 7, 8, 52, 812, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2006, 2, 15, 8, 57, 40, 662, observation.getStopTime());
        assertEquals("aatsr-en", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"aatsr-en", "v3", "2006", "02", "15", "ATS_TOA_1PUUPA20060215_070852_000065272045_00120_20715_4282.N1"}, false);
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
    }

    @Test
    public void testIngest_AMSRE() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "amsre-aq", "-start", "2005-048", "-end", "2005-048", "-v", "v12"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf", satelliteObservations);

        TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 36, 6, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 56, 0, observation.getStopTime());
        assertEquals("amsre-aq", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"amsre-aq", "v12", "2005", "02", "17", "AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf"}, false);
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
    }

    @Test
    public void testIngest_SSMT2() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "ssmt2-f14", "-start", "2001-165", "-end", "2001-165", "-v", "v01"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("F14200106141229.nc", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2001, 6, 14, 12, 29, 4, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2001, 6, 14, 14, 10, 58, 0, observation.getStopTime());

        assertEquals("ssmt2-f14", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"ssmt2-f14", "v01", "2001", "06", "14", "F14200106141229.nc"}, false);
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
    }

    @Test
    public void testIngest_insitu_SST_Drifter_v33() throws SQLException, ParseException {
        // @todo 2 tb/tb we have to supply dates here - which are not used during ingestion - rethink this 2016-11-03
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "drifter-sst", "-start", "2001-165", "-end", "2001-165", "-v", "v03.3"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(4, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("insitu_0_WMOID_51993_20040402_20060207.nc", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2004, 4, 2, 18, 43, 47, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2006, 2, 7, 5, 17, 59, 0, observation.getStopTime());

        assertEquals("drifter-sst", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "drifter-sst", "v03.3", "insitu_0_WMOID_51993_20040402_20060207.nc"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v03.3", observation.getVersion());

        assertNull(observation.getGeoBounds());
        assertNull(observation.getTimeAxes());
    }

    @Test
    public void testIngest_insitu_SST_Drifter_v40() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "drifter-sst", "-start", "1996-245", "-end", "1996-248", "-v", "v04.0"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("insitu_0_WMOID_42531_19960904_19960909.nc", satelliteObservations);
        TestUtil.assertCorrectUTCDate(1996, 9, 4, 22, 0, 0, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(1996, 9, 9, 13, 19, 47, 0, observation.getStopTime());

        assertEquals("drifter-sst", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "drifter-sst", "v04.0", "insitu_0_WMOID_42531_19960904_19960909.nc"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v04.0", observation.getVersion());

        assertNull(observation.getGeoBounds());
        assertNull(observation.getTimeAxes());
    }

    @Test
    public void testIngest_insitu_Sirds_mooring() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "mooring-sirds", "-start", "2016-032", "-end", "2016-061", "-v", "v1.0"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("SSTCCI2_refdata_mooring_201602.nc", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2016, 2, 1, 0, 0, 0, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2016, 2, 29, 23, 58, 11, 0, observation.getStopTime());

        assertEquals("mooring-sirds", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sirds", "v1.0", "SSTCCI2_refdata_mooring_201602.nc"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v1.0", observation.getVersion());

        assertNull(observation.getGeoBounds());
        assertNull(observation.getTimeAxes());
    }

    @Test
    public void testIngest_IASI_MA() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "iasi-ma", "-start", "2016-001", "-end", "2016-001", "-v", "v3-6N"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2016, 1, 1, 12, 47, 54, 870, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2016, 1, 1, 14, 26, 58, 414, observation.getStopTime());

        assertEquals("iasi-ma", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"iasi-ma", "v3-6N", "2016", "01", "IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v3-6N", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertTrue(geoBounds instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoBounds;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());
        assertEquals(TestData.IASI_MA_GEOMETRIES[0], geometryFactory.format(polygons.get(0)));
        assertEquals(TestData.IASI_MA_GEOMETRIES[1], geometryFactory.format(polygons.get(1)));

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        assertEquals(2, timeAxes.length);
        TestUtil.assertCorrectUTCDate(2016, 1, 1, 12, 47, 54, 870, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(2016, 1, 1, 13, 37, 26, 642, timeAxes[0].getEndTime());
        assertEquals(TestData.IASI_MA_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

        TestUtil.assertCorrectUTCDate(2016, 1, 1, 13, 37, 26, 642, timeAxes[1].getStartTime());
        TestUtil.assertCorrectUTCDate(2016, 1, 1, 14, 26, 58, 414, timeAxes[1].getEndTime());
        assertEquals(TestData.IASI_MA_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
    }

    @Test
    public void testIngest_MYD06_AQUA() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "myd06-aq", "-start", "2009-133", "-end", "2009-133", "-v", "v006"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("MYD06_L2.A2009133.1035.006.2014062050327.hdf", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 35, 0, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 40, 0, 0, observation.getStopTime());

        assertEquals("myd06-aq", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"myd06-aq", "v006", "2009", "133", "MYD06_L2.A2009133.1035.006.2014062050327.hdf"}, false);
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
    }

    @Test
    public void testIngest_OceanRain_Insitu() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "ocean-rain-sst", "-start", "2011-133", "-end", "2011-134", "-v", "v1.0"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("OceanRAIN_allships_2010-2017_SST.ascii", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2010, 6, 10, 21, 0, 0, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2016, 9, 16, 22, 59, 0, 0, observation.getStopTime());

        assertEquals("ocean-rain-sst", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ocean-rain-sst", "v1.0", "OceanRAIN_allships_2010-2017_SST.ascii"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v1.0", observation.getVersion());

        assertNull(observation.getGeoBounds());
        assertNull(observation.getTimeAxes());
    }

    @Test
    public void testIngest_AMSR2() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "amsr2-gcw1", "-start", "2017-196", "-end", "2017-197", "-v", "v220"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("GW1AM2_201707160510_232D_L1SGRTBR_2220220.h5.gz", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2017, 7, 16, 5, 10, 43, 876, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2017, 7, 16, 6, 0, 6, 92, observation.getStopTime());

        assertEquals("amsr2-gcw1", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"amsr2-gcw1", "v220", "2017", "07", "16", "GW1AM2_201707160510_232D_L1SGRTBR_2220220.h5.gz"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.DESCENDING, observation.getNodeType());
        assertEquals("v220", observation.getVersion());

        assertNotNull(observation.getGeoBounds());
        assertNotNull(observation.getTimeAxes());
    }

    @Test
    public void testIngest_AVHRR_FCDR() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-ma-fcdr", "-start", "2016-312", "-end", "2016-314", "-v", "v0.2.1"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(2, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("FIDUCEO_FCDR_L1C_AVHRR_METOPA_20161108073729_20161108082817_EASY_vBeta_fv2.0.0.nc", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2016, 11, 8, 7, 37, 29, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2016, 11, 8, 8, 28, 17, observation.getStopTime());

        assertEquals("avhrr-ma-fcdr", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-ma-fcdr", "v0.2.1", "2016", "11", "08", "FIDUCEO_FCDR_L1C_AVHRR_METOPA_20161108073729_20161108082817_EASY_vBeta_fv2.0.0.nc"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v0.2.1", observation.getVersion());

        assertNotNull(observation.getGeoBounds());
        assertNotNull(observation.getTimeAxes());
    }

    @Test
    public void testIngest_GruanUleic_Insitu() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "gruan-uleic", "-start", "2010-135", "-end", "2010-136", "-v", "v1.0"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("nya_matchup_points.txt", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2009, 1, 1, 5, 54, 22, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2018, 3, 27, 11, 9, 35, 0, observation.getStopTime());

        assertEquals("gruan-uleic", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "gruan-uleic", "v1.0", "nya_matchup_points.txt"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v1.0", observation.getVersion());

        assertNull(observation.getGeoBounds());
        assertNull(observation.getTimeAxes());
    }

    @Test
    public void testIngest_SLSTR_S3A() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "slstr-s3a", "-start", "2018-286", "-end", "2018-286", "-v", "1.0"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("S3A_SL_1_RBT____20181013T222436_20181013T222736_20181015T035102_0179_037_001_1620_LN2_O_NT_003.SEN3", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 24, 36, 182, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 27, 36, 182, observation.getStopTime());

        assertEquals("slstr-s3a", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a", "1.0", "2018", "10", "13", "S3A_SL_1_RBT____20181013T222436_20181013T222736_20181015T035102_0179_037_001_1620_LN2_O_NT_003.SEN3"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.DESCENDING, observation.getNodeType());
        assertEquals("1.0", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertEquals(TestData.SLSTR_S3A_GEOMETRY, geometryFactory.format(geoBounds));
        assertEquals(TestData.SLSTR_S3A_AXIS_GEOMETRY, geometryFactory.format(observation.getTimeAxes()[0].getGeometry()));
    }

    @Test
    public void testIngest_SLSTR_SUBSET_S3A() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "slstr-s3a-uor", "-start", "2020-143", "-end", "2020-145", "-v", "1.0"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(2, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.zip", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 12, 2, 240, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 15, 2, 240, observation.getStopTime());

        assertEquals("slstr-s3a-uor", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a-uor", "1.0", "2020", "05", "22", "S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.zip"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.ASCENDING, observation.getNodeType());
        assertEquals("1.0", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertEquals("POLYGON((-3.605947 -25.831709,-4.345505 -23.678649999999998,-5.052073 -21.520253000000004,-5.7285819999999985 -19.357061000000005,-6.377657 -17.189569,-6.876289 -15.461471999999999,-9.131362 -16.047251000000003,-11.394941 -16.60798799999999,-13.671509000000002 -17.155166,-15.968603 -17.663905,-18.269608000000005 -18.157285999999992,-20.575312 -18.609944000000002,-20.109500000000008 -20.816586999999995,-19.641969 -23.022255,-19.171696999999998 -25.226836000000002,-18.697578999999998 -27.430221,-18.316648999999998 -29.183193000000003,-15.807929999999999 -28.733388,-13.315321999999998 -28.24385499999999,-10.84056 -27.702752999999998,-8.400936999999999 -27.118316000000007,-5.984294 -26.500103000000006,-3.605947 -25.831709))",
                geometryFactory.format(geoBounds));
        assertEquals("LINESTRING(-10.851859 -27.702489000000003,-11.47645 -25.508398000000003,-12.080582999999999 -23.311636999999997,-12.666854999999996 -21.112256999999993,-13.226334000000001 -18.910415999999998,-13.671509000000002 -17.155166)",
                geometryFactory.format(observation.getTimeAxes()[0].getGeometry()));
    }

    @Test
    public void testIngest_MY021KM() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "myd021km-aq", "-start", "2011-168", "-end", "2011-168", "-v", "v61"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("MYD021KM.A2011168.2210.061.2018032001033.hdf", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2011, 6, 17, 22, 10, 0, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2011, 6, 17, 22, 15, 0, 0, observation.getStopTime());

        assertEquals("myd021km-aq", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"myd021km-aq", "v61", "2011", "06", "17", "MYD021KM.A2011168.2210.061.2018032001033.hdf"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v61", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertEquals(TestData.MYD012KM_AQ_GEOMETRY, geometryFactory.format(geoBounds));
        assertEquals(TestData.MYD021KM_AQ_AXIS_GEOMETRY, geometryFactory.format(observation.getTimeAxes()[0].getGeometry()));
    }

    @Test
    public void testIngest_MOD35() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "mod35-te", "-start", "2020-168", "-end", "2023-168", "-v", "v61"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("MOD35_L2.A2022115.1125.061.2022115193707.hdf", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2022, 4, 25, 11, 25, 0, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2022, 4, 25, 11, 30, 0, 0, observation.getStopTime());

        assertEquals("mod35-te", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"mod35-te", "v61", "2022", "115", "MOD35_L2.A2022115.1125.061.2022115193707.hdf"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v61", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertEquals(TestData.MOD35_TE_GEOMETRY, geometryFactory.format(geoBounds));
        assertEquals(TestData.MOD35_TE_AXIS_GEOMETRY, geometryFactory.format(observation.getTimeAxes()[0].getGeometry()));
    }

    @Test
    public void testIngest_AVHRR_FRAC_MB() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-frac-mb", "-start", "2019-254", "-end", "2019-254", "-v", "v1"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("NSS.FRAC.M1.D19254.S0220.E0319.B3621920.SV", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2019, 9, 11, 2, 20, 46, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2019, 9, 11, 3, 19, 28, 0, observation.getStopTime());

        assertEquals("avhrr-frac-mb", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-frac-mb", "v1", "2019", "09", "11", "NSS.FRAC.M1.D19254.S0220.E0319.B3621920.SV"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("v1", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertEquals(TestData.AVHRR_FRAC_MB_GEOMETRY, geometryFactory.format(geoBounds));
        assertEquals(TestData.AVHRR_FRAC_MB_AXIS_GEOMETRY, geometryFactory.format(observation.getTimeAxes()[0].getGeometry()));
    }

    @Test
    public void testIngest_miras_smos_CDF3TD() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "miras-smos-CDF3TD", "-start", "2017-324", "-end", "2017-324", "-v", "re07"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation observation = getSatelliteObservation("SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.tgz", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2017, 11, 20, 0, 0, 0, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2017, 11, 20, 23, 59, 59, 999, observation.getStopTime());

        assertEquals("miras-smos-CDF3TD", observation.getSensor().getName());

        final String expectedPath = TestUtil.assembleFileSystemPath(new String[]{"miras-smos-CDF3TD", "re07", "2017", "324", "SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.tgz"}, false);
        assertEquals(expectedPath, observation.getDataFilePath().toString());

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals("re07", observation.getVersion());

        final Geometry geoBounds = observation.getGeoBounds();
        assertEquals("POLYGON((-179.8703155517578 -83.51713562011719,179.8703155517578 -83.51713562011719,179.8703155517578 83.51713562011719,-179.8703155517578 83.51713562011719,-179.8703155517578 -83.51713562011719))",
                geometryFactory.format(geoBounds));
        final TimeAxis timeAxis = observation.getTimeAxes()[0];
        assertTrue(timeAxis instanceof L3TimeAxis);
        assertEquals("MULTILINESTRING((-179.8703155517578 0.0,179.8703155517578 0.0),(0.0 83.51713562011719,0.0 -83.51713562011719))", geometryFactory.format(timeAxis.getGeometry()));
    }

    @Test
    public void testIngest_DTUSIC1_sic_cci() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "DTUSIC1-sic-cci", "-start", "2016-001", "-end", "2016-002", "-v", "v3"};

        IngestionToolMain.main(args);
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(3, satelliteObservations.size());

        SatelliteObservation observation = getSatelliteObservation("ASCAT-vs-AMSR2-vs-ERA5-vs-DTUSIC1-2017-S.text", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2017, 1, 20, 23, 40, 15, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2017, 12, 28, 1, 25, 18, observation.getStopTime());

        assertEquals("DTUSIC1-sic-cci", observation.getSensor().getName());
        assertEquals("v3", observation.getVersion());
        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertNull(observation.getGeoBounds());

        observation = getSatelliteObservation("QSCAT-vs-ASCAT-vs-AMSR2-vs-ERA-vs-DTUSIC1-2016-S.text", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2016, 1, 16, 1, 9, 30, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2016, 12, 31, 6, 33, 5, observation.getStopTime());

        assertEquals("DTUSIC1-sic-cci", observation.getSensor().getName());
        assertEquals("v3", observation.getVersion());
        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertNull(observation.getGeoBounds());

        observation = getSatelliteObservation("QSCAT-vs-SMAP-vs-SMOS-vs-ASCAT-vs-AMSR2-vs-ERA-vs-DTUSIC1-2016-N.text", satelliteObservations);
        TestUtil.assertCorrectUTCDate(2016, 1, 2, 2, 12, 34, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2016, 12, 31, 23, 15, 10, observation.getStopTime());

        assertEquals("DTUSIC1-sic-cci", observation.getSensor().getName());
        assertEquals("v3", observation.getVersion());
        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertNull(observation.getGeoBounds());
    }

    @Test
    public void testIngest_windsat_coriolis() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "windsat-coriolis", "-start", "2018-119", "-end", "2018-119", "-v", "v1.0"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> observations = storage.get();
        assertEquals(1, observations.size());

        final SatelliteObservation observation = getSatelliteObservation("RSS_WindSat_TB_L1C_r79285_20180429T174238_2018119_V08.0.nc", observations);
        TestUtil.assertCorrectUTCDate(2018, 4, 29, 17, 42, 38, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2018, 4, 29, 19, 30, 45, observation.getStopTime());

        assertEquals("windsat-coriolis", observation.getSensor().getName());
        assertEquals("v1.0", observation.getVersion());
        assertEquals(NodeType.UNDEFINED, observation.getNodeType());

        final Geometry geoBounds = observation.getGeoBounds();
        assertEquals("POLYGON((-179.93750000000003 -89.9375,179.93750000000003 -89.9375,179.93750000000003 89.9375,-179.93750000000003 89.9375,-179.93750000000003 -89.9375))",
                geometryFactory.format(geoBounds));
        final Geometry intersection = geoBounds.getIntersection(geometryFactory.createPoint(0, 0));
        assertEquals(geometryFactory.createPoint(0, 0), intersection);
        final TimeAxis timeAxis = observation.getTimeAxes()[0];
        assertTrue(timeAxis instanceof L3TimeAxis);
        assertEquals("MULTILINESTRING((-179.93750000000003 0.0,179.93750000000003 0.0),(0.0 89.9375,0.0 -89.9375))", geometryFactory.format(timeAxis.getGeometry()));

    }

    @Test
    public void testIngest_ndbc_standard_meteo() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "ndbc-sm-cb", "-start", "2017-001", "-end", "2017-001", "-v", "v1"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> observations = storage.get();
        assertEquals(1, observations.size());

        final SatelliteObservation observation = getSatelliteObservation("42088h2017.txt", observations);
        TestUtil.assertCorrectUTCDate(2017, 1, 1, 0, 0, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2017, 10, 19, 1, 0, 0, observation.getStopTime());

        assertEquals("ndbc-sm-cb", observation.getSensor().getName());
        assertEquals("v1", observation.getVersion());
        assertEquals(NodeType.UNDEFINED, observation.getNodeType());

        assertNull(observation.getGeoBounds());
        assertNull(observation.getTimeAxes());
    }

    @Test
    public void testIngest_TAO() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "tao-sss", "-start", "2017-275", "-end", "2017-275", "-v", "v1"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> observations = storage.get();
        assertEquals(1, observations.size());

        final SatelliteObservation observation = getSatelliteObservation("TRITON_TR0N156E_1998_2017-10.txt", observations);
        TestUtil.assertCorrectUTCDate(2017, 10, 1, 12, 0, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2017, 10, 31, 12, 0, 0, observation.getStopTime());

        assertEquals("tao-sss", observation.getSensor().getName());
        assertEquals("v1", observation.getVersion());
        assertEquals(NodeType.UNDEFINED, observation.getNodeType());

        assertNull(observation.getGeoBounds());
        assertNull(observation.getTimeAxes());
    }

    @Test
    public void testIngest_PIRATA() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "pirata-sss", "-start", "2016-275", "-end", "2016-275", "-v", "v1"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> observations = storage.get();
        assertEquals(1, observations.size());

        final SatelliteObservation observation = getSatelliteObservation("PIRATA_0N35W_sss_2016-10.txt", observations);
        TestUtil.assertCorrectUTCDate(2016, 10, 1, 0, 0, 0, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2016, 10, 31, 23, 0, 0, observation.getStopTime());

        assertEquals("pirata-sss", observation.getSensor().getName());
        assertEquals("v1", observation.getVersion());
        assertEquals(NodeType.UNDEFINED, observation.getNodeType());

        assertNull(observation.getGeoBounds());
        assertNull(observation.getTimeAxes());
    }

    @Test
    public void testIngest_smap_sss__for_look() throws SQLException, ParseException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "smap-sss-for", "-start", "2018-031", "-end", "2018-040", "-v", "v05.0"};

        IngestionToolMain.main(args);

        final List<SatelliteObservation> observations = storage.get();
        assertEquals(1, observations.size());

        final SatelliteObservation observation = getSatelliteObservation("RSS_SMAP_SSS_L2C_r16092_20180204T202311_2018035_FNL_V05.0.nc", observations);
        TestUtil.assertCorrectUTCDate(2018, 2, 4, 20, 23, 11, observation.getStartTime());
        TestUtil.assertCorrectUTCDate(2018, 2, 4, 22, 4, 56, observation.getStopTime());

        assertEquals("smap-sss-for", observation.getSensor().getName());
        assertEquals("v05.0", observation.getVersion());
        assertEquals(NodeType.UNDEFINED, observation.getNodeType());

        final Geometry geoBounds = observation.getGeoBounds();
        assertEquals("POLYGON((-179.9690856933594 -86.61936950683594,179.9915313720703 -86.61936950683594,179.9915313720703 86.40787506103517,-179.9690856933594 86.40787506103517,-179.9690856933594 -86.61936950683594))",
                     geometryFactory.format(geoBounds));
        final Geometry intersection = geoBounds.getIntersection(geometryFactory.createPoint(0, 0));
        assertEquals(geometryFactory.createPoint(0, 0), intersection);
        final TimeAxis timeAxis = observation.getTimeAxes()[0];
        assertTrue(timeAxis instanceof L3TimeAxis);
        assertEquals("MULTILINESTRING((-179.9690856933594 0.0,179.9915313720703 0.0),(0.0 86.40787506103517,0.0 -86.61936950683594))", geometryFactory.format(timeAxis.getGeometry()));

    }

    private void callMainAndValidateSystemOutput(String[] args, boolean errorOutputExpected) throws ParseException {
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
                assertEquals("", out.toString());
                assertEquals(expected.toString(), err.toString());
            } else {
                assertEquals(expected.toString(), out.toString());
                assertEquals("", err.toString());
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
