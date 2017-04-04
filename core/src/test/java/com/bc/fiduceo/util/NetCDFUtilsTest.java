/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.mockito.InOrder;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.*;
import ucar.nc2.iosp.netcdf3.N3iosp;

import java.io.IOException;

public class NetCDFUtilsTest {

    @Test
    public void testGetDefaultFillValue_Array_Double() throws Exception {
        final Array array = mock(Array.class);
        when(array.getDataType()).thenReturn(DataType.DOUBLE);

        final Number value = NetCDFUtils.getDefaultFillValue(array);
        assertEquals(N3iosp.NC_FILL_DOUBLE, value);
    }

    @Test
    public void testGetDefaultFillValue_Array_Long() throws Exception {
        final Array array = mock(Array.class);
        when(array.getDataType()).thenReturn(DataType.LONG);

        final Number value = NetCDFUtils.getDefaultFillValue(array);
        assertEquals(N3iosp.NC_FILL_LONG, value);
    }

    @Test
    public void testGetDefaultFillValue() {
        assertEquals(N3iosp.NC_FILL_DOUBLE, NetCDFUtils.getDefaultFillValue(double.class));
        assertEquals(N3iosp.NC_FILL_FLOAT, NetCDFUtils.getDefaultFillValue(float.class));
        assertEquals(N3iosp.NC_FILL_LONG, NetCDFUtils.getDefaultFillValue(long.class));
        assertEquals(N3iosp.NC_FILL_INT, NetCDFUtils.getDefaultFillValue(int.class));
        assertEquals(N3iosp.NC_FILL_SHORT, NetCDFUtils.getDefaultFillValue(short.class));
        assertEquals(N3iosp.NC_FILL_BYTE, NetCDFUtils.getDefaultFillValue(byte.class));
    }

    @Test
    public void testGetDefaultFillValue_invalidInput() {
        try {
            NetCDFUtils.getDefaultFillValue(String.class);
            fail("RuntimeException expected");
        } catch (RuntimeException expected){
        }
    }

    @Test
    public void testToFloat() {
        final int[] ints = {12, 23, 45, 67};
        final Array intArray = Array.factory(ints);

        final Array floatArray = NetCDFUtils.toFloat(intArray);
        assertNotNull(floatArray);
        assertEquals(float.class, floatArray.getDataType().getPrimitiveClassType());
        assertEquals(12.0, floatArray.getFloat(0), 1e-8);
        assertEquals(23.0, floatArray.getFloat(1), 1e-8);
        assertEquals(45.0, floatArray.getFloat(2), 1e-8);
        assertEquals(67.0, floatArray.getFloat(3), 1e-8);
    }

    @Test
    public void testGetGlobalAttributeString() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Attribute attribute = mock(Attribute.class);

        when(attribute.getStringValue()).thenReturn("theValue");
        when(netcdfFile.findGlobalAttribute("the_attribute")).thenReturn(attribute);

