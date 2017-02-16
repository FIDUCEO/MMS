package com.bc.fiduceo.post.plugin.nwp;

import ucar.ma2.DataType;

import java.util.ArrayList;
import java.util.List;

class TemplateVariables {

    private final List<TemplateVariable> analysisVariables;

    TemplateVariables(Configuration configuration) {
        analysisVariables = createAnalysisVariables(configuration);
    }

    List<TemplateVariable> getAnalysisVariables() {
        return analysisVariables;
    }

    private ArrayList<TemplateVariable> createAnalysisVariables(Configuration configuration) {
        final ArrayList<TemplateVariable> variables = new ArrayList<>();
        final String anDimensions = "matchup_count matchup.nwp.an.time";

        final TemplateVariable seaIceFractionVariable = new TemplateVariable(configuration.getAnSeaIceFractionName(), "CI", DataType.FLOAT, anDimensions);
        seaIceFractionVariable.addAttribute("long_name", "Sea-ice cover");
        seaIceFractionVariable.addAttribute("_FillValue", 2.0E20);
        seaIceFractionVariable.addAttribute("source", "GRIB data");
        variables.add(seaIceFractionVariable);

        final TemplateVariable sstVariable = new TemplateVariable(configuration.getAnSSTName(), "SSTK", DataType.FLOAT, anDimensions);
        sstVariable.addAttribute("long_name", "Sea surface temperature");
        sstVariable.addAttribute("units", "K");
        sstVariable.addAttribute("_FillValue", 2.0E20);
        sstVariable.addAttribute("source", "GRIB data");
        variables.add(sstVariable);

        final TemplateVariable eastWindVariable = new TemplateVariable(configuration.getAnEastWindName(), "U10", DataType.FLOAT, anDimensions);
        eastWindVariable.addAttribute("standard_name", "eastward_wind");
        eastWindVariable.addAttribute("long_name", "10 metre U wind component");
        eastWindVariable.addAttribute("units", "m s**-1");
        eastWindVariable.addAttribute("_FillValue", 2.0E20);
        eastWindVariable.addAttribute("source", "GRIB data");
        variables.add(eastWindVariable);

        final TemplateVariable northWindVariable = new TemplateVariable(configuration.getAnNorthWindName(), "V10", DataType.FLOAT, anDimensions);
        northWindVariable.addAttribute("standard_name", "northward_wind");
        northWindVariable.addAttribute("long_name", "10 metre V wind component");
        northWindVariable.addAttribute("units", "m s**-1");
        northWindVariable.addAttribute("_FillValue", 2.0E20);
        northWindVariable.addAttribute("source", "GRIB data");
        variables.add(northWindVariable);

        return variables;
    }
}
