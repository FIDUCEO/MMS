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

public class DTUSIC1SectionParser extends ReferenceSectionParser {

    @Override
    List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        createReferenceCommonVariables(variables);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        variables.add(new VariableProxy("SIC", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        variables.add(new VariableProxy("areachange", DataType.FLOAT, attributes));

        return variables;
    }

    @Override
    Section parse(String[] tokens) throws ParseException {
        final Section section = new Section();

        final float lat = Float.parseFloat(tokens[0]);
        section.add("latitude", Array.factory(DataType.FLOAT, SCALAR, new float[]{lat}));

        final float lon = Float.parseFloat(tokens[1]);
        section.add("longitude", Array.factory(DataType.FLOAT, SCALAR, new float[]{lon}));

        final ProductData.UTC utcTime = ProductData.UTC.parse(tokens[2], DATE_PATTERN);
        final int utcSeconds = (int) (utcTime.getAsDate().getTime() / 1000);
        section.add("time", Array.factory(DataType.INT, SCALAR, new int[]{utcSeconds}));

        section.add("reference-id", Array.factory(DataType.CHAR, new int[] {tokens[3].length()}, tokens[3].toCharArray()));

        final float sic = Float.parseFloat(tokens[4]);
        section.add("SIC", Array.factory(DataType.FLOAT, SCALAR, new float[]{sic}));

        final float areaChange = Float.parseFloat(tokens[5]);
        section.add("areachange", Array.factory(DataType.FLOAT, SCALAR, new float[]{areaChange}));

        return section;
    }
}
