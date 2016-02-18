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

package com.bc.fiduceo.matchup;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class MatchupToolIntegrationTest {

    private File configDir;

    @Before
    public void setUp() {
        final File testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create test directory: " + configDir.getAbsolutePath());
        }
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }


    @Test
    public void testIngest_notInputParameter() throws ParseException, IOException, SQLException {
        // @todo 4 tb/tb find a way to steal system.err to implement assertions 2016-02-15
        final String[] args = new String[0];
        MatchupToolMain.main(args);
    }

    @Test
    public void testIngest_help() throws ParseException, IOException, SQLException {
        // @todo 4 tb/tb find a way to steal system.err to implement assertions 2016-02-16
        String[] args = new String[]{"-h"};
        MatchupToolMain.main(args);

        args = new String[]{"--help"};
        MatchupToolMain.main(args);
    }

    @Test
    public void testIngest_missingSystemProperties() throws ParseException, IOException, SQLException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "--start", "1999-124", "-e", "1999-176"};

        TestUtil.writeDatabaseProperties(configDir);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testIngest_missingDatabaseProperties() throws ParseException, IOException, SQLException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "--start", "1999-124", "-e", "1999-176"};

        TestUtil.writeSystemProperties(configDir);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testIngest_missingStartDate() throws ParseException, IOException, SQLException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-e", "1999-176"};

        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeDatabaseProperties(configDir);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testIngest_missingEndDate() throws ParseException, IOException, SQLException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "--start", "1999-124"};

        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeDatabaseProperties(configDir);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
