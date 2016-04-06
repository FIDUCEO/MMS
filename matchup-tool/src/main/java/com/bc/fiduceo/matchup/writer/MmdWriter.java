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

package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StopWatch;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MmdWriter {

    private final int cacheSize;
    private final Map<String, Array> dataCacheMap;
    private final Map<String, Variable> variableMap;
    private final Logger logger;

    private NetcdfFileWriter netcdfFileWriter;
    private int flushCount = 0;

    public MmdWriter(int cacheSize) {
        this.cacheSize = cacheSize;
        dataCacheMap = new HashMap<>();
        variableMap = new HashMap<>();
        logger = FiduceoLogger.getLogger();
    }

    public static String createMMDFileName(UseCaseConfig useCaseConfig, Date startDate, Date endDate) {
        final StringBuilder nameBuilder = new StringBuilder();

        nameBuilder.append(useCaseConfig.getName());
        nameBuilder.append("_");

        nameBuilder.append(useCaseConfig.getPrimarySensor().getName());
        nameBuilder.append("_");

        final List<Sensor> additionalSensors = useCaseConfig.getAdditionalSensors();
        if (additionalSensors.size() > 0) {
            for (final Sensor additionalSensor : additionalSensors) {
                nameBuilder.append(additionalSensor.getName());
                nameBuilder.append("_");
            }
        } else {
            nameBuilder.append("_");
        }

        nameBuilder.append(TimeUtils.formatToDOY(startDate));
        nameBuilder.append("_");

        nameBuilder.append(TimeUtils.formatToDOY(endDate));
        nameBuilder.append(".nc");

        return nameBuilder.toString();
    }

    public void writeMMD(MatchupCollection matchupCollection, ToolContext context) throws IOException, InvalidRangeException {
        if (matchupCollection.getNumMatchups() == 0) {
            logger.warning("No matchups in time interval, creation of MMD file skipped.");
            return;
        }

        logger.info("Start writing mmd-file ...");

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        extractPrototypes(variablesConfiguration, matchupCollection, context);
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final Path mmdFile = createMmdFile(context);
        initializeNetcdfFile(mmdFile, useCaseConfig, variablesConfiguration.get(), matchupCollection.getNumMatchups());

        final Sensor primarySensor = useCaseConfig.getPrimarySensor();
        final Sensor secondarySensor = useCaseConfig.getAdditionalSensors().get(0);
        final String primarySensorName = primarySensor.getName();
        final String secondarySensorName = secondarySensor.getName();
        final List<VariablePrototype> primaryVariables = variablesConfiguration.getPrototypesFor(primarySensorName);
        final List<VariablePrototype> secondaryVariables = variablesConfiguration.getPrototypesFor(secondarySensorName);
        final Dimension primaryDimension = useCaseConfig.getDimensionFor(primarySensorName);
        final Dimension secondaryDimension = useCaseConfig.getDimensionFor(secondarySensorName);
        final Interval primaryInterval = new Interval(primaryDimension.getNx(), primaryDimension.getNy());
        final Interval secondaryInterval = new Interval(secondaryDimension.getNx(), secondaryDimension.getNy());

        final ReaderFactory readerFactory = ReaderFactory.get();

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final List<MatchupSet> sets = matchupCollection.getSets();
        int zIndex = 0;
        for (MatchupSet set : sets) {
            final Path primaryObservationPath = set.getPrimaryObservationPath();
            final Path secondaryObservationPath = set.getSecondaryObservationPath();
            try (final Reader primaryReader = readerFactory.getReader(primarySensorName);
                 final Reader secondaryReader = readerFactory.getReader(secondarySensorName)) {
                primaryReader.open(primaryObservationPath.toFile());
                secondaryReader.open(secondaryObservationPath.toFile());
                final List<SampleSet> sampleSets = set.getSampleSets();
                for (SampleSet sampleSet : sampleSets) {
                    writeMmdValues(primarySensorName, primaryObservationPath, sampleSet.getPrimary(), zIndex, primaryVariables, primaryInterval, primaryReader);
                    writeMmdValues(secondarySensorName, secondaryObservationPath, sampleSet.getSecondary(), zIndex, secondaryVariables, secondaryInterval, secondaryReader);
                    zIndex++;
                    if (zIndex > 0 && zIndex % cacheSize == 0) {
                        flush();
                    }
                }
            }
        }

        stopWatch.stop();

        logger.info("Successfully wrote mmd-file to '" + mmdFile.toAbsolutePath().toString() + "'");
        logger.info("Write ting time: '" + stopWatch.getTimeDiffString());

        close();
    }

    static void createUseCaseAttributes(NetcdfFileWriter netcdfFileWriter, UseCaseConfig useCaseConfig) {
        netcdfFileWriter.addGroupAttribute(null, new Attribute(
                    "comment",
                    "The MMD file is created based on the use case configuration documented in the attribute 'use-case-configuration'."
        ));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        useCaseConfig.store(outputStream);
        netcdfFileWriter.addGroupAttribute(null, new Attribute(
                    "use-case-configuration",
                    outputStream.toString()
        ));
    }

    static void extractPrototypes(VariablesConfiguration variablesConfiguration, MatchupCollection matchupCollection, ToolContext context) throws IOException {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

        final Sensor primarySensor = useCaseConfig.getPrimarySensor();
        final List<Dimension> dimensions = useCaseConfig.getDimensions();
        final Sensor secondarySensor = useCaseConfig.getAdditionalSensors().get(0);

        final MatchupSet matchupSet = getFirstMatchupSet(matchupCollection);

        variablesConfiguration.extractPrototypes(primarySensor, matchupSet.getPrimaryObservationPath(), dimensions.get(0));
        variablesConfiguration.extractPrototypes(secondarySensor, matchupSet.getSecondaryObservationPath(), dimensions.get(1));
    }

    static MatchupSet getFirstMatchupSet(MatchupCollection matchupCollection) {
        final List<MatchupSet> sets = matchupCollection.getSets();
        if (sets.size() > 0) {
            return sets.get(0);
        }
        throw new IllegalStateException("Called getFirst() on empty matchupCollection.");
    }

    void initializeNetcdfFile(Path mmdFile, UseCaseConfig useCaseConfig, List<VariablePrototype> variablePrototypes, int numMatchups) throws IOException {
        netcdfFileWriter = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, mmdFile.toAbsolutePath().toString());

        createGlobalAttributes();
        createUseCaseAttributes(netcdfFileWriter, useCaseConfig);
        final List<Dimension> dimensions = useCaseConfig.getDimensions();
        createDimensions(dimensions, numMatchups);
        createExtraMmdVariablesPerSensor(dimensions);

        for (final VariablePrototype variablePrototype : variablePrototypes) {
            final Variable variable = netcdfFileWriter.addVariable(null,
                                                                   variablePrototype.getTargetVariableName(),
                                                                   DataType.getType(variablePrototype.getDataType()),
                                                                   variablePrototype.getDimensionNames());
            final List<Attribute> attributes = variablePrototype.getAttributes();
            for (Attribute attribute : attributes) {
                variable.addAttribute(attribute);
            }
        }
        netcdfFileWriter.create();
    }

    void close() throws IOException, InvalidRangeException {
        flush();
        variableMap.clear();
        dataCacheMap.clear();
        if (netcdfFileWriter != null) {
            netcdfFileWriter.close();
            netcdfFileWriter = null;
        }
    }

    private void write(int v, String variableName, int zIndex) throws IOException, InvalidRangeException {
        final Array data = Array.factory(new int[][]{{v}});
        write(data, variableName, zIndex);
    }

    private void write(String v, String variableName, int zIndex) throws IOException, InvalidRangeException {
        final int[] shape = getVariable(variableName).getShape();
        final char[] chars = new char[shape[1]];
        v.getChars(0, v.length(), chars, 0);
        final Array data = Array.factory(new char[][]{chars});
        write(data, variableName, zIndex);
    }

    private void write(Array data, String variableName, int stackIndex) throws IOException, InvalidRangeException {
        final Array target = getTarget(variableName);
        final Index index = target.getIndex();
        index.set(stackIndex % cacheSize);
        Array.arraycopy(data, 0, target, index.currentElement(), (int) data.getSize());
    }

    private void flush() throws IOException, InvalidRangeException {
        for (Map.Entry<String, Array> entry : dataCacheMap.entrySet()) {
            final String variableName = entry.getKey();
            final Variable variable = variableMap.get(variableName);

            Array dataToBeWritten = entry.getValue();
            final int[] shape = dataToBeWritten.getShape();
            final int[] origin = new int[shape.length];
            final int[] stride = createStride(shape);

            final int matchupCount = variable.getShape(0);
            final int zStart = flushCount * cacheSize;
            if (zStart + cacheSize > matchupCount) {
                final int restHeight = matchupCount - zStart;
                shape[0] = restHeight;
                dataToBeWritten = dataToBeWritten.sectionNoReduce(origin, shape, stride);
            }
            origin[0] = zStart;
            netcdfFileWriter.write(variable, origin, dataToBeWritten);
        }
        flushCount++;
    }

    private int[] createStride(int[] shape) {
        final int[] stride = new int[shape.length];
        for (int i = 0; i < shape.length; i++) {
            stride[i] = 1;
        }
        return stride;
    }

    private void writeMmdValues(String sensorName, Path observationPath, Sample sample, int zIndex, List<VariablePrototype> variables, Interval interval, Reader reader) throws IOException, InvalidRangeException {
        final int x = sample.x;
        final int y = sample.y;
        writeMmdValues(x, y, zIndex, variables, interval, reader);
        write(x, sensorName + "_x", zIndex);
        write(y, sensorName + "_y", zIndex);
        write(observationPath.getFileName().toString(), sensorName + "_file_name", zIndex);
        write(reader.readAcquisitionTime(x, y, interval), sensorName + "_acquisition_time", zIndex);
    }

    private void writeMmdValues(int x, int y, int zIndex, List<VariablePrototype> variables, Interval interval, Reader reader) throws IOException, InvalidRangeException {
        for (VariablePrototype variable : variables) {
            final String sourceVariableName = variable.getSourceVariableName();
            final String targetVariableName = variable.getTargetVariableName();
            final Array window = reader.readRaw(x, y, interval, sourceVariableName);
            write(window, targetVariableName, zIndex);
        }
    }

    private Path createMmdFile(ToolContext context) throws IOException {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final String mmdFileName = MmdWriter.createMMDFileName(useCaseConfig, context.getStartDate(), context.getEndDate());
        final Path mmdFile = Paths.get(useCaseConfig.getOutputPath(), mmdFileName);
        final Path targetDir = mmdFile.getParent();

        if (!Files.isDirectory(targetDir)) {
            try {
                Files.createDirectories(targetDir);
            } catch (IOException e) {
                throw new IOException("Unable to create mmd output directory '" + targetDir.toAbsolutePath().toString() + "'");
            }
        }

        // @todo 3 tb/tb we might set an overwrite property to the system config later, if requested 2016-03-16
        if (Files.exists(mmdFile)) {
            throw new IOException("Mmd output file already exists '" + mmdFile.toAbsolutePath().toString() + "'");
        }
        try {
            return Files.createFile(mmdFile);
        } catch (IOException e) {
            throw new IOException("unable to create mmd output file '" + mmdFile.toAbsolutePath().toString() + "'");
        }
    }

    private Array getTarget(String variableName) {
        if (!dataCacheMap.containsKey(variableName)) {
            Variable variable = getVariable(variableName);
            final int[] shape = variable.getShape();
            shape[0] = cacheSize;
            dataCacheMap.put(variableName, Array.factory(variable.getDataType(), shape));
        }
        return dataCacheMap.get(variableName);
    }

    private Variable getVariable(String variableName) {
        if (!variableMap.containsKey(variableName)) {
            variableMap.put(variableName, netcdfFileWriter.findVariable(variableName));
        }
        return variableMap.get(variableName);
    }

    private void createExtraMmdVariablesPerSensor(List<Dimension> dimensions) {
        for (Dimension dimension : dimensions) {
            final String sensorName = dimension.getName();
            netcdfFileWriter.addVariable(null, sensorName + "_x", DataType.INT, "matchup_count");
            netcdfFileWriter.addVariable(null, sensorName + "_y", DataType.INT, "matchup_count");
            netcdfFileWriter.addVariable(null, sensorName + "_file_name", DataType.CHAR, "matchup_count file_name");
            final String yDimension = getDimensionNameNy(sensorName);
            final String xDimension = getDimensionNameNx(sensorName);
            netcdfFileWriter.addVariable(null, sensorName + "_acquisition_time", DataType.INT, "matchup_count " + yDimension + " " + xDimension);
        }
    }

    private void createGlobalAttributes() {
        addGlobalAttribute("title", "FIDUCEO multi-sensor match-up dataset (MMD)");
        addGlobalAttribute("institution", "Brockmann Consult GmbH");
        addGlobalAttribute("contact", "Tom Block (tom.block@brockmann-consult.de)");
        addGlobalAttribute("license", "This dataset is released for use under CC-BY licence and was developed in the EC FIDUCEO project \"Fidelity and Uncertainty in Climate Data Records from Earth Observations\". Grant Agreement: 638822.");
        addGlobalAttribute("creation_date", TimeUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
    }

    private void addGlobalAttribute(String name, String val) {
        netcdfFileWriter.addGroupAttribute(null, new Attribute(name, val));
    }

    private void createDimensions(List<Dimension> dimensions, int numMatchups) {
        for (final Dimension dimension : dimensions) {
            netcdfFileWriter.addDimension(null, getDimensionNameNx(dimension.getName()), dimension.getNx());
            netcdfFileWriter.addDimension(null, getDimensionNameNy(dimension.getName()), dimension.getNy());
        }
        netcdfFileWriter.addDimension(null, "file_name", 128);
        netcdfFileWriter.addDimension(null, "matchup_count", numMatchups);
    }

    private String getDimensionNameNy(String sensorName) {
        return sensorName + "_ny";
    }

    private String getDimensionNameNx(String sensorName) {
        return sensorName + "_nx";
    }
}