package com.bc.fiduceo.post.plugin;


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ElevationToSolZenAnglePluginTest {

    private static final String FULL_CONFIG = "<elevation-to-solzen-angle>" +
            "    <convert source-name = \"elli_vation\" target-name = \"zenith\" remove-source = \"true\"/>" +
            "</elevation-to-solzen-angle>";
    private ElevationToSolZenAnglePlugin plugin;

    @Before
    public void setUp(){
        plugin = new ElevationToSolZenAnglePlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("elevation-to-solzen-angle", plugin.getPostProcessingName());
    }

    @Test
    public void testCreatePostProcessing() throws JDOMException, IOException {
        final Element rootElement = TestUtil.createDomElement(FULL_CONFIG);

        final PostProcessing postProcessing = plugin.createPostProcessing(rootElement);
        assertNotNull(postProcessing);
        assertTrue(postProcessing instanceof ElevationToSolZenAngle);
    }

    @Test
    public void testCreateConfiguration_emptyConfig() throws JDOMException, IOException {
        final String configXML = "<elevation-to-solzen-angle>" +
                "</elevation-to-solzen-angle>";

        final Element rootElement = TestUtil.createDomElement(configXML);
        final ElevationToSolZenAngle.Configuration configuration = ElevationToSolZenAngle.createConfiguration(rootElement);
        assertEquals(0, configuration.conversions.size());
    }
}
