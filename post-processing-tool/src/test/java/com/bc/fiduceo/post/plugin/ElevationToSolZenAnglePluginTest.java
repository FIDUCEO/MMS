package com.bc.fiduceo.post.plugin;


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ElevationToSolZenAnglePluginTest {

    private static final String FULL_CONFIG = "<elevation-to-solzen-angle>" +
            "    <convert source-name = \"elli_vation\" target-name = \"zenith\" remove-source = \"true\"/>" +
            "</elevation-to-solzen-angle>";
    private ElevationToSolZenAnglePlugin plugin;

    @Before
    public void setUp() {
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
        try {
            ElevationToSolZenAnglePlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_oneConversion() throws JDOMException, IOException {
        final Element rootElement = TestUtil.createDomElement(FULL_CONFIG);

        final ElevationToSolZenAngle.Configuration configuration = ElevationToSolZenAnglePlugin.createConfiguration(rootElement);
        assertEquals(1, configuration.conversions.size());
        final ElevationToSolZenAngle.Conversion conversion = configuration.conversions.get(0);
        assertEquals("elli_vation", conversion.sourceName);
        assertEquals("zenith", conversion.targetName);
        assertTrue(conversion.removeSource);
    }

    @Test
    public void testCreateConfiguration_twoConversions() throws JDOMException, IOException {
        final String configXML = "<elevation-to-solzen-angle>" +
                "    <convert source-name = \"elevate\" target-name = \"zenzi\" remove-source = \"true\"/>" +
                "    <convert source-name = \"up_up\" target-name = \"to_nadir\" remove-source = \"false\"/>" +
                "</elevation-to-solzen-angle>";
        final Element rootElement = TestUtil.createDomElement(configXML);

        final ElevationToSolZenAngle.Configuration configuration = ElevationToSolZenAnglePlugin.createConfiguration(rootElement);
        assertEquals(2, configuration.conversions.size());

        ElevationToSolZenAngle.Conversion conversion = configuration.conversions.get(0);
        assertEquals("elevate", conversion.sourceName);
        assertEquals("zenzi", conversion.targetName);
        assertTrue(conversion.removeSource);

        conversion = configuration.conversions.get(1);
        assertEquals("up_up", conversion.sourceName);
        assertEquals("to_nadir", conversion.targetName);
        assertFalse(conversion.removeSource);
    }

    @Test
    public void testCreateConfiguration_missingSourceName() throws JDOMException, IOException {
        final String configXML = "<elevation-to-solzen-angle>" +
                "    <convert target-name = \"zenzi\" remove-source = \"true\"/>" +
                "</elevation-to-solzen-angle>";

        final Element rootElement = TestUtil.createDomElement(configXML);
        try {
            ElevationToSolZenAnglePlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_missingTargetName() throws JDOMException, IOException {
        final String configXML = "<elevation-to-solzen-angle>" +
                "    <convert source-name = \"zenzi\" remove-source = \"true\"/>" +
                "</elevation-to-solzen-angle>";

        final Element rootElement = TestUtil.createDomElement(configXML);
        try {
            ElevationToSolZenAnglePlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_missingRemoveSource() throws JDOMException, IOException {
        final String configXML = "<elevation-to-solzen-angle>" +
                "    <convert source-name = \"zenzi\" target-name = \"zoppo\"/>" +
                "</elevation-to-solzen-angle>";

        final Element rootElement = TestUtil.createDomElement(configXML);
        final ElevationToSolZenAngle.Configuration configuration = ElevationToSolZenAnglePlugin.createConfiguration(rootElement);
        assertEquals(1, configuration.conversions.size());

        ElevationToSolZenAngle.Conversion conversion = configuration.conversions.get(0);
        assertEquals("zenzi", conversion.sourceName);
        assertEquals("zoppo", conversion.targetName);
        assertTrue(conversion.removeSource);
    }

    @Test
    public void testCreateConfiguration_wrongTagName() throws JDOMException, IOException {
        final String configXML = "<elevation-to-beer>" +
                "    <convert source-name = \"zenzi\" remove-source = \"true\"/>" +
                "</elevation-to-beer>";

        final Element rootElement = TestUtil.createDomElement(configXML);
        try {
            ElevationToSolZenAnglePlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
