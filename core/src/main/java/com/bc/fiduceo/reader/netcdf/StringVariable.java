package com.bc.fiduceo.reader.netcdf;

import com.bc.fiduceo.util.VariablePrototype;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainer;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

import java.util.ArrayList;
import java.util.List;

public class StringVariable extends VariablePrototype {

    private final Variable originalVariable;
    private final int stringLength;

    public StringVariable(Variable originalVariable, int stringLength) {
        this.originalVariable = originalVariable;
        this.stringLength = stringLength;
    }

    @Override
    public void setShortName(String shortName) {
        originalVariable.setShortName(shortName);
    }

    @Override
    public String getShortName() {
        return originalVariable.getShortName();
    }

    @Override
    public DataType getDataType() {
        return DataType.STRING;
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

    @Override
    public Dimension getDimension(int i) {
        return new Dimension(originalVariable.getShortName() + "_dim", stringLength);
    }
}
