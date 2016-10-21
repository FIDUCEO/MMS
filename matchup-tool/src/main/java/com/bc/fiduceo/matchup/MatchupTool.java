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

package com.bc.fiduceo.matchup;

import com.bc.fiduceo.core.*;
import com.bc.fiduceo.db.DatabaseConfig;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.matchup.condition.ConditionEngine;
import com.bc.fiduceo.matchup.condition.ConditionEngineContext;
import com.bc.fiduceo.matchup.screening.ScreeningEngine;
import com.bc.fiduceo.matchup.writer.AcquisitionTimeReadingIOVariable;
import com.bc.fiduceo.matchup.writer.CenterXWritingIOVariable;
import com.bc.fiduceo.matchup.writer.CenterYWritingIOVariable;
import com.bc.fiduceo.matchup.writer.IOVariable;
import com.bc.fiduceo.matchup.writer.IOVariablesList;
import com.bc.fiduceo.matchup.writer.MmdWriter;
import com.bc.fiduceo.matchup.writer.MmdWriterConfig;
import com.bc.fiduceo.matchup.writer.MmdWriterFactory;
import com.bc.fiduceo.matchup.writer.ReaderContainer;
import com.bc.fiduceo.matchup.writer.SourcePathWritingIOVariable;
import com.bc.fiduceo.matchup.writer.VariablesConfiguration;
import com.bc.fiduceo.math.Intersection;
import com.bc.fiduceo.math.IntersectionEngine;
import com.bc.fiduceo.math.TimeInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;

import java.io.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.bc.fiduceo.FiduceoConstants.VERSION_NUMBER;

class MatchupTool {

    private static final String UNIT_ATTRIBUTE_NAME = "unit";
    private static final String DESCRIPTION_ATTRIBUTE_NAME = "description";

    private final Logger logger;

    private ReaderFactory readerFactory;

    MatchupTool() {
        logger = FiduceoLogger.getLogger();
    }

    static PixelLocator getPixelLocator(Reader reader, boolean isSegmented, Polygon polygon) throws IOException {
        final PixelLocator pixelLocator;
        if (isSegmented) {
            pixelLocator = reader.getSubScenePixelLocator(polygon);
        } else {
            pixelLocator = reader.getPixelLocator();
        }
        return pixelLocator;
    }

    static void createIOVariablesPerSensor(IOVariablesList ioVariablesList, MatchupCollection matchupCollection,
                                           final UseCaseConfig useCaseConfig, VariablesConfiguration variablesConfiguration)
                throws IOException {

        final String primSensorName = useCaseConfig.getPrimarySensor().getName();
        final String secoSensorName = useCaseConfig.getAdditionalSensors().get(0).getName();
        final Dimension primDim = useCaseConfig.getDimensionFor(primSensorName);
        final Dimension secoDim = useCaseConfig.getDimensionFor(secoSensorName);

        final MatchupSet matchupSet = getFirstMatchupSet(matchupCollection);
        final Path primaryPath = matchupSet.getPrimaryObservationPath();
        final Path secondaryPath = matchupSet.getSecondaryObservationPath();

        ioVariablesList.extractVariables(primSensorName, primaryPath, primDim, variablesConfiguration);
        createExtraVariables(primSensorName, ioVariablesList, variablesConfiguration);

        ioVariablesList.extractVariables(secoSensorName, secondaryPath, secoDim, variablesConfiguration);
        createExtraVariables(secoSensorName, ioVariablesList, variablesConfiguration);
    }

