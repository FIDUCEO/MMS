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
}
