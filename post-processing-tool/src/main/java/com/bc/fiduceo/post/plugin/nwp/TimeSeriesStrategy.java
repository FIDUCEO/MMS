package com.bc.fiduceo.post.plugin.nwp;


import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TempFileUtils;
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
import java.util.Properties;

import static com.bc.fiduceo.post.plugin.nwp.Constants.CLWC_NAME;

class TimeSeriesStrategy extends Strategy {


    private static final String CDO_MATCHUP_AN_TEMPLATE =
            "#! /bin/sh\n" +
                    "${CDO} ${CDO_OPTS} -f nc2 mergetime ${GGAS_TIMESTEPS} ${GGAS_TIME_SERIES} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 mergetime ${GGAM_TIMESTEPS} ${GGAM_TIME_SERIES} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 setreftime,${REFTIME} -remapbil,${GEO} -selname," + CLWC_NAME + " ${GGAM_TIME_SERIES} ${GGAM_TIME_SERIES_REMAPPED} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 merge -setreftime,${REFTIME} -remapbil,${GEO} -selname,CI,SSTK,TCWV,U10,V10 ${GGAS_TIME_SERIES} ${GGAM_TIME_SERIES_REMAPPED} ${AN_TIME_SERIES}\n";

    private static final String CDO_MATCHUP_FC_TEMPLATE =
            "#! /bin/sh\n" +
                    "${CDO} ${CDO_OPTS} -f nc2 mergetime ${GAFS_TIMESTEPS} ${GAFS_TIME_SERIES} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 mergetime ${GGFS_TIMESTEPS} ${GGFS_TIME_SERIES} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 mergetime ${GGFM_TIMESTEPS} ${GGFM_TIME_SERIES} && " +
                    // attention: chaining the operations below results in a loss of the y dimension in the result file
                    "${CDO} ${CDO_OPTS} -f nc2 setreftime,${REFTIME} -remapbil,${GEO} -selname,SSTK,MSL,BLH,U10,V10,TCWV,T2,D2 ${GGFS_TIME_SERIES} ${GGFS_TIME_SERIES_REMAPPED} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 setreftime,${REFTIME} -remapbil,${GEO} -selname," + CLWC_NAME + " ${GGFM_TIME_SERIES} ${GGFM_TIME_SERIES_REMAPPED} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 merge -setreftime,${REFTIME} -remapbil,${GEO} -selname,SSHF,SLHF,SSRD,STRD,SSR,STR,EWSS,NSSS,E,TP ${GAFS_TIME_SERIES} ${GGFS_TIME_SERIES_REMAPPED} ${GGFM_TIME_SERIES_REMAPPED} ${FC_TIME_SERIES}\n";

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

        writer.addVariable(null, timeSeriesConfiguration.getAnCenterTimeName(), DataType.INT, FiduceoConstants.MATCHUP_COUNT);
        NetCDFUtils.ensureFillValue(
                writer.addVariable(null, timeSeriesConfiguration.getFcCenterTimeName(), DataType.INT, FiduceoConstants.MATCHUP_COUNT)
        );

