package com.bc.fiduceo.post.plugin.nwp;

import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

class SensorExtractionStrategy extends Strategy {

    @Override
    void prepare(Context context) {
        final Configuration configuration = context.getConfiguration();
        final SensorExtractConfiguration sensorExtractConfiguration = configuration.getSensorExtractConfiguration();

        final NetcdfFileWriter writer = context.getWriter();
        final String x_dimensionName = sensorExtractConfiguration.getX_DimensionName();
        if (!writer.hasDimension(null, x_dimensionName)) {  
            writer.addDimension(null, x_dimensionName, sensorExtractConfiguration.getX_Dimension());
        }

        final String y_dimensionName = sensorExtractConfiguration.getY_DimensionName();
        if (!writer.hasDimension(null, y_dimensionName)) {
            writer.addDimension(null, y_dimensionName, sensorExtractConfiguration.getY_Dimension());
        }

        final String z_dimensionName = sensorExtractConfiguration.getZ_DimensionName();
        if (!writer.hasDimension(null, z_dimensionName)) {
            writer.addDimension(null, z_dimensionName, sensorExtractConfiguration.getZ_Dimension());
        }

        final TemplateVariables templateVariables = context.getTemplateVariables();
        final List<TemplateVariable> sensorExtractVariables = templateVariables.getSensorExtractVariables();
        for (final TemplateVariable templateVariable : sensorExtractVariables) {
            final Variable variable = writer.addVariable(null, templateVariable.getName(), templateVariable.getDataType(), templateVariable.getDimensions());
            final List<Attribute> attributes = templateVariable.getAttributes();
            variable.addAll(attributes);
        }
    }

    @Override
    void compute(Context context) throws IOException {
        final Configuration configuration = context.getConfiguration();
        final SensorExtractConfiguration sensorExtractConfiguration = configuration.getSensorExtractConfiguration();

        final List<String> nwpDataDirectories = extractNwpDataDirectories(sensorExtractConfiguration.getTimeVariableName(), context.getReader());
    }

    @Override
    File writeGeoFile(Context context) {
        throw new RuntimeException("not implemented");
    }
}
