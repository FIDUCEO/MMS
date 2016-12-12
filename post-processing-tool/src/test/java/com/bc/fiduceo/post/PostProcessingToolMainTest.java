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

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.apache.commons.cli.ParseException;
import org.esa.snap.core.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@RunWith(IOTestRunner.class)
public class PostProcessingToolMainTest {

    private File testDir;
    private File configDir;
    private File dataDir;

    @Before
    public void setUp() throws Exception {
        testDir = new File(TestUtil.getTestDir(), "PostProcessingToolTest");
        configDir = new File(testDir, "config");
        dataDir = new File(testDir, "data");
    }

    @After
    public void tearDown() throws Exception {
        if (testDir.isDirectory()) {
            FileUtils.deleteTree(testDir);
        }
    }

    @Test
    public void acceptanceTest() throws IOException, URISyntaxException, ParseException {
        configDir.mkdirs();
        final FileWriter fileWriter = new FileWriter(new File(configDir, "system-config.xml"));
        fileWriter.write("<system-config></system-config>");
        fileWriter.close();

        final String processingConfigFileName = "processing-config.xml";
        OutputStream outputStream = Files.newOutputStream(configDir.toPath().resolve(processingConfigFileName));
        final Element rootElement = new Element("post-processing-config").addContent(Arrays.asList(
                    new Element("overwrite"),
                    new Element("post-processings").addContent(
                                new Element("spherical-distance").addContent(Arrays.asList(
                                            new Element("target").addContent(Arrays.asList(
                                                        new Element("data-type").addContent("Float"),
                                                        new Element("var-name").addContent("post_dist"),
                                                        new Element("dim-name").addContent("matchup_count")
                                            )),
                                            new Element("primary-lat-variable").setAttribute("scaleAttrName","Scale").addContent("amsub-n16_Latitude"),
                                            new Element("primary-lon-variable").setAttribute("scaleAttrName","Scale").addContent("amsub-n16_Longitude"),
                                            new Element("secondary-lat-variable").addContent("ssmt2-f14_lat"),
                                            new Element("secondary-lon-variable").addContent("ssmt2-f14_lon")
                                ))
                    )
        ));

        final Document document = new Document(rootElement);
        new XMLOutputter(Format.getPrettyFormat()).output(document, outputStream);
        outputStream.close();

        dataDir.mkdirs();
        final String filename = "mmd22_amsub-n16_ssmt2-f14_2000-306_2000-312.nc";
        final Path src = TestUtil.getTestDataDirectory().toPath().resolve("post-processing").resolve(filename);
        final Path target = dataDir.toPath().resolve(filename);
        outputStream = Files.newOutputStream(target);
        Files.copy(src, outputStream);
        outputStream.close();

        final String[] args = {
                    "-c", configDir.getAbsolutePath(),
                    "-i", dataDir.getAbsolutePath(),
                    "-start", "2000-306",
                    "-end", "2000-312",
                    "-j", processingConfigFileName
        };

        PostProcessingToolMain.main(args);

        final NetcdfFile netcdfFile = NetcdfFile.open(target.toAbsolutePath().toString());
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
