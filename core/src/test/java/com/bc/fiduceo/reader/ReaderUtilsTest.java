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

package com.bc.fiduceo.reader;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReaderUtilsTest {

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeDouble() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.DOUBLE);

        final Number value = ReaderUtils.getDefaultFillValue(mock);

        assertEquals(Double.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeFloat() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.FLOAT);

        final Number value = ReaderUtils.getDefaultFillValue(mock);

        assertEquals(Float.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeLong() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.LONG);

        final Number value = ReaderUtils.getDefaultFillValue(mock);

        assertEquals(Long.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeInt() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.INT);

        final Number value = ReaderUtils.getDefaultFillValue(mock);

        assertEquals(Integer.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeShort() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.SHORT);

        final Number value = ReaderUtils.getDefaultFillValue(mock);

        assertEquals(Short.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeByte() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.BYTE);

        final Number value = ReaderUtils.getDefaultFillValue(mock);

        assertEquals(Byte.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forUnknownType() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.STRUCTURE);

        try {
            ReaderUtils.getDefaultFillValue(mock);
            fail("RuntimeException expected");
        } catch (NullPointerException notExpected) {
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetDefaultFillValue_Array_Double() throws Exception {
        final Array array = mock(Array.class);
        when(array.getDataType()).thenReturn(DataType.DOUBLE);

        final Number value = ReaderUtils.getDefaultFillValue(array);

        assertEquals(Double.MIN_VALUE, value);
    }

    @Test
    public void testGetDefaultFillValue_Aray_Long() throws Exception {
        final Array array = mock(Array.class);
        when(array.getDataType()).thenReturn(DataType.LONG);

        final Number value = ReaderUtils.getDefaultFillValue(array);

        assertEquals(Long.MIN_VALUE, value);
    }

    @Test
    public void testMustScale() {
        assertTrue(ReaderUtils.mustScale(1.2, 0.45));
        assertTrue(ReaderUtils.mustScale(1.2, 0.0));
        assertTrue(ReaderUtils.mustScale(1.0, 0.45));

        assertFalse(ReaderUtils.mustScale(1.0, 0.0));
    }

    @Test
    public void testStripChannelSuffix() {
        assertEquals("btemps", ReaderUtils.stripChannelSuffix("btemps_ch17"));
        assertEquals("chanqual", ReaderUtils.stripChannelSuffix("chanqual_ch4"));

        assertEquals("Latitude", ReaderUtils.stripChannelSuffix("Latitude"));
        assertEquals("scnlindy", ReaderUtils.stripChannelSuffix("scnlindy"));
    }

    @Test
    public void testGetChannelIndex() {
        assertEquals(17, ReaderUtils.getChannelIndex("btemps_ch18"));
        assertEquals(4, ReaderUtils.getChannelIndex("chanqual_ch5"));

        assertEquals(0, ReaderUtils.getChannelIndex("lon"));
        assertEquals(0, ReaderUtils.getChannelIndex("a_strange_channel"));
    }

    @Test
    public void testToFloat() {
        final int[] ints = {12, 23, 45, 67};
        final Array intArray = Array.factory(ints);

        final Array floatArray = ReaderUtils.toFloat(intArray);
        assertNotNull(floatArray);
        assertEquals(float.class, floatArray.getDataType().getPrimitiveClassType());
        assertEquals(12.0, floatArray.getFloat(0), 1e-8);
        assertEquals(23.0, floatArray.getFloat(1), 1e-8);
        assertEquals(45.0, floatArray.getFloat(2), 1e-8);
        assertEquals(67.0, floatArray.getFloat(3), 1e-8);
    }
}
