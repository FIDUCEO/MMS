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
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_TAI1993Vector;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import ucar.ma2.*;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.reader.modis.ModisConstants.LATITUDE_VAR_NAME;
import static com.bc.fiduceo.reader.modis.ModisConstants.LONGITUDE_VAR_NAME;

public class MxD06_Reader extends NetCDFReader {

    private static final String REG_EX = "M([OY])D06_L2.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";

    private static final String GEOLOCATION_GROUP = "mod06/Geolocation_Fields";
    private static final String DATA_GROUP = "mod06/Data_Fields";

    private final GeometryFactory geometryFactory;
    private final Dimension size1Km;
    private final Dimension size5km;
    private TimeLocator timeLocator;
    private BowTiePixelLocator pixelLocator;
    private Dimension productSize;

    MxD06_Reader(ReaderContext readerContext) {
        this.size1Km = new Dimension("size", 1354, 0);
        this.size5km = new Dimension("size", 270, 0);
        this.geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
        timeLocator = null;
    }

    @Override
    public void close() throws IOException {
        timeLocator = null;
        productSize = null;

        if (pixelLocator != null) {
            pixelLocator.dispose();
            pixelLocator = null;
        }
        super.close();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        HdfEOSUtil.extractAcquisitionTimes(acquisitionInfo, netcdfFile);
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
        return LONGITUDE_VAR_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE_VAR_NAME;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            createPixelLocator();
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();   // we just have 5 minute products, no large geometries tb 2017-09-04
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
        final String rawVariableName = stripLayerSuffix(variableName);
        final String groupName = getGroupName(rawVariableName);
        final Array array = arrayCache.get(groupName, rawVariableName);
        final Number fillValue = arrayCache.getNumberAttributeValue(NetCDFUtils.CF_FILL_VALUE_NAME, groupName, rawVariableName);
        if (fillValue == null) {
            throw new RuntimeException("implement fill value handling here.");
        }

        if (is1KmVariable(array)) {
            return readRaw1km(centerX, centerY, interval, array, fillValue);
        } else if (variableName.equals("Cloud_Mask_5km")) {
            return readCloudMask5Km(centerX, centerY, interval, array);
        } else if (variableName.contains("Quality_Assurance_5km")) {
            return readQualiytAssurance5Km(centerX, centerY, interval, array, variableName);
        } else {
            return RawDataReader.read(centerX, centerY, interval, fillValue, array, size5km);
        }
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array rawData = readRaw(centerX, centerY, interval, variableName);
        final String groupName = getGroupName(variableName);

        final double scaleFactor = getScaleFactorCf(groupName, variableName);
        final double offset = getOffset(groupName, variableName);
        if (ReaderUtils.mustScale(scaleFactor, offset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            return MAMath.convert2Unpacked(rawData, scaleOffset);
        }

        return rawData;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException {
        final int height = interval.getY();
        final int width = interval.getX();
        final int y_offset = y - height / 2;
        int[] shape = new int[]{height, width};

        final TimeLocator timeLocator = getTimeLocator();
        final int pHeight = getProductSize().getNy();

        final Array acquisitionTime = Array.factory(DataType.INT, shape);
        final Index index = acquisitionTime.getIndex();

        final int acquisitionTimeFillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();

        for (int ya = 0; ya < height; ya++) {
            final int yRead = y_offset + ya;
            final int lineTimeInSeconds;
            if (yRead < 0 || yRead >= pHeight) {
                lineTimeInSeconds = acquisitionTimeFillValue;
            } else {
                final long lineTime = timeLocator.getTimeFor(0, yRead);
                lineTimeInSeconds = (int) (lineTime / 1000);
            }

            for (int xa = 0; xa < width; xa++) {
                index.set(ya, xa);
                acquisitionTime.setInt(index, lineTimeInSeconds);
            }
        }
        return (ArrayInt.D2) acquisitionTime;
    }

    @Override
    public List<Variable> getVariables() {
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
        if (productSize == null) {
            final Array longitude = arrayCache.get(GEOLOCATION_GROUP, LONGITUDE_VAR_NAME);
            final int[] shape = longitude.getShape();
            productSize = new Dimension("shape", shape[1], shape[0]);
        }
        return productSize;
    }

    // package access for testing only tb 2017-08-28
    static String getGroupName(String variableName) {
        if (LONGITUDE_VAR_NAME.equals(variableName) || LATITUDE_VAR_NAME.equals(variableName)) {
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

    // package access for testing only tb 2017-08-31
    static String stripLayerSuffix(String variableName) {
        if (variableName.contains("Quality_Assurance_5km")) {
            final int lastUnderscore = variableName.lastIndexOf("_");
            return variableName.substring(0, lastUnderscore);
        }
        return variableName;
    }

    // package access for testing only tb 2017-08-31
    static int extractLayerIndex(String variableName) {
        if (variableName.contains("Quality_Assurance_5km")) {
            final int lastUnderscore = variableName.lastIndexOf("_");
            final String intString = variableName.substring(lastUnderscore + 1);
            return Integer.parseInt(intString);
        }
        throw new RuntimeException("Invalid variable name for layer calculations");
    }

    private void extractGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(new Interval(50, 50), geometryFactory);
        final Array longitude = arrayCache.get(GEOLOCATION_GROUP, LONGITUDE_VAR_NAME);
        final Array latitude = arrayCache.get(GEOLOCATION_GROUP, LATITUDE_VAR_NAME);
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

        final Array section = NetCDFUtils.section(time, offsets, shape);
        timeLocator = new TimeLocator_TAI1993Vector(section);
    }

    private void createPixelLocator() throws IOException {
        pixelLocator = new BowTiePixelLocator(arrayCache.get(GEOLOCATION_GROUP, LONGITUDE_VAR_NAME), arrayCache.get(GEOLOCATION_GROUP, LATITUDE_VAR_NAME), geometryFactory);
    }

    private Array readRaw1km(int centerX, int centerY, Interval interval, Array array, Number fillValue) throws IOException, InvalidRangeException {
        final int x_1km = centerX * 5 + 2;
        final int y_1km = centerY * 5 + 2;
        final Interval extendedInterval = new Interval(interval.getX() * 5, interval.getY() * 5);
        final Array fullArray = RawDataReader.read(x_1km, y_1km, extendedInterval, fillValue, array, size1Km);

        final int[] shape = new int[]{interval.getY(), interval.getX()};
        final int[] origin = new int[]{2, 2};
        final int[] stride = new int[]{5, 5};
        return fullArray.section(origin, shape, stride);
    }

    private Array readCloudMask5Km(int centerX, int centerY, Interval interval, Array array) throws IOException {
        final int[] shape = array.getShape();
        shape[2] = 1;
        final int[] origin = {0, 0, 0};
        final Array lowByte = NetCDFUtils.section(array, origin, shape);

        origin[2] = 1;
        final Array highByte = NetCDFUtils.section(array, origin, shape);

        final Array lowSubset = RawDataReader.read(centerX, centerY, interval, 0, lowByte, size5km);
        final Array highSubset = RawDataReader.read(centerX, centerY, interval, 0, highByte, size5km);
        final Array targetArray = Array.factory(DataType.SHORT, lowSubset.getShape());

        final IndexIterator lowIndex = lowSubset.getIndexIterator();
        final IndexIterator highIndex = highSubset.getIndexIterator();
        final IndexIterator targetIndex = targetArray.getIndexIterator();
        while (lowIndex.hasNext() && highIndex.hasNext() && targetIndex.hasNext()) {
            final short lowByteShifted = (short) (lowIndex.getByteNext() << 8);
            final short byteNext = (short) (highIndex.getByteNext() & 0xFF);
            final short combinedFlag = (short) (lowByteShifted | byteNext);

            targetIndex.setShortNext(combinedFlag);
        }

        return targetArray;
    }

    private Array readQualiytAssurance5Km(int centerX, int centerY, Interval interval, Array array, String variableName) throws IOException {
        final int layerIndex = extractLayerIndex(variableName);
        final int[] shape = array.getShape();
        shape[2] = 1;   // we only want one z-layer
        final int[] offsets = {0, 0, layerIndex};
        final Array layerData = NetCDFUtils.section(array, offsets, shape);
        return RawDataReader.read(centerX, centerY, interval, 0, layerData, size5km);
    }
}
