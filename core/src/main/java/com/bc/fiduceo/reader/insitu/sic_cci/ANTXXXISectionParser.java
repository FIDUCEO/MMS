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

class ANTXXXISectionParser extends ReferenceSectionParser {

    @Override
    List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        createCommonVariables(variables);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        attributes.add(new Attribute(CF_UNITS_NAME, "percent"));
        variables.add(new VariableProxy("SIC-total", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        attributes.add(new Attribute(CF_UNITS_NAME, "percent"));
        variables.add(new VariableProxy("SIC-primary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice-type-primary ASPeCt code"));
        variables.add(new VariableProxy("Ice-type-primary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_thickness"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("SIT-primary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        variables.add(new VariableProxy("Ridged-ice-fraction-primary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Ridge-height-primary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Snow-cover-type-primary ASPeCt code"));
        variables.add(new VariableProxy("Snow-cover-type-primary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "thickness_of_snowfall_amount"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Snow-depth-primary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        attributes.add(new Attribute(CF_UNITS_NAME, "percent"));
        variables.add(new VariableProxy("SIC-secondary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice-type-secondary ASPeCt code"));
        variables.add(new VariableProxy("Ice-type-secondary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_thickness"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("SIT-secondary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        variables.add(new VariableProxy("Ridged-ice-fraction-secondary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Ridge-height-secondary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Snow-cover-type-secondary ASPeCt code"));
        variables.add(new VariableProxy("Snow-cover-type-secondary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "thickness_of_snowfall_amount"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Snow-depth-secondary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        attributes.add(new Attribute(CF_UNITS_NAME, "percent"));
        variables.add(new VariableProxy("SIC-tertiary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice-type-tertiary ASPeCt code"));
        variables.add(new VariableProxy("Ice-type-tertiary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_thickness"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("SIT-tertiary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        variables.add(new VariableProxy("Ridged-ice-fraction-tertiary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Ridge-height-tertiary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Snow-cover-type-tertiary ASPeCt code"));
        variables.add(new VariableProxy("Snow-cover-type-tertiary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "thickness_of_snowfall_amount"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Snow-depth-tertiary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_water_temperature"));
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_C"));
        variables.add(new VariableProxy("Sea-water-temperature", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "air_temperature"));
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_C"));
        variables.add(new VariableProxy("Air-temperature", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_speed"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m s-1"));
        variables.add(new VariableProxy("Wind-speed", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_UNITS_NAME, "degree"));
        variables.add(new VariableProxy("Wind-direction", DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        variables.add(new VariableProxy("Visibility", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        variables.add(new VariableProxy("Cloud-cover", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        variables.add(new VariableProxy("Weather", DataType.BYTE, attributes));

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

        section.add("reference-id", Array.factory(DataType.CHAR, new int[]{tokens[3].length()}, tokens[3].toCharArray()));

        final byte sic_total = Byte.parseByte(tokens[4]);
        section.add("SIC-total", Array.factory(DataType.BYTE, SCALAR, new byte[]{sic_total}));

        final byte sic_prim = Byte.parseByte(tokens[5]);
        section.add("SIC-primary", Array.factory(DataType.BYTE, SCALAR, new byte[]{sic_prim}));

        final byte ice_type_prim = Byte.parseByte(tokens[6]);
        section.add("Ice-type-primary", Array.factory(DataType.BYTE, SCALAR, new byte[]{ice_type_prim}));

        final float sit_prim = Float.parseFloat(tokens[7]);
        section.add("SIT-primary", Array.factory(DataType.FLOAT, SCALAR, new float[]{sit_prim}));

        final float rif_prim = Float.parseFloat(tokens[8]);
        section.add("Ridged-ice-fraction-primary", Array.factory(DataType.FLOAT, SCALAR, new float[]{rif_prim}));

        final float rih_prim = Float.parseFloat(tokens[9]);
        section.add("Ridged-height-primary", Array.factory(DataType.FLOAT, SCALAR, new float[]{rih_prim}));

        final byte sct_prim = Byte.parseByte(tokens[10]);
        section.add("Snow-cover-type-primary", Array.factory(DataType.BYTE, SCALAR, new byte[]{sct_prim}));

        final float sd_prim = Float.parseFloat(tokens[11]);
        section.add("Snow-depth-primary", Array.factory(DataType.FLOAT, SCALAR, new float[]{sd_prim}));

        final byte sic_sec = Byte.parseByte(tokens[12]);
        section.add("SIC-secondary", Array.factory(DataType.BYTE, SCALAR, new byte[]{sic_sec}));

        final byte ice_type_sec = Byte.parseByte(tokens[13]);
        section.add("Ice-type-secondary", Array.factory(DataType.BYTE, SCALAR, new byte[]{ice_type_sec}));

        final float sit_sec = Float.parseFloat(tokens[14]);
        section.add("SIT-secondary", Array.factory(DataType.FLOAT, SCALAR, new float[]{sit_sec}));

        final float rif_sec = Float.parseFloat(tokens[15]);
        section.add("Ridged-ice-fraction-secondary", Array.factory(DataType.FLOAT, SCALAR, new float[]{rif_sec}));

        final float rih_sec = Float.parseFloat(tokens[16]);
        section.add("Ridged-height-secondary", Array.factory(DataType.FLOAT, SCALAR, new float[]{rih_sec}));

        final byte sct_sec = Byte.parseByte(tokens[17]);
        section.add("Snow-cover-type-secondary", Array.factory(DataType.BYTE, SCALAR, new byte[]{sct_sec}));

        final float sd_sec = Float.parseFloat(tokens[18]);
        section.add("Snow-depth-secondary", Array.factory(DataType.FLOAT, SCALAR, new float[]{sd_sec}));

        final byte sic_ter = Byte.parseByte(tokens[19]);
        section.add("SIC-tertiary", Array.factory(DataType.BYTE, SCALAR, new byte[]{sic_ter}));

        final byte ice_type_ter = Byte.parseByte(tokens[20]);
        section.add("Ice-type-tertiary", Array.factory(DataType.BYTE, SCALAR, new byte[]{ice_type_ter}));

        final float sit_ter = Float.parseFloat(tokens[21]);
        section.add("SIT-tertiary", Array.factory(DataType.FLOAT, SCALAR, new float[]{sit_ter}));

        final float rif_ter = Float.parseFloat(tokens[22]);
        section.add("Ridged-ice-fraction-tertiary", Array.factory(DataType.FLOAT, SCALAR, new float[]{rif_ter}));

        final float rih_ter = Float.parseFloat(tokens[23]);
        section.add("Ridged-height-tertiary", Array.factory(DataType.FLOAT, SCALAR, new float[]{rih_ter}));

        final byte sct_ter = Byte.parseByte(tokens[24]);
        section.add("Snow-cover-type-tertiary", Array.factory(DataType.BYTE, SCALAR, new byte[]{sct_ter}));

        final float sd_ter = Float.parseFloat(tokens[25]);
        section.add("Snow-depth-tertiary", Array.factory(DataType.FLOAT, SCALAR, new float[]{sd_ter}));

        final float swt = Float.parseFloat(tokens[26]);
        section.add("Sea-water-temperature", Array.factory(DataType.FLOAT, SCALAR, new float[]{swt}));

        final float at = Float.parseFloat(tokens[27]);
        section.add("Air-temperature", Array.factory(DataType.FLOAT, SCALAR, new float[]{at}));

        final float ws = Float.parseFloat(tokens[28]);
        section.add("Wind-speed", Array.factory(DataType.FLOAT, SCALAR, new float[]{ws}));

        final short wd = Short.parseShort(tokens[29]);
        section.add("Wind-direction", Array.factory(DataType.SHORT, SCALAR, new short[]{wd}));

        final byte vis = Byte.parseByte(tokens[30]);
        section.add("Visibility", Array.factory(DataType.BYTE, SCALAR, new byte[]{vis}));

        final byte cc = Byte.parseByte(tokens[31]);
        section.add("Cloud-cover", Array.factory(DataType.BYTE, SCALAR, new byte[]{cc}));

        final byte weather = Byte.parseByte(tokens[32]);
        section.add("Weather", Array.factory(DataType.BYTE, SCALAR, new byte[]{weather}));

        return section;
    }
}
