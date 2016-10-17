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


import com.bc.fiduceo.FiduceoConstants;
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
import ucar.nc2.NetcdfFile;
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

abstract class AbstractMmdWriter implements MmdWriter {

    private static final String UNIT_ATTRIBUTE_NAME = "unit";
    private static final String DESCRIPTION_ATTRIBUTE_NAME = "description";

    private final Logger logger;
    private final Map<String, Array> dataCacheMap;
    private final Map<String, Variable> variableMap;
    private final MmdWriterConfig writerConfig;
    NetcdfFileWriter netcdfFileWriter;
    private int flushCount = 0;

    AbstractMmdWriter(MmdWriterConfig writerConfig) {
        this.writerConfig = writerConfig;
        logger = FiduceoLogger.getLogger();

        dataCacheMap = new HashMap<>();
        variableMap = new HashMap<>();
    }

    /**
     * Writes the complete MatchupCollection matchup data to the MD file.
     * Closes the file!
     *
     * @param matchupCollection the matchup data collection
     * @param context           the ToolContext
     *
     * @throws IOException           on disk access errors
     * @throws InvalidRangeException on dimension errors
     */
    public void writeMMD(MatchupCollection matchupCollection, ToolContext context) throws IOException, InvalidRangeException {
        if (matchupCollection.getNumMatchups() == 0) {
            logger.warning("No matchups in time interval, creation of MMD file skipped.");
            return;
        }

        final ReaderFactory readerFactory = ReaderFactory.get(context.getGeometryFactory());
        final VariablePrototypeList variablePrototypeList = new VariablePrototypeList(readerFactory);

        try {
            logger.info("Start writing mmd-file ...");

            extractPrototypes(variablePrototypeList, matchupCollection, context);
            applyExcludesAndRenames(variablePrototypeList, writerConfig.getVariablesConfiguration());

            final Path mmdFile = createMmdFile(context, writerConfig);
            final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
            initializeNetcdfFile(mmdFile, useCaseConfig, variablePrototypeList.get(), matchupCollection.getNumMatchups());

            final Sensor primarySensor = useCaseConfig.getPrimarySensor();
            final Sensor secondarySensor = useCaseConfig.getAdditionalSensors().get(0);
            final String primarySensorName = primarySensor.getName();
            final String secondarySensorName = secondarySensor.getName();
            final List<VariablePrototype> primaryVariables = variablePrototypeList.getPrototypesFor(primarySensorName);
            final List<VariablePrototype> secondaryVariables = variablePrototypeList.getPrototypesFor(secondarySensorName);
            final Dimension primaryDimension = useCaseConfig.getDimensionFor(primarySensorName);
            final Dimension secondaryDimension = useCaseConfig.getDimensionFor(secondarySensorName);
            final Interval primaryInterval = new Interval(primaryDimension.getNx(), primaryDimension.getNy());
            final Interval secondaryInterval = new Interval(secondaryDimension.getNx(), secondaryDimension.getNy());

            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            final List<MatchupSet> sets = matchupCollection.getSets();
            int zIndex = 0;
            final int cacheSize = writerConfig.getCacheSize();
            for (MatchupSet set : sets) {

                final Path primaryObservationPath = set.getPrimaryObservationPath();
                final Path secondaryObservationPath = set.getSecondaryObservationPath();
                variablePrototypeList.setDataSourcePath(primarySensorName, primaryObservationPath);
                variablePrototypeList.setDataSourcePath(secondarySensorName, secondaryObservationPath);

                try (final Reader primaryReader = readerFactory.getReader(primarySensorName);
                     final Reader secondaryReader = readerFactory.getReader(secondarySensorName)) {
                    primaryReader.open(primaryObservationPath.toFile());
                    secondaryReader.open(secondaryObservationPath.toFile());
                    final List<SampleSet> sampleSets = set.getSampleSets();
                    for (SampleSet sampleSet : sampleSets) {
                        writeMmdValues(primarySensorName, primaryObservationPath, sampleSet.getPrimary(), zIndex, primaryVariables, primaryInterval, primaryReader);
                        writeMmdValues(secondarySensorName, secondaryObservationPath, sampleSet.getSecondary(), zIndex, secondaryVariables, secondaryInterval, secondaryReader);
                        if (useCaseConfig.isWriteDistance()) {
                            write(sampleSet.getSphericalDistance(), "matchup_spherical_distance", zIndex);
                        }
                        zIndex++;
                        if (zIndex > 0 && zIndex % cacheSize == 0) {
                            flush();
                        }
                    }
                }
            }

            stopWatch.stop();

            logger.info("Successfully wrote mmd-file to '" + mmdFile.toAbsolutePath().toString() + "'");
            logger.info("Writing time: '" + stopWatch.getTimeDiffString());

        } finally {
            variablePrototypeList.close();
            close();
        }
    }

