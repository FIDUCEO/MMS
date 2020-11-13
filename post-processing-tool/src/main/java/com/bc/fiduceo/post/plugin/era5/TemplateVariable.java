package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.NetCDFUtils;

class TemplateVariable {

    private static final float FILL_VALUE = NetCDFUtils.getDefaultFillValue(float.class).floatValue();

    private final String name;
    private final String units;
    private final String longName;
    private final String standardName;

    TemplateVariable(String name, String units, String longName, String standardName) {
        this.name = name;
        this.units = units;
        this.longName = longName;
        this.standardName = standardName;
    }

    float getFillValue() {
        return FILL_VALUE;
    }

    String getName() {
        return name;
    }

    String getLongName() {
        return longName;
    }

    String getUnits() {
        return units;
    }

    String getStandardName() {
        return standardName;
    }
}