    static void createExtraVariables(String sensorName, IOVariablesList ioVariablesList, VariablesConfiguration variablesConfiguration) {
        final Map<String, String> sensorRenames = variablesConfiguration.getSensorRenames();
        final Map<String, String> renames = variablesConfiguration.getRenames(sensorName);
        final List<String> excludes = variablesConfiguration.getExcludes(sensorName);
        final String separator = variablesConfiguration.getSeparator(sensorName);

        final ReaderContainer readerContainer = ioVariablesList.getReaderContainer(sensorName);

        final String targetSensorName;
        if (sensorRenames.containsKey(sensorName)) {
            targetSensorName = sensorRenames.get(sensorName);
        } else {
            targetSensorName = sensorName;
        }

        String varName;

        varName = "x";
        if (!excludes.contains(varName)) {
            varName = renames.containsKey(varName) ? renames.get(varName) : varName;
            final CenterXWritingIOVariable ioVariable = new CenterXWritingIOVariable();
            ioVariable.setTargetVariableName(targetSensorName + separator + varName);
            ioVariable.setDataType(DataType.INT.toString());
            ioVariable.setDimensionNames("matchup_count");
            final List<Attribute> attributes = ioVariable.getAttributes();
            attributes.add(new Attribute(DESCRIPTION_ATTRIBUTE_NAME, "pixel original x location in satellite raster"));
            ioVariablesList.add(ioVariable, sensorName);
        }

        varName = "y";
        if (!excludes.contains(varName)) {
            varName = renames.containsKey(varName) ? renames.get(varName) : varName;
            final CenterYWritingIOVariable ioVariable = new CenterYWritingIOVariable();
            ioVariable.setTargetVariableName(targetSensorName + separator + varName);
            ioVariable.setDataType(DataType.INT.toString());
            ioVariable.setDimensionNames("matchup_count");
            final List<Attribute> attributes = ioVariable.getAttributes();
            attributes.add(new Attribute(DESCRIPTION_ATTRIBUTE_NAME, "pixel original y location in satellite raster"));
            ioVariablesList.add(ioVariable, sensorName);
        }

        varName = "file_name";
        if (!excludes.contains(varName)) {
            varName = renames.containsKey(varName) ? renames.get(varName) : varName;
            final SourcePathWritingIOVariable ioVariable = new SourcePathWritingIOVariable(readerContainer);
            ioVariable.setTargetVariableName(targetSensorName + separator + varName);
            ioVariable.setDataType(DataType.CHAR.toString());
            ioVariable.setDimensionNames("matchup_count file_name");
            final List<Attribute> attributes = ioVariable.getAttributes();
            attributes.add(new Attribute(DESCRIPTION_ATTRIBUTE_NAME, "file name of the original data file"));
            ioVariablesList.add(ioVariable, sensorName);
        }

        varName = "acquisition_time";
        if (!excludes.contains(varName)) {
            varName = renames.containsKey(varName) ? renames.get(varName) : varName;
            final AcquisitionTimeReadingIOVariable ioVariable = new AcquisitionTimeReadingIOVariable(readerContainer);
            ioVariable.setTargetVariableName(targetSensorName + separator + varName);
            ioVariable.setDataType(DataType.INT.toString());
            ioVariable.setDimensionNames("matchup_count " + sensorName + "_ny " + sensorName + "_nx");
            final List<Attribute> attributes = ioVariable.getAttributes();
            attributes.add(new Attribute(DESCRIPTION_ATTRIBUTE_NAME, "acquisition time of original pixel"));
            attributes.add(new Attribute(UNIT_ATTRIBUTE_NAME, "seconds since 1970-01-01"));
            attributes.add(new Attribute("_FillValue", -2147483648));
            ioVariablesList.add(ioVariable, sensorName);
        }
    }

    static boolean isSegmented(Geometry primaryGeoBounds) {
        return primaryGeoBounds instanceof GeometryCollection && ((GeometryCollection) primaryGeoBounds).getGeometries().length > 1;
    }

    // package access for testing only tb 2016-03-14
    static QueryParameter getSecondarySensorParameter(UseCaseConfig useCaseConfig, Date searchTimeStart, Date searchTimeEnd) {
        final QueryParameter parameter = new QueryParameter();
        final Sensor secondarySensor = getSecondarySensor(useCaseConfig);
        assignSensor(parameter, secondarySensor);
        parameter.setStartTime(searchTimeStart);
        parameter.setStopTime(searchTimeEnd);
        return parameter;
    }

