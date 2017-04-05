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
import java.util.Properties;

class SensorExtractionStrategy extends Strategy {

    private static final String CDO_NWP_TEMPLATE =
            "#! /bin/sh\n" +
                    "${CDO} ${CDO_OPTS} -f nc2 mergetime ${GGAS_TIMESTEPS} ${GGAS_TIME_SERIES} && " +
                    "${CDO} ${CDO_OPTS} -f grb mergetime ${GGAM_TIMESTEPS} ${GGAM_TIME_SERIES} && " +
                    "${CDO} ${CDO_OPTS} -f grb mergetime ${SPAM_TIMESTEPS} ${SPAM_TIME_SERIES} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 mergetime ${GAFS_TIMESTEPS} ${GAFS_TIME_SERIES} && " +
                    // attention: chaining the operations below results in a loss of the y dimension in the result file
                    "${CDO} ${CDO_OPTS} -f nc2 -R -t ecmwf setreftime,${REFTIME} -remapbil,${GEO} -selname,Q,O3,CLWC,CIWC ${GGAM_TIME_SERIES} ${GGAM_TIME_SERIES_REMAPPED} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 -R -t ecmwf setreftime,${REFTIME} -remapbil,${GEO} -sp2gp -selname,LNSP,T ${SPAM_TIME_SERIES} ${SPAM_TIME_SERIES_REMAPPED} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 -R -t ecmwf setreftime,${REFTIME} -remapbil,${GEO} -selname,TP -selhour,0,6,12,18 ${GAFS_TIME_SERIES} ${GAFS_TIME_SERIES_REMAPPED} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 merge -setreftime,${REFTIME} -remapbil,${GEO} -selname,CI,ASN,SSTK,TCWV,MSL,TCC,U10,V10,T2,D2,AL,SKT ${GGAS_TIME_SERIES} ${GGAM_TIME_SERIES_REMAPPED} ${SPAM_TIME_SERIES_REMAPPED} ${GAFS_TIME_SERIES_REMAPPED} ${NWP_TIME_SERIES}\n";

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

        final File analysisFile;
        try {
            analysisFile = createAnalysisFile(geoFile, nwpDataDirectories, context);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        final TemplateVariables templateVariables = context.getTemplateVariables();
        final NetcdfFileWriter writer = context.getWriter();

        final FileMerger fileMerger = new FileMerger(configuration, templateVariables);
        NetcdfFile analysisNetCDF = null;

        try {
            analysisNetCDF = NetcdfFile.open(analysisFile.getAbsolutePath());
            fileMerger.mergeSensorExtractAnalysisFile(writer, analysisNetCDF);
        }   finally {
            if (analysisNetCDF != null) {
                analysisNetCDF.close();
            }
        }

    }

    private File createAnalysisFile(File geoFile, List<String> nwpDataDirectories, Context context) throws IOException, InterruptedException {
        final TempFileManager tempFileManager = context.getTempFileManager();

        final File ggasTimeSeriesFile = tempFileManager.create("ggas", "nc");
        final File ggamTimeSeriesFile = tempFileManager.create("ggam", "nc");
        final File spamTimeSeriesFile = tempFileManager.create("spam", "nc");
        final File gafsTimeSeriesFile = tempFileManager.create("gafs", "nc");
        final File ggamRemappedFile = tempFileManager.create("ggar", "nc");
        final File spamRemappedFile = tempFileManager.create("spar", "nc");
        final File gafsRemappedFile = tempFileManager.create("gafr", "nc");
        final File analysisFile = tempFileManager.create("analysis", "nc");

        final Configuration configuration = context.getConfiguration();
        final String ggasTimeSteps = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/ggas", nwpDataDirectories, "ggas[0-9]*.nc", 1);
        final String ggamTimeSteps = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/ggam", nwpDataDirectories, "ggam[0-9]*.grb", 1);
        final String spamTimeSteps = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/spam", nwpDataDirectories, "spam[0-9]*.grb", 1);
        final String gafsTimeSteps = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/gafs", nwpDataDirectories, "gafs[0-9]*[62].nc", -1);

        final Properties templateProperties = createAnalysisFileTemplateProperties(configuration.getCDOHome(),
                geoFile.getAbsolutePath(),
                ggasTimeSteps, ggamTimeSteps, spamTimeSteps, gafsTimeSteps,
                ggasTimeSeriesFile.getAbsolutePath(),
                ggamTimeSeriesFile.getAbsolutePath(),
                spamTimeSeriesFile.getAbsolutePath(),
                gafsTimeSeriesFile.getAbsolutePath(),
                ggamRemappedFile.getAbsolutePath(),
                spamRemappedFile.getAbsolutePath(),
                gafsRemappedFile.getAbsolutePath(),
                analysisFile.getAbsolutePath());

        final String resolvedExecutable = BashTemplateResolver.resolve(CDO_NWP_TEMPLATE, templateProperties);
        final File scriptFile = ProcessRunner.writeExecutableScript(resolvedExecutable, tempFileManager);

        final ProcessRunner processRunner = new ProcessRunner();
        processRunner.execute(scriptFile.getPath());

        return analysisFile;
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

    // package access for testing only tb 2017-04-03
    static Properties createAnalysisFileTemplateProperties(String cdoHome, String geoFileLocation, String ggasSteps, String ggamSteps, String spamSteps,
                                                           String gafsSteps, String ggasFile, String ggamFile, String spamFile, String gafsFile,
                                                           String ggamRemapped, String spamRemapped, String gafsRemapped, String nwpFile) {
        final Properties properties = createBaseTemplateProperties(cdoHome, geoFileLocation);

        properties.setProperty("GGAS_TIMESTEPS", ggasSteps);
        properties.setProperty("GGAM_TIMESTEPS", ggamSteps);
        properties.setProperty("SPAM_TIMESTEPS", spamSteps);
        properties.setProperty("GAFS_TIMESTEPS", gafsSteps);
        properties.setProperty("GGAS_TIME_SERIES", ggasFile);
        properties.setProperty("GGAM_TIME_SERIES", ggamFile);
        properties.setProperty("SPAM_TIME_SERIES", spamFile);
        properties.setProperty("GAFS_TIME_SERIES", gafsFile);
        properties.setProperty("GGAM_TIME_SERIES_REMAPPED", ggamRemapped);
        properties.setProperty("SPAM_TIME_SERIES_REMAPPED", spamRemapped);
        properties.setProperty("GAFS_TIME_SERIES_REMAPPED", gafsRemapped);
        properties.setProperty("NWP_TIME_SERIES", nwpFile);

        return properties;
    }
}
