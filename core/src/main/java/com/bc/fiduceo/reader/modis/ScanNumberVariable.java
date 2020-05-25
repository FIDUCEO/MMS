package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.util.VariablePrototype;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

class ScanNumberVariable extends VariablePrototype {

    private final Variable originalVariable;

    ScanNumberVariable(Variable variable) {
        originalVariable = variable;
    }

    @Override
    public String getShortName() {
        return originalVariable.getShortName();
    }

    @Override
    public DataType getDataType() {
        return originalVariable.getDataType();
    }
}
