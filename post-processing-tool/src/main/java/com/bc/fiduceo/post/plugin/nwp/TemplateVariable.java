package com.bc.fiduceo.post.plugin.nwp;


import ucar.ma2.DataType;
import ucar.nc2.Attribute;

import java.util.ArrayList;
import java.util.List;

class TemplateVariable {

    private String name;
    private String originalName;
    private DataType dataType;
    private String dimensions;
    private List<Attribute> attributes;

    TemplateVariable(String name, String originalName, DataType dataType, String dimensions) {
        this.name = name;
        this.originalName = originalName;
        this.dataType = dataType;
        this.dimensions = dimensions;
        attributes = new ArrayList<>();
    }

    String getName() {
        return name;
    }

    DataType getDataType() {
        return dataType;
    }

    String getDimensions() {
        return dimensions;
    }

    List<Attribute> getAttributes() {
        return attributes;
    }

    String getOriginalName() {
        return originalName;
    }

    void addAttribute(String name, Number value) {
        attributes.add(new Attribute(name, value));
    }

    void addAttribute(String name, String value) {
        attributes.add(new Attribute(name, value));
    }
}
