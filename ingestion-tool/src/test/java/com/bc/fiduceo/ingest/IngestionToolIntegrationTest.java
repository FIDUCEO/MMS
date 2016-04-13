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
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.TimeAxis;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

@RunWith(DbAndIOTestRunner.class)
public class IngestionToolIntegrationTest {

    private File configDir;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() {
        final File testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create testGroupInputProduct directory: " + configDir.getAbsolutePath());
        }

        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testIngest_notInputParameter() throws ParseException, IOException, SQLException {
        // @todo 4 tb/tb find a way to steal system.err to implement assertions 2015-12-09
        final String[] args = new String[0];
        IngestionToolMain.main(args);
    }

    @Test
    public void testIngest_help() throws ParseException, IOException, SQLException {
        // @todo 4 tb/tb find a way to steal system.err to implement assertions 2015-12-09
        String[] args = new String[]{"-h"};
        IngestionToolMain.main(args);

        args = new String[]{"--help"};
        IngestionToolMain.main(args);
    }

    @Test
    public void testIngest_missingSystemProperties() throws ParseException, IOException, SQLException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "airs-aqua"};

        TestUtil.writeDatabaseProperties_MongoDb(configDir);

        try {
            IngestionToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testIngest_missingDatabaseProperties() throws ParseException, IOException, SQLException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "airs-aqua"};

        writeSystemProperties();

        try {
            IngestionToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testIngest_AVHRR_GAC_NOAA17() throws SQLException, IOException, ParseException {
        final Storage storage = Storage.create(TestUtil.getdatasourceMongoDb(), new GeometryFactory(GeometryFactory.Type.S2));
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-n17", "-start", "2007-091", "-end", "2007-093", "-v", "1.01"};

        try {
            writeSystemProperties();
            TestUtil.writeDatabaseProperties_MongoDb(configDir);

            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = satelliteObservations.get(0);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 3, 34, 54, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 5, 28, 48, 0, observation.getStopTime());
            assertEquals("avhrr-n17", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "1.01", "2007", "04", "01", "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);
            assertEquals(TestData.AVHRR_GAC_N17_GEOMETRIES[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.AVHRR_GAC_N17_GEOMETRIES[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 3, 34, 54, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 4, 31, 51, 0, timeAxes[0].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N17_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(2007, 4, 1, 4, 31, 51, 0, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 5, 28, 48, 0, timeAxes[1].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N17_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_AVHRR_GAC_NOAA18() throws SQLException, IOException, ParseException {
        final Storage storage = Storage.create(TestUtil.getdatasourceMongoDb(), new GeometryFactory(GeometryFactory.Type.S2));
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "avhrr-n18", "-start", "2007-090", "-end", "2007-092", "-v", "1.02"};

        try {
            writeSystemProperties();
            TestUtil.writeDatabaseProperties_MongoDb(configDir);

            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());

            final SatelliteObservation observation = satelliteObservations.get(0);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 4, 12, 0, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 9, 46, 11, 0, observation.getStopTime());
            assertEquals("avhrr-n18", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n18", "1.02", "2007", "04", "01", "20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());

            final Geometry geoBounds = observation.getGeoBounds();
            assertTrue(geoBounds instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) geoBounds;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);
            assertEquals(TestData.AVHRR_GAC_N18_GEOMETRIES[0], geometryFactory.format(geometries[0]));
            assertEquals(TestData.AVHRR_GAC_N18_GEOMETRIES[1], geometryFactory.format(geometries[1]));

            final TimeAxis[] timeAxes = observation.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 4, 12, 0, timeAxes[0].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 55, 11, 500, timeAxes[0].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N18_AXIS_GEOMETRIES[0], geometryFactory.format(timeAxes[0].getGeometry()));

            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 55, 11, 500, timeAxes[1].getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 9, 46, 11, 0, timeAxes[1].getEndTime());
            assertEquals(TestData.AVHRR_GAC_N18_AXIS_GEOMETRIES[1], geometryFactory.format(timeAxes[1].getGeometry()));
        } finally {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testIngest_AMSUB_NOAA15() throws SQLException, IOException, ParseException {
        final Storage storage = Storage.create(TestUtil.getdatasourceMongoDb(), new GeometryFactory(GeometryFactory.Type.S2));
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "amsub-n15", "-start", "2007-233", "-end", "2007-235", "-v", "v1.0"};

        try {
            writeSystemProperties();
            TestUtil.writeDatabaseProperties_MongoDb(configDir);

            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(3, satelliteObservations.size());

            final SatelliteObservation observation = satelliteObservations.get(0);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 16, 40, 37, 120, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 18, 24, 53, 119, observation.getStopTime());
            assertEquals("amsub-n15", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsub-n15", "v1.0", "2007", "08", "22", "L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());

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
    public void testIngest_MHS_NOAA18() throws SQLException, IOException, ParseException {
        final Storage storage = Storage.create(TestUtil.getdatasourceMongoDb(), new GeometryFactory(GeometryFactory.Type.S2));
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "mhs-n18", "-start", "2007-233", "-end", "2007-235", "-v", "v1.0"};

        try {
            writeSystemProperties();
            TestUtil.writeDatabaseProperties_MongoDb(configDir);

            IngestionToolMain.main(args);
            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(3, satelliteObservations.size());

            final SatelliteObservation observation = satelliteObservations.get(1);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 11, 51, 27, 277, observation.getStartTime());
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 13, 37, 32, 610, observation.getStopTime());
            assertEquals("mhs-n18", observation.getSensor().getName());

            final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mhs-n18", "v1.0", "2007", "08", "22", "NSS.MHSX.NN.D07234.S1151.E1337.B1162021.GC.h5"}, true);
            final String expectedPath = TestUtil.getTestDataDirectory().getAbsolutePath() + testFilePath;
            assertEquals(expectedPath, observation.getDataFilePath().toString());

            assertEquals(NodeType.UNDEFINED, observation.getNodeType());

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

    // @todo 2 tb/tb reanimate test when we have usecase 2 running - temporarily disabled 2016-03-03
//    @Ignore
//    @Test
//    public void testIngest_AIRS() throws ParseException, IOException, SQLException {
//        // @todo 1 tb/** this testGroupInputProduct relies on the results being returned in a specifi order - change this 2015-12-22
//        // @todo 2 tb/tb move geometry factory type to some other location, parametrize testGroupInputProduct 2015-12-16
//        final Storage storage = Storage.create(TestUtil.getDatasource_H2(), new GeometryFactory(GeometryFactory.Type.JTS));
//        storage.initialize();
//
//        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "airs"};
//        try {
//            writeSystemProperties();
//            TestUtil.writeDatabaseProperties_MongoDb(configDir);
//
//            IngestionToolMain.main(args);
//
//            final List<SatelliteObservation> satelliteObservations = storage.get();
//            final SatelliteObservation observation = satelliteObservations.get(1);
//            final Sensor sensor = observation.getSensor();
//            assertTrue(sensor.getName().contains("AIRS"));
//
//            assertEquals("02-Sep-2015 02:17:22", TimeUtils.format(observation.getStartTime()));
//            assertEquals("02-Sep-2015 02:23:21", TimeUtils.format(observation.getStopTime()));
//            // @todo 1 tb/** something is wrong with the path stored in the DB check and resolve 2015-12-22
//            assertTrue(observation.getDataFilePath().toString().contains("AIRS.2015.09.02.023.L1B.AIRS_Rad.v5.0.23.0.G15246021652.hdf"));
//            assertEquals("POLYGON ((129.0250670999712 89.79658975406977, 84.86982219864322 88.25602001883792, 81.90290725421067 85.65352859896593, 81.3576435143464 83.81490908412849, 81.11962740918655 82.26953345570404, 80.9699719499203 80.78247578538293, 80.84489337073471 79.15438865050432, 80.71104039446459 77.08294691030505, 80.53453700811556 73.81603312585354, 79.3675206918862 73.81209529369254, 72.40057120621034 73.69182913234798, 65.61766811537095 73.3539676636789, 59.18281156604972 72.80034444822064, 53.20262229851623 72.05601617696243, 47.73275674707719 71.14340662799174, 42.79684164519756 70.08122973044242, 38.34567255972898 68.90462336877947, 34.37287139761094 67.61918933634195, 30.823735270859643 66.24658402113647, 27.651190877552047 64.80095475900657, 24.809181150357116 63.29386187193271, 21.850407220205888 64.21820075117444, 16.607890159954643 65.57760911279104, 12.587203385901809 66.41330926760962, 9.013604411603218 67.02699129324324, 5.402603891171518 67.53889306433688, 1.2785654966557936 68.00522531950848, -4.17068964541869 68.44711693204631, -13.074610533265286 68.77849058101255, -13.02427652238762 69.09764314472332, -12.718673119359968 71.0130353793829, -12.405163962874246 72.9280900870151, -12.080750819552595 74.84320632611005, -11.740975374740096 76.7579444858891, -11.377940291650905 78.67196596645067, -10.975842076883234 80.58535883463844, -10.505485010935299 82.49870429049207, -9.893744504161857 84.41191323747495, -8.920099154760043 86.32499905024413, -6.4170300611108315 88.23613967607469, 129.0250670999712 89.79658975406977))",
//                    observation.getGeoBounds().toString());
//
//            // @todo 1 tb/** this is not correct, check why and correct 2015-12-22
//            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
//        } finally {
//            storage.clear();
//            storage.close();
//        }
//    }



    private void writeSystemProperties() throws IOException {
        final Properties properties = new Properties();
        properties.setProperty("archive-root", TestUtil.getTestDataDirectory().getAbsolutePath());
        properties.setProperty("geometry-library-type", "S2");
        TestUtil.storePropertiesToTemp(properties, configDir, "system.properties");
    }
}