    // package access for testing only tb 2016-03-14
    static Sensor getSecondarySensor(UseCaseConfig useCaseConfig) {
        final List<Sensor> additionalSensors = useCaseConfig.getAdditionalSensors();
        if (additionalSensors.size() != 1) {
            throw new RuntimeException("Unable to run matchup with given sensor number");
        }

        return additionalSensors.get(0);
    }

    // package access for testing only tb 2016-02-23
    static QueryParameter getPrimarySensorParameter(ToolContext context) {
        final QueryParameter parameter = new QueryParameter();
        final Sensor primarySensor = context.getUseCaseConfig().getPrimarySensor();
        if (primarySensor == null) {
            throw new RuntimeException("primary sensor not present in configuration file");
        }

        assignSensor(parameter, primarySensor);
        parameter.setStartTime(context.getStartDate());
        parameter.setStopTime(context.getEndDate());
        return parameter;
    }

    // package access for testing only tb 2016-02-18
    static Options getOptions() {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Prints the tool usage.");
        options.addOption(helpOption);

        final Option configOption = new Option("c", "config", true, "Defines the configuration directory. Defaults to './config'.");
        options.addOption(configOption);

        final Option startOption = new Option("start", "start-time", true, "Defines the processing start-date, format 'yyyy-DDD'");
        options.addOption(startOption);

        final Option endOption = new Option("end", "end-time", true, "Defines the processing end-date, format 'yyyy-DDD'");
        options.addOption(endOption);

        final Option useCaseOption = new Option("u", "usecase", true, "Defines the path to the use-case configuration file. Path is relative to the configuration directory.");
        options.addOption(useCaseOption);

        return options;
    }

    // package access for testing only tb 2016-02-23
    static Date getEndDate(CommandLine commandLine) {
        final String endDateString = commandLine.getOptionValue("end");
        if (StringUtils.isNullOrEmpty(endDateString)) {
            throw new RuntimeException("cmd-line parameter `end` missing");
        }
        return TimeUtils.parseDOYEndOfDay(endDateString);
    }

    // package access for testing only tb 2016-02-23
    static Date getStartDate(CommandLine commandLine) {
        final String startDateString = commandLine.getOptionValue("start");
        if (StringUtils.isNullOrEmpty(startDateString)) {
            throw new RuntimeException("cmd-line parameter `start` missing");
        }
        return TimeUtils.parseDOYBeginOfDay(startDateString);
    }

    // package access for testing only tb 2016-09-30
    static MmdWriterConfig loadWriterConfig(CommandLine commandLine) throws IOException {
        final String configValue = commandLine.getOptionValue("config", "./config");
        final File configDirectory = new File(configValue);

        final File useCaseConfigFile = new File(configDirectory, "mmd-writer-config.xml");
        if (!useCaseConfigFile.isFile()) {
            throw new RuntimeException("Use case config file does not exist: '" + "mmd-writer-config.xml" + "'");
        }

        final MmdWriterConfig mmdWriterConfig;
        try (FileInputStream inputStream = new FileInputStream(useCaseConfigFile)) {
            mmdWriterConfig = MmdWriterConfig.load(inputStream);
        }

        return mmdWriterConfig;
    }

    // package access for testing only tb 2016-08-12
    static StringBuilder createErrorMessage(ValidationResult validationResult) {
        final StringBuilder builder = new StringBuilder();
        final List<String> messages = validationResult.getMessages();
        for (final String message : messages) {
            builder.append(message);
            builder.append("\n");
        }
        return builder;
    }

    static MatchupSet getFirstMatchupSet(MatchupCollection matchupCollection) {
        final List<MatchupSet> sets = matchupCollection.getSets();
        if (sets.size() > 0) {
            return sets.get(0);
        }
        throw new IllegalStateException("Called getFirst() on empty matchupCollection.");
    }

