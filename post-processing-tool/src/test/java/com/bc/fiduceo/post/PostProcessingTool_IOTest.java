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

package com.bc.fiduceo.post;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.FileUtils;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

import static com.bc.fiduceo.post.plugin.point_distance.SphericalDistancePlugin.TAG_NAME_SPHERICAL_DISTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class PostProcessingTool_IOTest {

    private final String processingConfigName = "processing-config.xml";

    private File configDir;
    private File testDir;

    @Before
    public void setUp() throws Exception {
        testDir = TestUtil.createTestDirectory();
        configDir = new File(testDir, "config");
        if (!configDir.mkdirs()) {
            fail("unable to create test directory");
        }

        final OutputStream outputStream = Files.newOutputStream(configDir.toPath().resolve(processingConfigName));
        final PrintWriter pw = new PrintWriter(outputStream);
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<post-processing-config>");
        pw.println("    <overwrite/>");
        pw.println("    <post-processings>");
        pw.println("        <spherical-point_distance>");
        pw.println("            <target>");
        pw.println("                <data-type>Float</data-type>");
        pw.println("                <var-name>post_dist</var-name>");
        pw.println("                <dim-name>matchup_count</dim-name>");
        pw.println("            </target>");
        pw.println("            <primary-lat-variable scaleAttrName=\"Scale\">amsub-n16_Latitude</primary-lat-variable>");
        pw.println("            <primary-lon-variable scaleAttrName=\"Scale\">amsub-n16_Longitude</primary-lon-variable>");
        pw.println("            <secondary-lat-variable>ssmt2-f14_lat</secondary-lat-variable>");
        pw.println("            <secondary-lon-variable>ssmt2-f14_lon</secondary-lon-variable>");
        pw.println("        </spherical-point_distance>");
        pw.println("    </post-processings>");
        pw.println("</post-processing-config>");
        pw.close();
    }

    @After
    public void tearDown() throws Exception {
        if (testDir.isDirectory()) {
            FileUtils.deleteTree(testDir);
        }
    }

    @Test
    public void testInitialisation() throws Exception {
        final Options options = PostProcessingTool.getOptions();
        final PosixParser parser = new PosixParser();
        final CommandLine commandLine = parser.parse(options, new String[]{
                    "-j", processingConfigName,
                    "-i", "/mmd_files",
                    "-start", "2011-123",
                    "-end", "2011-124",
                    "-c", configDir.getPath()
        });

        final FileWriter fileWriter = new FileWriter(new File(configDir, "system-config.xml"));
        fileWriter.write("<system-config></system-config>");
        fileWriter.close();

        final PostProcessingContext context = PostProcessingTool.initializeContext(commandLine);

        final String separator = FileSystems.getDefault().getSeparator();
        assertEquals(separator + "mmd_files", context.getMmdInputDirectory().toString());
        assertEquals("03-May-2011 00:00:00", ProductData.UTC.createDateFormat().format(context.getStartDate()));
        assertEquals("04-May-2011 23:59:59", ProductData.UTC.createDateFormat().format(context.getEndDate()));

        final SystemConfig sysConfig = context.getSystemConfig();
        assertNotNull(sysConfig);
        assertNull(sysConfig.getArchiveConfig());

        final PostProcessingConfig config = context.getProcessingConfig();
        assertNotNull(config);
        final List<Element> postProcessingElements = config.getPostProcessingElements();
        assertNotNull(postProcessingElements);
        assertEquals("java.util.Collections$UnmodifiableList", postProcessingElements.getClass().getTypeName());
        assertEquals(1, postProcessingElements.size());
        assertEquals(TAG_NAME_SPHERICAL_DISTANCE, postProcessingElements.get(0).getName());
    }

}
