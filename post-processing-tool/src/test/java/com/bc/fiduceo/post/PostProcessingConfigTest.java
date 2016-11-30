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

import com.bc.fiduceo.post.distance.PostSphericalDistance;
import com.bc.fiduceo.util.JDomUtils;
import org.esa.snap.core.dataio.dimap.DimapProductHelpers;
import org.esa.snap.core.util.Debug;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.junit.*;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class PostProcessingConfigTest {

    private PostProcessingConfig config;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("processing_config.xml");
        config = PostProcessingConfig.load(stream);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testStore() {
        final String ls = System.lineSeparator();

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            config.store(outputStream);
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<post-processing-config>");
            pw.println("  <post-processings>");
            pw.println("    <spherical-distance>");
            pw.println("      <target>");
            pw.println("        <data-type>Float</data-type>");
            pw.println("        <var-name>post_dist</var-name>");
            pw.println("        <dim-name>matchup_count</dim-name>");
            pw.println("      </target>");
            pw.println("      <primary-lat-variable scaleAttrName=\"Scale\">amsub-n16_Latitude</primary-lat-variable>");
            pw.println("      <primary-lon-variable scaleAttrName=\"Scale\">amsub-n16_Longitude</primary-lon-variable>");
            pw.println("      <secondary-lat-variable>ssmt2-f14_lat</secondary-lat-variable>");
            pw.println("      <secondary-lon-variable>ssmt2-f14_lon</secondary-lon-variable>");
            pw.println("    </spherical-distance>");
            pw.println("  </post-processings>");
            pw.println("</post-processing-config>");
            pw.flush();

            assertEquals(sw.toString().trim(), outputStream.toString().trim());
        } catch (IOException e) {
            fail("should never come here");
        }
    }

    @Test
    public void testValidInitialised() throws Exception {
        final List<PostProcessing> processings = config.getProcessings();

        assertNotNull(processings);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", processings.getClass().getTypeName());
        assertEquals(1, processings.size());

        final PostProcessing postProcessing = processings.get(0);

        assertNotNull(postProcessing);
        assertEquals("com.bc.fiduceo.post.distance.PostSphericalDistance", postProcessing.getClass().getTypeName());

        final PostSphericalDistance sphericalDistance = (PostSphericalDistance) postProcessing;
        assertEquals("post_dist", sphericalDistance.targetVarName);
        assertEquals("Float", sphericalDistance.targetDataType);
        assertEquals("matchup_count", sphericalDistance.targetDimName);
        assertEquals("amsub-n16_Latitude", sphericalDistance.primLatVar);
        assertEquals("Scale", sphericalDistance.primLatScaleAttrName);
        assertEquals(null, sphericalDistance.primLatOffsetAttrName);
        assertEquals("amsub-n16_Longitude", sphericalDistance.primLonVar);
        assertEquals("Scale", sphericalDistance.primLonScaleAttrName);
        assertEquals(null, sphericalDistance.primLonOffsetAttrName);
        assertEquals("ssmt2-f14_lat", sphericalDistance.secoLatVar);
        assertEquals(null, sphericalDistance.secoLatScaleAttrName);
        assertEquals(null, sphericalDistance.secoLatOffsetAttrName);
        assertEquals("ssmt2-f14_lon", sphericalDistance.secoLonVar);
        assertEquals(null, sphericalDistance.secoLonScaleAttrName);
        assertEquals(null, sphericalDistance.secoLonOffsetAttrName);
    }
}