        final String value = NetCDFUtils.getGlobalAttributeString("the_attribute", netcdfFile);
        assertEquals("theValue", value);
    }

    @Test
    public void testGetGlobalAttributeString_missingAttribute() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        when(netcdfFile.findGlobalAttribute("the_attribute")).thenReturn(null);

        try {
            NetCDFUtils.getGlobalAttributeString("the_attribute", netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testGetFillValue_fromAttribute() {
        final Variable variable = mock(Variable.class);
        final Attribute attribute = mock(Attribute.class);
        when(attribute.getNumericValue()).thenReturn(19);
        when(variable.findAttribute("_FillValue")).thenReturn(attribute);

        final Number fillValue = NetCDFUtils.getFillValue(variable);
        assertEquals(19, fillValue.intValue());
    }

    @Test
    public void testGetFillValue_fromDataType() {
        final Variable variable = mock(Variable.class);
        when(variable.getDataType()).thenReturn(DataType.FLOAT);

        final Number fillValue = NetCDFUtils.getFillValue(variable);
        assertEquals(N3iosp.NC_FILL_FLOAT, fillValue.floatValue(), 1e-8);
    }

    @Test
    public void testGetDimensionSize() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Dimension dimension = mock(Dimension.class);
        when(dimension.getLength()).thenReturn(22);
        when(netcdfFile.findDimension("matchup_count")).thenReturn(dimension);

        assertEquals(22, NetCDFUtils.getDimensionLength("matchup_count", netcdfFile));
    }

    @Test
    public void testGetDimensionSize_dimensionNotPresent() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        try {
            NetCDFUtils.getDimensionLength("matchup_count", netcdfFile);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testEnsureFillValue_DOUBLE() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.DOUBLE);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute("_FillValue");
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute("_FillValue", N3iosp.NC_FILL_DOUBLE)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_FLOAT() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.FLOAT);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute("_FillValue");
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute("_FillValue", N3iosp.NC_FILL_FLOAT)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_LONG() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.LONG);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute("_FillValue");
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute("_FillValue", N3iosp.NC_FILL_LONG)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_INT() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.INT);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute("_FillValue");
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute("_FillValue", N3iosp.NC_FILL_INT)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_SHORT() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.SHORT);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute("_FillValue");
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute("_FillValue", N3iosp.NC_FILL_SHORT)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_BYTE() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.BYTE);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute("_FillValue");
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute("_FillValue", N3iosp.NC_FILL_BYTE)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_DOUBLE_existingFillValue() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.findAttribute(anyString())).thenReturn(new Attribute("name", "value"));

        NetCDFUtils.ensureFillValue(mock);

        verify(mock, times(1)).findAttribute("_FillValue");
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testGetVariable_reader() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Variable variable = mock(Variable.class);

        when(netcdfFile.findVariable(null, "schneckchen")).thenReturn(variable);

        final Variable resultVariable = NetCDFUtils.getVariable(netcdfFile, "schneckchen");
        assertSame(variable, resultVariable);

        verify(netcdfFile, times(1)).findVariable(null, "schneckchen");
        verifyNoMoreInteractions(netcdfFile);
    }

    @Test
    public void testGetVariable_reader_escapedName() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Variable variable = mock(Variable.class);

        when(netcdfFile.findVariable(null, "schneck\\.chen")).thenReturn(variable);

        final Variable resultVariable = NetCDFUtils.getVariable(netcdfFile, "schneck.chen");
        assertSame(variable, resultVariable);

        verify(netcdfFile, times(1)).findVariable(null, "schneck\\.chen");
        verifyNoMoreInteractions(netcdfFile);
    }

    @Test
    public void testGetVariable_reader_variableNotPresent() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        when(netcdfFile.findVariable(null, "not_there")).thenReturn(null);

        try {
            NetCDFUtils.getVariable(netcdfFile, "not_there");
            fail("RuntimeException expected");
        } catch (RuntimeException expected){
        }
    }

    @Test
    public void testGetVariable_writer() {
        final NetcdfFileWriter fileWriter = mock(NetcdfFileWriter.class);
        final Variable variable = mock(Variable.class);

        when(fileWriter.findVariable("var-iable")).thenReturn(variable);

        final Variable resultVariable = NetCDFUtils.getVariable(fileWriter, "var-iable");
        assertSame(variable, resultVariable);

        verify(fileWriter, times(1)).findVariable("var-iable");
        verifyNoMoreInteractions(fileWriter);
    }

    @Test
    public void testGetVariable_writer_escapedName() {
        final NetcdfFileWriter fileWriter = mock(NetcdfFileWriter.class);
        final Variable variable = mock(Variable.class);

        when(fileWriter.findVariable("var\\.iable")).thenReturn(variable);

        final Variable resultVariable = NetCDFUtils.getVariable(fileWriter, "var.iable");
        assertSame(variable, resultVariable);

        verify(fileWriter, times(1)).findVariable("var\\.iable");
        verifyNoMoreInteractions(fileWriter);
    }

    @Test
    public void testGetVariable_writer_variableNotPresent() {
        final NetcdfFileWriter fileWriter = mock(NetcdfFileWriter.class);

        when(fileWriter.findVariable("not_there")).thenReturn(null);

        try {
            NetCDFUtils.getVariable(fileWriter, "not_there");
            fail("RuntimeException expected");
        } catch (RuntimeException expected){
        }
    }

    @Test
    public void testGetAttributeFloat_attributeValue() {
        final Attribute attribute = mock(Attribute.class);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("schlumpf")).thenReturn(attribute);
        when(attribute.getNumericValue()).thenReturn(1.87645f);

        final float attributeFloat = NetCDFUtils.getAttributeFloat(variable, "schlumpf", -12.8f);
        assertEquals(1.87645f, attributeFloat, 1e-8);
    }

    @Test
    public void testGetAttributeFloat_defaultValue() {
        final Variable variable = mock(Variable.class);

        final float attributeFloat = NetCDFUtils.getAttributeFloat(variable, "schlimm", -12.8f);
        assertEquals(-12.8f, attributeFloat, 1e-8);
    }
}
