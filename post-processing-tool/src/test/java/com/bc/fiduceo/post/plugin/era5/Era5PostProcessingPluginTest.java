package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Era5PostProcessingPluginTest {

    private Era5PostProcessingPlugin plugin;

    @Before
    public void setUp() {
        plugin = new Era5PostProcessingPlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("era5", plugin.getPostProcessingName());
    }

    @Test
    public void testCreateConfiguration_missing_nwpAuxDir() throws JDOMException, IOException {
        final String XML = "<era5>" +
                "</era5>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            Era5PostProcessingPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_nwpAuxDir() throws JDOMException, IOException {
        final String XML = "<era5>" +
                "    <nwp-aux-dir>/where/the/data/is</nwp-aux-dir>" +
                "</era5>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = Era5PostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("/where/the/data/is", configuration.getNWPAuxDir());
    }

    @Test
    public void testCreateConfiguration_satelliteFields() throws JDOMException, IOException {
        final String XML = "<era5>" +
                "    <nwp-aux-dir>/where/the/data/is</nwp-aux-dir>" +
                "    <satellite-fields>" +
                "        <an_ml_q>Kjuh</an_ml_q>" +
                "        <an_ml_t>tea</an_ml_t>" +
                "    </satellite-fields>" +
                "</era5>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = Era5PostProcessingPlugin.createConfiguration(rootElement);
        final SatelliteFieldsConfiguration satConfig = configuration.getSatelliteFields();
        assertNotNull(satConfig);

        assertEquals("Kjuh", satConfig.get_an_q_name());
        assertEquals("tea", satConfig.get_an_t_name());
    }
}

