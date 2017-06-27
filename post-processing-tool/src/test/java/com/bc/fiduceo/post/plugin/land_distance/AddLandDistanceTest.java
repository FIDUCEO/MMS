package com.bc.fiduceo.post.plugin.land_distance;


import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class AddLandDistanceTest {

    @Test
    public void testCreateConfig() throws JDOMException, IOException {
        final Element fullConfigElement = createFullConfigElement();

        final AddLandDistance.Configuration configuration = AddLandDistance.createConfiguration(fullConfigElement);
        assertNotNull(configuration);
        assertEquals("close_or_far", configuration.targetVariableName);
        assertEquals("path-to-file", configuration.auxDataFilePath);
        assertEquals("longi", configuration.lonVariableName);
        assertEquals("latte", configuration.latVariableName);
    }

    @Test
    public void testCreateConfig_missingTargetVariable() throws JDOMException, IOException {
        final String configXML = "<add-distance-to-land>" +
                "    <aux-file-path name=\"path-to-file\" />" +
                "</add-distance-to-land>";
        final Element element = TestUtil.createDomElement(configXML);

        try {
            AddLandDistance.createConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfig_missingAuxPath() throws JDOMException, IOException {
        final String configXML = "<add-distance-to-land>" +
                "    <target-variable name=\"close_or_far\" />" +
                "</add-distance-to-land>";
        final Element element = TestUtil.createDomElement(configXML);

        try {
            AddLandDistance.createConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfig_invalidTag() throws JDOMException, IOException {
        final String configXML = "<bert>" +
                "    <target-variable name=\"close_or_far\" />" +
                "    <aux-file-path name=\"path-to-file\" />" +
                "</bert>";
        final Element element = TestUtil.createDomElement(configXML);

        try {
            AddLandDistance.createConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testPreapare() throws IOException, InvalidRangeException {
        final NetcdfFile reader = mock(NetcdfFile.class);
        final Variable variable = mock(Variable.class);
        when(reader.findVariable(null, "longitude")).thenReturn(variable);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);

        final AddLandDistance.Configuration configuration = new AddLandDistance.Configuration();
        configuration.lonVariableName = "longitude";

        final AddLandDistance plugin = new AddLandDistance(configuration);

        plugin.prepare(reader, writer);

        verify(reader, times(1)).findVariable(null, "longitude");

        // @todo 1 tb/tb continue here 2016-06-27
    }

    static Element createFullConfigElement() throws JDOMException, IOException {
        final String configXML = "<add-distance-to-land>" +
                "    <aux-file-path>path-to-file</aux-file-path>" +
                "    <target-variable name=\"close_or_far\" />" +
                "    <lon-variable name=\"longi\" />" +
                "    <lat-variable name=\"latte\" />" +
                "</add-distance-to-land>";

        return TestUtil.createDomElement(configXML);
    }
}
