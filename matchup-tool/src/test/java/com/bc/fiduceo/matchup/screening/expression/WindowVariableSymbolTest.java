/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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

package com.bc.fiduceo.matchup.screening.expression;

import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.jexp.Term;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class WindowVariableSymbolTest {

    private WindowVariableSymbol.NoDataListener listener;
    private Variable variable;
    private WindowReaderEvalEnv evalEnv;

    @Before
    public void setUp() {
        listener = mock(WindowVariableSymbol.NoDataListener.class);
        variable = mock(Variable.class);
        evalEnv = mock(WindowReaderEvalEnv.class);
    }

    @Test
    public void testGetName() {
        when(variable.getDataType()).thenReturn(DataType.INT);
        when(variable.getShortName()).thenReturn("schlumpf");

        final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);
        assertEquals("schlumpf", symbol.getName());
    }

    @Test
    public void testGetRetType() {
        when(variable.getDataType()).thenReturn(DataType.SHORT);

        final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);
        assertEquals(Term.TYPE_D, symbol.getRetType());
    }

    @Test
    public void testIsConst() {
        when(variable.getDataType()).thenReturn(DataType.BYTE);

        final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);
        assertFalse(symbol.isConst());
    }

    @Test
    public void testGetRetType_throwsOnNonNumericType() {
        when(variable.getDataType()).thenReturn(DataType.STRING);

        try {
            final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);
            symbol.getRetType();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testEvalB() {
        when(variable.getDataType()).thenReturn(DataType.DOUBLE);

        final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);
        try {
            symbol.evalB(evalEnv);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testEvalI() throws IOException, InvalidRangeException {
        final Array array = NetCDFUtils.create(new short[]{19});
        when(evalEnv.fetchPixel(any())).thenReturn(array);

        when(variable.getDataType()).thenReturn(DataType.SHORT);

        final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);

        assertEquals(19, symbol.evalI(evalEnv));
    }

    @Test
    public void testEvalI_fillValue() throws IOException, InvalidRangeException {
        final Array array = NetCDFUtils.create(new int[]{NetCDFUtils.getDefaultFillValue(int.class).intValue()});
        when(evalEnv.fetchPixel(any())).thenReturn(array);

        when(variable.getDataType()).thenReturn(DataType.INT);

        final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);

        assertEquals(-2147483647, symbol.evalI(evalEnv));

        verify(listener, times(1)).fireNoData();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testEvalD() throws IOException, InvalidRangeException {
        final Array array = NetCDFUtils.create(new double[]{20.21});
        when(evalEnv.fetchPixel(any())).thenReturn(array);

        when(variable.getDataType()).thenReturn(DataType.DOUBLE);

        final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);

        assertEquals(20.21, symbol.evalD(evalEnv), 1e-8);
    }

    @Test
    public void testEvalD_NaN() throws IOException, InvalidRangeException {
        final Array array = NetCDFUtils.create(new double[]{Double.NaN});
        when(evalEnv.fetchPixel(any())).thenReturn(array);

        when(variable.getDataType()).thenReturn(DataType.DOUBLE);

        final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);

        assertEquals(Double.NaN, symbol.evalD(evalEnv), 1e-8);

        verify(listener, times(1)).fireNoData();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testEvalD_fillValue() throws IOException, InvalidRangeException {
        final Array array = NetCDFUtils.create(new double[]{NetCDFUtils.getDefaultFillValue(double.class).doubleValue()});
        when(evalEnv.fetchPixel(any())).thenReturn(array);

        when(variable.getDataType()).thenReturn(DataType.DOUBLE);

        final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);

        assertEquals(Double.NaN, symbol.evalD(evalEnv), 1e-8);

        verify(listener, times(1)).fireNoData();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testEvalS() {
        when(variable.getDataType()).thenReturn(DataType.SHORT);

        final WindowVariableSymbol symbol = new WindowVariableSymbol(variable, listener);
        try {
            symbol.evalS(evalEnv);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
