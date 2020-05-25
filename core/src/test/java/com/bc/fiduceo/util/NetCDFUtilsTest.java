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

import com.bc.fiduceo.FiduceoConstants;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.opengis.metadata.Datatype;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;
import ucar.nc2.iosp.netcdf3.N3iosp;

import java.io.IOException;

import static com.bc.fiduceo.util.NetCDFUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NetCDFUtilsTest {

    @Test
    public void testGetDefaultFillValue_Array_Double() {
        final Array array = mock(Array.class);
        when(array.getDataType()).thenReturn(DataType.DOUBLE);

        final Number value = NetCDFUtils.getDefaultFillValue(array);
        assertEquals(N3iosp.NC_FILL_DOUBLE, value);
    }

    @Test
    public void testGetDefaultFillValue_Array_Long() {
        final Array array = mock(Array.class);
        when(array.getDataType()).thenReturn(DataType.LONG);

        final Number value = NetCDFUtils.getDefaultFillValue(array);
        assertEquals(N3iosp.NC_FILL_INT64, value);
    }

    @Test
    public void testGetDefaultFillValue() {
        assertEquals(N3iosp.NC_FILL_DOUBLE, NetCDFUtils.getDefaultFillValue(double.class));
        assertEquals(N3iosp.NC_FILL_FLOAT, NetCDFUtils.getDefaultFillValue(float.class));
        assertEquals(N3iosp.NC_FILL_INT64, NetCDFUtils.getDefaultFillValue(long.class));
        assertEquals(N3iosp.NC_FILL_INT, NetCDFUtils.getDefaultFillValue(int.class));
        assertEquals(N3iosp.NC_FILL_SHORT, NetCDFUtils.getDefaultFillValue(short.class));
        assertEquals(N3iosp.NC_FILL_BYTE, NetCDFUtils.getDefaultFillValue(byte.class));
    }

    @Test
    public void testGetDefaultFillValue_invalidInput() {
        try {
            NetCDFUtils.getDefaultFillValue(String.class);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetDefaultFillValue_withUnsigned() {
        assertEquals(N3iosp.NC_FILL_DOUBLE, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false));
        assertEquals(N3iosp.NC_FILL_DOUBLE, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, true));
        assertEquals(N3iosp.NC_FILL_FLOAT, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false));
        assertEquals(N3iosp.NC_FILL_FLOAT, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, true));
        assertEquals(N3iosp.NC_FILL_INT64, NetCDFUtils.getDefaultFillValue(DataType.LONG, false));
        assertEquals(N3iosp.NC_FILL_INT64, NetCDFUtils.getDefaultFillValue(DataType.LONG, true));
        assertEquals(N3iosp.NC_FILL_INT, NetCDFUtils.getDefaultFillValue(DataType.INT, false));
        assertEquals(N3iosp.NC_FILL_UINT, NetCDFUtils.getDefaultFillValue(DataType.INT, true));
        assertEquals(N3iosp.NC_FILL_SHORT, NetCDFUtils.getDefaultFillValue(DataType.SHORT, false));
        assertEquals(N3iosp.NC_FILL_USHORT, NetCDFUtils.getDefaultFillValue(DataType.SHORT, true));
        assertEquals(N3iosp.NC_FILL_BYTE, NetCDFUtils.getDefaultFillValue(DataType.BYTE, false));
        assertEquals(N3iosp.NC_FILL_UBYTE, NetCDFUtils.getDefaultFillValue(DataType.BYTE, true));
    }

    @Test
    public void testGetDefaultFillValue_withUnsigned_invalidInput() {
        try {
            NetCDFUtils.getDefaultFillValue(DataType.SEQUENCE, true);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetNetcdfDataType() {
        assertEquals(DataType.BYTE, NetCDFUtils.getNetcdfDataType(ProductData.TYPE_INT8));
        assertEquals(DataType.BYTE, NetCDFUtils.getNetcdfDataType(ProductData.TYPE_UINT8));
        assertEquals(DataType.SHORT, NetCDFUtils.getNetcdfDataType(ProductData.TYPE_INT16));
        assertEquals(DataType.SHORT, NetCDFUtils.getNetcdfDataType(ProductData.TYPE_UINT16));
        assertEquals(DataType.INT, NetCDFUtils.getNetcdfDataType(ProductData.TYPE_INT32));
        assertEquals(DataType.INT, NetCDFUtils.getNetcdfDataType(ProductData.TYPE_UINT32));
        assertEquals(DataType.FLOAT, NetCDFUtils.getNetcdfDataType(ProductData.TYPE_FLOAT32));
        assertEquals(DataType.DOUBLE, NetCDFUtils.getNetcdfDataType(ProductData.TYPE_FLOAT64));
        assertEquals(DataType.STRING, NetCDFUtils.getNetcdfDataType(ProductData.TYPE_ASCII));
        assertEquals(DataType.STRING, NetCDFUtils.getNetcdfDataType(ProductData.TYPE_UTC));
    }

    @Test
    public void testEscalateUnsignedDataType() {
        assertEquals(DataType.SHORT, NetCDFUtils.escalateUnsignedType(DataType.UBYTE));
        assertEquals(DataType.SHORT, NetCDFUtils.escalateUnsignedType(DataType.BYTE));

        assertEquals(DataType.INT, NetCDFUtils.escalateUnsignedType(DataType.USHORT));
        assertEquals(DataType.INT, NetCDFUtils.escalateUnsignedType(DataType.SHORT));

        assertEquals(DataType.LONG, NetCDFUtils.escalateUnsignedType(DataType.UINT));
        assertEquals(DataType.LONG, NetCDFUtils.escalateUnsignedType(DataType.INT));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testGetNetcdfDataType_invalidInput() {
        try {
            NetCDFUtils.getNetcdfDataType(ProductData.TYPE_UNDEFINED);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testToFloat() {
        final int[] ints = {12, 23, 45, 67};
        final Array intArray = Array.factory(DataType.INT, new int[]{4}, ints);

        final Array floatArray = NetCDFUtils.toFloat(intArray);
        assertNotNull(floatArray);
        assertEquals(float.class, floatArray.getDataType().getPrimitiveClassType());
        assertEquals(12.0, floatArray.getFloat(0), 1e-8);
        assertEquals(23.0, floatArray.getFloat(1), 1e-8);
        assertEquals(45.0, floatArray.getFloat(2), 1e-8);
        assertEquals(67.0, floatArray.getFloat(3), 1e-8);
    }

    @Test
    public void testGetGlobalAttributeString() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Attribute attribute = mock(Attribute.class);

        when(attribute.getStringValue()).thenReturn("theValue");
        when(netcdfFile.findGlobalAttribute("the_attribute")).thenReturn(attribute);

        final String value = NetCDFUtils.getGlobalAttributeString("the_attribute", netcdfFile);
        assertEquals("theValue", value);
    }

    @Test
    public void testGetGlobalAttributeString_missingAttribute() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        when(netcdfFile.findGlobalAttribute("the_attribute")).thenReturn(null);

        try {
            NetCDFUtils.getGlobalAttributeString("the_attribute", netcdfFile);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetGlobalAttributeInt() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Attribute attribute = mock(Attribute.class);

        when(attribute.getNumericValue()).thenReturn(38);
        when(netcdfFile.findGlobalAttribute("the_attribute")).thenReturn(attribute);

        final int intValue = NetCDFUtils.getGlobalAttributeInt("the_attribute", netcdfFile);
        assertEquals(38, intValue);
    }

    @Test
    public void testGetGlobalAttributeInt_missingAttribute() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        when(netcdfFile.findGlobalAttribute("the_attribute")).thenReturn(null);

        try {
            NetCDFUtils.getGlobalAttributeInt("the_attribute", netcdfFile);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetFillValue_fromAttribute() {
        final Variable variable = mock(Variable.class);
        final Attribute attribute = mock(Attribute.class);
        when(attribute.getNumericValue()).thenReturn(19);
        when(variable.findAttribute(CF_FILL_VALUE_NAME)).thenReturn(attribute);

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
        when(netcdfFile.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(dimension);

        assertEquals(22, NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, netcdfFile));
    }

    @Test
    public void testGetDimensionSize_dimensionNotPresent() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        try {
            NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, netcdfFile);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testEnsureFillValue_DOUBLE() {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.DOUBLE);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute(CF_FILL_VALUE_NAME);
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute(CF_FILL_VALUE_NAME, N3iosp.NC_FILL_DOUBLE)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_FLOAT() {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.FLOAT);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute(CF_FILL_VALUE_NAME);
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute(CF_FILL_VALUE_NAME, N3iosp.NC_FILL_FLOAT)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_LONG() {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.LONG);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute(CF_FILL_VALUE_NAME);
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute(CF_FILL_VALUE_NAME, N3iosp.NC_FILL_INT64)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_INT() {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.INT);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute(CF_FILL_VALUE_NAME);
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute(CF_FILL_VALUE_NAME, N3iosp.NC_FILL_INT)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_SHORT() {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.SHORT);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute(CF_FILL_VALUE_NAME);
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute(CF_FILL_VALUE_NAME, N3iosp.NC_FILL_SHORT)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_BYTE() {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.BYTE);
        when(mock.findAttribute(anyString())).thenReturn(null);

        NetCDFUtils.ensureFillValue(mock);

        final InOrder order = inOrder(mock);
        order.verify(mock, times(1)).findAttribute(CF_FILL_VALUE_NAME);
        order.verify(mock, times(1)).getDataType();
        order.verify(mock, times(1)).addAttribute(eq(new Attribute(CF_FILL_VALUE_NAME, N3iosp.NC_FILL_BYTE)));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testEnsureFillValue_DOUBLE_existingFillValue() {
        final Variable mock = mock(Variable.class);
        when(mock.findAttribute(anyString())).thenReturn(new Attribute("name", "value"));

        NetCDFUtils.ensureFillValue(mock);

        verify(mock, times(1)).findAttribute(CF_FILL_VALUE_NAME);
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
        } catch (RuntimeException expected) {
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
    @Ignore // @todo 1 tb/tb repair this 2020-05-08
    public void testGetVariable_writer_escapedName() {
        final NetcdfFileWriter fileWriter = mock(NetcdfFileWriter.class);
        final Variable variable = mock(Variable.class);

        when(fileWriter.findVariable("var.iable")).thenReturn(variable);

        final Variable resultVariable = NetCDFUtils.getVariable(fileWriter, "var.iable");
        assertSame(variable, resultVariable);

        verify(fileWriter, times(1)).findVariable("var.iable");
        verifyNoMoreInteractions(fileWriter);
    }

    @Test
    public void testGetVariable_writer_variableNotPresent() {
        final NetcdfFileWriter fileWriter = mock(NetcdfFileWriter.class);

        when(fileWriter.findVariable("not_there")).thenReturn(null);

        try {
            NetCDFUtils.getVariable(fileWriter, "not_there");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetAttributeInt_attributeValue() {
        final Attribute attribute = mock(Attribute.class);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("herman")).thenReturn(attribute);
        when(attribute.getNumericValue()).thenReturn(36);

        final int attributeInt = NetCDFUtils.getAttributeInt(variable, "herman", -999);
        assertEquals(36, attributeInt);
    }

    @Test
    public void testGetAttributeInt_defaultValue() {
        final Variable variable = mock(Variable.class);

        final int attributeInt = NetCDFUtils.getAttributeInt(variable, "is_not_present", -999);
        assertEquals(-999, attributeInt);
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

    @Test
    public void testGetAttributeDouble_attributeValue() {
        final Attribute attribute = mock(Attribute.class);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("doppel")).thenReturn(attribute);
        when(attribute.getNumericValue()).thenReturn(2.9833);

        final double attributeDouble = NetCDFUtils.getAttributeDouble(variable, "doppel", -19.8);
        assertEquals(2.9833, attributeDouble, 1e-8);
    }

    @Test
    public void testGetAttributeDouble_defaultValue() {
        final Variable variable = mock(Variable.class);

        final double attributeDouble = NetCDFUtils.getAttributeDouble(variable, "duppeell", -13.9);
        assertEquals(-13.9, attributeDouble, 1e-8);
    }

    @Test
    public void testGetAttributeString_attributeValue() {
        final Attribute attribute = mock(Attribute.class);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("Text")).thenReturn(attribute);
        when(attribute.getStringValue()).thenReturn("Jippie");

        final String attributeString = NetCDFUtils.getAttributeString(variable, "Text", "default");
        assertEquals("Jippie", attributeString);
    }

    @Test
    public void testGetAttributeString_defaultValue() {
        final Variable variable = mock(Variable.class);

        final String attributeString = NetCDFUtils.getAttributeString(variable, "what?", "the_default");
        assertEquals("the_default", attributeString);
    }

    @Test
    public void testGetGlobalAttributeSafe() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Attribute attribute = mock(Attribute.class);

        when(netcdfFile.findGlobalAttribute("the_attribute")).thenReturn(attribute);

        final Attribute globalAttribute = NetCDFUtils.getGlobalAttributeSafe("the_attribute", netcdfFile);
        assertNotNull(globalAttribute);
    }

    @Test
    public void testGetGlobalAttributeSafe_missingAttribute() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        when(netcdfFile.findGlobalAttribute("the_attribute")).thenReturn(null);

        try {
            NetCDFUtils.getGlobalAttributeSafe("the_attribute", netcdfFile);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testGetScaleFactor_CF() {
        final Attribute attribute = mock(Attribute.class);
        when(attribute.getNumericValue()).thenReturn(28.6);

        final Variable variable = mock(Variable.class);
        when(variable.findAttribute(CF_SCALE_FACTOR_NAME)).thenReturn(attribute);

        final double scale = NetCDFUtils.getScaleFactor(variable);
        assertEquals(28.6, scale, 1e-8);
    }

    @Test
    public void testGetScaleFactor_MHS() {
        final Attribute attribute = mock(Attribute.class);
        when(attribute.getNumericValue()).thenReturn(24.0);

        final Variable variable = mock(Variable.class);
        when(variable.findAttribute("Scale")).thenReturn(attribute);

        final double scale = NetCDFUtils.getScaleFactor(variable);
        assertEquals(24.0, scale, 1e-8);
    }

    @Test
    public void testGetScaleFactor_notPresent() {
        final Variable variable = mock(Variable.class);

        final double scale = NetCDFUtils.getScaleFactor(variable);
        assertEquals(1.0, scale, 1e-8);
    }

    @Test
    public void testGetOffset_CF() {
        final Attribute attribute = mock(Attribute.class);
        when(attribute.getNumericValue()).thenReturn(30.8);

        final Variable variable = mock(Variable.class);
        when(variable.findAttribute(CF_OFFSET_NAME)).thenReturn(attribute);

        final double offset = NetCDFUtils.getOffset(variable);
        assertEquals(30.8, offset, 1e-8);
    }

    @Test
    public void testGetOffset_notPresent() {
        final Variable variable = mock(Variable.class);

        final double offset = NetCDFUtils.getOffset(variable);
        assertEquals(0.0, offset, 1e-8);
    }

    @Test
    public void testSection() throws InvalidRangeException, IOException {
        final Array array = mock(Array.class);
        final Array result = mock(Array.class);

        when(array.section(any(), any())).thenReturn(result);

        final Array section = NetCDFUtils.section(array, new int[2], new int[2]);
        assertSame(section, result);

        verify(array, times(1)).section(any(), any());
        verifyNoMoreInteractions(array);
    }

    @Test
    public void testSection_failure() throws InvalidRangeException {
        final Array array = mock(Array.class);
        when(array.section(any(), any())).thenThrow(new InvalidRangeException());

        try {
            NetCDFUtils.section(array, new int[2], new int[2]);
            fail("IOException expected");
        } catch (IOException expected) {
        }

        verify(array, times(1)).section(any(), any());
        verifyNoMoreInteractions(array);
    }
}


