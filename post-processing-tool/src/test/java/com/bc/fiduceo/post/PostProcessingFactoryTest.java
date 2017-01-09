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

import org.jdom.Element;
import org.junit.*;

import static com.bc.fiduceo.post.plugin.distance.SphericalDistancePlugin.TAG_NAME_DATA_TYPE;
import static com.bc.fiduceo.post.plugin.distance.SphericalDistancePlugin.TAG_NAME_DIM_NAME;
import static com.bc.fiduceo.post.plugin.distance.SphericalDistancePlugin.TAG_NAME_TARGET;
import static com.bc.fiduceo.post.plugin.distance.SphericalDistancePlugin.TAG_NAME_VAR_NAME;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Map;

public class PostProcessingFactoryTest {

    private PostProcessingFactory postProcessingFactory;

    @Before
    public void setUp() throws Exception {
        postProcessingFactory = PostProcessingFactory.get();
    }

    @Test
    public void testInitializePostProcessingFactory() {
        assertNotNull(postProcessingFactory);
        final Map<String, PostProcessingPlugin> plugins = postProcessingFactory.getPlugins();
        assertEquals("java.util.Collections$UnmodifiableMap", plugins.getClass().getTypeName());
        assertEquals(5, plugins.size());
        /* 01 */  assertTrue(plugins.containsKey("dummy-post-processing"));
        /* 02 */  assertTrue(plugins.containsKey("spherical-distance"));
        /* 03 */  assertTrue(plugins.containsKey("sst-insitu-time-series"));
        /* 04 */  assertTrue(plugins.containsKey("add-amsre-solar-angles"));
        /* 04 */  assertTrue(plugins.containsKey("nwp"));
    }

    @Test
    public void testGetPostProcessing() throws Exception {

        final Element element = new Element("spherical-distance").addContent(Arrays.asList(
                    new Element(TAG_NAME_TARGET).addContent(Arrays.asList(
                                new Element(TAG_NAME_VAR_NAME).addContent("post_sphere_distance"),
                                new Element(TAG_NAME_DIM_NAME).addContent("matchup_count"),
                                new Element(TAG_NAME_DATA_TYPE).addContent("Float")
                    )),
                    new Element("primary-lat-variable").addContent("p_lat"),
                    new Element("primary-lon-variable").addContent("p_lon"),
                    new Element("secondary-lat-variable").addContent("s_lat"),
                    new Element("secondary-lon-variable").addContent("s_lon")
        ));

        final PostProcessing postProcessing = postProcessingFactory.getPostProcessing(element);
        assertNotNull(postProcessing);
        assertEquals("com.bc.fiduceo.post.plugin.distance.SphericalDistance", postProcessing.getClass().getName());
    }

    @Test
    public void testGetPostProcessing_nonExistingPostProcessing() throws Exception {

        final Element element = new Element("non-existing-post-processing");

        try {
            postProcessingFactory.getPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("PostProcessing for name 'non-existing-post-processing' not available.", expected.getMessage());
        }
    }
}
