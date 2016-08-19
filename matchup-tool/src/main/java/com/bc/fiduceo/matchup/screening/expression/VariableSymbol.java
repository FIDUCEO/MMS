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

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderUtils;
import org.esa.snap.core.jexp.EvalEnv;
import org.esa.snap.core.jexp.EvalException;
import org.esa.snap.core.jexp.Symbol;
import org.esa.snap.core.jexp.Term;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;

public class VariableSymbol implements Symbol {

    private final Variable variable;
    private final Interval singlePixel = new Interval(1, 1);

    public VariableSymbol(Variable variable) {
        this.variable = variable;
    }

    @Override
    public String getName() {
        return variable.getShortName();
    }

    @Override
    public int getRetType() {
        final DataType dataType = variable.getDataType();
        if (dataType.isFloatingPoint()) {
            return Term.TYPE_D;
        } else if (dataType.isIntegral()) {
            return Term.TYPE_I;
        }
        throw new RuntimeException("Unsupported data type: " + dataType.toString());
    }

    @Override
    public boolean evalB(EvalEnv env) throws EvalException {
        final Array array = readArray((ReaderEvalEnv) env);
        return array.getBoolean(0);
    }

    @Override
    public int evalI(EvalEnv env) throws EvalException {
        final Array array = readArray((ReaderEvalEnv) env);
        return array.getInt(0);
    }

    @Override
    public double evalD(EvalEnv env) throws EvalException {
        final Array array = readArray((ReaderEvalEnv) env);
        return array.getDouble(0);
    }

    @Override
    public String evalS(EvalEnv env) throws EvalException {
        throw new RuntimeException("String expressions are not supported");
    }

    @Override
    public boolean isConst() {
        return false;
    }

    private Array readArray(ReaderEvalEnv env) {
        Array array;
        final Reader reader = env.getReader();

        try {
            final int x = env.getX();
            final int y = env.getY();
            array = reader.readScaled(x, y, singlePixel, variable.getFullName());
        } catch (IOException | InvalidRangeException e) {
            throw new EvalException("Unable to read data: " + e.getMessage());
        }
        return array;
    }
}
