/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.insitu.ocean_rain;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import com.bc.fiduceo.util.VariableProxy;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;
import static com.bc.fiduceo.util.TimeUtils.millisSince1978;

public class OceanRainInsituReader implements Reader {

    private static final int LINE_SIZE = 64;
    private static final String WHITESPACE_REGEXP = "\\s{1,}";  // means: one or more whitespace characters tb 2017-08-07

    private final HashMap<String, LineDecoder> decoder;
    private FileInputStream inputStream;
    private int numLines;
    private FileChannel channel;
    private List<Variable> variableList;

    OceanRainInsituReader() {
        decoder = new HashMap<>();
        decoder.put("lon", new LineDecoder.Lon());
        decoder.put("lat", new LineDecoder.Lat());
        decoder.put("time", new LineDecoder.Time());
        decoder.put("sst", new LineDecoder.Sst());
    }

    @Override
    public void open(File file) throws IOException {
        inputStream = new FileInputStream(file);

        channel = inputStream.getChannel();
        final long fileSize = channel.size();
        numLines = (int)(fileSize / LINE_SIZE);
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            channel = null;
            inputStream.close();
            inputStream = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        Line line = readLine(0);
        acquisitionInfo.setSensingStart(TimeUtils.create(line.getTime() * 1000L));

        line = readLine(numLines - 1);
        acquisitionInfo.setSensingStop(TimeUtils.create(line.getTime() * 1000L));

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "OceanRAIN_allships_2010-2017_SST.ascii";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        return new OceanRainTimeLocator(this);
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Variable variable = findVariable(variableName);
        final Number fillValue = NetCDFUtils.getFillValue(variable);
        final LineDecoder lineDecoder = decoder.get(variableName);// @todo 1 tb/tb encapsulate and throw on errors 2017-08-08

        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();
        final int windowCenterX = windowWidth / 2;
        final int windowCenterY = windowHeight / 2;

        final int[] shape = {windowWidth, windowHeight};
        final Array windowArray = Array.factory(variable.getDataType(), shape);
        for (int y = 0; y < windowHeight; y++) {
            for (int x = 0; x < windowWidth; x++) {
                windowArray.setObject(windowWidth * y + x, fillValue);
            }
        }

        final Line line = readLine(centerY);
        windowArray.setObject(windowWidth * windowCenterY + windowCenterX, lineDecoder.get(line));

        return windowArray;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        return (ArrayInt.D2) readRaw(x, y, interval, "time");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        ensureVariableListExists();

        return variableList;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return new Dimension("product_size", 1, numLines);
    }

    // package access for testing only tb 2017-08-07
    static Line decode(byte[] lineBuffer) {
        final String lineString = new String(lineBuffer);
        final String[] tokens = lineString.split(WHITESPACE_REGEXP);

        final int time = Integer.parseInt(tokens[3]);
        final float lat = Float.parseFloat(tokens[4]);
        final float lon = Float.parseFloat(tokens[5]);
        final float sst = Float.parseFloat(tokens[6]);

        return new Line(lon, lat, time, sst);
    }

    private Line readLine(long lineNumber) throws IOException {
        final byte[] lineBuffer = new byte[LINE_SIZE];
        channel.position(lineNumber * LINE_SIZE);

        final int numBytes = inputStream.read(lineBuffer);
        if (numBytes != LINE_SIZE) {
            throw new IOException("Could not read full buffer!");
        }
        return decode(lineBuffer);
    }

    private Variable findVariable(String name) {
        ensureVariableListExists();
        for (final Variable variable : variableList) {
            if (name.equalsIgnoreCase(variable.getShortName())) {
                return variable;
            }
        }
        throw new RuntimeException("Variable not contained in file: " + name);
    }

    private void ensureVariableListExists() {
        if (variableList == null) {
            createVariables();
        }
    }

    private void createVariables() {
        variableList = new ArrayList<>();

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_east"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "longitude"));
        variableList.add(new VariableProxy("lon", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_north"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "latitude"));
        variableList.add(new VariableProxy("lat", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "Seconds since 1970-01-01"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(int.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "time"));
        variableList.add(new VariableProxy("time", DataType.INT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "celsius"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_surface_temperature"));
        variableList.add(new VariableProxy("sst", DataType.FLOAT, attributes));
    }
}
