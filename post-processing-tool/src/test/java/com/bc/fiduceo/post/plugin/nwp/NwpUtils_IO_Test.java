/*
 * Copyright (C) 2017 Brockmann Consult GmbH
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class NwpUtils_IO_Test {

    private File testDataDirectory;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
    }

    @Test
    public void testComposeFilesString_ggas_emptyPathList() throws IOException {
        final String eraInterimDir = TestUtil.assembleFileSystemPath(new String[]{testDataDirectory.getAbsolutePath(), "era-interim", "v1", "ggas"}, true);
        final ArrayList<String> subPaths = new ArrayList<>();

        final String filesString = NwpUtils.composeFilesString(eraInterimDir, subPaths, "ggas[0-9]*.nc", 0);
        assertEquals("", filesString);
    }

    @Test
    public void testComposeFilesString_ggas() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final String eraInterimDir = Paths.get(testDataDirectory.getAbsolutePath(), "era-interim", "v1", "ggas").toString();
        final ArrayList<String> subPaths = new ArrayList<>();
        subPaths.add("2004/01/08");
        subPaths.add("2004/01/09");
        subPaths.add("2004/01/10");

        final File rootDir = new File(eraInterimDir);
        final File dir_01_08 = new File(rootDir, "2004/01/08");
        final File dir_01_09 = new File(rootDir, "2004/01/09");
        final File dir_01_10 = new File(rootDir, "2004/01/10");

        final String filesString = NwpUtils.composeFilesString(eraInterimDir, subPaths, "ggas[0-9]*.nc", 0);
        final String expected = new File(dir_01_08, "ggas200401080000.nc").getAbsolutePath() + " " +
                new File(dir_01_08, "ggas200401080600.nc").getAbsolutePath() + " " +
                new File(dir_01_08, "ggas200401081200.nc").getAbsolutePath() + " " +
                new File(dir_01_08, "ggas200401081800.nc").getAbsolutePath() + " " +
                new File(dir_01_09, "ggas200401090000.nc").getAbsolutePath() + " " +
                new File(dir_01_09, "ggas200401090600.nc").getAbsolutePath() + " " +
                new File(dir_01_09, "ggas200401091200.nc").getAbsolutePath() + " " +
                new File(dir_01_09, "ggas200401091800.nc").getAbsolutePath() + " " +
                new File(dir_01_10, "ggas200401100000.nc").getAbsolutePath() + " " +
                new File(dir_01_10, "ggas200401100600.nc").getAbsolutePath() + " " +
                new File(dir_01_10, "ggas200401101200.nc").getAbsolutePath() + " " +
                new File(dir_01_10, "ggas200401101800.nc").getAbsolutePath();
        assertEquals(expected, filesString);
    }
}
