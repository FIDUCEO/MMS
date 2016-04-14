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

package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AMSUB_MHS_L1C_Reader implements Reader {

    private static final String GEOLOCATION_GROUP_NAME = "Geolocation";
    private static final int NUM_SPLITS = 2;

    private NetcdfFile netcdfFile;

    private ArrayCache arrayCache;
    private TimeLocator timeLocator;
    private PixelLocator pixelLocator;
    private GeometryFactory geometryFactory;
    private BoundingPolygonCreator boundingPolygonCreator;

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);
        timeLocator = null;
    }

    @Override
    public void close() throws IOException {
        timeLocator = null;
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        final int startYear = getGlobalAttributeAsInteger("startdatayr");
        final int startDay = getGlobalAttributeAsInteger("startdatady");
        final int startTime = getGlobalAttributeAsInteger("startdatatime_ms");

        final int endYear = getGlobalAttributeAsInteger("enddatayr");
        final int endDay = getGlobalAttributeAsInteger("enddatady");
        final int endTime = getGlobalAttributeAsInteger("enddatatime_ms");

        final Date sensingStart = getDate(startYear, startDay, startTime);
        acquisitionInfo.setSensingStart(sensingStart);

        final Date sensingStop = getDate(endYear, endDay, endTime);
        acquisitionInfo.setSensingStop(sensingStop);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = extractGeometries();
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());

        setTimeAxes(acquisitionInfo, sensingStart, sensingStop, geometries);

        return acquisitionInfo;
    }


    @Override
    public String getRegEx() {
        return "'?[A-Z].+[AMBX|MHSX].+[NK|M1].D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[GC|WI].h5";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array longitudes = arrayCache.getScaled(GEOLOCATION_GROUP_NAME, "Longitude", "Scale", null);
            final Array latitudes = arrayCache.getScaled(GEOLOCATION_GROUP_NAME, "Latitude", "Scale", null);

            final int[] shape = longitudes.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = PixelLocatorFactory.getSwathPixelLocator(toFloat(longitudes), toFloat(latitudes), width, height);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneIndex) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            final Array scnlinyr = arrayCache.get("Data", "scnlinyr");
            final Array scnlindy = arrayCache.get("Data", "scnlindy");
            final Array scnlintime = arrayCache.get("Data", "scnlintime");

            timeLocator = new AMSUB_MHS_TimeLocator(scnlinyr, scnlindy, scnlintime);
        }
        return timeLocator;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Variable> getVariables() {
        throw new RuntimeException("not implemented");
    }

    // package access for testing only tb 2016-04-12
    static Date getDate(int year, int dayOfYear, int millisecsInDay) {
        final Calendar calendar = TimeUtils.getUTCCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.MILLISECOND, millisecsInDay);
        return calendar.getTime();
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            final GeometryFactory geometryFactory = getGeometryFactory();

            // @todo 2 tb/tb move intervals to config 2016-04-12
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(10, 40), geometryFactory);
        }

        return boundingPolygonCreator;
    }

    private GeometryFactory getGeometryFactory() {
        if (geometryFactory == null) {
            // @todo 1 tb/tb inject geometry factory 2016-04-12
            geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        }

        return geometryFactory;
    }

    private int getGlobalAttributeAsInteger(String attributeName) throws IOException {
        final Attribute attribute = netcdfFile.findGlobalAttribute(attributeName);
        if (attribute == null) {
            throw new IOException("Global attribute '" + attributeName + "' not found.");
        }
        return attribute.getNumericValue().intValue();
    }

    // @todo 3 tb/tb duplicated code - refactor and move to common reader helper class 2016-04-12
    private void setTimeAxes(AcquisitionInfo acquisitionInfo, Date startDate, Date stopDate, Geometries geometries) {
        final Geometry timeAxesGeometry = geometries.getTimeAxesGeometry();
        if (timeAxesGeometry instanceof GeometryCollection) {
            final GeometryCollection axesCollection = (GeometryCollection) timeAxesGeometry;
            final Geometry[] axesGeometries = axesCollection.getGeometries();
            final TimeAxis[] timeAxes = new TimeAxis[axesGeometries.length];
            final TimeInterval timeInterval = new TimeInterval(startDate, stopDate);
            final TimeInterval[] timeSplits = timeInterval.split(axesGeometries.length);
            for (int i = 0; i < axesGeometries.length; i++) {
                final LineString axisGeometry = (LineString) axesGeometries[i];
                final TimeInterval currentTimeInterval = timeSplits[i];
                timeAxes[i] = geometryFactory.createTimeAxis(axisGeometry, currentTimeInterval.getStartTime(), currentTimeInterval.getStopTime());
            }
            acquisitionInfo.setTimeAxes(timeAxes);
        } else {
            final TimeAxis timeAxis = geometryFactory.createTimeAxis((LineString) timeAxesGeometry, startDate, stopDate);
            acquisitionInfo.setTimeAxes(new TimeAxis[]{timeAxis});
        }
    }

    private Geometries extractGeometries() throws IOException {
        final Geometries geometries = new Geometries();

        final Array longitudes = arrayCache.getScaled(GEOLOCATION_GROUP_NAME, "Longitude", "Scale", null);
        final Array latitudes = arrayCache.getScaled(GEOLOCATION_GROUP_NAME, "Latitude", "Scale", null);

        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        Geometry timeAxisGeometry;

        // AMSU-B scans from west to east on the ascending node. Thus we have to run clockwise around the lon/lat arrays to
        // have the correct inside/outside relation on the polygons tb 2016-04-12
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometryClockwise(longitudes, latitudes);
        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, NUM_SPLITS, true);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }

            final int height = longitudes.getShape()[0];
            geometries.setSubsetHeight(boundingPolygonCreator.getSubsetHeight(height, NUM_SPLITS));
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, NUM_SPLITS);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);

        return geometries;
    }

    private Array toFloat(Array original) {
        final Array floatArray = Array.factory(Float.class, original.getShape());
        MAMath.copyFloat(floatArray, original);
        return floatArray;
    }

}
