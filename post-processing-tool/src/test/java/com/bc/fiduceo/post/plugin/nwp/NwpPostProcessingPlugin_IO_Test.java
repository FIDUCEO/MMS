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
import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static com.bc.fiduceo.post.plugin.nwp.TestDirUtil.createDirectory;
import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class NwpPostProcessingPlugin_IO_Test {

    private NwpPostProcessingPlugin plugin;
    private File testDir;

    @Before
    public void setUp(){
        plugin = new NwpPostProcessingPlugin();
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
    public void testCreatePostProcessing() throws JDOMException, IOException {
        final File cdoDir = createDirectory(testDir, "cdo_exec");
        final File nwpDir = createDirectory(testDir, "nwp");

        final String config = "<nwp>" +
                "    <cdo-home>" + cdoDir.getAbsolutePath() +"</cdo-home>" +
                "    <nwp-aux-dir>" + nwpDir.getAbsolutePath() + "</nwp-aux-dir>" +
                "    <time-variable-name>the_time</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(config);

        final PostProcessing postProcessing = plugin.createPostProcessing(rootElement);
        assertNotNull(postProcessing);
        assertTrue(postProcessing instanceof NwpPostProcessing);
    }
}
