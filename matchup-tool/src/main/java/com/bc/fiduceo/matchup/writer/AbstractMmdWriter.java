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
import com.bc.fiduceo.core.*;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderCache;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StopWatch;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

abstract class AbstractMmdWriter implements MmdWriter, Target {

    static final String GLOBAL_ATTR_TITLE = "title";
    static final String GLOBAL_ATTR_INSTITUTION = "institution";
    static final String GLOBAL_ATTR_CONTACT = "contact";
    static final String GLOBAL_ATTR_LICENSE = "license";

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
     * @param ioVariablesList   the variables which has to be part of the mmd file
     * @throws IOException           on disk access errors
     * @throws InvalidRangeException on dimension errors
     */
    public void writeMMD(MatchupCollection matchupCollection, ToolContext context, IOVariablesList ioVariablesList) throws IOException, InvalidRangeException {
        if (matchupCollection.getNumMatchups() == 0) {
            logger.warning("No matchups in time interval, creation of MMD file skipped.");
            return;
        }

        final List<IOVariable> ioVariables = ioVariablesList.get();
        for (IOVariable variable : ioVariables) {
            variable.setTarget(this);
        }

        final ReaderFactory readerFactory = context.getReaderFactory();
        final ReaderCache readerCache = new ReaderCache(writerConfig.getReaderCacheSize(), readerFactory, context.getArchive());

        try {
            logger.info("Start writing mmd-file ...");

            final Path mmdFile = createMmdFile(context, writerConfig);
            final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

            initializeNetcdfFile(mmdFile, useCaseConfig, ioVariablesList.get(), matchupCollection.getNumMatchups());
            logger.info("Initialized target file");

            final Sensor primarySensor = useCaseConfig.getPrimarySensor();
            final String primarySensorName = primarySensor.getName();
            final List<IOVariable> primaryVariables = ioVariablesList.getVariablesFor(primarySensorName);
            final Dimension primaryDimension = useCaseConfig.getDimensionFor(primarySensorName);
            final Interval primaryInterval = new Interval(primaryDimension.getNx(), primaryDimension.getNy());

            final List<Sensor> secondarySensors = useCaseConfig.getSecondarySensors();
            final int secSize = secondarySensors.size();
            final String[] secSensorNames = new String[secSize];
            final List<List<IOVariable>> secVariablesList = new ArrayList<>();
            final Interval[] secIntervals = new Interval[secSize];
            for (int i = 0; i < secondarySensors.size(); i++) {
                final Sensor secondarySensor = secondarySensors.get(i);
                final String secondarySensorName = secondarySensor.getName();
                secSensorNames[i] = secondarySensorName;
                secVariablesList.add(ioVariablesList.getVariablesFor(secondarySensorName));
                final Dimension secondaryDimension = useCaseConfig.getDimensionFor(secondarySensorName);
                secIntervals[i] = new Interval(secondaryDimension.getNx(), secondaryDimension.getNy());
            }

            final List<SampleSetIOVariable> sampleSetVariables = ioVariablesList.getSampleSetIOVariables();
            logger.info("Collected IO Variables");

            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            final List<MatchupSet> sets = matchupCollection.getSets();
            int zIndex = 0;
            final int cacheSize = writerConfig.getCacheSize();
            for (MatchupSet set : sets) {
                int numObservations = set.getNumObservations();
                if (numObservations == 0) {
                    continue;
                }

                final Path primaryObservationPath = set.getPrimaryObservationPath();
                final String primaryVersion = set.getPrimaryProcessingVersion();
                final Reader primaryReader = readerCache.getReaderFor(primarySensorName, primaryObservationPath, primaryVersion);
                ioVariablesList.setReaderAndPath(primarySensorName, primaryReader, primaryObservationPath, primaryVersion);

                logger.info("writing samples for " + primaryObservationPath.getFileName());
                for (String secSensorName : secSensorNames) {
                    final Path secondaryObservationPath = set.getSecondaryObservationPath(secSensorName);
                    final String secondaryVersion = set.getSecondaryProcessingVersion(secSensorName);
                    final Reader secondaryReader = readerCache.getReaderFor(secSensorName, secondaryObservationPath, secondaryVersion);
                    ioVariablesList.setReaderAndPath(secSensorName, secondaryReader, secondaryObservationPath, secondaryVersion);
                    logger.info("... and " + secondaryObservationPath.getFileName());
                }
                logger.info("Num matchups: " + numObservations);

                final List<SampleSet> sampleSets = set.getSampleSets();
                for (SampleSet sampleSet : sampleSets) {
                    writeMmdValues(sampleSet.getPrimary(), zIndex, primaryVariables, primaryInterval);
                    for (int i = 0; i < secSensorNames.length; i++) {
                        String secSensorName = secSensorNames[i];
                        final List<IOVariable> secIOVariables = secVariablesList.get(i);
                        writeMmdValues(sampleSet.getSecondary(secSensorName), zIndex, secIOVariables, secIntervals[i]);
                    }
                    writeSampleSetVariables(sampleSet, sampleSetVariables, zIndex);
                    zIndex++;
                    if (zIndex % cacheSize == 0) {
                        flush();
                    }
                }
            }

            stopWatch.stop();

            logger.info("Successfully wrote mmd-file to '" + mmdFile.toAbsolutePath().toString() + "'");
            logger.info("Writing time: '" + stopWatch.getTimeDiffString());

        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw e;
        } finally {
            readerCache.close();
            close();
        }
    }

