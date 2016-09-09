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

package com.bc.fiduceo.reader.ssmt2;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class SSMT2_Reader implements Reader {

    private static final String START_DATE_UTC_NAME = "start_date_UTC";
    private static final String START_TIME_UTC_NAME = "start_time_UTC";
    private static final String END_DATE_UTC_NAME = "end_date_UTC";
    private static final String END_TIME_UTC_NAME = "end_time_UTC";

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private static final String REG_EX = "F(11|12|14|15)[0-9]{12}.nc";

    private final GeometryFactory geometryFactory;

    private NetcdfFile netcdfFile;
    private ArrayCache arrayCache;
    private BoundingPolygonCreator boundingPolygonCreator;

    public SSMT2_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);
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
        setNodeType(acquisitionInfo);
        setGeometries(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
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
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Array lonArray = arrayCache.get("lon");
        final int[] shape = lonArray.getShape();
        return new Dimension("lon", shape[1], shape[0]);
    }

    // package access for testing only tb 2016-09-09
    static String assembleDateString(String startDateString, String startTimeString) {
        return startDateString + "T" + startTimeString.substring(0, startTimeString.length() - 7);
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        final String startDateString = NetCDFUtils.getGlobalAttributeString(START_DATE_UTC_NAME, netcdfFile);
        final String startTimeString = NetCDFUtils.getGlobalAttributeString(START_TIME_UTC_NAME, netcdfFile);
        final String endDateString = NetCDFUtils.getGlobalAttributeString(END_DATE_UTC_NAME, netcdfFile);
        final String endTimeString = NetCDFUtils.getGlobalAttributeString(END_TIME_UTC_NAME, netcdfFile);
        try {
            final String startUTCString = assembleDateString(startDateString, startTimeString);
            final ProductData.UTC startDateUTC = ProductData.UTC.parse(startUTCString, DATE_PATTERN);
            acquisitionInfo.setSensingStart(startDateUTC.getAsDate());

            final String endUTCString = assembleDateString(endDateString, endTimeString);
            final ProductData.UTC endDateUTC = ProductData.UTC.parse(endUTCString, DATE_PATTERN);
            acquisitionInfo.setSensingStop(endDateUTC.getAsDate());
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }

    private void setNodeType(AcquisitionInfo acquisitionInfo) throws IOException {
        final String startDirection = NetCDFUtils.getGlobalAttributeString("start_direction", netcdfFile);
        if ("ascending".equals(startDirection)) {
            acquisitionInfo.setNodeType(NodeType.ASCENDING);
        } else if ("descending".equals(startDirection)) {
            acquisitionInfo.setNodeType(NodeType.DESCENDING);
        } else {
            acquisitionInfo.setNodeType(NodeType.UNDEFINED);
        }
    }

    private void setGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final Array lonArray = arrayCache.get("lon");
        final Array latArray = arrayCache.get("lat");
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(lonArray, latArray);
        if (!boundingGeometry.isValid()) {
            throw new RuntimeException("Must split geometries");
        }
        acquisitionInfo.setBoundingGeometry(boundingGeometry);

        final Geometries geometries = new Geometries();
        geometries.setBoundingGeometry(boundingGeometry);

        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(lonArray, latArray);

        geometries.setTimeAxesGeometry(timeAxisGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries, geometryFactory);
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            // @todo 2 tb/tb move intervals to config 2016-03-02
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(5, 25), geometryFactory);
        }

        return boundingPolygonCreator;
    }
}
