package com.bc.fiduceo.post.plugin.era5;

class FieldsProcessor {

    TemplateVariable createTemplate(String name, String units, String longName, String standardName, boolean is3d) {
        return new TemplateVariable(name, units, longName, standardName, is3d);
    }
}
