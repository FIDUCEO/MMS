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
 */
package com.bc.fiduceo.post.plugin;

import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

public class SstInsituTimeSeries extends PostProcessing {

    public static final String D_8_D_8_NC = ".*_\\d{8}_\\d{8}.nc";

    public final String processingVersion;
    public final int timeRangeSeconds;
    public final int timeSeriesSize;
    private Variable fileNameVar;
    private String sensorType;
    private int filenameSize;

    public SstInsituTimeSeries(String processingVersion, int timeRangeSeconds, int timeSeriesSize) {
        this.processingVersion = processingVersion;
        this.timeRangeSeconds = timeRangeSeconds;
        this.timeSeriesSize = timeSeriesSize;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        sensorType = extractSensorType(reader);
        fileNameVar = getFileNameVariable(reader, sensorType);
        filenameSize = findDimensionMandatory(reader, "file_name").getLength();
        final String insituFileName = getInsituFileName(fileNameVar, 0, filenameSize);

        final SystemConfig systemConfig = getContext().getSystemConfig();
        final Reader insituReader = getInsituFileOpened(insituFileName, sensorType, processingVersion, systemConfig);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        getContext().getProcessingConfig().store(os);
        os.close();

        final List<Variable> variables = insituReader.getVariables();

        final int matchupCount = findDimensionMandatory(reader, "matchup_count").getLength();
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    static String getInsituFileName(Variable fileNameVar, int position, int filenameSize) throws IOException, InvalidRangeException {
        final Array nameArray = fileNameVar.read(new int[]{position, 0}, new int[]{1, filenameSize});
        final String insituFileName = String.valueOf((char[]) nameArray.getStorage()).trim();
        if (!insituFileName.matches(D_8_D_8_NC)) {
            throw new RuntimeException("The insitu file name '"+insituFileName+"' does not match the regular expression '" + D_8_D_8_NC + "'");
        }
        return insituFileName;
    }

    static Reader getInsituFileOpened(String insituFileName, String sensorType, String processingVersion, SystemConfig systemConfig) throws IOException {
        final Date[] startEnd = extractStarEndDateFromInsituFilename(insituFileName);
        final String geomType = systemConfig.getGeometryLibraryType();
        final ReaderFactory readerFactory = ReaderFactory.get(new GeometryFactory(geomType));
        final Reader insituReader = readerFactory.getReader(sensorType);

        final Archive archive = new Archive(systemConfig.getArchiveConfig());

        final Path[] paths = archive.get(startEnd[0], startEnd[1], processingVersion, sensorType);
        for (Path path : paths) {
            if (insituFileName.equals(path.getFileName().toString())) {
                insituReader.open(path.toFile());
            }
        }
        return insituReader;
    }

    static Date[] extractStarEndDateFromInsituFilename(String insituFileName) {
        final String[] strings = insituFileName.split("_");
        final String start = strings[strings.length - 2];
        final String end = strings[strings.length - 1].substring(0,8);
        final String pattern = "yyyyMMdd";
        final Date[] startEnd = new Date[2];
        startEnd[0] = TimeUtils.parse(start, pattern);
        startEnd[1] = TimeUtils.parse(end, pattern);
        return startEnd;
    }

    static Variable getFileNameVariable(NetcdfFile reader, final String sensorType) {
        final String fileNameVarName = sensorType + "_file_name";

        final Variable fileNameVar = reader.findVariable(fileNameVarName);
        if (fileNameVar == null) {
            throw new RuntimeException("Variable '" + fileNameVarName + "' does not exist.");
        }
        return fileNameVar;
    }

    static String extractSensorType(NetcdfFile reader) {
        final List<Variable> variables = reader.getVariables();
        for (Variable variable : variables) {
            final String shortName = variable.getShortName();
            final int insituIndex = shortName.indexOf("_insitu.");
            if (insituIndex > 0) {
                return shortName.substring(0, insituIndex);
            }
        }
        throw new RuntimeException("Unable to extract sensor type.");
    }

    static Dimension findDimensionMandatory(NetcdfFile reader, String dimName) {
        final Dimension dim = reader.findDimension(dimName);
        if (dim == null) {
            throw new RuntimeException("Dimension '" + dimName + "' does not exist.");
        }
        return dim;
    }
}
