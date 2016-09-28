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

import com.bc.fiduceo.matchup.screening.expression.ReaderNamespace;
import com.bc.fiduceo.reader.Reader;
import org.esa.snap.core.jexp.Function;
import org.esa.snap.core.jexp.Symbol;
import org.esa.snap.core.jexp.Term;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReaderNamespaceTest {

    private Reader reader;

    @Before
    public void setUp() {
        reader = mock(Reader.class);
    }

    @Test
    public void testResolveFunction_onlyFromDefaultNamespace() throws InvalidRangeException, IOException {
        when(reader.getVariables()).thenReturn(new ArrayList<>());
        final ReaderNamespace namespace = new ReaderNamespace(reader);

        final Function function = namespace.resolveFunction("cos", new Term[]{new Term.ConstD(12.8)});
        assertNotNull(function);
        assertEquals("cos", function.getName());
    }

    @Test
    public void testResolveSymbol() throws InvalidRangeException, IOException {
        final ArrayList<Variable> variables = new ArrayList<>();
        final Variable first = mock(Variable.class);
        when(first.getShortName()).thenReturn("first_name");

        final Variable second = mock(Variable.class);
        when(second.getShortName()).thenReturn("second_name");

        variables.add(first);
        variables.add(second);

        when(reader.getVariables()).thenReturn(variables);

        final ReaderNamespace namespace = new ReaderNamespace(reader);
        Symbol symbol = namespace.resolveSymbol("first_name");
        assertNotNull(symbol);

        symbol = namespace.resolveSymbol("second_name");
        assertNotNull(symbol);

        symbol = namespace.resolveSymbol("nonsense_name");
        assertNull(symbol);
    }

    @Test
    public void testResolveSymbol_fromDefaultNamespace() throws InvalidRangeException, IOException {
        when(reader.getVariables()).thenReturn(new ArrayList<>());
        final ReaderNamespace namespace = new ReaderNamespace(reader);

        final Symbol pi = namespace.resolveSymbol("PI");
        assertNotNull(pi);
        assertEquals("PI", pi.getName());
    }
}
