package com.bc.fiduceo.reader.amsr.amsr2;

import com.bc.fiduceo.util.VariablePrototype;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

class PixelDataQualityVariable extends VariablePrototype {

    private final Variable originalVariable;

    PixelDataQualityVariable(Variable originalVariable) {
        this.originalVariable = originalVariable;
    }

    @Override
    public String getShortName() {
        return originalVariable.getShortName();
    }

    @Override
    public DataType getDataType() {
        return DataType.SHORT;
    }
}
