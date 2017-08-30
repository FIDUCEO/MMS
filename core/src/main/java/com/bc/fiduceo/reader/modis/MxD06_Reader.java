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
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.TimeLocator_TAI1993Vector;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class MxD06_Reader implements Reader {

    private static final String REG_EX = "M([OY])D06_L2.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";

    private static final String GEOLOCATION_GROUP = "mod06/Geolocation_Fields";
    private static final String DATA_GROUP = "mod06/Data_Fields";

    private final GeometryFactory geometryFactory;
    private NetcdfFile netcdfFile;
    private ArrayCache arrayCache;
    private TimeLocator timeLocator;

    MxD06_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;

    }

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
    public String getLongitudeVariableName() {
        return "Longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "Latitude";
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
        if (timeLocator == null) {
            createTimeLocator();
        }

        return timeLocator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final String groupName = getGroupName(variableName);
        final Array array = arrayCache.get(groupName, variableName);
        final Number fillValue = arrayCache.getNumberAttributeValue(NetCDFUtils.CF_FILL_VALUE_NAME, groupName, variableName);
        if (fillValue == null) {
            throw new RuntimeException("implement fill value handling here.");
        }

        if (is1KmVariable(array)) {
            return readRaw1km(centerX, centerY, interval, array, fillValue);
        } else {
            return RawDataReader.read(centerX, centerY, interval, fillValue, array, 270);
        }
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final int height = interval.getY();
        final int width = interval.getX();
        final int y_offset = y - height / 2;
        int[] shape = new int[]{height, width};

        final TimeLocator timeLocator = getTimeLocator();

        final Array acquisitionTime = Array.factory(DataType.INT, shape);
        final Index index = acquisitionTime.getIndex();

        for (int ya = 0; ya < height; ya++) {
            final int yRead = y_offset + ya;
            final long lineTime = timeLocator.getTimeFor(0, yRead);
            final int lineTimeInSeconds = (int) (lineTime / 1000);

            for (int xa = 0; xa < width; xa++) {
                index.set(ya, xa);
                acquisitionTime.setInt(index, lineTimeInSeconds);
            }
        }
        return (ArrayInt.D2) acquisitionTime;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        final List<Variable> variablesInFile = netcdfFile.getVariables();

        final ArrayList<Variable> exportVariables = new ArrayList<>();
        for (Variable variable : variablesInFile) {
            final String variableName = variable.getShortName();
            if (variableName.contains("StructMetadata") ||
                    variableName.contains("CoreMetadata") ||
                    variableName.contains("Statistics_1km") ||
                    variableName.contains("Cell_Across_Swath_1km") ||
                    variableName.contains("Cell_Along_Swath_1km") ||
                    variableName.contains("Band_Number") ||
                    variableName.contains("Brightness_Temperature") ||
                    variableName.contains("Spectral_Cloud_Forcing") ||
                    variableName.contains("Cloud_Top_Pressure_From_Ratios") ||
                    variableName.contains("Cloud_Mask_1km") ||
                    variableName.contains("Extinction_Efficiency_Ice") ||
                    variableName.contains("Asymmetry_Parameter_Ice") ||
                    variableName.contains("Single_Scatter_Albedo_Ice") ||
                    variableName.contains("Extinction_Efficiency_Liq") ||
                    variableName.contains("Asymmetry_Parameter_Liq") ||
                    variableName.contains("Single_Scatter_Albedo_Liq") ||
                    variableName.contains("Cloud_Mask_SPI") ||
                    variableName.contains("Retrieval_Failure_Metric") ||
                    variableName.contains("Atm_Corr_Refl") ||
                    variableName.contains("Quality_Assurance_1km") ||
                    variableName.contains("ArchiveMetadata")) {
                continue;
            }

            if (variableName.equals("Cloud_Mask_5km")) {
                final List<Attribute> attributes = variable.getAttributes();
                variable = new VariableProxy(variable.getShortName(), DataType.SHORT, attributes);
            }

            if (variableName.equals("Quality_Assurance_5km")) {
                final List<Attribute> attributes = variable.getAttributes();
                variable = new VariableProxy("Quality_Assurance_5km_03", DataType.BYTE, attributes);
                exportVariables.add(variable);

                variable = new VariableProxy("Quality_Assurance_5km_04", DataType.BYTE, attributes);
                exportVariables.add(variable);

                variable = new VariableProxy("Quality_Assurance_5km_05", DataType.BYTE, attributes);
                exportVariables.add(variable);

                variable = new VariableProxy("Quality_Assurance_5km_09", DataType.BYTE, attributes);
            }

            exportVariables.add(variable);
        }

        return exportVariables;
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
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);
    }

    private void createTimeLocator() throws IOException {
        final Array time = arrayCache.get("mod06/Data_Fields", "Scan_Start_Time");
        final int[] offsets = new int[]{0, 0};
        final int[] shape = time.getShape();
        shape[1] = 1;
        try {
            final Array section = time.section(offsets, shape);
            timeLocator = new TimeLocator_TAI1993Vector(section);
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }
    }

    // package access for testing only tb 2017-08-28
    static String getGroupName(String variableName) {
        if ("Longitude".equals(variableName) || "Latitude".equals(variableName)) {
            return GEOLOCATION_GROUP;
        }
        return DATA_GROUP;
    }

    static boolean is1KmVariable(Array array) {
        final int[] shape = array.getShape();
        int maxDim = Integer.MIN_VALUE;
        for (int dimLength : shape) {
            if (dimLength > maxDim) {
                maxDim = dimLength;
            }
        }

        return maxDim > 1000;
    }

    private Array readRaw1km(int centerX, int centerY, Interval interval, Array array, Number fillValue) throws InvalidRangeException {
        final int x_1km = centerX * 5 + 2;
        final int y_1km = centerY * 5 + 2;
        final Interval extendedInterval = new Interval(interval.getX() * 5, interval.getY() * 5);
        final Array fullArray = RawDataReader.read(x_1km, y_1km, extendedInterval, fillValue, array, 1354);

        final int[] shape = new int[]{interval.getY(), interval.getX()};
        final int[] origin = new int[]{2, 2};
        final int[] stride = new int[]{5, 5};
        return fullArray.section(origin, shape, stride);
    }
}
