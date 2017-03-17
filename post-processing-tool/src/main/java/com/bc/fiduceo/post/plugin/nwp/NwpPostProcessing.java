/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.nwp;


import com.bc.fiduceo.core.TimeRange;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

class NwpPostProcessing extends PostProcessing {

    private static final int SEVENTY_TWO_HOURS_IN_SECONDS = 72 * 60 * 60;
    private static final int FOURTY_EIGHT_HOURS_IN_SECONDS = 48 * 60 * 60;

    // grib files have a strange naming pattern - we introduce constants that refers to the physical entity tb 2017-03-15
    static final String CLWC_NAME = "var246";

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

    private final Configuration configuration;
    private final TemplateVariables templateVariables;
    private final TempFileManager tempFileManager;

    NwpPostProcessing(Configuration configuration) {
        this.configuration = configuration;
        templateVariables = new TemplateVariables(configuration);
        tempFileManager = new TempFileManager();
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Dimension matchupCountDimension = reader.findDimension("matchup_count");
        if (matchupCountDimension == null) {
            throw new RuntimeException("Expected dimension not present in file: 'matchup_count'");
        }

        if (!writer.hasDimension(null, "matchup.nwp.an.time")) {
            writer.addDimension(null, "matchup.nwp.an.time", configuration.getAnalysisSteps());
        }
        if (!writer.hasDimension(null, "matchup.nwp.fc.time")) {
            writer.addDimension(null, "matchup.nwp.fc.time", configuration.getForecastSteps());
        }

        writer.addVariable(null, configuration.getAnCenterTimeName(), DataType.INT, "matchup_count");
        writer.addVariable(null, configuration.getFcCenterTimeName(), DataType.INT, "matchup_count");

        final List<TemplateVariable> allVariables = templateVariables.getAllVariables();
        for (final TemplateVariable templateVariable : allVariables) {
            final Variable variable = writer.addVariable(null, templateVariable.getName(), templateVariable.getDataType(), templateVariable.getDimensions());
            final List<Attribute> attributes = templateVariable.getAttributes();
            variable.addAll(attributes);
        }
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final List<String> nwpDataDirectories = extractNwpDataDirectories(reader);

        final File geoFile = writeGeoFile(reader);

        final File analysisFile;
        final File forecastFile;
        try {
            analysisFile = createAnalysisFile(geoFile, nwpDataDirectories);
            forecastFile = createForecastFile(geoFile, nwpDataDirectories);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        NetcdfFile analysisNetCDF = null;
        NetcdfFile forecastNetCDF = null;
        try {
            final FileMerger fileMerger = new FileMerger(configuration, templateVariables);
            analysisNetCDF = NetcdfFile.open(analysisFile.getAbsolutePath());
            forecastNetCDF = NetcdfFile.open(forecastFile.getAbsolutePath());
            final int[] analysisCenterTimes = fileMerger.mergeAnalysisFile(writer, analysisNetCDF);
            final int[] forecastCenterTimes = fileMerger.mergeForecastFile(writer, forecastNetCDF);

            Variable variable = NetCDFUtils.getVariable(writer, configuration.getAnCenterTimeName());
            writer.write(variable, Array.factory(analysisCenterTimes));

            variable = NetCDFUtils.getVariable(writer, configuration.getFcCenterTimeName());
            writer.write(variable, Array.factory(forecastCenterTimes));

        } finally {
            if (analysisNetCDF != null) {
                analysisNetCDF.close();
            }

            if (forecastNetCDF != null) {
                forecastNetCDF.close();
            }

            if (configuration.isDeleteOnExit()) {
                tempFileManager.cleanup();
            }
        }
    }

    private File createForecastFile(File geoFile, List<String> nwpDataDirectories) throws IOException, InterruptedException {
        final File gafsTimeSeriesFile = tempFileManager.create("gafs", "nc");
        final File ggfsTimeSeriesFile = tempFileManager.create("ggfs", "nc");
        final File ggfrTimeSeriesFile = tempFileManager.create("ggfr", "nc");
        final File ggfmTimeSeriesFile = tempFileManager.create("ggfm", "nc");
        final File ggfmRemapTimeSeriesFile = tempFileManager.create("ggfmr", "nc");
        final File forecastFile = tempFileManager.create("forecast", "nc");

        final String gafsTimeSteps = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/gafs", nwpDataDirectories, "gafs[0-9]*.nc", 0);
        final String ggfsTimeSteps = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/ggfs", nwpDataDirectories, "ggfs[0-9]*.nc", 0);
        final String ggfmTimeSteps = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/ggfm", nwpDataDirectories, "ggfm[0-9]*.grb", 0);

        final Properties templateProperties = createForecastFileTemplateProperties(configuration.getCDOHome(), geoFile.getAbsolutePath(), gafsTimeSteps,
                ggfsTimeSteps, ggfmTimeSteps, gafsTimeSeriesFile.getAbsolutePath(), ggfsTimeSeriesFile.getAbsolutePath(),
                ggfrTimeSeriesFile.getAbsolutePath(), ggfmTimeSeriesFile.getAbsolutePath(), ggfmRemapTimeSeriesFile.getAbsolutePath(),
                forecastFile.getAbsolutePath());
        final String resolvedExecutable = BashTemplateResolver.resolve(CDO_MATCHUP_FC_TEMPLATE, templateProperties);

        final File scriptFile = ProcessRunner.writeExecutableScript(resolvedExecutable, tempFileManager);

        final ProcessRunner processRunner = new ProcessRunner();
        processRunner.execute(scriptFile.getPath());

        return forecastFile;
    }

    private File createAnalysisFile(File geoFile, List<String> nwpDataDirectories) throws IOException, InterruptedException {
        final File ggasTimeSeriesFile = tempFileManager.create("ggas", "nc");
        final File ggamTimeSeriesFile = tempFileManager.create("ggam", "nc");
        final File ggamRemappedTimeSeriesFile = tempFileManager.create("ggar", "nc");
        final File analysisFile = tempFileManager.create("analysis", "nc");

        final String ggasTimeStepFiles = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/ggas", nwpDataDirectories, "ggas[0-9]*.nc", 0);
        final String ggamTimeStepFiles = NwpUtils.composeFilesString(configuration.getNWPAuxDir() + "/ggam", nwpDataDirectories, "ggam[0-9]*.grb", 0);

        final Properties templateProperties = createAnalysisFileTemplateProperties(configuration.getCDOHome(), geoFile.getAbsolutePath(), ggasTimeStepFiles,
                ggasTimeSeriesFile.getAbsolutePath(), ggamTimeStepFiles, ggamTimeSeriesFile.getAbsolutePath(), ggamRemappedTimeSeriesFile.getAbsolutePath(),
                analysisFile.getAbsolutePath());

        final String resolvedExecutable = BashTemplateResolver.resolve(CDO_MATCHUP_AN_TEMPLATE, templateProperties);
        final File scriptFile = ProcessRunner.writeExecutableScript(resolvedExecutable, tempFileManager);

        final ProcessRunner processRunner = new ProcessRunner();
        processRunner.execute(scriptFile.getPath());

        return analysisFile;
    }

    private List<String> extractNwpDataDirectories(NetcdfFile reader) throws IOException {
        final Variable timeVariable = NetCDFUtils.getVariable(reader, configuration.getTimeVariableName());
        final Array timeArray = timeVariable.read();

        final Number fillValue = NetCDFUtils.getFillValue(timeVariable);
        final TimeRange timeRange = extractTimeRange(timeArray, fillValue);
        return toDirectoryNamesList(timeRange);
    }

    private File writeGeoFile(NetcdfFile reader) throws IOException, InvalidRangeException {
        final Variable lonVariable = NetCDFUtils.getVariable(reader, configuration.getLongitudeVariableName());
        final Array longitudes = lonVariable.read();

        final Variable latVariable = NetCDFUtils.getVariable(reader, configuration.getLatitudeVariableName());
        final Array latitudes = latVariable.read();

        final int matchupCount = NetCDFUtils.getDimensionLength("matchup_count", reader);

        final GeoFile geoFile = new GeoFile(matchupCount);
        try {
            geoFile.create(tempFileManager);
            geoFile.write(longitudes, latitudes);
        } finally {
            geoFile.close();
        }
        return geoFile.getFile();
    }

    // package access for testing only tb 2017-01-06
    static TimeRange extractTimeRange(Array timesArray, Number fillValue) {
        int startTimeSeconds = Integer.MAX_VALUE;
        int endTimeSeconds = Integer.MIN_VALUE;
        final int fill = fillValue.intValue();

        for (int i = 0; i < timesArray.getSize(); i++) {
            final int currentTime = timesArray.getInt(i);
            if (currentTime == fill) {
                continue;
            }

            if (currentTime > endTimeSeconds) {
                endTimeSeconds = currentTime;
            }
            if (currentTime < startTimeSeconds) {
                startTimeSeconds = currentTime;
            }
        }

        final Date startDate = TimeUtils.create(startTimeSeconds * 1000L);
        final Date endDate = TimeUtils.create(endTimeSeconds * 1000L);
        return new TimeRange(startDate, endDate);
    }

    // package access for testing only tb 2017-01-06
    static List<String> toDirectoryNamesList(TimeRange timeRange) {
        final Date startDate = timeRange.getStartDate();
        final Date extractStartDate = TimeUtils.addSeconds(-SEVENTY_TWO_HOURS_IN_SECONDS, startDate);
        final Date beginningOfDay = TimeUtils.getBeginningOfDay(extractStartDate);

        final Date stopDate = timeRange.getStopDate();
        final Date extractStopDate = TimeUtils.addSeconds(FOURTY_EIGHT_HOURS_IN_SECONDS, stopDate);

        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(beginningOfDay);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        final List<String> directoryNameList = new ArrayList<>();
        while (!utcCalendar.getTime().after(extractStopDate)) {
            directoryNameList.add(simpleDateFormat.format(utcCalendar.getTime()));
            utcCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return directoryNameList;
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

    private static Properties createBaseTemplateProperties(String cdoHome, String geoFileLocation) {
        final Properties properties = new Properties();
        properties.setProperty("CDO", cdoHome + "/cdo");
        properties.setProperty("CDO_OPTS", "-M -R");
        properties.setProperty("REFTIME", "1970-01-01,00:00:00,seconds");
        properties.setProperty("GEO", geoFileLocation);
        return properties;
    }
}
