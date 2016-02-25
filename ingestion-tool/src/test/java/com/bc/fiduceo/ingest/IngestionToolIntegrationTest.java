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

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class IngestionToolIntegrationTest {
    private File configDir;

    @Before
    public void setUp() {
        final File testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create testGroupInputProduct directory: " + configDir.getAbsolutePath());
        }
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

    @Ignore
    @Test
    public void testIngest_AIRS() throws ParseException, IOException, SQLException {
        // @todo 1 tb/** this testGroupInputProduct relies on the results being returned in a specifi order - change this 2015-12-22
        // @todo 2 tb/tb move geometry factory type to some other location, parametrize testGroupInputProduct 2015-12-16
        final Storage storage = Storage.create(TestUtil.getDatasource_H2(), new GeometryFactory(GeometryFactory.Type.JTS));
        storage.initialize();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "airs"};
        try {
            writeSystemProperties();
            TestUtil.writeDatabaseProperties_MongoDb(configDir);

            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            final SatelliteObservation observation = satelliteObservations.get(1);
            final Sensor sensor = observation.getSensor();
            assertTrue(sensor.getName().contains("AIRS"));

            assertEquals("02-Sep-2015 02:17:22", TimeUtils.format(observation.getStartTime()));
            assertEquals("02-Sep-2015 02:23:21", TimeUtils.format(observation.getStopTime()));
            // @todo 1 tb/** something is wrong with the path stored in the DB check and resolve 2015-12-22
            assertTrue(observation.getDataFile().getAbsolutePath().contains("AIRS.2015.09.02.023.L1B.AIRS_Rad.v5.0.23.0.G15246021652.hdf"));
            assertEquals("POLYGON ((129.0250670999712 89.79658975406977, 84.86982219864322 88.25602001883792, 81.90290725421067 85.65352859896593, 81.3576435143464 83.81490908412849, 81.11962740918655 82.26953345570404, 80.9699719499203 80.78247578538293, 80.84489337073471 79.15438865050432, 80.71104039446459 77.08294691030505, 80.53453700811556 73.81603312585354, 79.3675206918862 73.81209529369254, 72.40057120621034 73.69182913234798, 65.61766811537095 73.3539676636789, 59.18281156604972 72.80034444822064, 53.20262229851623 72.05601617696243, 47.73275674707719 71.14340662799174, 42.79684164519756 70.08122973044242, 38.34567255972898 68.90462336877947, 34.37287139761094 67.61918933634195, 30.823735270859643 66.24658402113647, 27.651190877552047 64.80095475900657, 24.809181150357116 63.29386187193271, 21.850407220205888 64.21820075117444, 16.607890159954643 65.57760911279104, 12.587203385901809 66.41330926760962, 9.013604411603218 67.02699129324324, 5.402603891171518 67.53889306433688, 1.2785654966557936 68.00522531950848, -4.17068964541869 68.44711693204631, -13.074610533265286 68.77849058101255, -13.02427652238762 69.09764314472332, -12.718673119359968 71.0130353793829, -12.405163962874246 72.9280900870151, -12.080750819552595 74.84320632611005, -11.740975374740096 76.7579444858891, -11.377940291650905 78.67196596645067, -10.975842076883234 80.58535883463844, -10.505485010935299 82.49870429049207, -9.893744504161857 84.41191323747495, -8.920099154760043 86.32499905024413, -6.4170300611108315 88.23613967607469, 129.0250670999712 89.79658975406977))",
                    observation.getGeoBounds().toString());

            // @todo 1 tb/** this is not correct, check why and correct 2015-12-22
            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        } finally {
            storage.clear();
            storage.close();
        }
    }


    @Test
    public void testIngest_AMSU_MHS() throws ParseException, IOException, SQLException {
        final Storage storage = Storage.create(TestUtil.getDatasourceMongo_DB(), new GeometryFactory(GeometryFactory.Type.S2));

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "amsub-n15", "-start", "2015-340", "-end", "2015-350", "-v", "1.0", "-concurrent", "2"};
        try {
            writeSystemProperties();
            TestUtil.writeDatabaseProperties_MongoDb(configDir);
            IngestionToolMain.main(args);
            List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(satelliteObservations.size(), 3);
            SatelliteObservation observationFromDb = satelliteObservations.get(0);


            assertEquals(observationFromDb.getSensor().getName(), "amsub-n15");
            assertEquals(observationFromDb.getGeoBounds().getCoordinates().length, 121);

            ArrayList<Polygon> polygonArrayList = (ArrayList<Polygon>) observationFromDb.getGeoBounds().getInner();
            Polygon polygon = polygonArrayList.get(0);
            assertEquals("Polygon: (1) loops:\n" +
                    "loop <\n" +
                    "(-72.57329816664424, -108.08909726943968)\n" +
                    "(-70.1165982287057, -76.04489807894424)\n" +
                    "(-64.76089836400206, -57.61509854452016)\n" +
                    "(-58.812298514276335, -68.72199826393626)\n" +
                    "(-52.171798682029475, -76.37179807068605)\n" +
                    "(-45.13829885971063, -81.92099793050149)\n" +
                    "(-37.863499043487536, -86.15729782348353)\n" +
                    "(-30.42999923127354, -89.53569773813797)\n" +
                    "(-22.885899421853537, -92.32869766758085)\n" +
                    "(-15.261899614451977, -94.7059976075252)\n" +
                    "(-7.578899808540879, -96.7779975551821)\n" +
                    "(0.14739999627636274, -98.61859750868462)\n" +
                    "(7.904799800307953, -100.27869746674696)\n" +
                    "(15.683599603798942, -101.79379742847235)\n" +
                    "(23.47589940694889, -103.18829739324428)\n" +
                    "(31.27499920992703, -104.47799736066372)\n" +
                    "(39.076099012854684, -105.67029733054369)\n" +
                    "(46.8753988158278, -106.76239730295495)\n" +
                    "(54.67069861890196, -107.73499727838498)\n" +
                    "(62.46049842211505, -108.53209725824857)\n" +
                    "(70.24379822549236, -108.99329724659765)\n" +
                    "(78.01719802911975, -108.51989725855674)\n" +
                    "(85.74459783390921, -102.09769742079517)\n" +
                    "(86.27999782038387, 51.67149869466812)\n" +
                    "(78.55919801542768, 59.60949849413737)\n" +
                    "(70.77699821202259, 60.209298478985154)\n" +
                    "(62.97959840900148, 59.783098489751865)\n" +
                    "(55.17089860626582, 59.00039850952453)\n" +
                    "(50.16819873264467, 58.39869852472476)\n" +
                    "(48.76939876798133, 73.69319813835318)\n" +
                    "(46.10319883533522, 85.61009783730697)\n" +
                    "(53.16989865681536, 91.35719769212301)\n" +
                    "(59.81529848893843, 99.37629748954352)\n" +
                    "(65.70499834015209, 111.17139719157421)\n" +
                    "(70.16929822737438, 128.85679674480343)\n" +
                    "(72.11949817810819, 152.90369613732764)\n" +
                    "(70.78099821192154, 177.72829551020553)\n" +
                    "(66.7147983146424, -163.26579587555898)\n" +
                    "(61.045498457860965, -150.50989619780012)\n" +
                    "(54.52879862248665, -141.90689641513018)\n" +
                    "(47.547898798839014, -135.80659656923672)\n" +
                    "(40.29289898211573, -131.24019668459368)\n" +
                    "(32.863899169788056, -127.65649677512556)\n" +
                    "(25.31819936040847, -124.73249684899203)\n" +
                    "(17.690999553087742, -122.27009691119747)\n" +
                    "(10.005999747227179, -120.1431969649275)\n" +
                    "(2.280299942394776, -118.26839701228891)\n" +
                    "(-5.473099861737865, -116.58909705471162)\n" +
                    "(-13.243799665433468, -115.0661970931833)\n" +
                    "(-21.02349946890172, -113.67319712837343)\n" +
                    "(-28.805699272306814, -112.39329716070645)\n" +
                    "(-36.585099075782644, -111.21869719037933)\n" +
                    "(-44.35839887941257, -110.15209721732391)\n" +
                    "(-52.12319868325723, -109.21289724105009)\n" +
                    "(-59.87839848734439, -108.45449726020888)\n" +
                    "(-67.62319829169428, -108.01939727120045)\n" +
                    "(-72.57329816664424, -108.08909726943968)\n" +
                    ">\n", polygon.toString());


        } finally {
            storage.clear();
            storage.close();
        }
    }

    private void writeSystemProperties() throws IOException {
        final Properties properties = new Properties();
        properties.setProperty("archive-root", TestUtil.getTestDataDirectory().getAbsolutePath());
        properties.setProperty("geometry-library-type", "S2");
        TestUtil.storePropertiesToTemp(properties, configDir, "system.properties");
    }
}
