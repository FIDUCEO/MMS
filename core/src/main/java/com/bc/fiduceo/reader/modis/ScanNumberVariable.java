package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariablePrototype;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainer;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public Array read() throws IOException {
        final Array originalData = originalVariable.read();
        final int[] shape = originalData.getShape();
        final short[] expandedData = new short[shape[0] * 10];
        for (int i = 0; i < shape[0]; i++) {
            final short scanNumber = originalData.getShort(i);
            final int ten_i = 10 * i;
            for (int k = 0; k < 10; k++) {
                expandedData[ten_i + k] = scanNumber;
            }
        }

        return NetCDFUtils.create(expandedData);
    }

    @Override
    public List<Attribute> getAttributes() {
        final AttributeContainer attributeContainer = originalVariable.attributes();
        final ArrayList<Attribute> attributeList = new ArrayList<>();
        for (Attribute attribute : attributeContainer) {
            attributeList.add(attribute);
        }
        return attributeList;
    }
}
