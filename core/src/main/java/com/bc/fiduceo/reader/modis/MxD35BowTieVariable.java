package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.datamodel.TiePointGrid;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
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

public class MxD35BowTieVariable extends VariableProxy {

    private final Variable _variable;
    private TiePointGrid[] grids;

    public MxD35BowTieVariable(Variable variable, int width, int height) throws IOException {
        super(variable.getShortName(), DataType.FLOAT, getAttributes(variable));
        setShape(new int[]{height, width});
        this._variable = variable;
        init();
    }

    @Override
    public Array read() throws IOException {
        final int[] shape = getShape();
        final int width = shape[1];
        final DataType dataType = _variable.getDataType();
        final DataType targetDataType = dataType == DataType.DOUBLE ? dataType : DataType.FLOAT;
        final Array targetArray = Array.factory(targetDataType, shape);
        final Object storage = targetArray.getStorage();
        final int scanLength = width * 10;
        final double[] scanD = new double[scanLength];
        final float[] scanF = new float[scanLength];
        for (int i = 0; i < grids.length; i++) {
            TiePointGrid grid = grids[i];
            final Object scan;
            if (DataType.DOUBLE == targetDataType) {
                scan = grid.readPixels(0, 0, width, 10, scanD);
            } else {
                scan = grid.readPixels(0, 0, width, 10, scanF);
            }
            System.arraycopy(scan, 0, storage, i * scanLength, scanLength);
        }
        return targetArray;
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

    private void init() throws IOException {
        final int[] shape = getShape();
        final int[] tiePointShape = _variable.getShape();
        final int height = shape[0];
        final int tHeight = tiePointShape[0];
        final int tWidth = tiePointShape[1];
        final int[] sectionShape = {2, tWidth};
        final int[] sectionOrigin = {0, 0};

        Array tiePointData = _variable.read();
        final double scaling = _variable.findAttribute(CF_SCALE_FACTOR_NAME).getNumericValue().doubleValue();
        if (scaling != 1.0) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaling, 0.0);
            tiePointData = MAMath.convert2Unpacked(tiePointData, scaleOffset);
        }

        if (tiePointData.getDataType() != DataType.FLOAT) {
            tiePointData = MAMath.convert(tiePointData, DataType.FLOAT);
        }

        grids = new TiePointGrid[height / 10];
        final String units = _variable.findAttribute("units").getStringValue();
        for (int scanLine = 0; scanLine < tHeight / 2; scanLine++) {
            sectionOrigin[0] = scanLine * 2;
            final Array section;
            try {
                section = tiePointData.section(sectionOrigin, sectionShape);
            } catch (InvalidRangeException e) {
                throw new IOException(e);
            }
            final float[] floats = (float[]) section.copyTo1DJavaArray();
            grids[scanLine] = new TiePointGrid("scanLine_" + scanLine, tWidth, 2, 2.5, 2.5, 5, 5, floats, units.contains("degree"));
        }
    }
}
