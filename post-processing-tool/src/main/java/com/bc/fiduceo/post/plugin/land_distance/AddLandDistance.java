package com.bc.fiduceo.post.plugin.land_distance;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.util.DistanceToLandMap;
import com.bc.fiduceo.util.JDomUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;


class AddLandDistance extends PostProcessing {

    private final Configuration configuration;

    private DistanceToLandMap distanceToLandMap;

    AddLandDistance(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable longitudeVariable = NetCDFUtils.getVariable(reader, configuration.lonVariableName);
        final List<Dimension> dimensions = longitudeVariable.getDimensions();

        final Variable variable = writer.addVariable(null, configuration.targetVariableName, DataType.FLOAT, dimensions);
        variable.addAttribute(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        variable.addAttribute(new Attribute("description", "Pixel distance to land. Land pixels have a value of 0.0"));
        variable.addAttribute(new Attribute(NetCDFUtils.CF_UNITS_NAME, "km"));
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable lonVariable = NetCDFUtils.getVariable(reader, configuration.lonVariableName);
        final Variable latVariable = NetCDFUtils.getVariable(reader, configuration.latVariableName);
        final Array lonArray = lonVariable.read();
        final Array latArray = latVariable.read();

        final DistanceToLandMap distanceMap = getDistanceToLandMap();
        final Array targetArray = Array.factory(DataType.FLOAT, lonArray.getShape());

        try {
            final float fillValue = NetCDFUtils.getDefaultFillValue(float.class).floatValue();
            final long size = lonArray.getSize();
            for (int i = 0; i < size; i++) {
                final double longitude = lonArray.getDouble(i);
                final double latitude = latArray.getDouble(i);

                if (longitude >= -180.0 && longitude <= 180.0 && latitude >= -90.0 && latitude <= 90.0) {

                    final double distance = distanceMap.getDistance(longitude, latitude);
                    targetArray.setFloat(i, (float) distance);
                } else {
                    targetArray.setFloat(i, fillValue);
                }
            }

            final Variable targetVariable = NetCDFUtils.getVariable(writer, configuration.targetVariableName);
            writer.write(targetVariable, targetArray);
        } finally {
            distanceMap.close();
        }
    }

    // for testing only - to inject a mock tb 2017-06-28
    void setDistanceToLandMap(DistanceToLandMap distanceToLandMap) {
        this.distanceToLandMap = distanceToLandMap;
    }

    private DistanceToLandMap getDistanceToLandMap() {
        if (distanceToLandMap == null) {
            distanceToLandMap = new DistanceToLandMap(Paths.get(configuration.auxDataFilePath));
        }
        return distanceToLandMap;
    }

    static Configuration createConfiguration(Element fullConfigElement) {
        if (!AddLandDistancePlugin.POST_PROCESSING_NAME.equals(fullConfigElement.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + AddLandDistancePlugin.POST_PROCESSING_NAME + "' expected.");
        }

        final Configuration configuration = new Configuration();

        final Element targetVariableElement = JDomUtils.getMandatoryChild(fullConfigElement, "target-variable");
        configuration.targetVariableName = JDomUtils.getValueFromAttributeMandatory(targetVariableElement, "name");

        final Element auxFileElement = JDomUtils.getMandatoryChild(fullConfigElement, "aux-file-path");
        configuration.auxDataFilePath = JDomUtils.getMandatoryText(auxFileElement);

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