    @Override
    public void write(Array data, String variableName, int zIndex) {
        final Array target = getTarget(variableName);
        final Index index = target.getIndex();
        index.set(zIndex % writerConfig.getCacheSize());
        Array.arraycopy(data, 0, target, index.currentElement(), (int) data.getSize());
    }

    @Override
    public void write(int v, String variableName, int zIndex) {
        final Array data = NetCDFUtils.create(new int[][]{{v}});
        write(data, variableName, zIndex);
    }

    @Override
    public void write(float value, String variableName, int zIndex) {
        final Array data = NetCDFUtils.create(new float[][]{{value}});
        write(data, variableName, zIndex);
    }

    @Override
    public void write(String v, String variableName, int zIndex) {
        final int[] shape = getVariable(variableName).getShape();
        final char[] chars = new char[shape[1]];
        v.getChars(0, v.length(), chars, 0);
        final Array data = NetCDFUtils.create(new char[][]{chars});
        write(data, variableName, zIndex);
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

        netcdfFileWriter.addGroupAttribute(null, new Attribute(
                "sensor-names", getCommaSeparatedListOfSensors(useCaseConfig)
        ));
    }

    static String getCommaSeparatedListOfSensors(UseCaseConfig useCaseConfig) {
        final StringBuilder sensors = new StringBuilder();
        sensors.append(useCaseConfig.getPrimarySensor().getName());
        final List<Sensor> additionalSensors = useCaseConfig.getSecondarySensors();
        for (Sensor additionalSensor : additionalSensors) {
            sensors.append(",");
            sensors.append(additionalSensor.getName());
        }
        return sensors.toString();
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

    abstract void createNetCdfFileWriter(Path mmdFile) throws IOException;

    void initializeNetcdfFile(Path mmdFile, UseCaseConfig useCaseConfig, List<IOVariable> ioVariables, int numMatchups) throws IOException {
        createNetCdfFileWriter(mmdFile);

        createGlobalAttributes();
        createUseCaseAttributes(netcdfFileWriter, useCaseConfig);

        final List<Dimension> dimensions = useCaseConfig.getDimensions();
        createDimensions(dimensions, numMatchups);

        for (final IOVariable ioVariable : ioVariables) {
            if (ioVariable.hasCustomDimension()) {
                ucar.nc2.Dimension dimension = ioVariable.getCustomDimension();
                netcdfFileWriter.addDimension(null, dimension.getShortName(), dimension.getLength());
            }
            final Variable variable = netcdfFileWriter.addVariable(null,
                                                                   ioVariable.getTargetVariableName(),
                                                                   DataType.getType(ioVariable.getDataType()),
                                                                   ioVariable.getDimensionNames());
            final List<Attribute> attributes = ioVariable.getAttributes();
            for (Attribute attribute : attributes) {
                variable.addAttribute(attribute);
            }
            ensureCfConformUsageOf_units_Attribute(variable);
            if (variable.getDataType().isNumeric()) {
                final Attribute fillValueAtrribute = variable.findAttribute(NetCDFUtils.CF_FILL_VALUE_NAME);
                if (fillValueAtrribute == null) {
                    // @todo tb/** throw exception when the refactoring is finished 2017-03-17
                    logger.warning("Variable does not have a fill value: " + variable.getFullName());
                }
            }
        }
        netcdfFileWriter.create();
    }

    private void ensureCfConformUsageOf_units_Attribute(Variable variable) {
        final Attribute nonCfConformUnitAtt = variable.findAttribute("unit");
        if (nonCfConformUnitAtt != null) {
            final Attribute cfConformUnitsAtt = variable.findAttribute("units");
            if (cfConformUnitsAtt == null) {
                variable.addAttribute(new Attribute("units", nonCfConformUnitAtt.getStringValue()));
            }
        }
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

    private void writeSampleSetVariables(SampleSet sampleSet, List<SampleSetIOVariable> sampleSetVariables, int zIndex)
            throws IOException, InvalidRangeException {
        for (SampleSetIOVariable variable : sampleSetVariables) {
            variable.setSampleSet(sampleSet);
            variable.writeData(0, 0, null, zIndex);
        }
    }

    private void writeMmdValues(Sample sample, int zIndex, List<IOVariable> variables, Interval interval) throws IOException, InvalidRangeException {
        final int x = sample.getX();
        final int y = sample.getY();

        for (IOVariable variable : variables) {
            variable.writeData(x, y, interval, zIndex);
        }
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
            final String escapedName = NetCDFUtils.escapeVariableName(variableName);
            variableMap.put(variableName, netcdfFileWriter.findVariable(escapedName));
        }
        return variableMap.get(variableName);
    }

    private void createGlobalAttributes() {
        final Map<String, String> ga = writerConfig.getGlobalAttributes();

        final String title = ga.getOrDefault(GLOBAL_ATTR_TITLE, "FIDUCEO multi-sensor match-up dataset (MMD)");
        final String institution = ga.getOrDefault(GLOBAL_ATTR_INSTITUTION, "Brockmann Consult GmbH");
        final String contact = ga.getOrDefault(GLOBAL_ATTR_CONTACT, "Tom Block (tom.block@brockmann-consult.de)");
        final String licence = ga.getOrDefault(GLOBAL_ATTR_LICENSE, "This dataset is released for use under CC-BY licence and was developed in the EC FIDUCEO project \"Fidelity and Uncertainty in Climate Data Records from Earth Observations\". Grant Agreement: 638822.");

        addGlobalAttribute(GLOBAL_ATTR_TITLE, title);
        addGlobalAttribute(GLOBAL_ATTR_INSTITUTION, institution);
        addGlobalAttribute(GLOBAL_ATTR_CONTACT, contact);
        addGlobalAttribute(GLOBAL_ATTR_LICENSE, licence);
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
        netcdfFileWriter.addDimension(null, FiduceoConstants.FILE_NAME, 128);
        netcdfFileWriter.addDimension(null, FiduceoConstants.PROCESSING_VERSION, 30);
        netcdfFileWriter.addDimension(null, FiduceoConstants.MATCHUP_COUNT, numMatchups);
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
            final int matchupCount = variable.getShape(0);
            final int zStart = flushCount * cacheSize;
            final int restHeight = matchupCount - zStart;
            if (restHeight <= 0) {
                break;
            }
            Array dataToBeWritten = entry.getValue();
            final int[] origin = new int[dataToBeWritten.getRank()];
            if (zStart + cacheSize > matchupCount) {
                final int[] shape = dataToBeWritten.getShape();
                shape[0] = restHeight;
                dataToBeWritten = dataToBeWritten.sectionNoReduce(origin, shape, null);
            }
            origin[0] = zStart;
            netcdfFileWriter.write(variable, origin, dataToBeWritten);
        }
        flushCount++;
        if (netcdfFileWriter != null) {
            netcdfFileWriter.flush();
        }
    }
}