    // package access for testing only tb 2016-10-05
    static void applyExcludesAndRenames(IOVariablesList ioVariablesList, VariablesConfiguration variablesConfiguration) {
        final List<String> sensorNames = ioVariablesList.getSensorNames();

        for (final String sensorName : sensorNames) {
            final List<IOVariable> ioVariables = ioVariablesList.getVariablesFor(sensorName);
            final Map<String, String> renames = variablesConfiguration.getRenames(sensorName);
            for (Map.Entry<String, String> rename : renames.entrySet()) {
                final String sourceName = rename.getKey();
                final IOVariable variable = getVariable(sourceName, ioVariables);
                if (variable != null) {
                    variable.setTargetVariableName(rename.getValue());
                }
            }

            final List<String> excludes = variablesConfiguration.getExcludes(sensorName);
            for (final String sourceName : excludes) {
                final IOVariable variable = getVariable(sourceName, ioVariables);
                if (variable != null) {
                    ioVariables.remove(variable);
                }
            }
        }
    }

    static IOVariable getVariable(String sourceName, List<IOVariable> ioVariables) {
        for (final IOVariable variable : ioVariables) {
            if (sourceName.equals(variable.getSourceVariableName())) {
                return variable;
            }
        }
        return null;
    }

    // package access for testing only tb 2016-08-16
    static void calculateDistance(MatchupSet matchupSet) {
        final List<SampleSet> sourceSamples = matchupSet.getSampleSets();
        for (final SampleSet sampleSet : sourceSamples) {
            final double km = SphericalDistanceCalculator.calculateKm(sampleSet);
            sampleSet.setSphericalDistance((float) km);
        }
    }

    void run(CommandLine commandLine) throws IOException, SQLException, InvalidRangeException {
        final ToolContext context = initialize(commandLine);
        final MmdWriterConfig mmdWriterConfig = loadWriterConfig(commandLine);

        readerFactory = ReaderFactory.get(context.getGeometryFactory());

        runMatchupGeneration(context, mmdWriterConfig);
    }

    // package access for testing only tb 2016-02-18
    void printUsageTo(OutputStream outputStream) {
        final String ls = System.lineSeparator();
        final PrintWriter writer = new PrintWriter(outputStream);
        writer.write("matchup-tool version " + VERSION_NUMBER);
        writer.write(ls + ls);

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 120, "matchup-tool <options>", "Valid options are:", getOptions(), 3, 3, "");

