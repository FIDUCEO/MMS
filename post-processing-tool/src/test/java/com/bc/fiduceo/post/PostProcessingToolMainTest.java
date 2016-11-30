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
import org.junit.*;
import org.junit.runner.*;

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

        dataDir.mkdirs();
        final String filename = "mmd22_amsub-n16_ssmt2-f14_2000-306_2000-312.nc";
        final Path src = Paths.get( getClass().getResource(filename).toURI());
        final OutputStream outputStream = Files.newOutputStream(dataDir.toPath().resolve(filename));
        Files.copy(src, outputStream);
        outputStream.flush();
        outputStream.close();


        final String[] args = {
                    "-c", configDir.getAbsolutePath(),
                    "-d", dataDir.getAbsolutePath(),
                    "-start", "2000-306",
                    "-end", "2000-312",
                    "-j", Paths.get(getClass().getResource("processing_config.xml").toURI()).toAbsolutePath().toString()
        };

        PostProcessingToolMain.main(args);
    }
}
