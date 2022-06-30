package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.datamodel.TiePointGrid;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainer;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;

import static com.bc.fiduceo.util.NetCDFUtils.CF_ADD_OFFSET_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_SCALE_FACTOR_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_VALID_RANGE_NAME;

public class SlstrSubsetTiePointVariable extends VariableProxy {

    private final Variable _variable;
    private TiePointGrid _tiePointGrid;

    public SlstrSubsetTiePointVariable(Variable variable, int width, int height, double offsetX, double subsamplingX) throws IOException {
        super(variable.getShortName(), variable.getDataType(), getAttributes(variable));
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

    private static ArrayList<Attribute> getAttributes(Variable variable) {
        final AttributeContainer inAtts = variable.attributes();
        final double scaling = inAtts.findAttributeDouble(CF_SCALE_FACTOR_NAME, 1.0);
        final double offset = inAtts.findAttributeDouble(CF_ADD_OFFSET_NAME, 0.0);
        final Attribute fillAtt = inAtts.findAttribute(CF_FILL_VALUE_NAME);
        final Number newFill;
        if (fillAtt != null && (scaling != 1.0 || offset != 0.0)) {
            final Number value = fillAtt.getNumericValue();
            newFill = value.doubleValue() * scaling + offset;
        } else {
            newFill = null;
        }

        final ArrayList<Attribute> attributes = new ArrayList<>();
        inAtts.forEach(attribute -> {
            final String name = attribute.getShortName();
            if (CF_SCALE_FACTOR_NAME.equals(name)) {
                attributes.add(new Attribute(CF_SCALE_FACTOR_NAME, 1.0F));
            } else if (CF_ADD_OFFSET_NAME.equals(name)) {
                attributes.add(new Attribute(CF_ADD_OFFSET_NAME, 0.0F));
            } else if (newFill != null && CF_FILL_VALUE_NAME.equals(name)) {
                attributes.add(new Attribute(CF_FILL_VALUE_NAME, newFill.floatValue()));
            } else if (!(name.contains("_Swath_Sampling") || CF_VALID_RANGE_NAME.equals(name))) {
                attributes.add(attribute);
            }
        });
        return attributes;
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
