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
                "        <an_ml_o3>ozone</an_ml_o3>" +
                "        <an_ml_lnsp>pressure</an_ml_lnsp>" +
                "        <an_sfc_t2m>tempi</an_sfc_t2m>" +
                "        <an_sfc_u10>blowUp</an_sfc_u10>" +
                "        <an_sfc_v10>blowVert</an_sfc_v10>" +
                "        <an_sfc_siconc>concentrate</an_sfc_siconc>" +
                "        <an_sfc_msl>meanPress</an_sfc_msl>" +
                "        <an_sfc_skt>skinTemp</an_sfc_skt>" +
                "        <an_sfc_sst>ozeanTemp</an_sfc_sst>" +
                "        <an_sfc_tcc>cloudy</an_sfc_tcc>" +
                "        <an_sfc_tcwv>steam!</an_sfc_tcwv>" +
                "    </satellite-fields>" +
                "</era5>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = Era5PostProcessingPlugin.createConfiguration(rootElement);
        final SatelliteFieldsConfiguration satConfig = configuration.getSatelliteFields();
        assertNotNull(satConfig);

        assertEquals("Kjuh", satConfig.get_an_q_name());
        assertEquals("tea", satConfig.get_an_t_name());
        assertEquals("ozone", satConfig.get_an_o3_name());
        assertEquals("pressure", satConfig.get_an_lnsp_name());
        assertEquals("tempi", satConfig.get_an_t2m_name());
        assertEquals("blowUp", satConfig.get_an_u10_name());
        assertEquals("blowVert", satConfig.get_an_v10_name());
        assertEquals("concentrate", satConfig.get_an_siconc_name());
        assertEquals("meanPress", satConfig.get_an_msl_name());
        assertEquals("skinTemp", satConfig.get_an_skt_name());
        assertEquals("ozeanTemp", satConfig.get_an_sst_name());
        assertEquals("cloudy", satConfig.get_an_tcc_name());
        assertEquals("steam!", satConfig.get_an_tcwv_name());
    }

    @Test
    public void testCreateConfiguration_matchupFields() throws JDOMException, IOException {
        final String XML = "<era5>" +
                "    <nwp-aux-dir>/where/the/data/is</nwp-aux-dir>" +
                "    <matchup-fields>" +
                "        <an_sfc_u10>blowUp</an_sfc_u10>" +
                "        <an_sfc_v10>vertico</an_sfc_v10>" +
                "        <an_sfc_siconc>sindbad</an_sfc_siconc>" +
                "        <an_sfc_sst>warmwater</an_sfc_sst>" +
                "    </matchup-fields>" +
                "</era5>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = Era5PostProcessingPlugin.createConfiguration(rootElement);
        final MatchupFieldsConfiguration matchupConfig = configuration.getMatchupFields();
        assertNotNull(matchupConfig);

        assertEquals("blowUp", matchupConfig.get_an_u10_name());
        assertEquals("vertico", matchupConfig.get_an_v10_name());
        assertEquals("sindbad", matchupConfig.get_an_siconc_name());
        assertEquals("warmwater", matchupConfig.get_an_sst_name());
    }
}

