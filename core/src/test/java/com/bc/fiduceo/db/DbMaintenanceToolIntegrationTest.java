package com.bc.fiduceo.db;

import com.bc.fiduceo.TestData;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(DatabaseTestRunner.class)
public class DbMaintenanceToolIntegrationTest {

    private File configDir;

    @Before
    public void setUp() throws SQLException, IOException {
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
    public void testInvalidCommandLine() throws ParseException {
        final PrintStream _err = System.err;
        final PrintStream _out = System.out;

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ByteArrayOutputStream err = new ByteArrayOutputStream();
            final PrintStream psO = new PrintStream(out);
            final PrintStream psE = new PrintStream(err);
            System.setOut(psO);
            System.setErr(psE);

            DbMaintenanceToolMain.main(new String[0]);

            psO.flush();
            psE.flush();

            assertEquals("", out.toString());
            assertEquals("db-maintenance-tool version 1.4.2\n" +
                    "\n" +
                    "usage: db-maintenance-tool <options>\n" +
                    "Valid options are:\n" +
                    "   -c,--config <arg>    Defines the configuration directory. Defaults to './config'.\n" +
                    "   -h,--help            Prints the tool usage.\n" +
                    "   -p,--path <arg>      Observation path segment to be replaced.\n" +
                    "   -r,--replace <arg>   Observation path segment replacement.\n", err.toString());
        } finally {
            System.setOut(_out);
            System.setErr(_err);
        }
    }

    @Test
    public void testCorrectPaths_MongoDb_empty_Db() throws IOException, ParseException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemConfig(configDir);

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(),
                "-p", "/data/archive/wrong", "-r", "/archive/correct"};

        DbMaintenanceToolMain.main(args);
        // a dumb test - just should not throw anything - no testable effects on DB tb 2019-04-03
    }

    @Test
    public void testCorrectPaths_MongoDb_alterNoPath() throws IOException, ParseException, SQLException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemConfig(configDir);
        final BasicDataSource dataSource = TestUtil.getDataSource_MongoDb();

        runTest_alterNoPath(dataSource);
    }

    @Test
    public void testCorrectPaths_Postgres_alterNoPath() throws IOException, ParseException, SQLException {
        TestUtil.writeDatabaseProperties_Postgres(configDir);
        TestUtil.writeSystemConfig(configDir);
        final BasicDataSource dataSource = TestUtil.getDataSource_Postgres();

        runTest_alterNoPath(dataSource);
    }

    @Test
    public void testCorrectPaths_H2_alterNoPath() throws IOException, ParseException, SQLException {
        TestUtil.writeDatabaseProperties_H2(configDir);
        TestUtil.writeSystemConfig(configDir);
        final BasicDataSource dataSource = TestUtil.getDatasource_H2();

        runTest_alterNoPath(dataSource);
    }

    @Test
    public void testCorrectPaths_MongoDb_alterSomePaths() throws IOException, ParseException, SQLException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemConfig(configDir);
        final BasicDataSource dataSource = TestUtil.getDataSource_MongoDb();

        runTest_alterSomePaths(dataSource);
    }

    @Test
    public void testCorrectPaths_Postgres_alterSomePaths() throws IOException, ParseException, SQLException {
        TestUtil.writeDatabaseProperties_Postgres(configDir);
        TestUtil.writeSystemConfig(configDir);
        final BasicDataSource dataSource = TestUtil.getDataSource_Postgres();

        runTest_alterSomePaths(dataSource);
    }

    @Test
    public void testCorrectPaths_H2_alterSomePaths() throws IOException, ParseException, SQLException {
        TestUtil.writeDatabaseProperties_H2(configDir);
        TestUtil.writeSystemConfig(configDir);
        final BasicDataSource dataSource = TestUtil.getDatasource_H2();

        runTest_alterSomePaths(dataSource);
    }

    private void runTest_alterNoPath(BasicDataSource dataSource) throws SQLException, ParseException {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Storage storage = Storage.create(dataSource, geometryFactory);

        if (!storage.isInitialized()) {
            storage.initialize();
        }

        storage.insert(new Sensor(TestData.SENSOR_NAME));

        for (int i = 0; i < 12; i++) {
            final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
            observation.setDataFilePath("/archive/correct/the_file_number_" + i);
            storage.insert(observation);
        }

        try {

            final String[] args = new String[]{"-c", configDir.getAbsolutePath(),
                    "-p", "/data/archive/wrong", "-r", "/archive/correct"};

            DbMaintenanceToolMain.main(args);

            final List<SatelliteObservation> observations = storage.get();
            assertEquals(12, observations.size());
            for (SatelliteObservation satelliteObservation : observations) {
                assertTrue(satelliteObservation.getDataFilePath().toString().contains("/archive/correct"));
            }
        } finally {
            storage.clear();
            storage.close();
        }
    }

    private void runTest_alterSomePaths(BasicDataSource dataSource) throws SQLException, ParseException {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Storage storage = Storage.create(dataSource, geometryFactory);

        if (!storage.isInitialized()) {
            storage.initialize();
        }

        storage.insert(new Sensor(TestData.SENSOR_NAME));

        for (int i = 0; i < 16; i++) {
            final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
            if (i%2 == 0 ) {
                observation.setDataFilePath("/archive/correct/the_file_number_" + i);
            } else {
                observation.setDataFilePath("/data/archive/wrong/the_file_number_" + i);
            }
            storage.insert(observation);
        }

        try {

            final String[] args = new String[]{"-c", configDir.getAbsolutePath(),
                    "-p", "/data/archive/wrong", "-r", "/archive/correct"};

            DbMaintenanceToolMain.main(args);

            final List<SatelliteObservation> observations = storage.get();
            assertEquals(16, observations.size());
            for (SatelliteObservation satelliteObservation : observations) {
                assertTrue(satelliteObservation.getDataFilePath().toString().contains("/archive/correct"));
            }
        } finally {
            storage.clear();
            storage.close();
        }
    }
}
