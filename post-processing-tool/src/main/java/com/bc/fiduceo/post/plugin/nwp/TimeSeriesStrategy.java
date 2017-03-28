package com.bc.fiduceo.post.plugin.nwp;


import com.bc.fiduceo.post.Constants;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

class TimeSeriesStrategy extends Strategy {

    @Override
    void prepare(Context context) {
        final Configuration configuration = context.getConfiguration();
        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();

        final NetcdfFileWriter writer = context.getWriter();
        if (!writer.hasDimension(null, "matchup.nwp.an.time")) {
            writer.addDimension(null, "matchup.nwp.an.time", timeSeriesConfiguration.getAnalysisSteps());
        }
        if (!writer.hasDimension(null, "matchup.nwp.fc.time")) {
            writer.addDimension(null, "matchup.nwp.fc.time", timeSeriesConfiguration.getForecastSteps());
        }

        writer.addVariable(null, timeSeriesConfiguration.getAnCenterTimeName(), DataType.INT, Constants.MATCHUP_COUNT);
        writer.addVariable(null, timeSeriesConfiguration.getFcCenterTimeName(), DataType.INT, Constants.MATCHUP_COUNT);

        final TemplateVariables templateVariables = context.getTemplateVariables();
        final List<TemplateVariable> allVariables = templateVariables.getAllVariables();
        for (final TemplateVariable templateVariable : allVariables) {
            final Variable variable = writer.addVariable(null, templateVariable.getName(), templateVariable.getDataType(), templateVariable.getDimensions());
            final List<Attribute> attributes = templateVariable.getAttributes();
            variable.addAll(attributes);
        }
    }

    @Override
    File writeGeoFile(Context context) throws IOException, InvalidRangeException {
        final Configuration configuration = context.getConfiguration();
        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();

        final NetcdfFile reader = context.getReader();
        final Variable lonVariable = NetCDFUtils.getVariable(reader, timeSeriesConfiguration.getLongitudeVariableName());
        final Array longitudes = lonVariable.read();

        final Variable latVariable = NetCDFUtils.getVariable(reader, timeSeriesConfiguration.getLatitudeVariableName());
        final Array latitudes = latVariable.read();

        final int matchupCount = NetCDFUtils.getDimensionLength(Constants.MATCHUP_COUNT, reader);

        final GeoFile geoFile = new GeoFile(matchupCount);
        try {
            final TempFileManager tempFileManager = context.getTempFileManager();
            geoFile.create(tempFileManager);
            geoFile.write(longitudes, latitudes);
        } finally {
            geoFile.close();
        }
        return geoFile.getFile();
    }
}
