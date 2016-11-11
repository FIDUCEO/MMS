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
package com.bc.fiduceo.reader.insitu;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.TimeLocator;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSTInsituReader implements Reader {

    public final long millisSince1978;
    public final int secondsSince1978;

    private NetcdfFile netcdfFile;
    private String insituType;
    private Map<String, Number> fillValueMap = new HashMap<>();
    private Map<String, Array> arrayMap = new HashMap<>();

    SSTInsituReader() {
        final Calendar calendar = ProductData.UTC.createCalendar();
        calendar.clear();
        calendar.set(1978, Calendar.JANUARY, 1);
        millisSince1978 = calendar.getTime().getTime();
        secondsSince1978 = (int) (millisSince1978 / 1000);
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        final List<Variable> variables = netcdfFile.getVariables();
        for (Variable variable : variables) {
            final String shortName = variable.getShortName();
            final Array array = variable.read();
            final Number fillValue = variable.findAttribute("_FillValue").getNumericValue();
            arrayMap.put(shortName, array);
            fillValueMap.put(shortName, fillValue);
        }
        insituType = netcdfFile.findGlobalAttribute("dataset").getStringValue();
    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }

        arrayMap.clear();
        fillValueMap.clear();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo info = new AcquisitionInfo();

        extractSensingTimes(info);
        info.setNodeType(NodeType.UNDEFINED);

        return info;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        return netcdfFile.getVariables();
    }

    public int getNumObservations() {
        return netcdfFile.findDimension("record").getLength();
    }

    @Override
    public String getRegEx() {
        return "insitu_[0-9][0-9]?_WMOID_[^_]+_[12][09]\\d{2}[01]\\d[0123]\\d_[12][09]\\d{2}[01]\\d[0123]\\d.nc";
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
        return (x, y) -> millisSince1978 + ((long) getTime(y)) * 1000L;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array sourceArray = arrayMap.get(variableName);
        final Number fillValue = fillValueMap.get(variableName);

        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();
        final int windowCenterX = windowWidth / 2;
        final int windowCenterY = windowHeight / 2;

        final int[] shape = {windowWidth, windowHeight};
        Array windowArray = Array.factory(sourceArray.getDataType(), shape);
        for (int y = 0; y < windowHeight; y++) {
            for (int x = 0; x < windowWidth; x++) {
                windowArray.setObject(windowWidth * y + x, fillValue);
            }
        }
        windowArray.setObject(windowWidth * windowCenterY + windowCenterX, sourceArray.getObject(centerY));
        return windowArray;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final Array acquisitionTime_1978 = readRaw(x, y, interval, "insitu.time");
        final int fillValue = fillValueMap.get("insitu.time").intValue();
        final Array acquisitionTime_1970 = Array.factory(acquisitionTime_1978.getDataType(), acquisitionTime_1978.getShape());
        for (int i = 0; i < acquisitionTime_1978.getSize(); i++) {
            final int time1978 = acquisitionTime_1978.getInt(i);
            if (time1978 != fillValue) {
                acquisitionTime_1970.setInt(i, time1978 + secondsSince1978);
            } else {
                acquisitionTime_1970.setInt(i, fillValue);
            }
        }
        return (ArrayInt.D2) acquisitionTime_1970;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return new Dimension("product_size", 1, getNumObservations());
    }

    /**
     * @return the insitu type as a String
     */
    String getInsituType() {
        return insituType;
    }

    /**
     * Returns the time in seconds since 1978-01-01
     *
     * @param y the y index
     *
     * @return the time in seconds since 1978-01-01
     */
    int getTime(int y) {
        // package access for testing only tb 2016-10-31
        return arrayMap.get("insitu.time").getInt(y);
    }

    private void extractSensingTimes(AcquisitionInfo info) {
        final Array timeArray = arrayMap.get("insitu.time");
        final int[] ints = (int[]) timeArray.getStorage();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int anInt : ints) {
            min = Math.min(anInt, min);
            max = Math.max(anInt, max);
        }
        info.setSensingStart(new Date(millisSince1978 + (long) min * 1000));
        info.setSensingStop(new Date(millisSince1978 + (long) max * 1000));
    }
}
