package com.bc.fiduceo.post.plugin.avhrr_fcdr;

import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class AddAvhrrCorrCoeffsTest {

    @Test
    public void testCreateConfiguration() throws JDOMException, IOException {
        final String configXML = "<add-avhrr-corr-coeffs>" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "    <target-x-elem-variable name=\"exelem\" />" +
                "    <target-x-line-variable name=\"exline\" />" +
                "</add-avhrr-corr-coeffs>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        final AddAvhrrCorrCoeffs.Configuration configuration = AddAvhrrCorrCoeffs.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("fileName", configuration.fileNameVariableName);
        assertEquals("proc-ver", configuration.versionVariableName);
        assertEquals("exelem", configuration.targetXElemName);
        assertEquals("exline", configuration.targetXLineName);
    }

    @Test
    public void testCreateConfiguration_missingFileName() throws JDOMException, IOException {
        final String configXML = "<add-avhrr-corr-coeffs>" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "    <target-x-elem-variable name=\"exelem\" />" +
                "    <target-x-line-variable name=\"exline\" />" +
                "</add-avhrr-corr-coeffs>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddAvhrrCorrCoeffs.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_missingProcessingVersion() throws JDOMException, IOException {
        final String configXML = "<add-avhrr-corr-coeffs>" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <target-x-elem-variable name=\"exelem\" />" +
                "    <target-x-line-variable name=\"exline\" />" +
                "</add-avhrr-corr-coeffs>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddAvhrrCorrCoeffs.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_missingXElemName() throws JDOMException, IOException {
        final String configXML = "<add-avhrr-corr-coeffs>" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "    <target-x-line-variable name=\"exline\" />" +
                "</add-avhrr-corr-coeffs>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddAvhrrCorrCoeffs.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_missingXLineName() throws JDOMException, IOException {
        final String configXML = "<add-avhrr-corr-coeffs>" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "    <target-x-elem-variable name=\"exelem\" />" +
                "</add-avhrr-corr-coeffs>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddAvhrrCorrCoeffs.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testExtractSensorKey() {
        assertEquals("avhrr-n19-fcdr", AddAvhrrCorrCoeffs.extractSensorKey("FIDUCEO_FCDR_L1C_AVHRR_N19ALL_20110705055721_20110705073927_EASY_v0.2Bet_fv2.0.0.nc"));
        assertEquals("avhrr-ma-fcdr", AddAvhrrCorrCoeffs.extractSensorKey("FIDUCEO_FCDR_L1C_AVHRR_MTAC3A_20161108185739_20161108203900_EASY_v0.2Bet_fv2.0.0.nc"));
        assertEquals("avhrr-n11-fcdr", AddAvhrrCorrCoeffs.extractSensorKey("FIDUCEO_FCDR_L1C_AVHRR_N11ALL_19911222154531_19911222172732_EASY_v0.3Bet_fv2.0.0.nc"));
        assertEquals("avhrr-n12-fcdr", AddAvhrrCorrCoeffs.extractSensorKey("FIDUCEO_FCDR_L1C_AVHRR_N12ALL_19950317065908_19950317084026_EASY_v0.3Bet_fv2.0.0.nc"));
        assertEquals("avhrr-n17-fcdr", AddAvhrrCorrCoeffs.extractSensorKey("FIDUCEO_FCDR_L1C_AVHRR_N17ALL_20040911053100_20040911071213_EASY_v0.3Bet_fv2.0.0.nc"));
    }
}
