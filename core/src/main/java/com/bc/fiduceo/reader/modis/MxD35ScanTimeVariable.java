package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.util.VariableProxy;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;

public class MxD35ScanTimeVariable extends VariableProxy {

    private final Variable _variable;
    private Array _timeData;

    public MxD35ScanTimeVariable(Variable variable, int width, int height) throws InvalidRangeException, IOException {
        super(variable.getShortName(), variable.getDataType(), new ArrayList<Attribute>() {{
            variable.attributes().forEach(attribute -> {
                final String name = attribute.getShortName();
                if (!name.contains("_Swath_Sampling")) {
                    add(attribute);
                }
            });
        }});
        setShape(new int[]{height, width});
        this._variable = variable;
        init();
    }

    @Override
    public Array read() throws IOException {
        final int[] shape = getShape();
        final int width = shape[1];
        final DataType targetDataType = _variable.getDataType();
        final Array targetArray = Array.factory(targetDataType, shape);
        final Object targetStorage = targetArray.getStorage();
        final int scanLength = width * 10;
        final double[] timeData = (double[]) _timeData.getStorage();
        final Array scanArray = Array.factory(targetDataType, new int[]{scanLength});
        for (int i = 0; i < timeData.length; i++) {
            double time = timeData[i];
            MAMath.setDouble(scanArray, time);
            System.arraycopy(scanArray.getStorage(), 0, targetStorage, i * scanLength, scanLength);
        }
        return targetArray;
    }

    @Override
    public Array read(int[] origin, int[] shape) throws IOException, InvalidRangeException {
        return read().section(origin, shape);
    }

    private void init() throws IOException, InvalidRangeException {
        final int[] tiePointShape = _variable.getShape();
        _timeData = _variable.read(new Section(new int[2], new int[]{tiePointShape[0] / 2, 1}, new int[]{2, 1}));
    }
}
