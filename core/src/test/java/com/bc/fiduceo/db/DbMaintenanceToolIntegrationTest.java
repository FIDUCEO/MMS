package com.bc.fiduceo.db;

import com.bc.fiduceo.TestUtil;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(DbAndIOTestRunner.class)
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
            assertEquals("db-maintenance-tool version 1.4.2-SNAPSHOT\n" +
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

    }
}
