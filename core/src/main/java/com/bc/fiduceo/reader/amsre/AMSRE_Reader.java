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

package com.bc.fiduceo.reader.amsre;


import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.TimeLocator;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

class AMSRE_Reader implements Reader {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private NetcdfFile netcdfFile;

    AMSRE_Reader(GeometryFactory geometryFactory) {

    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        setSensingTimes(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new RuntimeException("not implemenetd");
    }

    // Package access for testing only tb 2016-09-02
    static String assembleUTCString(String dateString, String timeString) {
        String startDateString = dateString + "T" + timeString;
        final int lastDotIndex = startDateString.lastIndexOf('.');
        startDateString = startDateString.substring(0, lastDotIndex);
        return startDateString;
    }

    // Package access for testing only tb 2016-09-02
    static ProductData.UTC getUtcData(Attribute rangeBeginningDateAttribute, Attribute rangeBeginningTimeAttribute) throws IOException {
        try {
            final String startDateString = assembleUTCString(rangeBeginningDateAttribute.getStringValue(), rangeBeginningTimeAttribute.getStringValue());
            return ProductData.UTC.parse(startDateString, DATE_PATTERN);
        } catch (ParseException | IndexOutOfBoundsException e) {
            throw new IOException(e.getMessage());
        }
    }

    private Attribute getGlobalAttributeSafe(String attributeName) {
        final Attribute globalAttribute = netcdfFile.findGlobalAttribute(attributeName);
        if (globalAttribute == null) {
            throw new RuntimeException("Required attribute not present in file: " + attributeName);
        }
        return globalAttribute;
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        final Attribute rangeBeginningDateAttribute = getGlobalAttributeSafe("RangeBeginningDate");
        final Attribute rangeBeginningTimeAttribute = getGlobalAttributeSafe("RangeBeginningTime");
        final Attribute rangeEndingDateAttribute = getGlobalAttributeSafe("RangeEndingDate");
        final Attribute rangeEndingTimeAttribute = getGlobalAttributeSafe("RangeEndingTime");

        final ProductData.UTC sensingStart = getUtcData(rangeBeginningDateAttribute, rangeBeginningTimeAttribute);
        acquisitionInfo.setSensingStart(sensingStart.getAsDate());

        final ProductData.UTC sensingStop = getUtcData(rangeEndingDateAttribute, rangeEndingTimeAttribute);
        acquisitionInfo.setSensingStop(sensingStop.getAsDate());
    }
}
