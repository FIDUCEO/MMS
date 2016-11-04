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

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.core.ValidationResult;
import com.bc.fiduceo.db.DatabaseConfig;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.log.FiduceoLogger;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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

    static void createIOVariablesPerSensor(IOVariablesList ioVariablesList, MatchupCollection matchupCollection,
                                           final UseCaseConfig useCaseConfig, VariablesConfiguration variablesConfiguration)
            throws IOException {

        final String primSensorName = useCaseConfig.getPrimarySensor().getName();
        // todo 3 se/** 2016-10-21 adopt when multiple secondary sensors are needed
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
            final String attName = variablesConfiguration.getRenamedAttributeName(sensorName, varName, DESCRIPTION_ATTRIBUTE_NAME);
            attributes.add(new Attribute(attName, "pixel original x location in satellite raster"));
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
            final String attName = variablesConfiguration.getRenamedAttributeName(sensorName, varName, DESCRIPTION_ATTRIBUTE_NAME);
            attributes.add(new Attribute(attName, "pixel original y location in satellite raster"));
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
            final String attName = variablesConfiguration.getRenamedAttributeName(sensorName, varName, DESCRIPTION_ATTRIBUTE_NAME);
            attributes.add(new Attribute(attName, "file name of the original data file"));
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
            String attName;
            attName = variablesConfiguration.getRenamedAttributeName(sensorName, varName, DESCRIPTION_ATTRIBUTE_NAME);
            attributes.add(new Attribute(attName, "acquisition time of original pixel"));
            attName = variablesConfiguration.getRenamedAttributeName(sensorName, varName, UNIT_ATTRIBUTE_NAME);
            attributes.add(new Attribute(attName, "seconds since 1970-01-01"));
            attName = variablesConfiguration.getRenamedAttributeName(sensorName, varName, "_FillValue");
            attributes.add(new Attribute(attName, -2147483648));
            ioVariablesList.add(ioVariable, sensorName);
        }
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


    private ToolContext initialize(CommandLine commandLine) throws IOException, SQLException {
        logger.info("Loading configuration ...");
        final ToolContext context = new ToolContext();

        final String configValue = commandLine.getOptionValue("config", "./config");
        final File configDirectory = new File(configValue);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDirectory);

        final SystemConfig systemConfig = SystemConfig.loadFrom(configDirectory);
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
        // @todo 1 tb/tb create MatchupStrategy Factory 2016-11-04
        // @todo 1 tb/tb request MatchupStrategy from config  2016-11-04

        final MatchupStrategy matchupStrategy = new MatchupStrategy(logger);
        final MatchupCollection matchupCollection = matchupStrategy.createMatchupCollection(context);


        if (matchupCollection.getNumMatchups() == 0) {
            logger.warning("No matchups in time interval, creation of MMD file skipped.");
            return;
        }

        final MmdWriter mmdWriter = MmdWriterFactory.createFileWriter(writerConfig);

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
