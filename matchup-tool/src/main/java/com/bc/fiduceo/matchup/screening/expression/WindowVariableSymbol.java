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
 */

package com.bc.fiduceo.matchup.screening.expression;

import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.jexp.EvalEnv;
import org.esa.snap.core.jexp.EvalException;
import org.esa.snap.core.jexp.Symbol;
import org.esa.snap.core.jexp.Term;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

class WindowVariableSymbol implements Symbol {

    private final Variable variable;
    private final Number fillValue;
    private final NoDataListener listener;

    WindowVariableSymbol(Variable variable, NoDataListener listener) {
        this.variable = variable;
        this.listener = listener;
        fillValue = NetCDFUtils.getFillValue(variable);
    }

    @Override
    public String getName() {
        return variable.getShortName();
    }

    @Override
    public int getRetType() {
        final DataType dataType = variable.getDataType();
        if (dataType.isNumeric()) {
            return Term.TYPE_D;
        }
        throw new RuntimeException("Unsupported data type: " + dataType.toString());
    }

    @Override
    public boolean evalB(EvalEnv env) throws EvalException {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public int evalI(EvalEnv env) throws EvalException {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public double evalD(EvalEnv env) throws EvalException {
        final Array array = fetchPixel((WindowReaderEvalEnv) env);
        final Double v = array.getDouble(array.getIndex());
        if (Double.isNaN(v) || v.equals(fillValue.doubleValue())) {
            listener.fireNoData();
            return Double.NaN;
        }
        return v;
    }

    @Override
    public String evalS(EvalEnv env) throws EvalException {
        throw new RuntimeException("String expressions are not supported");
    }

    @Override
    public boolean isConst() {
        return false;
    }

    private Array fetchPixel(WindowReaderEvalEnv windowReaderEvalEnv) {
        try {
            return windowReaderEvalEnv.fetchPixel(getName());
        } catch (Exception e) {
            throw new EvalException("Unable to fetch pixel value.", e);
        }
    }

    interface NoDataListener {

        void fireNoData();
    }
}