        writer.flush();
    }

    private static void assignSensor(QueryParameter parameter, Sensor secondarySensor) {
        parameter.setSensorName(secondarySensor.getName());
        final String dataVersion = secondarySensor.getDataVersion();
        if (StringUtils.isNotNullAndNotEmpty(dataVersion)) {
            parameter.setVersion(dataVersion);
        }
    }

    private ToolContext initialize(CommandLine commandLine) throws IOException, SQLException {
        logger.info("Loading configuration ...");
        final ToolContext context = new ToolContext();

        final String configValue = commandLine.getOptionValue("config", "./config");
        final File configDirectory = new File(configValue);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDirectory);

        final SystemConfig systemConfig = new SystemConfig();
        systemConfig.loadFrom(configDirectory);
        context.setSystemConfig(systemConfig);

        context.setStartDate(getStartDate(commandLine));
        context.setEndDate(getEndDate(commandLine));

        final UseCaseConfig useCaseConfig = loadUseCaseConfig(commandLine, configDirectory);
        final ValidationResult validationResult = useCaseConfig.checkValid();
        if (!validationResult.isValid()) {
            final StringBuilder builder = createErrorMessage(validationResult);
            throw new IllegalArgumentException("Use case configuration errors: " + builder.toString());
        }
        context.setUseCaseConfig(useCaseConfig);

        final GeometryFactory geometryFactory = new GeometryFactory(systemConfig.getGeometryLibraryType());
        context.setGeometryFactory(geometryFactory);

        final Storage storage = Storage.create(databaseConfig.getDataSource(), geometryFactory);
        context.setStorage(storage);

        logger.info("Success loading configuration.");
        return context;
    }

    private void runMatchupGeneration(ToolContext context, MmdWriterConfig writerConfig) throws SQLException, IOException, InvalidRangeException {
        final MatchupCollection matchupCollection = createMatchupCollection(context);

        if (matchupCollection.getNumMatchups() == 0) {
            logger.warning("No matchups in time interval, creation of MMD file skipped.");
            return;
        }

        final MmdWriter mmdWriter = MmdWriterFactory.createFileWriter(writerConfig);

        final ReaderFactory readerFactory = ReaderFactory.get(context.getGeometryFactory());
        final IOVariablesList ioVariablesList = new IOVariablesList(readerFactory);

        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final VariablesConfiguration variablesConfiguration = writerConfig.getVariablesConfiguration();
        createIOVariablesPerSensor(ioVariablesList, matchupCollection, useCaseConfig, variablesConfiguration);
        if (useCaseConfig.isWriteDistance()) {
            ioVariablesList.addSampleSetVariable(createSphericalDistanceVariable());
        }
        try {
            mmdWriter.writeMMD(matchupCollection, context, ioVariablesList);
        } finally {
            ioVariablesList.close();
        }
    }

    private SphericalDistanceIOVariable createSphericalDistanceVariable() {
        final SphericalDistanceIOVariable variable = new SphericalDistanceIOVariable();
        variable.setTargetVariableName("matchup_spherical_distance");
        variable.setDataType(DataType.FLOAT.toString());
        variable.setDimensionNames("matchup_count");

        final List<Attribute> attributes = variable.getAttributes();
        attributes.add(new Attribute(DESCRIPTION_ATTRIBUTE_NAME, "spherical distance of matchup center locations"));
        attributes.add(new Attribute(UNIT_ATTRIBUTE_NAME, "km"));
        return variable;
    }

    // @todo 2 tb/** this method wants to be refactured 2016-05-11
    private MatchupCollection createMatchupCollection(ToolContext context) throws IOException, SQLException, InvalidRangeException {
        final MatchupCollection matchupCollection = new MatchupCollection();
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

        final ConditionEngine conditionEngine = new ConditionEngine();
        final ConditionEngineContext conditionEngineContext = ConditionEngine.createContext(context);
        conditionEngine.configure(useCaseConfig);

        final long timeDeltaInMillis = conditionEngine.getMaxTimeDeltaInMillis();
        final int timeDeltaSeconds = (int) (timeDeltaInMillis / 1000);

        final List<SatelliteObservation> primaryObservations = getPrimaryObservations(context);
        for (final SatelliteObservation primaryObservation : primaryObservations) {
            try (Reader primaryReader = readerFactory.getReader(primaryObservation.getSensor().getName())) {
                primaryReader.open(primaryObservation.getDataFilePath().toFile());

                final Date searchTimeStart = TimeUtils.addSeconds(-timeDeltaSeconds, primaryObservation.getStartTime());
                final Date searchTimeEnd = TimeUtils.addSeconds(timeDeltaSeconds, primaryObservation.getStopTime());

                final Geometry primaryGeoBounds = primaryObservation.getGeoBounds();
                final boolean isPrimarySegmented = isSegmented(primaryGeoBounds);

                final List<SatelliteObservation> secondaryObservations = getSecondaryObservations(context, searchTimeStart, searchTimeEnd);
                for (final SatelliteObservation secondaryObservation : secondaryObservations) {
                    try (Reader secondaryReader = readerFactory.getReader(secondaryObservation.getSensor().getName())) {
                        secondaryReader.open(secondaryObservation.getDataFilePath().toFile());

                        final Geometry secondaryGeoBounds = secondaryObservation.getGeoBounds();
                        final boolean isSecondarySegmented = isSegmented(secondaryGeoBounds);

                        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(primaryObservation, secondaryObservation);
                        if (intersectingIntervals.length == 0) {
                            continue;
                        }

                        final MatchupSet matchupSet = new MatchupSet();
                        matchupSet.setPrimaryObservationPath(primaryObservation.getDataFilePath());
                        matchupSet.setSecondaryObservationPath(secondaryObservation.getDataFilePath());

                        for (final Intersection intersection : intersectingIntervals) {
                            final TimeInfo timeInfo = intersection.getTimeInfo();
                            if (timeInfo.getMinimalTimeDelta() < timeDeltaInMillis) {
                                final PixelLocator primaryPixelLocator = getPixelLocator(primaryReader, isPrimarySegmented, (Polygon) intersection.getPrimaryGeometry());

                                SampleCollector sampleCollector = new SampleCollector(context, primaryPixelLocator);
                                sampleCollector.addPrimarySamples((Polygon) intersection.getGeometry(), matchupSet, primaryReader.getTimeLocator());

                                final PixelLocator secondaryPixelLocator = getPixelLocator(secondaryReader, isSecondarySegmented, (Polygon) intersection.getSecondaryGeometry());

                                sampleCollector = new SampleCollector(context, secondaryPixelLocator);
                                final List<SampleSet> completeSamples = sampleCollector.addSecondarySamples(matchupSet.getSampleSets(), secondaryReader.getTimeLocator());
                                matchupSet.setSampleSets(completeSamples);
                            }
                        }

                        if (matchupSet.getNumObservations() > 0) {
                            final Dimension primarySize = primaryReader.getProductSize();
                            conditionEngineContext.setPrimarySize(primarySize);
                            final Dimension secondarySize = secondaryReader.getProductSize();
                            conditionEngineContext.setSecondarySize(secondarySize);

                            logger.info("Found " + matchupSet.getNumObservations() + " matchup pixels");
                            conditionEngine.process(matchupSet, conditionEngineContext);
                            logger.info("Remaining " + matchupSet.getNumObservations() + " after condition processing");

                            final ScreeningEngine screeningEngine = new ScreeningEngine();
                            screeningEngine.configure(useCaseConfig);

                            screeningEngine.process(matchupSet, primaryReader, secondaryReader);
                            logger.info("Remaining " + matchupSet.getNumObservations() + " after matchup screening");

                            if (matchupSet.getNumObservations() > 0) {
                                matchupCollection.add(matchupSet);
                            }
                        }
                    }
                }
            }
        }

        return matchupCollection;
    }

    private List<SatelliteObservation> getSecondaryObservations(ToolContext context, Date searchTimeStart, Date searchTimeEnd) throws SQLException {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final QueryParameter parameter = getSecondarySensorParameter(useCaseConfig, searchTimeStart, searchTimeEnd);
        logger.info("Requesting secondary data ... (" + parameter.getSensorName() + ", " + parameter.getStartTime() + ", " + parameter.getStopTime());

        final Storage storage = context.getStorage();
        final List<SatelliteObservation> secondaryObservations = storage.get(parameter);

        logger.info("Received " + secondaryObservations.size() + " secondary satellite observations");

        return secondaryObservations;
    }

    private List<SatelliteObservation> getPrimaryObservations(ToolContext context) throws SQLException {
        final QueryParameter parameter = getPrimarySensorParameter(context);
        logger.info("Requesting primary data ... (" + parameter.getSensorName() + ", " + parameter.getStartTime() + ", " + parameter.getStopTime());

        final Storage storage = context.getStorage();
        final List<SatelliteObservation> primaryObservations = storage.get(parameter);

        logger.info("Received " + primaryObservations.size() + " primary satellite observations");

        return primaryObservations;
    }

    private UseCaseConfig loadUseCaseConfig(CommandLine commandLine, File configDirectory) throws IOException {
        final String usecaseConfigFileName = commandLine.getOptionValue("usecase");
        if (StringUtils.isNullOrEmpty(usecaseConfigFileName)) {
            throw new RuntimeException("Use case configuration file not supplied");
        }

        final File useCaseConfigFile = new File(configDirectory, usecaseConfigFileName);
        if (!useCaseConfigFile.isFile()) {
            throw new RuntimeException("Use case config file does not exist: '" + usecaseConfigFileName + "'");
        }

        final UseCaseConfig useCaseConfig;
        try (FileInputStream inputStream = new FileInputStream(useCaseConfigFile)) {
            useCaseConfig = UseCaseConfig.load(inputStream);
        }

        return useCaseConfig;
    }
}
