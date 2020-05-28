package com.bc.fiduceo.reader.modis;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThermalNoiseVariableTest {

    private static byte[] DATA = {26, 27, 33, 35, 35, 31, 28, 28, 29, 15,
            29, 26, 27, 25, 27, 30, 35, 30, 21, 26,
            31, 26, 28, 25, 25, 30, 27, 27, 26, 31,
            30, 26, 21, 24, 18, 24, 25, 25, 25, 23,
            24, 28, 27, 26, 26, 27, 26, 28, 25, 25,
            27, 27, 27, 25, 27, 29, 28, 28, 26, 25,
            34, 26, 36, 27, 29, 27, 25, 25, 34, 30,
            28, 30, 30, 30, 29, 31, 30, 28, 31, 28,
            32, 51, 32, 31, 30, 35, 31, 38, 38, 32,
            31, 35, 44, 88, 70, 32, 32, 31, 30, 37,
            25, 16, 13, 19, 17, 20, 21, 18, 24, 26,
            24, 24, 23, 22, 22, 24, 20, 24, 22, 24,
            28, 29, 30, 29, 29, 28, 29, 28, 28, 29,
            29, 30, 28, 29, 28, 29, 29, 29, 29, 28,
            30, 28, 28, 29, 29, 29, 29, 31, 30, 31,
            29, 28, 29, 28, 30, 31, 29, 30, 29, 30
    };

    @Test
    public void testGetShortName() {
        final Variable variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("NOISE");

        final ThermalNoiseVariable noiseVariable = new ThermalNoiseVariable(variable, 0, 230);

        assertEquals("NOISE_ch20", noiseVariable.getShortName());
    }

    @Test
    public void testGetDataType() {
        final Variable variable = mock(Variable.class);
        when(variable.getDataType()).thenReturn(DataType.UBYTE);

        final ThermalNoiseVariable noiseVariable = new ThermalNoiseVariable(variable, 1, 240);

        assertEquals(DataType.UBYTE, noiseVariable.getDataType());
    }

    @Test
    public void testRead() throws IOException {
        final Array originalData = Array.factory(DataType.UBYTE, new int[]{16, 10}, DATA);
        final Variable variable = mock(Variable.class);
        when(variable.read()).thenReturn(originalData);

        final ThermalNoiseVariable noiseVariable = new ThermalNoiseVariable(variable, 2, 250);
        final Array expandedArray = noiseVariable.read();
        final int[] shape = expandedArray.getShape();
        assertEquals(1, shape.length);
        assertEquals(250, shape[0]);
        assertEquals(31, expandedArray.getShort(0));
        assertEquals(26, expandedArray.getShort(1));
        assertEquals(31, expandedArray.getShort(9));
        assertEquals(31, expandedArray.getShort(10));
        assertEquals(25, expandedArray.getShort(14));
        assertEquals(25, expandedArray.getShort(24));

    }
}
