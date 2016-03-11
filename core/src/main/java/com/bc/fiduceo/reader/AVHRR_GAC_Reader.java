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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.SwathPixelLocator;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.math.CosineDistance;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;

class AVHRR_GAC_Reader implements Reader {

    private static final int NUM_SPLITS = 2;
    private static final String START_TIME_ATTRIBUTE_NAME = "start_time";
    private static final String STOP_TIME_ATTRIBUTE_NAME = "stop_time";

    private NetcdfFile netcdfFile;
    private BoundingPolygonCreator boundingPolygonCreator;
    private GeometryFactory geometryFactory;
    private ArrayCache arrayCache;
    private SwathPixelLocator pixelLocator;

    // package access for testing only tb 2016-03-02
    static Date parseDateAttribute(Attribute timeAttribute) throws IOException {
        if (timeAttribute == null) {
            throw new IOException("required global attribute '" + START_TIME_ATTRIBUTE_NAME + "' not present");
        }
        final String startTimeString = timeAttribute.getStringValue();
        if (StringUtils.isNullOrEmpty(startTimeString)) {
            throw new IOException("required global attribute '" + START_TIME_ATTRIBUTE_NAME + "' contains no data");
        }
        return TimeUtils.parse(startTimeString, "yyyyMMdd'T'HHmmss'Z'");
    }

    // package access for testing only tb 2016-03-03
    static void checkForValidity(GeometryCollection boundingGeometry) {
        final Geometry[] geometries = boundingGeometry.getGeometries();
        for (final Geometry geometry : geometries) {
            if (!geometry.isValid()) {
                throw new RuntimeException("Invalid geometry detected");
            }
        }
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

        final Date startDate = parseDateAttribute(netcdfFile.findGlobalAttribute(START_TIME_ATTRIBUTE_NAME));
        acquisitionInfo.setSensingStart(startDate);

        final Date stopDate = parseDateAttribute(netcdfFile.findGlobalAttribute(STOP_TIME_ATTRIBUTE_NAME));
        acquisitionInfo.setSensingStop(stopDate);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = calculateGeometries();
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        acquisitionInfo.setSubsetHeight(geometries.getSubsetHeight());

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


        return acquisitionInfo;
    }

    private Geometries calculateGeometries() throws IOException {
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        final Geometries geometries = new Geometries();

        final Array longitudes = arrayCache.get("lon");
        final Array latitudes = arrayCache.get("lat");
        Geometry timeAxisGeometry;
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitudes, latitudes);
        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, NUM_SPLITS);
            final int height = longitudes.getShape()[0];
            geometries.setSubsetHeight(boundingPolygonCreator.getSubsetHeight(height, NUM_SPLITS));
            checkForValidity((GeometryCollection) boundingGeometry);
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, NUM_SPLITS);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        return geometries;
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            final GeometryFactory geometryFactory = getGeometryFactory();

            // @todo 2 tb/tb move intervals to config 2016-03-02
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(40, 100), geometryFactory);
        }

        return boundingPolygonCreator;
    }

    private GeometryFactory getGeometryFactory() {
        if (geometryFactory == null) {
            // @todo 1 tb/tb inject geometry factory 2016-03-02
            geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        }

        return geometryFactory;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final ArrayFloat lonStorage = (ArrayFloat) arrayCache.get("lon");
            final ArrayFloat latStorage = (ArrayFloat) arrayCache.get("lat");
            final int[] shape = lonStorage.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = new SwathPixelLocator(lonStorage, latStorage, width, height);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        final ArrayFloat longitudes = (ArrayFloat) arrayCache.get("lon");
        final int[] shape = longitudes.getShape();

        final int height = shape[0];
        final int sh = getBoundingPolygonCreator().getSubsetHeight(height, NUM_SPLITS);
        final int sh2 = sh / 2;

        final int width = shape[1];
        final double centerX = width / 2 + 0.5;

        final Point centroid = sceneGeometry.getCentroid();
        final PixelLocator pixelLocator = getPixelLocator();
        final Point2D g1 = pixelLocator.getGeoLocation(centerX, sh2 + 0.5, null);
        final Point2D g2 = pixelLocator.getGeoLocation(centerX, sh2 + sh + 0.5, null);
        final CosineDistance cd1 = new CosineDistance(g1.getX(), g1.getY());
        final CosineDistance cd2 = new CosineDistance(g2.getX(), g2.getY());
        final double cLon = centroid.getLon();
        final double cLat = centroid.getLat();
        final double d1 = cd1.distance(cLon, cLat);
        final double d2 = cd2.distance(cLon, cLat);
        final int minY;
        final int maxY;
        if (d1 < d2) {
            minY = 0;
            maxY = sh - 1;
        } else {
            minY = sh - 1;
            maxY = height - 1;
        }
        return new PixelLocator() {
            @Override
            public Point2D getGeoLocation(double x, double y, Point2D g) {
                return pixelLocator.getGeoLocation(x, y, g);
            }

            @Override
            public Point2D[] getPixelLocation(double lon, double lat) {
                final Point2D[] pixelLocation = pixelLocator.getPixelLocation(lon, lat);
                if (pixelLocation.length > 1) {
                    final Point2D p0 = pixelLocation[0];
                    final int y = (int) Math.round(p0.getY());
                    if (y >= minY && y <= maxY) {
                        return new Point2D[]{p0};
                    } else {
                        return new Point2D[]{pixelLocation[1]};
                    }
                } else {
                    return pixelLocation;
                }
            }
        };
    }

    @Override
    public String getRegEx() {
        return "[0-9]{14}-ESACCI-L1C-AVHRR([0-9]{2}|MTA)_G-fv\\d\\d.\\d.nc";
    }

    private class Geometries {

        private Geometry boundingGeometry;
        private Geometry timeAxesGeometry;
        private Integer subsetHeight;

        public Geometry getBoundingGeometry() {
            return boundingGeometry;
        }

        public void setBoundingGeometry(Geometry boundingGeometry) {
            this.boundingGeometry = boundingGeometry;
        }

        public Geometry getTimeAxesGeometry() {
            return timeAxesGeometry;
        }

        public void setTimeAxesGeometry(Geometry timeAxesGeometry) {
            this.timeAxesGeometry = timeAxesGeometry;
        }

        public Integer getSubsetHeight() {
            return subsetHeight;
        }

        public void setSubsetHeight(Integer subsetHeight) {
            this.subsetHeight = subsetHeight;
        }
    }
}
