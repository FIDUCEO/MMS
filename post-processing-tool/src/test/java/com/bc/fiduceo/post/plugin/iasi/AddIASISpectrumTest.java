/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.iasi;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.reader.iasi.EpsMetopConstants;
import com.bc.fiduceo.reader.iasi.IASI_Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AddIASISpectrumTest {

    private NetcdfFile reader;
    private NetcdfFileWriter writer;

    @Before
    public void setUp() {
        reader = mock(NetcdfFile.class);
        writer = mock(NetcdfFileWriter.class);
    }

    @Test
    public void testPrepare() throws IOException, InvalidRangeException {
        final AddIASISpectrum.Configuration configuration = new AddIASISpectrum.Configuration();
        configuration.targetVariableName = "GS1cSpect";
        configuration.referenceVariableName = "ref_var";

        final Variable referenceVariable = mock(Variable.class);
        final ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("matchup_count", 142));
        dimensions.add(new Dimension("height", 1));
        dimensions.add(new Dimension("width", 1));
        when(referenceVariable.getDimensions()).thenReturn(dimensions);
        when(reader.findVariable(null, "ref_var")).thenReturn(referenceVariable);

        final ArrayList<Dimension> targetDimensions = new ArrayList<>();
        targetDimensions.addAll(dimensions);
        final Dimension iasiSsDimension = new Dimension("iasi_ss", 8700);
        targetDimensions.add(iasiSsDimension);

        when(writer.addDimension(null, "iasi_ss", 8700)).thenReturn(iasiSsDimension);

        final Variable targetVariable = mock(Variable.class);
        when(writer.addVariable(any(), eq("GS1cSpect"), eq(DataType.FLOAT), (List<Dimension>) any())).thenReturn(targetVariable);

        final AddIASISpectrum plugin = new AddIASISpectrum(configuration);

        plugin.prepare(reader, writer);

        verify(reader, times(1)).findVariable(null, "ref_var");
        verify(writer, times(1)).addVariable(null, "GS1cSpect", DataType.FLOAT, targetDimensions);
        verify(writer, times(1)).addDimension(null, "iasi_ss", 8700);
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testPrepare_missingReferenceVariable() throws IOException, InvalidRangeException {
        final AddIASISpectrum.Configuration configuration = new AddIASISpectrum.Configuration();
        configuration.targetVariableName = "GS1cSpect";
        configuration.referenceVariableName = "ref_var";

        final AddIASISpectrum plugin = new AddIASISpectrum(configuration);

        try {
            plugin.prepare(reader, writer);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

        verify(reader, times(1)).findVariable(null, "ref_var");
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testGetSensorKey() {
        assertEquals("iasi-ma", AddIASISpectrum.getSensorKey("IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat"));
        assertEquals("iasi-mb", AddIASISpectrum.getSensorKey("IASI_xxx_1C_M01_20140425124756Z_20140425142652Z_N_O_20140425133911Z.nat"));
    }

    @Test
    public void testGetSensorKey_invalidFileName() {
        try {
            AddIASISpectrum.getSensorKey("NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetFillValueSpectrum() {
        final Array spectrum = AddIASISpectrum.getFillValueSpectrum();
        assertNotNull(spectrum);

        final int[] shape = spectrum.getShape();
        assertEquals(1, shape.length);
        assertEquals(8700, shape[0]);

        final float expectedFill = NetCDFUtils.getDefaultFillValue(float.class).floatValue();
        assertEquals(expectedFill, spectrum.getFloat(0), 1e-8);
        assertEquals(expectedFill, spectrum.getFloat(3467), 1e-8);
        assertEquals(expectedFill, spectrum.getFloat(8699), 1e-8);
    }

    @Test
    public void testGetBoundingRectangle() {
        final IASI_Reader iasiReader = mock(IASI_Reader.class);
        when(iasiReader.getProductSize()).thenReturn(new com.bc.fiduceo.core.Dimension("bla", 101, 209));

        final Rectangle rectangle = AddIASISpectrum.getBoundingRectangle(iasiReader);
        assertNotNull(rectangle);
        assertEquals(0, rectangle.x);
        assertEquals(0, rectangle.y);
        assertEquals(101, rectangle.width);
        assertEquals(209, rectangle.height);
    }

    @Test
    public void testCreateConfiguration() throws JDOMException, IOException {
        final Element rootElement = createFullConfigElement();

        final AddIASISpectrum.Configuration configuration = AddIASISpectrum.createConfiguration(rootElement);
        assertEquals("schnecktrum", configuration.targetVariableName);
        assertEquals("reffi", configuration.referenceVariableName);
        assertEquals("exxi", configuration.xCoordinateName);
        assertEquals("yppsi", configuration.yCoordinateName);
        assertEquals("fileName", configuration.filenameVariableName);
        assertEquals("proc-ver", configuration.processingVersionVariableName);
    }

    static Element createFullConfigElement() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"reffi\" />" +
                "    <x-variable name=\"exxi\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        return TestUtil.createDomElement(configXML);
    }

    @Test
    public void testCreateConfiguration_missingTargetVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <reference-variable name=\"reffi\" />" +
                "    <x-variable name=\"exxi\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingTargetVariableNameAttribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable  />" +
                "    <reference-variable name=\"reffi\" />" +
                "    <x-variable name=\"exxi\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingReferenceVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <x-variable name=\"exxi\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingReferenceVariable_nameAttribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable  />" +
                "    <x-variable name=\"exxi\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingXVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingXVariable_nameAtribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable  />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingYVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingYVariable_nameAttribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <y-variable  />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingFileNameVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <y-variable  name=\"ypps\"/>" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingFileNameVariable_nameAttribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <y-variable  name=\"ypps\"/>" +
                "    <file-name-variable />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingProcessingVersionVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <y-variable  name=\"ypps\"/>" +
                "    <file-name-variable name=\"filius\"/>" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingProcessingVersionVariable_nameAttribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <y-variable  name=\"ypps\"/>" +
                "    <file-name-variable name=\"filius\"/>" +
                "    <processing-version-variable  />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testAddSpectrumDimension_noAdditionalDimensions() {
        final Dimension iasi_ss = new Dimension("iasi_ss", 8700);
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addDimension(null, "iasi_ss", EpsMetopConstants.SS)).thenReturn(iasi_ss);

        final List<ucar.nc2.Dimension> dimensions = new ArrayList<>();

        final List<Dimension> allDimensions = AddIASISpectrum.addSpectrumDimension(writer, dimensions);
        assertEquals(1, allDimensions.size());
        final Dimension spectrumDimension = allDimensions.get(0);
        assertEquals("iasi_ss", spectrumDimension.getFullName());

        verify(writer, times(1)).addDimension(null, "iasi_ss", EpsMetopConstants.SS);
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testAddSpectrumDimension_additionalDimensions() {
        final Dimension iasi_ss = new Dimension("iasi_ss", 8700);
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addDimension(null, "iasi_ss", EpsMetopConstants.SS)).thenReturn(iasi_ss);

        final List<ucar.nc2.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("the_other_one", 156));

        final List<Dimension> allDimensions = AddIASISpectrum.addSpectrumDimension(writer, dimensions);
        assertEquals(2, allDimensions.size());
        Dimension spectrumDimension = allDimensions.get(0);
        assertEquals("the_other_one", spectrumDimension.getFullName());

        spectrumDimension = allDimensions.get(1);
        assertEquals("iasi_ss", spectrumDimension.getFullName());

        verify(writer, times(1)).addDimension(null, "iasi_ss", EpsMetopConstants.SS);
        verifyNoMoreInteractions(writer);
    }
}
