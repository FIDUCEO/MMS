package com.bc.fiduceo.post.plugin.era5;

import org.esa.snap.core.util.StringUtils;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

class VariableUtils {

    // package access for testing purpose only tb 2020-12-02
    static void addAttributes(TemplateVariable template, Variable variable) {
        variable.addAttribute(new Attribute("units", template.getUnits()));
        variable.addAttribute(new Attribute("long_name", template.getLongName()));
        final String standardName = template.getStandardName();
        if (StringUtils.isNotNullAndNotEmpty(standardName)) {
            variable.addAttribute(new Attribute("standard_name", standardName));
        }
        variable.addAttribute(new Attribute("_FillValue", template.getFillValue()));
    }
}
