package com.bc.fiduceo.post.plugin.land_distance;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.util.JDomUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import org.jdom.Element;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;


class AddLandDistance extends PostProcessing {

    private final Configuration configuration;

    AddLandDistance(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable longitudeVariable = NetCDFUtils.getVariable(reader, configuration.lonVariableName);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {

    }

    static Configuration createConfiguration(Element fullConfigElement) {
        if (!AddLandDistancePlugin.POST_PROCESSING_NAME.equals(fullConfigElement.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + AddLandDistancePlugin.POST_PROCESSING_NAME + "' expected.");
        }

        final Configuration configuration = new Configuration();

        final Element targetVariableElement = JDomUtils.getMandatoryChild(fullConfigElement, "target-variable");
        configuration.targetVariableName = JDomUtils.getValueFromAttributeMandatory(targetVariableElement, "name");

        final Element auxFileElement = JDomUtils.getMandatoryChild(fullConfigElement, "aux-file-path");
        configuration.auxDataFilePath =  JDomUtils.getMandatoryText(auxFileElement);

        final Element lonVariableElement = JDomUtils.getMandatoryChild(fullConfigElement, "lon-variable");
        configuration.lonVariableName = JDomUtils.getValueFromAttributeMandatory(lonVariableElement, "name");

        final Element latVariableElement = JDomUtils.getMandatoryChild(fullConfigElement, "lat-variable");
        configuration.latVariableName = JDomUtils.getValueFromAttributeMandatory(latVariableElement, "name");

        return configuration;
    }

    static class Configuration {
        String targetVariableName;
        String auxDataFilePath;
        String lonVariableName;
        String latVariableName;
    }
}
