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
 */

package com.bc.fiduceo.post;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.esa.snap.core.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class PostProcessingToolMain_IO_Test {

    private File configDir;
    private File dataDir;

    @Before
    public void setUp() {
        File testDir = new File(TestUtil.getTestDir(), "PostProcessingToolTest");
        configDir = new File(testDir, "config");
        dataDir = new File(testDir, "data");
    }

    @After
    public void tearDown() {
        final File testDir = TestUtil.getTestDir();
        if (testDir.isDirectory()) {
            FileUtils.deleteTree(testDir);
            assertFalse(testDir.exists());
        }
    }

    @Test
    public void acceptanceTest() throws IOException {
        configDir.mkdirs();
        TestUtil.writeSystemConfig(configDir);

        final Element rootElement = new Element("post-processing-config").addContent(Arrays.asList(
                new Element("overwrite"),
                new Element("post-processings").addContent(
                        new Element("spherical-distance").addContent(Arrays.asList(
                                new Element("target").addContent(Arrays.asList(
                                        new Element("data-type").addContent("Float"),
                                        new Element("var-name").addContent("post_dist"),
                                        new Element("dim-name").addContent(FiduceoConstants.MATCHUP_COUNT)
                                )),
                                new Element("primary-lat-variable").setAttribute("scaleAttrName", "Scale").addContent("amsub-n16_Latitude"),
                                new Element("primary-lon-variable").setAttribute("scaleAttrName", "Scale").addContent("amsub-n16_Longitude"),
                                new Element("secondary-lat-variable").addContent("ssmt2-f14_lat"),
                                new Element("secondary-lon-variable").addContent("ssmt2-f14_lon")
                        ))
                )
        ));

        final Document document = new Document(rootElement);
        final String processingConfigFileName = "processing-config.xml";
        final File file = new File(configDir, processingConfigFileName);
        try (OutputStream stream = Files.newOutputStream(file.toPath())) {
            new XMLOutputter(Format.getPrettyFormat()).output(document, stream);
        }

        dataDir.mkdirs();
        final String filename = "mmd22_amsub-n16_ssmt2-f14_2000-306_2000-312.nc";
        final File src = new File(new File(TestUtil.getTestDataDirectory(), "post-processing"), filename);
        final File target = new File(dataDir, filename);
        Files.copy(src.toPath(), target.toPath());

        final String[] args = {
                "-c", configDir.getAbsolutePath(),
                "-i", dataDir.getAbsolutePath(),
                "-start", "2000-306",
                "-end", "2000-312",
                "-j", processingConfigFileName
        };

        PostProcessingToolMain.main(args);

        try (NetcdfFile netcdfFile = NetcdfFile.open(target.getAbsolutePath())) {
            final Variable postDistVar = netcdfFile.findVariable("post_dist");
            final Variable matchupDistVar = netcdfFile.findVariable("matchup_spherical_distance");

            assertNotNull(postDistVar);
            assertNotNull(matchupDistVar);

            final Array pDistArr = postDistVar.read();
            final Array mDistArr = matchupDistVar.read();

            final float[] pStorage = (float[]) pDistArr.getStorage();
            final float[] mStorage = (float[]) mDistArr.getStorage();

            assertEquals(4, pStorage.length);
            assertEquals(4, mStorage.length);

            for (int i = 0; i < pStorage.length; i++) {
                assertEquals(pStorage[i], mStorage[i], 1e-3);
            }
        }
    }
}
