package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.NetCDFUtils;

class TemplateVariable {

    private static final float FILL_VALUE = NetCDFUtils.getDefaultFillValue(float.class).floatValue();

    private final String name;
    private final String units;
    private final String longName;
    private final String standardName;
    private final boolean is3d;

    TemplateVariable(String name, String units, String longName, String standardName, boolean is3d) {
        this.name = name;
        this.units = units;
        this.longName = longName;
        this.standardName = standardName;
        this.is3d = is3d;
    }

    static float getFillValue() {
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

    boolean is3d() {
        return is3d;
    }
}