    static void createUseCaseAttributes(NetcdfFileWriter netcdfFileWriter, UseCaseConfig useCaseConfig) {
        netcdfFileWriter.addGroupAttribute(null, new Attribute(
                    "comment",
                    "This MMD file is created based on the use case configuration documented in the attribute 'use-case-configuration'."
        ));
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            useCaseConfig.store(outputStream);
            netcdfFileWriter.addGroupAttribute(null, new Attribute(
                        "use-case-configuration",
                        outputStream.toString()
            ));
        } catch (IOException e) {
            throw new RuntimeException("should never come here");
        }
    }

    static void extractPrototypes(VariablePrototypeList variablePrototypeList, MatchupCollection matchupCollection, ToolContext context) throws IOException {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

        final Sensor primarySensor = useCaseConfig.getPrimarySensor();
        final List<Dimension> dimensions = useCaseConfig.getDimensions();
        final Sensor secondarySensor = useCaseConfig.getAdditionalSensors().get(0);

        final MatchupSet matchupSet = getFirstMatchupSet(matchupCollection);

        variablePrototypeList.extractPrototypes(primarySensor, matchupSet.getPrimaryObservationPath(), dimensions.get(0));
        variablePrototypeList.extractPrototypes(secondarySensor, matchupSet.getSecondaryObservationPath(), dimensions.get(1));
    }

    static MatchupSet getFirstMatchupSet(MatchupCollection matchupCollection) {
        final List<MatchupSet> sets = matchupCollection.getSets();
        if (sets.size() > 0) {
            return sets.get(0);
        }
        throw new IllegalStateException("Called getFirst() on empty matchupCollection.");
    }

    static void ensureFillValue(VariablePrototype prototype) {
        final String name = "_FillValue";
        final List<Attribute> attributes = prototype.getAttributes();
        for (Attribute attribute : attributes) {
            if (name.equals(attribute.getShortName())) {
                return;
            }
        }
        final DataType dataType = DataType.getType(prototype.getDataType());
        if (DataType.DOUBLE.equals(dataType)) {
            attributes.add(new Attribute(name, Double.MIN_VALUE));
        } else if (DataType.FLOAT.equals(dataType)) {
            attributes.add(new Attribute(name, Float.MIN_VALUE));
        } else if (DataType.LONG.equals(dataType)) {
            attributes.add(new Attribute(name, Long.MIN_VALUE));
        } else if (DataType.INT.equals(dataType)) {
            attributes.add(new Attribute(name, Integer.MIN_VALUE));
        } else if (DataType.SHORT.equals(dataType)) {
            attributes.add(new Attribute(name, Short.MIN_VALUE));
        } else if (DataType.BYTE.equals(dataType)) {
            attributes.add(new Attribute(name, Byte.MIN_VALUE));
        }
    }

    // package access for testing only tb 2016-09-29
    static Path createMmdFile(ToolContext context, MmdWriterConfig writerConfig) throws IOException {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final String mmdFileName = MmdWriterFactory.createMMDFileName(useCaseConfig, context.getStartDate(), context.getEndDate());
        final Path mmdFile = Paths.get(useCaseConfig.getOutputPath(), mmdFileName);
        final Path targetDir = mmdFile.getParent();
        if (targetDir == null) {
            throw new RuntimeException("Target directory does not exist for: " + mmdFile.toString());
        }

        if (!Files.isDirectory(targetDir)) {
            try {
                Files.createDirectories(targetDir);
            } catch (IOException e) {
                throw new IOException("Unable to create mmd output directory '" + targetDir.toAbsolutePath().toString() + "'");
            }
        }

        if (Files.exists(mmdFile)) {
            if (!writerConfig.isOverwrite()) {
                throw new IOException("Mmd output file already exists '" + mmdFile.toAbsolutePath().toString() + "'");
            } else {
                Files.delete(mmdFile);
            }
        }

        try {
            return Files.createFile(mmdFile);
        } catch (IOException e) {
            throw new IOException("unable to create mmd output file '" + mmdFile.toAbsolutePath().toString() + "'");
        }
    }

    // package access for testing only tb 2016-10-05
    static void applyExcludesAndRenames(VariablePrototypeList variablePrototypeList, VariablesConfiguration variablesConfiguration) {
        final List<String> sensorNames = variablePrototypeList.getSensorNames();

        for (final String sensorName : sensorNames) {
            final List<VariablePrototype> variablePrototypes = variablePrototypeList.getPrototypesFor(sensorName);
            final List<VariableRename> renames = variablesConfiguration.getRenames(sensorName);
            for (final VariableRename rename : renames) {
                final String sourceName = rename.getSourceName();
                final VariablePrototype prototype = getPrototype(sourceName, variablePrototypes);
                if (prototype != null) {
                    prototype.setTargetVariableName(rename.getTargetName());
                }
            }

            final List<VariableExclude> excludes = variablesConfiguration.getExcludes(sensorName);
            for (final VariableExclude exclude : excludes) {
                final String sourceName = exclude.getSourceName();
                final VariablePrototype prototype = getPrototype(sourceName, variablePrototypes);
                if (prototype != null) {
                    variablePrototypes.remove(prototype);
                }
            }
        }
    }

    static VariablePrototype getPrototype(String sourceName, List<VariablePrototype> variablePrototypes) {
        for (final VariablePrototype prototype : variablePrototypes) {
            if (sourceName.equals(prototype.getSourceVariableName())) {
                return prototype;
            }
        }
        return null;
    }

    static VariableExclude getExclude(String sourceVariableName, List<VariableExclude> excludes) {
        for (final VariableExclude exclude : excludes) {
            if (sourceVariableName.equals(exclude.getSourceName())) {
                return exclude;
            }
        }
        return null;
    }

    static VariableRename getRename(String sourceVariableName, List<VariableRename> renames) {
        for (final VariableRename rename : renames) {
            if (sourceVariableName.equals(rename.getSourceName())) {
                return rename;
            }
        }
        return null;
    }

    abstract void createNetCdfFileWriter(Path mmdFile) throws IOException;

    void initializeNetcdfFile(Path mmdFile, UseCaseConfig useCaseConfig, List<VariablePrototype> variablePrototypes, int numMatchups) throws IOException {
        createNetCdfFileWriter(mmdFile);

        createGlobalAttributes();
        createUseCaseAttributes(netcdfFileWriter, useCaseConfig);
        final List<Dimension> dimensions = useCaseConfig.getDimensions();
        createDimensions(dimensions, numMatchups);
        createExtraMmdVariablesPerSensor(dimensions);

        if (useCaseConfig.isWriteDistance()) {
            final Variable variableDistance = netcdfFileWriter.addVariable(null, "matchup_spherical_distance", DataType.FLOAT, "matchup_count");
            variableDistance.addAttribute(new Attribute(DESCRIPTION_ATTRIBUTE_NAME, "spherical distance of matchup center locations"));
            variableDistance.addAttribute(new Attribute(UNIT_ATTRIBUTE_NAME, "km"));
        }

        for (final VariablePrototype variablePrototype : variablePrototypes) {
            ensureFillValue(variablePrototype);
            final Variable variable = netcdfFileWriter.addVariable(null,
                                                                   variablePrototype.getTargetVariableName(),
                                                                   DataType.getType(variablePrototype.getDataType()),
                                                                   variablePrototype.getDimensionNames());
            final List<Attribute> attributes = variablePrototype.getAttributes();
            for (Attribute attribute : attributes) {
                if (attribute.getFullName().startsWith("_Chunk")) {
                    continue;
                }
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

    private void writeMmdValues(String sensorName, Path observationPath, Sample sample, int zIndex, List<VariablePrototype> variables, Interval interval, Reader reader) throws IOException, InvalidRangeException {
        writeMmdValues(sample, zIndex, variables, interval);

        writeMMSStandardVariables(sensorName, observationPath, zIndex, interval, reader, sample);
    }

    private void writeMMSStandardVariables(String sensorName, Path observationPath, int zIndex, Interval interval, Reader reader, Sample sample) throws IOException, InvalidRangeException {
        final int x = sample.x;
        final int y = sample.y;

        final VariablesConfiguration variablesConfiguration = writerConfig.getVariablesConfiguration();
        final List<VariableExclude> excludes = variablesConfiguration.getExcludes(sensorName);
        final List<VariableRename> renames = variablesConfiguration.getRenames(sensorName);

        writeIntWithExcludeAndRename(zIndex, x, excludes, renames, sensorName + "_x");
        writeIntWithExcludeAndRename(zIndex, y, excludes, renames, sensorName + "_y");

        final String fileVariableName = sensorName + "_file_name";
        if (getExclude(fileVariableName, excludes) == null) {
            final VariableRename rename = getRename(fileVariableName, renames);
            if (rename != null) {
                final String targetName = rename.getTargetName();
                write(observationPath.getFileName().toString(), targetName, zIndex);
            } else {
                write(observationPath.getFileName().toString(), fileVariableName, zIndex);
            }
        }
        final String acTimeVariableName = sensorName + "_acquisition_time";
        if (getExclude(acTimeVariableName, excludes) == null) {
            final VariableRename rename = getRename(acTimeVariableName, renames);
            if (rename != null) {
                final String targetName = rename.getTargetName();
                write(reader.readAcquisitionTime(x, y, interval), targetName, zIndex);
            } else {
                write(reader.readAcquisitionTime(x, y, interval), acTimeVariableName, zIndex);
            }
        }
    }

    private void writeIntWithExcludeAndRename(int zIndex, int value, List<VariableExclude> excludes, List<VariableRename> renames, String variableName) throws IOException, InvalidRangeException {
        if (getExclude(variableName, excludes) == null) {
            final VariableRename rename = getRename(variableName, renames);
            if (rename != null) {
                final String targetName = rename.getTargetName();
                write(value, targetName, zIndex);
            } else {
                write(value, variableName, zIndex);
            }
        }
    }

    private void writeMmdValues(Sample sample, int zIndex, List<VariablePrototype> variables, Interval interval) throws IOException, InvalidRangeException {
        final int x = sample.x;
        final int y = sample.y;

        for (VariablePrototype variable : variables) {
            final String sourceVariableName = variable.getSourceVariableName();
            final String targetVariableName = variable.getTargetVariableName();
            final Array window = variable.readRaw(x, y, interval, sourceVariableName);
            write(window, targetVariableName, zIndex);
        }
    }

    private void write(Array data, String variableName, int stackIndex) {
        final Array target = getTarget(variableName);
        final Index index = target.getIndex();
        index.set(stackIndex % writerConfig.getCacheSize());
        Array.arraycopy(data, 0, target, index.currentElement(), (int) data.getSize());
    }

    private void write(int v, String variableName, int zIndex) throws IOException, InvalidRangeException {
        final Array data = Array.factory(new int[][]{{v}});
        write(data, variableName, zIndex);
    }

    private void write(float value, String variableName, int zIndex) throws IOException, InvalidRangeException {
        final Array data = Array.factory(new float[][]{{value}});
        write(data, variableName, zIndex);
    }

    private void write(String v, String variableName, int zIndex) throws IOException, InvalidRangeException {
        final int[] shape = getVariable(variableName).getShape();
        final char[] chars = new char[shape[1]];
        v.getChars(0, v.length(), chars, 0);
        final Array data = Array.factory(new char[][]{chars});
        write(data, variableName, zIndex);
    }

    private Array getTarget(String variableName) {
        if (!dataCacheMap.containsKey(variableName)) {
            Variable variable = getVariable(variableName);
            final int[] shape = variable.getShape();
            shape[0] = writerConfig.getCacheSize();
            dataCacheMap.put(variableName, Array.factory(variable.getDataType(), shape));
        }
        return dataCacheMap.get(variableName);
    }

    private Variable getVariable(String variableName) {
        if (!variableMap.containsKey(variableName)) {
            final String escapedName = NetcdfFile.makeValidCDLName(variableName);
            variableMap.put(variableName, netcdfFileWriter.findVariable(escapedName));
        }
        return variableMap.get(variableName);
    }

    private void createExtraMmdVariablesPerSensor(List<Dimension> dimensions) {
        final VariablesConfiguration variablesConfiguration = writerConfig.getVariablesConfiguration();

        for (Dimension dimension : dimensions) {
            final String sensorName = dimension.getName();
            final List<VariableExclude> excludes = variablesConfiguration.getExcludes(sensorName);
            final List<VariableRename> renames = variablesConfiguration.getRenames(sensorName);

            final String xVariableName = sensorName + "_x";
            if (getExclude(xVariableName, excludes) == null) {
                final String targetName = getTargetName(renames, xVariableName);
                final Variable variableX = netcdfFileWriter.addVariable(null, targetName, DataType.INT, "matchup_count");
                variableX.addAttribute(new Attribute(DESCRIPTION_ATTRIBUTE_NAME, "pixel original x location in satellite raster"));
            }

            final String yVariableName = sensorName + "_y";
            if (getExclude(yVariableName, excludes) == null) {
                final String targetName = getTargetName(renames, yVariableName);
                final Variable variableY = netcdfFileWriter.addVariable(null, targetName, DataType.INT, "matchup_count");
                variableY.addAttribute(new Attribute(DESCRIPTION_ATTRIBUTE_NAME, "pixel original y location in satellite raster"));
            }

            final String fileNameVariableName = sensorName + "_file_name";
            if (getExclude(fileNameVariableName, excludes) == null) {
                final String targetName = getTargetName(renames, fileNameVariableName);
                final Variable variableFileName = netcdfFileWriter.addVariable(null, targetName, DataType.CHAR, "matchup_count file_name");
                variableFileName.addAttribute(new Attribute(DESCRIPTION_ATTRIBUTE_NAME, "file name of the original data file"));
            }

            final String acTimeVariableName = sensorName + "_acquisition_time";
            if (getExclude(acTimeVariableName, excludes) == null) {
                final String targetName = getTargetName(renames, acTimeVariableName);
                final String yDimension = getDimensionNameNy(sensorName);
                final String xDimension = getDimensionNameNx(sensorName);
                final Variable variableAcqTime = netcdfFileWriter.addVariable(null, targetName, DataType.INT, "matchup_count " + yDimension + " " + xDimension);
                variableAcqTime.addAttribute(new Attribute(DESCRIPTION_ATTRIBUTE_NAME, "acquisition time of original pixel"));
                variableAcqTime.addAttribute(new Attribute(UNIT_ATTRIBUTE_NAME, "seconds since 1970-01-01"));
                variableAcqTime.addAttribute(new Attribute("_FillValue", -2147483648));
            }
        }
    }

    private String getTargetName(List<VariableRename> renames, String originalVariableName) {
        String targetName = originalVariableName;
        final VariableRename rename = getRename(originalVariableName, renames);
        if (rename != null) {
            targetName = rename.getTargetName();
        }
        return targetName;
    }

    private void createGlobalAttributes() {
        addGlobalAttribute("title", "FIDUCEO multi-sensor match-up dataset (MMD)");
        addGlobalAttribute("institution", "Brockmann Consult GmbH");
        addGlobalAttribute("contact", "Tom Block (tom.block@brockmann-consult.de)");
        addGlobalAttribute("license", "This dataset is released for use under CC-BY licence and was developed in the EC FIDUCEO project \"Fidelity and Uncertainty in Climate Data Records from Earth Observations\". Grant Agreement: 638822.");
        addGlobalAttribute("creation_date", TimeUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        addGlobalAttribute("software_version", FiduceoConstants.VERSION);
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

    private void flush() throws IOException, InvalidRangeException {
        final int cacheSize = writerConfig.getCacheSize();
        for (Map.Entry<String, Array> entry : dataCacheMap.entrySet()) {
            final String variableName = entry.getKey();
            final Variable variable = variableMap.get(variableName);
            Array dataToBeWritten = entry.getValue();
            final int[] origin = new int[dataToBeWritten.getRank()];

            final int matchupCount = variable.getShape(0);
            final int zStart = flushCount * cacheSize;
            if (zStart + cacheSize > matchupCount) {
                final int restHeight = matchupCount - zStart;
                final int[] shape = dataToBeWritten.getShape();
                shape[0] = restHeight;
                dataToBeWritten = dataToBeWritten.sectionNoReduce(origin, shape, null);
            }
            origin[0] = zStart;
            netcdfFileWriter.write(variable, origin, dataToBeWritten);
        }
        flushCount++;
        netcdfFileWriter.flush();
    }
}
