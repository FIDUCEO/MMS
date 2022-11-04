package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

class ReferenceDataSection {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    static Date parseTime(String line) throws ParseException {
        // todo 2 add some tests for negative indices tb 2022-11-03
        int first = line.indexOf(",");
        int startIndex = line.indexOf(",", first + 1) + 1;
        int stopIndex = line.indexOf(",", startIndex);

        final String timeString = line.substring(startIndex, stopIndex);
        ProductData.UTC utcTime = ProductData.UTC.parse(timeString, DATE_PATTERN);
        return utcTime.getAsDate();
    }

    static List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_east"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "longitude"));
        variables.add(new VariableProxy("longitude", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_north"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "latitude"));
        variables.add(new VariableProxy("latitude", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "seconds since 1970-01-01"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(int.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "time"));
        variables.add(new VariableProxy("time", DataType.INT, attributes));

        attributes = new ArrayList<>();
        variables.add(new VariableProxy("reference-id", DataType.CHAR, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        variables.add(new VariableProxy("SIC", DataType.FLOAT, attributes));

        return variables;
    }
}
