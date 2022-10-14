package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariablePrototype;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainer;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ThermalNoiseVariable extends VariablePrototype {

    private final Variable originalVariable;
    private final ModisL1EmissiveExtension nameExtension;
    private final int layerIndex;
    private final int productHeight;

    ThermalNoiseVariable(Variable variable, int channel, int productHeight) {
        this.originalVariable = variable;
        this.layerIndex = channel;
        this.productHeight = productHeight;

        nameExtension = new ModisL1EmissiveExtension();
    }

    @Override
    public String getShortName() {
        final String variableName = originalVariable.getShortName();
        return variableName + nameExtension.getExtension(layerIndex);
    }

    @Override
    public DataType getDataType() {
        return originalVariable.getDataType();
    }

    public Array read() throws IOException {
        final Array originalData = originalVariable.read();
        final int[] shape = originalData.getShape();
        shape[0] = 1;
        final int[] origin = new int[]{layerIndex, 0};

        final Array section;
        try {
            section = originalData.section(origin, shape);
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }

        final byte[] sensorData = (byte[]) section.copyTo1DJavaArray();
        final int numScans = productHeight / 10;
        final byte[] targetData = new byte[productHeight];
        for (int i = 0; i < numScans; i++) {
            System.arraycopy(sensorData, 0, targetData, 10 * i, 10);
        }

        return NetCDFUtils.create(targetData);
    }

    @Override
    // @todo 1 tb/tb write test 2020-06-05
    public List<Attribute> getAttributes() {
        final AttributeContainer attributeContainer = originalVariable.attributes();
        final ArrayList<Attribute> attributeList = new ArrayList<>();
        for (Attribute attribute : attributeContainer) {
            attributeList.add(attribute);
        }
        return attributeList;
    }
}
