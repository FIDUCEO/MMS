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

package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.hdf.HdfEOSUtil;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

class MxD06_Reader implements Reader {

    private static final String REG_EX = "M([OY])D06_L2.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";

    private static final String GEOLOCATION_GROUP = "mod06/Geolocation_Fields";

    private final GeometryFactory geometryFactory;
    private NetcdfFile netcdfFile;
    private ArrayCache arrayCache;

    MxD06_Reader(GeometryFactory geometryFactory) {
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

        extractAcquisitionTimes(acquisitionInfo);
        extractGeometries(acquisitionInfo);
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

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
        final Array time = arrayCache.get("mod06/Data_Fields", "Scan_Start_Time");
        final int[] offsets = new int[] {0, 0};
        final int[] shape = time.getShape();
        shape[1] = 1;
        try {
            final Array section = time.section(offsets, shape);
            return new TimeLocator_TAI1993Vector(section);
        } catch (InvalidRangeException e) {
           throw new IOException(e.getMessage());
        }
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
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Array longitude = arrayCache.get(GEOLOCATION_GROUP, "Longitude");
        final int[] shape = longitude.getShape();
        return new Dimension("shape", shape[1], shape[0]);
    }

    private void extractAcquisitionTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        final Group rootGroup = netcdfFile.getRootGroup();
        final String coreMetaString = HdfEOSUtil.getEosMetadata("CoreMetadata.0", rootGroup);
        final Element eosElement = HdfEOSUtil.getEosElement(coreMetaString);
        final String rangeBeginDateElement = HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_BEGINNING_DATE);
        final String rangeBeginTimeElement = HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_BEGINNING_TIME);
        final Date sensingStart = HdfEOSUtil.parseDate(rangeBeginDateElement, rangeBeginTimeElement);
        acquisitionInfo.setSensingStart(sensingStart);

        final String rangeEndDateElement = HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_ENDING_DATE);
        final String rangeEndTimeElement = HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_ENDING_TIME);
        final Date sensingStop = HdfEOSUtil.parseDate(rangeEndDateElement, rangeEndTimeElement);
        acquisitionInfo.setSensingStop(sensingStop);
    }

    private void extractGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(new Interval(50, 50), geometryFactory);
        final Array longitude = arrayCache.get(GEOLOCATION_GROUP, "Longitude");
        final Array latitude = arrayCache.get(GEOLOCATION_GROUP, "Latitude");
        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitude, latitude);
        if (!boundingGeometry.isValid()) {
            throw new RuntimeException("Detected invalid bounding geometry");
        }
        acquisitionInfo.setBoundingGeometry(boundingGeometry);

        final Geometries geometries = new Geometries();
        geometries.setBoundingGeometry(boundingGeometry);
        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitude, latitude);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries, geometryFactory);
    }
}
