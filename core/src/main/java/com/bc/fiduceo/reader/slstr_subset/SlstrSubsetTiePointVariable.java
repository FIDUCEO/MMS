package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.datamodel.TiePointGrid;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.MAMath;
import ucar.nc2.Variable;

import java.io.IOException;

public class SlstrSubsetTiePointVariable extends VariableProxy {

    private final Variable _variable;
    private TiePointGrid _tiePointGrid;

    public SlstrSubsetTiePointVariable(Variable variable, int width, int height, double offsetX, double subsamplingX) throws IOException {
        super(variable.getShortName(), variable.getDataType(), NetCDFUtils.getAttributes(variable));
        setShape(new int[]{height, width});
        this._variable = variable;
        init(offsetX, subsamplingX);
    }

    @Override
    public Array read() throws IOException {
        final int[] shape = getShape();
        final double[] doubles = _tiePointGrid.readPixels(0, 0, shape[1], shape[0], new double[shape[0] * shape[1]]);
        final Array array = Array.makeFromJavaArray(doubles).reshape(shape);
        if (getDataType() != array.getDataType()) {
            return MAMath.convert(array, getDataType());
        }
        return array;
    }

    private void init(double offsetX, double subsamplingX) throws IOException {
        final int[] varShape = _variable.getShape();

        Array tiePointData = _variable.read();

        if (tiePointData.getDataType() != DataType.FLOAT) {
            tiePointData = MAMath.convert(tiePointData, DataType.FLOAT);
        }

        final String units = _variable.findAttribute("units").getStringValue();
        _tiePointGrid = new TiePointGrid(
                _variable.getShortName(), varShape[1], varShape[0], offsetX + 0.5, 0.5, subsamplingX, 1.0,
                (float[]) tiePointData.getStorage(), units.contains("degree"));
    }
}
