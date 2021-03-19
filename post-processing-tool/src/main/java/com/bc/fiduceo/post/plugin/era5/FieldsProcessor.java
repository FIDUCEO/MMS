package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.NetCDFUtils;

class FieldsProcessor {

    TemplateVariable createTemplate(String name, String units, String longName, String standardName, boolean is3d) {
        return new TemplateVariable(NetCDFUtils.escapeVariableName(name),
                units, longName, standardName, is3d);
    }
}
