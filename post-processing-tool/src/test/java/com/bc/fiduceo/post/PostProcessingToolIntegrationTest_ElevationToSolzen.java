/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class PostProcessingToolIntegrationTest_ElevationToSolzen {

    private File configDir;
    private File testDirectory;

    @Before
    public void setUp() throws IOException {
        testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create test directory: " + configDir.getAbsolutePath());
        }

        TestUtil.writeSystemConfig(configDir);
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testElevationToSolzen_removeSource() throws ParseException, IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration(true);

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2006-119", "-end", "2006-120",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd14_sst_amsre-aq_aatsr-en_2006-119_2006-120.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assert3DVariable("aatsr-en_view_zenith_fward", 0, 0, 0, 55.27372360229492, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_view_zenith_nadir", 1, 1, 2, 18.978195190429688, mmd);

            NCTestUtils.assert3DVariable("aatsr-en_sun_zenith_fward", 2, 2, 2, 58.21630859375, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_sun_zenith_nadir", 3, 3, 3, 55.733280181884766, mmd);

            assertNull(NCTestUtils.getVariable("aatsr-en_view_elev_fward", mmd));
            assertNull(NCTestUtils.getVariable("aatsr-en_view_elev_nadir", mmd));
            assertNull(NCTestUtils.getVariable("aatsr-en_sun_elev_fward", mmd));
            assertNull(NCTestUtils.getVariable("aatsr-en_sun_elev_nadir", mmd));
        }
    }

    @Test
    public void testElevationToSolzen_keepSource() throws ParseException, IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration(false);

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2006-119", "-end", "2006-120",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd14_sst_amsre-aq_aatsr-en_2006-119_2006-120.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assert3DVariable("aatsr-en_view_zenith_fward", 4, 4, 4, 52.90900802612305, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_view_elev_fward", 4, 4, 4, 37.09099197387695, mmd);

            NCTestUtils.assert3DVariable("aatsr-en_view_zenith_nadir", 5, 5, 5, 0.6720352172851562, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_view_elev_nadir", 5, 5, 5, 89.32796478271484, mmd);

            NCTestUtils.assert3DVariable("aatsr-en_sun_zenith_fward", 6, 6, 6, 55.281005859375, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_sun_elev_fward", 6, 6, 6, 34.718994140625, mmd);

            NCTestUtils.assert3DVariable("aatsr-en_sun_zenith_nadir", 7, 7, 7, 52.76158142089844, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_sun_elev_nadir", 7, 7, 7, 37.23841857910156, mmd);
        }
    }

    private void writeConfiguration(boolean removeSource) throws IOException {
        final String postProcessingConfig = "<post-processing-config>\n" +
                "    <create-new-files>\n" +
                "        <output-directory>\n" +
                testDirectory.getAbsolutePath() +
                "        </output-directory>\n" +
                "    </create-new-files>\n" +
                "    <post-processings>\n" +
                "        <elevation-to-solzen-angle>\n" +
                "            <convert source-name = \"aatsr-en_view_elev_fward\" target-name = \"aatsr-en_view_zenith_fward\" remove-source = \"" + Boolean.toString(removeSource) + "\"/>" +
                "            <convert source-name = \"aatsr-en_view_elev_nadir\" target-name = \"aatsr-en_view_zenith_nadir\" remove-source = \"" + Boolean.toString(removeSource) + "\"/>" +
                "            <convert source-name = \"aatsr-en_sun_elev_fward\" target-name = \"aatsr-en_sun_zenith_fward\" remove-source = \"" + Boolean.toString(removeSource) + "\"/>" +
                "            <convert source-name = \"aatsr-en_sun_elev_nadir\" target-name = \"aatsr-en_sun_zenith_nadir\" remove-source = \"" + Boolean.toString(removeSource) + "\"/>" +
                "        </elevation-to-solzen-angle>" +
                "    </post-processings>\n" +
                "</post-processing-config>";

        final File postProcessingConfigFile = new File(configDir, "post-processing-config.xml");
        if (!postProcessingConfigFile.createNewFile()) {
            fail("unable to create test file");
        }
        TestUtil.writeStringTo(postProcessingConfigFile, postProcessingConfig);
    }

    private File getInputDirectory() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        return new File(testDataDirectory, "post-processing");
    }
}
