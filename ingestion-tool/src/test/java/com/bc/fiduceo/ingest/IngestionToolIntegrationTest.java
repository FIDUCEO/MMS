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
            assertTrue(observation.getDataFilePath().toString().contains("AIRS.2015.09.02.023.L1B.AIRS_Rad.v5.0.23.0.G15246021652.hdf"));
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

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "amsub-n15", "-start", "2015-340", "-end", "2015-350", "-v", "1.0"};
        try {
            writeSystemProperties();
            TestUtil.writeDatabaseProperties_MongoDb(configDir);
            IngestionToolMain.main(args);
            List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(1, satelliteObservations.size());
            SatelliteObservation observationFromDb = satelliteObservations.get(0);


            assertEquals(observationFromDb.getSensor().getName(), "amsub-n15");
            assertEquals(observationFromDb.getGeoBounds()[0].getCoordinates().length, 106);

            ArrayList<Polygon> polygonArrayList = (ArrayList<Polygon>) observationFromDb.getGeoBounds()[0].getInner();
            Polygon polygon = polygonArrayList.get(0);
            assertEquals("Polygon: (1) loops:\n" +
                    "loop <\n" +
                    "(21.409899459140426, -97.86539752771206)\n" +
                    "(23.37569940948015, -87.36939779286331)\n" +
                    "(24.473999381734757, -78.28879802225856)\n" +
                    "(32.309399183795904, -79.61389798878372)\n" +
                    "(40.14389898587979, -80.8503979575471)\n" +
                    "(47.97329878809251, -81.99969792851334)\n" +
                    "(55.79489859050227, -83.04969790198811)\n" +
                    "(63.60739839314192, -83.95919787901221)\n" +
                    "(71.41009819602914, -84.60049786281161)\n" +
                    "(79.20129799920687, -84.45279786654282)\n" +
                    "(86.95229780340014, -77.60479803953785)\n" +
                    "(85.1658978485284, 83.14399789960589)\n" +
                    "(77.40039804470145, 86.30149781984073)\n" +
                    "(69.61679824133171, 86.1600978234128)\n" +
                    "(61.827198438113555, 85.43649784169247)\n" +
                    "(54.03249863502424, 84.48979786560812)\n" +
                    "(46.23289883205871, 83.4170978927068)\n" +
                    "(38.429999029176535, 82.25039792218013)\n" +
                    "(30.625799226327217, 80.99789795382094)\n" +
                    "(22.824399423407158, 79.65619798771513)\n" +
                    "(15.030799620290047, 78.21409802414564)\n" +
                    "(7.251599816809179, 76.65309806357982)\n" +
                    "(-0.5048999872451532, 74.94649810669216)\n" +
                    "(-8.228299792135662, 73.0563981544401)\n" +
                    "(-15.906599598165483, 70.92909820818022)\n" +
                    "(-23.523499405746406, 68.48709826987033)\n" +
                    "(-30.00809924193164, 66.04839833147707)\n" +
                    "(-32.292799184215255, 77.71499803675397)\n" +
                    "(-33.357899157308566, 87.94689777827443)\n" +
                    "(-25.573299353964103, 89.21159774632542)\n" +
                    "(-17.788399550627222, 90.57729771182494)\n" +
                    "(-10.009099747148863, 92.05809767441679)\n" +
                    "(-2.242899943339581, 93.67609763354267)\n" +
                    "(5.50149986102042, 95.46419758837146)\n" +
                    "(13.212299666229224, 97.46889753772851)\n" +
                    "(20.874799472658196, 99.75799747990095)\n" +
                    "(28.469099280810042, 102.43129741236771)\n" +
                    "(35.96709909139463, 105.64229733125103)\n" +
                    "(43.32499890551844, 109.63469723039455)\n" +
                    "(50.47019872501551, 114.81299709957966)\n" +
                    "(57.270398553228006, 121.87539692116844)\n" +
                    "(63.46869839664578, 132.0412966643562)\n" +
                    "(68.54659826836723, 147.2297962806624)\n" +
                    "(71.56179819219687, 168.993895730855)\n" +
                    "(71.47999819426332, -165.71089581379056)\n" +
                    "(68.33329827375564, -144.24439635607996)\n" +
                    "(63.17339840410568, -129.34169673255383)\n" +
                    "(56.921898562031856, -119.36029698470523)\n" +
                    "(50.08059873485763, -112.40929716030223)\n" +
                    "(42.89839891629528, -107.29989728937656)\n" +
                    "(35.504099103090994, -103.35219738910382)\n" +
                    "(27.969099293441104, -100.17149746945506)\n" +
                    "(21.409899459140426, -97.86539752771206)\n" +
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
