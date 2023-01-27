package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

abstract class AbstractSectionParser {

    static final int[] SCALAR = new int[1];
    static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    abstract List<Variable> getVariables();

    abstract int getNumVariables();

    abstract String getNamePrefix();

    abstract Section parse(String[] tokens, int offset) throws ParseException;

    static void createCommonVariables(List<Variable> variables) {
        createCommonVariables(variables, "");
    }

    static void createCommonVariables(List<Variable> variables, String prefix) {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_east"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "longitude"));
        variables.add(new VariableProxy(prefix + "longitude", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_north"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "latitude"));
        variables.add(new VariableProxy(prefix + "latitude", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "seconds since 1970-01-01"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(int.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "time"));
        variables.add(new VariableProxy(prefix + "time", DataType.INT, attributes));

        attributes = new ArrayList<>();
        variables.add(new VariableProxy(prefix + "reference-id", DataType.CHAR, attributes));
    }

    static Array parseFloat(String token) {
        final float floatVal;
        if (token.isEmpty() || token.equals("noval")) {
            floatVal = NetCDFUtils.getDefaultFillValue(float.class).floatValue();
        } else {
            floatVal = Float.parseFloat(token);
        }

        return Array.factory(DataType.FLOAT, SCALAR, new float[]{floatVal});
    }

    static Array parseShort(String token) {
        final short shortVal;

        if (token.isEmpty() || token.equals("noval")) {
            shortVal = NetCDFUtils.getDefaultFillValue(short.class).shortValue();
        } else {
            shortVal = Short.parseShort(token);
        }
        return Array.factory(DataType.SHORT, SCALAR, new short[]{shortVal});
    }

    static Array parseByte(String token) {
        final byte byteVal;
        if (token.isEmpty() || token.equals("noval")) {
            byteVal = NetCDFUtils.getDefaultFillValue(byte.class).byteValue();
        } else {
            byteVal = Byte.parseByte(token);
        }

        return Array.factory(DataType.BYTE, SCALAR, new byte[]{byteVal});
    }

    static Array parseUtcTime(String token) throws ParseException {
        final ProductData.UTC utcTime = ProductData.UTC.parse(token, DATE_PATTERN);
        final int utcSeconds = (int) (utcTime.getAsDate().getTime() / 1000);
        return Array.factory(DataType.INT, SCALAR, new int[]{utcSeconds});
    }

    static Array parseString(String token) {
        return Array.factory(DataType.CHAR, new int[]{token.length()}, token.toCharArray());
    }
}
