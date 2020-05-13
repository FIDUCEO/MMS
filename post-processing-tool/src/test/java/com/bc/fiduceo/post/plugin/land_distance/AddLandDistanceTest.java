package com.bc.fiduceo.post.plugin.land_distance;


import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.util.DistanceToLandMap;
import com.bc.fiduceo.util.NetCDFUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AddLandDistanceTest {

    static Element createFullConfigElement() throws JDOMException, IOException {
        final String configXML = "<add-distance-to-land>" +
                "    <aux-file-path>path-to-file</aux-file-path>" +
                "    <target-variable name=\"close_or_far\" />" +
                "    <lon-variable name=\"longi\" />" +
                "    <lat-variable name=\"latte\" />" +
                "</add-distance-to-land>";

        return TestUtil.createDomElement(configXML);
    }

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
    public void testPrepare() {
        final AddLandDistance.Configuration configuration = new AddLandDistance.Configuration();
        configuration.lonVariableName = "longitude";
        configuration.targetVariableName = "distance_to_land";

        final NetcdfFile reader = mock(NetcdfFile.class);

        final Variable variable = mock(Variable.class);
        final ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension(FiduceoConstants.MATCHUP_COUNT, 142));
        dimensions.add(new Dimension("height", 1));
        dimensions.add(new Dimension("width", 1));
        when(variable.getDimensions()).thenReturn(dimensions);
        when(reader.findVariable(null, "longitude")).thenReturn(variable);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final Variable targetVariable = mock(Variable.class);
        when(writer.addVariable(any(), eq(configuration.targetVariableName), eq(DataType.FLOAT), (List<Dimension>) any())).thenReturn(targetVariable);

        final AddLandDistance plugin = new AddLandDistance(configuration);

        plugin.prepare(reader, writer);

        verify(reader, times(1)).findVariable(null, "longitude");
        verify(variable, times(1)).getDimensions();
        verify(writer, times(1)).addVariable(null, configuration.targetVariableName, DataType.FLOAT, dimensions);
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testCompute() throws IOException, InvalidRangeException {
        final AddLandDistance.Configuration configuration = new AddLandDistance.Configuration();
        configuration.lonVariableName = "longitude";
        configuration.latVariableName = "latitude";
        configuration.targetVariableName = "distance_to_land";

        final double[] longitudes = {100, 101, 102, 103, 104};
        final Array lonArray = NetCDFUtils.create(longitudes);
        final double[] latitudes = {10, 11, 12, 13, 14};
        final Array latArray = NetCDFUtils.create(latitudes);

        final Variable lonVariable = mock(Variable.class);
        when(lonVariable.read()).thenReturn(lonArray);

        final Variable latVariable = mock(Variable.class);
        when(latVariable.read()).thenReturn(latArray);

        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.findVariable(null, "longitude")).thenReturn(lonVariable);
        when(reader.findVariable(null, "latitude")).thenReturn(latVariable);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final Variable targetVariable = mock(Variable.class);
        when(writer.findVariable(configuration.targetVariableName)).thenReturn(targetVariable);

        final DistanceToLandMap distanceToLandMap = mock(DistanceToLandMap.class);
        when(distanceToLandMap.getDistance(anyDouble(), anyDouble())).thenReturn(14.8);

        final AddLandDistance plugin = new AddLandDistance(configuration);
        plugin.setDistanceToLandMap(distanceToLandMap);

        plugin.compute(reader, writer);

        verify(reader, times(1)).findVariable(null, "longitude");
        verify(lonVariable, times(1)).read();
        verify(reader, times(1)).findVariable(null, "latitude");
        verify(latVariable, times(1)).read();
        verify(writer, times(1)).findVariable("distance_to_land");
        verify(writer, times(1)).write(any(Variable.class), any());
        verifyNoMoreInteractions(reader, writer);
    }
}
