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
import org.esa.snap.core.jexp.Function;
import org.esa.snap.core.jexp.Namespace;
import org.esa.snap.core.jexp.Symbol;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.impl.DefaultNamespace;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ReaderNamespace implements Namespace {

    private final HashMap<String, Symbol> symbols = new HashMap<>();
    private final DefaultNamespace defaultNamespace;

    public ReaderNamespace(Reader reader) throws InvalidRangeException, IOException {
        defaultNamespace = new DefaultNamespace();

        final List<Variable> variables = reader.getVariables();
        for(final Variable variable: variables) {
            final Symbol symbol = new VariableSymbol(variable);
            symbols.put(symbol.getName(), symbol);
        }
    }

    @Override
    public Symbol resolveSymbol(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol == null) {
            symbol = defaultNamespace.resolveSymbol(name);
        }

        return symbol;
    }

    @Override
    public Function resolveFunction(String name, Term[] args) {
        return defaultNamespace.resolveFunction(name, args);
    }
}
