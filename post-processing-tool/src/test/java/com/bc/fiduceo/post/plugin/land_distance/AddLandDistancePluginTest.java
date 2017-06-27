package com.bc.fiduceo.post.plugin.land_distance;


import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AddLandDistancePluginTest {

    private AddLandDistancePlugin plugin;

    @Before
    public void setUp() {
        plugin = new AddLandDistancePlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("add-distance-to-land", plugin.getPostProcessingName());
    }

    @Test
    public void testCreatePostProcessing() throws JDOMException, IOException {
        final Element configElement = AddLandDistanceTest.createFullConfigElement();

        final PostProcessing postProcessing = plugin.createPostProcessing(configElement);
        assertTrue(postProcessing instanceof AddLandDistance);
    }

}
