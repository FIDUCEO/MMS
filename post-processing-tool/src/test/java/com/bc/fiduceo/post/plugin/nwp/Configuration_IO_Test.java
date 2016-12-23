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

package com.bc.fiduceo.post.plugin.nwp;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static com.bc.fiduceo.post.plugin.nwp.TestDirUtil.createDirectory;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class Configuration_IO_Test {

    private File testDir;

    @Before
    public void setUp(){
        testDir = TestUtil.getTestDir();
        if (!testDir.mkdirs()) {
            fail("unable to create test directory");
        }
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testVerify() {
        final File cdoDir = createDirectory(testDir, "cdo_exec");
        final File nwpDir = createDirectory(testDir, "nwp");

        final Configuration configuration = new Configuration();
        configuration.setCDOHome(cdoDir.getAbsolutePath());
        configuration.setNWPAuxDir(nwpDir.getAbsolutePath());

        assertTrue(configuration.verify());
    }

    @Test
    public void testVerify_missingCdo() {
        final File nwpDir = createDirectory(testDir, "nwp");

        final Configuration configuration = new Configuration();
        configuration.setCDOHome("/nirvana");
        configuration.setNWPAuxDir(nwpDir.getAbsolutePath());

        try {
            configuration.verify();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testVerify_missingNwp() {
        final File cdoDir = createDirectory(testDir, "cdo_exec");

        final Configuration configuration = new Configuration();
        configuration.setCDOHome(cdoDir.getAbsolutePath());
        configuration.setNWPAuxDir("way/off/the/disk");

        try {
            configuration.verify();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
