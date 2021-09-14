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
package com.bc.fiduceo.reader.insitu.sst_cci;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.insitu.UniqueIdVariable;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bc.fiduceo.reader.insitu.InsituUtils.getResultArray;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.TimeUtils.millisSince1978;
import static com.bc.fiduceo.util.TimeUtils.secondsSince1978;

public class SSTInsituReader extends NetCDFReader {

    private final Map<String, Number> fillValueMap = new HashMap<>();
    private final Map<String, Array> arrayMap = new HashMap<>();

    private List<Variable> variables;

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        variables = netcdfFile.getVariables();
        for (Variable variable : variables) {
            final String shortName = variable.getShortName();
            final Array array = variable.read();
            final Number fillValue = variable.findAttribute(CF_FILL_VALUE_NAME).getNumericValue();
            arrayMap.put(shortName, array);
            fillValueMap.put(shortName, fillValue);
        }

        addIdVariableAndData();
    }

    @Override
    public void close() throws IOException {
        variables = null;

        arrayMap.clear();
        fillValueMap.clear();

        super.close();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo info = new AcquisitionInfo();

        extractSensingTimes(info);
        info.setNodeType(NodeType.UNDEFINED);

        return info;
    }

    @Override
    public List<Variable> getVariables() {
        return variables;
    }

    @Override
    public String getRegEx() {
        return "insitu_[0-9][0-9]?_WMOID_[^_]+_[12][09]\\d{2}[01]\\d[0123]\\d_[12][09]\\d{2}[01]\\d[0123]\\d.nc";
    }

    @Override
    public String getLongitudeVariableName() {
        return "insitu.lon";
    }

    @Override
    public String getLatitudeVariableName() {
        return "insitu.lat";
    }

    @Override
    public PixelLocator getPixelLocator() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() {
        return (x, y) -> millisSince1978 + ((long) getTime(y)) * 1000L;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        return new int[3];
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) {
        final Array sourceArray = arrayMap.get(variableName);
        final Number fillValue = fillValueMap.get(variableName);

        return getResultArray(centerY, interval, sourceArray, fillValue);
    }

    public Array getSourceArray(String variableName) {
        return arrayMap.get(variableName);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) {
        return readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) {
        final Array acquisitionTime_1978 = readRaw(x, y, interval, "insitu.time");
        final int fillValue = fillValueMap.get("insitu.time").intValue();
        final int targetFillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        final Array acquisitionTime_1970 = Array.factory(acquisitionTime_1978.getDataType(), acquisitionTime_1978.getShape());
        for (int i = 0; i < acquisitionTime_1978.getSize(); i++) {
            final int time1978 = acquisitionTime_1978.getInt(i);
            if (time1978 != fillValue) {
                acquisitionTime_1970.setInt(i, time1978 + secondsSince1978);
            } else {
                acquisitionTime_1970.setInt(i, targetFillValue);
            }
        }
        return (ArrayInt.D2) acquisitionTime_1970;
    }

    @Override
    public Dimension getProductSize() {
        return new Dimension("product_size", 1, getNumObservations());
    }

    /**
     * Returns the time in seconds since 1978-01-01
     *
     * @param y the y index
     * @return the time in seconds since 1978-01-01
     */
    int getTime(int y) {
        // package access for testing only tb 2016-10-31
        return arrayMap.get("insitu.time").getInt(y);
    }

    int getNumObservations() {
        return NetCDFUtils.getDimensionLength("record", netcdfFile);
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
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();

        utcCalendar.setTimeInMillis(millisSince1978 + (long) min * 1000);
        info.setSensingStart(utcCalendar.getTime());

        utcCalendar.setTimeInMillis(millisSince1978 + (long) max * 1000);
        info.setSensingStop(utcCalendar.getTime());
    }

    private void addIdVariableAndData() {
        final UniqueIdVariable uniqueIdVariable = new UniqueIdVariable("insitu.id");
        variables.add(uniqueIdVariable);

        final Number fillValue = fillValueMap.get("insitu.mohc_id");
        fillValueMap.put(uniqueIdVariable.getShortName(), fillValue);

        final Array idArray = createIdArray(arrayMap.get("insitu.mohc_id"), arrayMap.get("insitu.time"), fillValue.intValue());
        arrayMap.put(uniqueIdVariable.getShortName(), idArray);
    }

    static Array createIdArray(Array mohc_idArray, Array timeArray, int fillValue) {
        final int[] shape = mohc_idArray.getShape();
        final Array idArray = Array.factory(DataType.LONG, shape);

        final Calendar utcCalendar = TimeUtils.getUTCCalendar();

        for (int i = 0; i < shape[0]; i++) {
            final int mohc_id = mohc_idArray.getInt(i);
            if (mohc_id == fillValue) {
                idArray.setLong(i, fillValue);
                continue;
            }

            final int time = timeArray.getInt(i);
            final long utcTime = millisSince1978 + (long) time * 1000;
            utcCalendar.setTimeInMillis(utcTime);
            final int year = utcCalendar.get(Calendar.YEAR);
            final int month = utcCalendar.get(Calendar.MONTH) + 1;

            final int year_month = month + year * 100;
            final long uniqueId = (long) mohc_id + (long) year_month * 10000000000L;
            idArray.setLong(i, uniqueId);
        }

        return idArray;
    }
}