        final TemplateVariables templateVariables = context.getTemplateVariables();
        final List<TemplateVariable> allVariables = templateVariables.getAllTimeSeriesVariables();
        for (final TemplateVariable templateVariable : allVariables) {
            final Variable variable = writer.addVariable(null, templateVariable.getName(), templateVariable.getDataType(), templateVariable.getDimensions());
            final List<Attribute> attributes = templateVariable.getAttributes();
            variable.addAll(attributes);
        }
    }

    @Override
    void compute(Context context) throws IOException, InvalidRangeException {
        final Configuration configuration = context.getConfiguration();
        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();

        final List<String> nwpDataDirectories = extractNwpDataDirectories(timeSeriesConfiguration.getTimeVariableName(), context.getReader());
        final File geoFile = writeGeoFile(context);

        final File analysisFile;
        final File forecastFile;
        try {
            analysisFile = createAnalysisFile(geoFile, nwpDataDirectories, context);
            forecastFile = createForecastFile(geoFile, nwpDataDirectories, context);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        NetcdfFile analysisNetCDF = null;
        NetcdfFile forecastNetCDF = null;
        final TemplateVariables templateVariables = context.getTemplateVariables();
        final NetcdfFileWriter writer = context.getWriter();

        final FileMerger fileMerger = new FileMerger(configuration, templateVariables);
        try {
            analysisNetCDF = NetcdfFile.open(analysisFile.getAbsolutePath());
            final Variable analysisVariable = NetCDFUtils.getVariable(writer, timeSeriesConfiguration.getAnCenterTimeName());
            final int[] analysisCenterTimes = fileMerger.mergeTimeSeriesAnalysisFile(writer, analysisNetCDF);
            writer.write(analysisVariable, NetCDFUtils.create(analysisCenterTimes));

            forecastNetCDF = NetcdfFile.open(forecastFile.getAbsolutePath());
            final Variable forecastVariable = NetCDFUtils.getVariable(writer, timeSeriesConfiguration.getFcCenterTimeName());
            final int forecastFillValue = NetCDFUtils.getFillValue(forecastVariable).intValue();
            final int[] forecastCenterTimes = fileMerger.mergeForecastFile(writer, forecastNetCDF, forecastFillValue);
            writer.write(forecastVariable, NetCDFUtils.create(forecastCenterTimes));
        } finally {
            if (analysisNetCDF != null) {
                analysisNetCDF.close();
            }

            if (forecastNetCDF != null) {
                forecastNetCDF.close();
            }
        }
    }

    private File writeGeoFile(Context context) throws IOException, InvalidRangeException {
        final Configuration configuration = context.getConfiguration();
        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();

        final NetcdfFile reader = context.getReader();
        final Variable lonVariable = NetCDFUtils.getVariable(reader, timeSeriesConfiguration.getLongitudeVariableName());
        final Array longitudes = lonVariable.read();

        final Variable latVariable = NetCDFUtils.getVariable(reader, timeSeriesConfiguration.getLatitudeVariableName());
        final Array latitudes = latVariable.read();

        final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, reader);

        final GeoFile geoFile = new GeoFile(matchupCount);
        try {
            final TempFileUtils tempFileUtils = context.getTempFileUtils();
            geoFile.createTimeSeries(tempFileUtils);
            geoFile.writeTimeSeries(longitudes, latitudes);
        } finally {
            geoFile.close();
        }
        return geoFile.getFile();
    }

    private File createAnalysisFile(File geoFile, List<String> nwpDataDirectories, Context context) throws IOException, InterruptedException {
        final TempFileUtils tempFileUtils = context.getTempFileUtils();

        final File ggasTimeSeriesFile = tempFileUtils.create("ggas", "nc");
        final File ggamTimeSeriesFile = tempFileUtils.create("ggam", "nc");
        final File ggamRemappedTimeSeriesFile = tempFileUtils.create("ggar", "nc");
        final File analysisFile = tempFileUtils.create("analysis", "nc");

        final Configuration configuration = context.getConfiguration();
        final String ggasTimeStepFiles = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/ggas", nwpDataDirectories, "ggas[0-9]*.nc", 0);
        final String ggamTimeStepFiles = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/ggam", nwpDataDirectories, "ggam[0-9]*.grb", 0);

        final Properties templateProperties = TimeSeriesStrategy.createAnalysisFileTemplateProperties(configuration.getCDOHome(), geoFile.getAbsolutePath(), ggasTimeStepFiles,
                ggasTimeSeriesFile.getAbsolutePath(), ggamTimeStepFiles, ggamTimeSeriesFile.getAbsolutePath(), ggamRemappedTimeSeriesFile.getAbsolutePath(),
                analysisFile.getAbsolutePath());

        final String resolvedExecutable = BashTemplateResolver.resolve(CDO_MATCHUP_AN_TEMPLATE, templateProperties);
        final File scriptFile = ProcessRunner.writeExecutableScript(resolvedExecutable, tempFileUtils);

        final ProcessRunner processRunner = new ProcessRunner();
        processRunner.execute(scriptFile.getPath());

        return analysisFile;
    }

    private File createForecastFile(File geoFile, List<String> nwpDataDirectories, Context context) throws IOException, InterruptedException {
        final TempFileUtils tempFileUtils = context.getTempFileUtils();

        final File gafsTimeSeriesFile = tempFileUtils.create("gafs", "nc");
        final File ggfsTimeSeriesFile = tempFileUtils.create("ggfs", "nc");
        final File ggfrTimeSeriesFile = tempFileUtils.create("ggfr", "nc");
        final File ggfmTimeSeriesFile = tempFileUtils.create("ggfm", "nc");
        final File ggfmRemapTimeSeriesFile = tempFileUtils.create("ggfmr", "nc");
        final File forecastFile = tempFileUtils.create("forecast", "nc");

        final Configuration configuration = context.getConfiguration();
        final String gafsTimeSteps = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/gafs", nwpDataDirectories, "gafs[0-9]*.nc", 0);
        final String ggfsTimeSteps = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/ggfs", nwpDataDirectories, "ggfs[0-9]*.nc", 0);
        final String ggfmTimeSteps = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/ggfm", nwpDataDirectories, "ggfm[0-9]*.grb", 0);

        final Properties templateProperties = TimeSeriesStrategy.createForecastFileTemplateProperties(configuration.getCDOHome(), geoFile.getAbsolutePath(), gafsTimeSteps,
                ggfsTimeSteps, ggfmTimeSteps, gafsTimeSeriesFile.getAbsolutePath(), ggfsTimeSeriesFile.getAbsolutePath(),
                ggfrTimeSeriesFile.getAbsolutePath(), ggfmTimeSeriesFile.getAbsolutePath(), ggfmRemapTimeSeriesFile.getAbsolutePath(),
                forecastFile.getAbsolutePath());
        final String resolvedExecutable = BashTemplateResolver.resolve(CDO_MATCHUP_FC_TEMPLATE, templateProperties);

        final File scriptFile = ProcessRunner.writeExecutableScript(resolvedExecutable, tempFileUtils);

        final ProcessRunner processRunner = new ProcessRunner();
        processRunner.execute(scriptFile.getPath());

        return forecastFile;
    }

    // package access for testing only tb 2017-01-11
    static Properties createAnalysisFileTemplateProperties(String cdoHome, String geoFileLocation, String ggasStepLocations, String ggasTimeSeriesLocation,
                                                           String ggamStepLocations, String ggamTimeSeriesLocation, String ggamRemappedFileLocation, String analysisTimeSeriesLocation) {
        final Properties properties = createBaseTemplateProperties(cdoHome, geoFileLocation);
        properties.setProperty("GGAS_TIMESTEPS", ggasStepLocations);
        properties.setProperty("GGAS_TIME_SERIES", ggasTimeSeriesLocation);
        properties.setProperty("GGAM_TIMESTEPS", ggamStepLocations);
        properties.setProperty("GGAM_TIME_SERIES", ggamTimeSeriesLocation);
        properties.setProperty("GGAM_TIME_SERIES_REMAPPED", ggamRemappedFileLocation);
        properties.setProperty("AN_TIME_SERIES", analysisTimeSeriesLocation);
        return properties;
    }

    // package access for testing only tb 2017-01-13
    static Properties createForecastFileTemplateProperties(String cdoHome, String geoFileLocation, String gafsStepLocations, String ggfsStepLocations,
                                                           String ggfmStepLocations, String gafsTimeSeriesLocation, String ggfsTimeSeriesLocation, String ggfsTimeSeriesRemapped,
                                                           String ggfmTimeSeriesLocation, String ggfmTimeSeriesRemapped, String forecastFileLocation) {
        final Properties properties = createBaseTemplateProperties(cdoHome, geoFileLocation);
        properties.setProperty("GAFS_TIMESTEPS", gafsStepLocations);
        properties.setProperty("GGFS_TIMESTEPS", ggfsStepLocations);
        properties.setProperty("GGFM_TIMESTEPS", ggfmStepLocations);
        properties.setProperty("GAFS_TIME_SERIES", gafsTimeSeriesLocation);
        properties.setProperty("GGFS_TIME_SERIES", ggfsTimeSeriesLocation);
        properties.setProperty("GGFS_TIME_SERIES_REMAPPED", ggfsTimeSeriesRemapped);
        properties.setProperty("GGFM_TIME_SERIES", ggfmTimeSeriesLocation);
        properties.setProperty("GGFM_TIME_SERIES_REMAPPED", ggfmTimeSeriesRemapped);
        properties.setProperty("FC_TIME_SERIES", forecastFileLocation);
        return properties;
    }

}
