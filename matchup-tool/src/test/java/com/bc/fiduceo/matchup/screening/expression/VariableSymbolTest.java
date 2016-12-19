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

package com.bc.fiduceo.matchup.screening.expression;

import com.bc.fiduceo.reader.Reader;
import org.esa.snap.core.jexp.Term;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class VariableSymbolTest {

    @Test
    public void testGetName() {
        final Variable variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("the_var_name");

        final VariableSymbol symbol = new VariableSymbol(variable);
        assertEquals("the_var_name", symbol.getName());
    }

    @Test
    public void testIsConst() {
        final Variable variable = mock(Variable.class);
        final VariableSymbol symbol = new VariableSymbol(variable);

        assertFalse(symbol.isConst());
    }

    @Test
    public void testGetRetType() {
        final Variable variable = mock(Variable.class);
        when(variable.getDataType()).thenReturn(DataType.DOUBLE);

        final VariableSymbol doubleSymbol = new VariableSymbol(variable);
        assertEquals(Term.TYPE_D, doubleSymbol.getRetType());

        when(variable.getDataType()).thenReturn(DataType.FLOAT);
        final VariableSymbol floatSymbol = new VariableSymbol(variable);
        assertEquals(Term.TYPE_D, floatSymbol.getRetType());

        when(variable.getDataType()).thenReturn(DataType.INT);
        final VariableSymbol intSymbol = new VariableSymbol(variable);
        assertEquals(Term.TYPE_I, intSymbol.getRetType());

        when(variable.getDataType()).thenReturn(DataType.SHORT);
        final VariableSymbol shortSymbol = new VariableSymbol(variable);
        assertEquals(Term.TYPE_I, shortSymbol.getRetType());
    }

    @Test
    public void testGetRetType_invalidType() {
        final Variable variable = mock(Variable.class);
        when(variable.getDataType()).thenReturn(DataType.ENUM1);

        final VariableSymbol enumSymbol = new VariableSymbol(variable);

        try {
            enumSymbol.getRetType();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testEvalB() throws IOException, InvalidRangeException {
        final Reader reader = mock(Reader.class);
        final ReaderEvalEnv evalEnv = new ReaderEvalEnv(reader);
        evalEnv.setLocation(167, 22348);

        final Array array = mock(Array.class);
        when(array.getBoolean(0)).thenReturn(true);

        when(reader.readScaled(eq(167), eq(22348), anyObject(), eq("variable_name"))).thenReturn(array);

        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn("variable_name");
        final VariableSymbol symbol = new VariableSymbol(variable);

        assertTrue(symbol.evalB(evalEnv));

        verify(reader, times(1)).readScaled(eq(167), eq(22348), anyObject(), eq("variable_name"));
        verifyNoMoreInteractions(reader);
    }

    @Test
    public void testEvalI() throws IOException, InvalidRangeException {
        final Reader reader = mock(Reader.class);
        final ReaderEvalEnv evalEnv = new ReaderEvalEnv(reader);
        evalEnv.setLocation(168, 22349);

        final Array array = mock(Array.class);
        when(array.getInt(0)).thenReturn(3254);

        when(reader.readScaled(eq(168), eq(22349), anyObject(), eq("int_var"))).thenReturn(array);

        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn("int_var");
        final VariableSymbol symbol = new VariableSymbol(variable);

        assertEquals(3254, symbol.evalI(evalEnv));

        verify(reader, times(1)).readScaled(eq(168), eq(22349), anyObject(), eq("int_var"));
        verifyNoMoreInteractions(reader);
    }

    @Test
    public void testEvalD() throws IOException, InvalidRangeException {
        final Reader reader = mock(Reader.class);
        final ReaderEvalEnv evalEnv = new ReaderEvalEnv(reader);
        evalEnv.setLocation(169, 22350);

        final Array array = mock(Array.class);
        when(array.getDouble(0)).thenReturn(0.088745);

        when(reader.readScaled(eq(169), eq(22350), anyObject(), eq("double_var"))).thenReturn(array);

        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn("double_var");
        final VariableSymbol symbol = new VariableSymbol(variable);

        assertEquals(0.088745, symbol.evalD(evalEnv), 1e-8);

        verify(reader, times(1)).readScaled(eq(169), eq(22350), anyObject(), eq("double_var"));
        verifyNoMoreInteractions(reader);
    }
}
