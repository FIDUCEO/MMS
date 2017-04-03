package com.bc.fiduceo.post.plugin.nwp;

import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
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
    void compute(Context context) throws IOException, InvalidRangeException {
        final Configuration configuration = context.getConfiguration();
        final SensorExtractConfiguration sensorExtractConfiguration = configuration.getSensorExtractConfiguration();

        final NetcdfFile reader = context.getReader();
        final List<String> nwpDataDirectories = extractNwpDataDirectories(sensorExtractConfiguration.getTimeVariableName(), reader);

        final File geoFile = writeGeoFile(context);

    }

    private File writeGeoFile(Context context) throws IOException, InvalidRangeException {
        final Configuration configuration = context.getConfiguration();
        final SensorExtractConfiguration sensorExtractConfiguration = configuration.getSensorExtractConfiguration();

        final NetcdfFile reader = context.getReader();
        final Variable lonVariable = NetCDFUtils.getVariable(reader, sensorExtractConfiguration.getLongitudeVariableName());
        final Array longitudes = lonVariable.read();

        final Variable latVariable = NetCDFUtils.getVariable(reader, sensorExtractConfiguration.getLatitudeVariableName());
        final Array latitudes = latVariable.read();

        final int[] shape = lonVariable.getShape();
        final int strideX = calculateStride(shape[2], sensorExtractConfiguration.getX_Dimension());
        final int strideY = calculateStride(shape[1], sensorExtractConfiguration.getY_Dimension());

        final int matchupCount = NetCDFUtils.getDimensionLength(com.bc.fiduceo.post.Constants.MATCHUP_COUNT, reader);

        final GeoFile geoFile = new GeoFile(matchupCount);

        try {
            geoFile.createSensorExtract(context.getTempFileManager(), sensorExtractConfiguration);
            geoFile.writeSensorExtract(longitudes, latitudes, strideX, strideY, sensorExtractConfiguration);
        } finally {
            geoFile.close();
        }
        return geoFile.getFile();
    }

    // package access for testing only tb 2015-12-08
    static int calculateStride(int n, int nwpN) {
        int stride;
        if (nwpN > 1) {
            stride = (n - 1) / (nwpN - 1);
        } else {
            stride = 1;
        }
        return stride;
    }
}
