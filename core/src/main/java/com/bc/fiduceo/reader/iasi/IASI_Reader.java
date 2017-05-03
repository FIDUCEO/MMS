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
 * Copyright (C) 2015 Brockmann Consult GmbH
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

package com.bc.fiduceo.reader.iasi;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class IASI_Reader implements Reader {

    private static final String REG_EX = "IASI_xxx_1C_M0[1-3]_\\d{14}Z_\\d{14}Z_\\w_\\w_\\d{14}Z.nat";

    private static final int PN = 4;
    private static final int SNOT = 30;
    private static final int LON = 0;
    private static final int LAT = 1;

    private static final int G_EPS_DAT_IASI_OFFSET = 9122;
    private static final int G_GEO_SOND_LOC_OFFSET = 255893;

    private static final float G_GEO_SOND_LOC_SCALING_FACTOR = 1.0E-6f;

    private ImageInputStream iis;
    private GenericRecordHeader mphrHeader;
    private MainProductHeaderRecord mainProductHeaderRecord;
    private GiadrScaleFactors giadrScaleFactors;
    private IASI_TimeLocator timeLocator;
    private GeolocationData geolocationData;
    private IASI_PixelLocator pixelLocator;

    private final GeometryFactory geometryFactory;

    private long firstMdrOffset;
    private long mdrSize;
    private int mdrCount;

    IASI_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        iis = null;
    }

    @Override
    public void open(File file) throws IOException {
        if (iis != null) {
            throw new RuntimeException("Stream already opened");
        }

        iis = new FileImageInputStream(file);

        readHeader();
    }

    @Override
    public void close() throws IOException {
        timeLocator = null;
        if (iis != null) {
            iis.close();
            iis = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        acquisitionInfo.setSensingStart(mphrHeader.recordStartTime.getAsDate());
        acquisitionInfo.setSensingStop(mphrHeader.recordEndTime.getAsDate());

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = createGeometries();

        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries, geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        return getPixelLocator_internal();
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneIndex) throws IOException {
        return getPixelLocator_internal();
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            final long[][] timeArray = readGEPSDatIasi();
            timeLocator = new IASI_TimeLocator(timeArray);
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

    @Override
    public Dimension getProductSize() {
        final Dimension size = new Dimension();
        size.setNx(2 * SNOT);
        size.setNy(2 * mdrCount);
        return size;
    }

    private PixelLocator getPixelLocator_internal() throws IOException {
        if (pixelLocator == null) {
            final GeolocationData geolocationData = getGeolocationData();

            pixelLocator = new IASI_PixelLocator(geolocationData, geometryFactory);
        }
        return pixelLocator;
    }

    private void readHeader() throws IOException {
        mphrHeader = GenericRecordHeader.readGenericRecordHeader(iis);
        if (mphrHeader.recordClass != RecordClass.MPHR
                || mphrHeader.instrumentGroup != InstrumentGroup.GENERIC
                || mphrHeader.recordSubclass != 0) {
            throw new IOException("Illegal Main Product Header Record");
        }

        mainProductHeaderRecord = new MainProductHeaderRecord();
        mainProductHeaderRecord.readRecord(iis);

        final List<InternalPointerRecord> iprList = readInternalPointerRecordList();

        for (final InternalPointerRecord ipr : iprList) {
            if (ipr.targetRecordClass == RecordClass.GIADR) {
                if (ipr.targetRecordSubclass == 0) {
                    iis.seek(ipr.targetRecordOffset);
                    GiadrQuality giadrQuality = new GiadrQuality();
                    giadrQuality.readRecord(iis);
                } else if (ipr.targetRecordSubclass == 1) {
                    iis.seek(ipr.targetRecordOffset);
                    giadrScaleFactors = new GiadrScaleFactors();
                    giadrScaleFactors.readRecord(iis);
                }
            } else if (ipr.targetRecordClass == RecordClass.MDR) {
                firstMdrOffset = ipr.targetRecordOffset;
            }
        }

        determineMdrParameter(iis);
    }

    private List<InternalPointerRecord> readInternalPointerRecordList() throws IOException {
        final List<InternalPointerRecord> iprList = new ArrayList<>();
        for (; ; ) {
            final InternalPointerRecord ipr = InternalPointerRecord.readInternalPointerRecord(iis);
            iprList.add(ipr);
            if (ipr.targetRecordClass == RecordClass.MDR) {
                break;
            }
        }
        return iprList;
    }

    private void determineMdrParameter(ImageInputStream iis) throws IOException {
        iis.seek(firstMdrOffset);
        final GenericRecordHeader mdrHeader = GenericRecordHeader.readGenericRecordHeader(iis);

        checkRecordSubClass(mdrHeader);

        mdrSize = mdrHeader.recordSize;
        mdrCount = (int) ((iis.length() - firstMdrOffset) / mdrSize);
    }

    private void checkRecordSubClass(GenericRecordHeader mdrHeader) {
        if (mdrHeader.recordSubclassVersion != 5) {
            throw new RuntimeException("Unsupported processing version");
        }
    }

    private long[][] readGEPSDatIasi() throws IOException {
        final long[][] data = new long[mdrCount][];

        for (int i = 0; i < mdrCount; i++) {
            data[i] = readGEPSDatIasiMdr(i);
        }

        return data;
    }

    private long[] readGEPSDatIasiMdr(int mdrIndex) throws IOException {
        final long[] data = new long[SNOT];
        final long mdrOffset = getMdrOffset(mdrIndex);

        iis.seek(mdrOffset + G_EPS_DAT_IASI_OFFSET);

        for (int j = 0; j < SNOT; j++) {
            data[j] = EpsMetopUtil.readShortCdsTime(iis).getAsCalendar().getTimeInMillis();
        }
        return data;
    }

    private float[][][][] readGGeoSondLoc() throws IOException {
        final float[][][][] data = new float[mdrCount][SNOT][PN][2];
        final int[] mdrBlock = new int[SNOT * PN * 2];

        for (int mdrIndex = 0; mdrIndex < mdrCount; mdrIndex++) {
            final long mdrOffset = getMdrOffset(mdrIndex);
            final float[][][] scanLineData = data[mdrIndex];

            iis.seek(mdrOffset + G_GEO_SOND_LOC_OFFSET);
            iis.readFully(mdrBlock, 0, mdrBlock.length);

            for (int i = 0, j = 0; j < SNOT; j++) {
                final float[][] efovData = scanLineData[j];
                for (int k = 0; k < PN; k++) {
                    efovData[k][LON] = mdrBlock[i++] * G_GEO_SOND_LOC_SCALING_FACTOR;
                    efovData[k][LAT] = mdrBlock[i++] * G_GEO_SOND_LOC_SCALING_FACTOR;
                }
            }
        }

        return data;
    }

    private long getMdrOffset(int i) {
        return firstMdrOffset + (i * mdrSize);
    }

    private Geometries createGeometries() throws IOException {
        final Geometries geometries = new Geometries();

        final GeolocationData geolocationData = getGeolocationData();

        final BoundingPolygonCreator polygonCreator = new BoundingPolygonCreator(new Interval(6, 24), geometryFactory);
        final Geometry boundingGeometry = polygonCreator.createBoundingGeometrySplitted(geolocationData.longitudes, geolocationData.latitudes, 2, true);
        if (!boundingGeometry.isValid()) {
            throw new RuntimeException("Unable to extract valid bounding geometry");
        }
        geometries.setBoundingGeometry(boundingGeometry);

        final Geometry timeAxisGeometry = polygonCreator.createTimeAxisGeometrySplitted(geolocationData.longitudes, geolocationData.latitudes, 2);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        return geometries;
    }

    private GeolocationData getGeolocationData() throws IOException {
        if (geolocationData == null) {
            geolocationData = readGeolocationData();
        }

        return geolocationData;
    }

    private GeolocationData readGeolocationData() throws IOException {
        final float[][] longitudes = new float[2 * mdrCount][2 * SNOT];
        final float[][] latitudes = new float[2 * mdrCount][2 * SNOT];

        final float[][][][] doubles = readGGeoSondLoc();
        for (int line = 0; line < mdrCount; line++) {
            float[][][] lineData = doubles[line];

            final int targetLine = 2 * line;
            final float[] longitudeLine_0 = longitudes[targetLine];
            final float[] longitudeLine_1 = longitudes[targetLine + 1];

            final float[] latitudeLine_0 = latitudes[targetLine];
            final float[] latitudeLine_1 = latitudes[targetLine + 1];

            for (int efov = 0; efov < SNOT; efov++) {
                final float[][] efovData = lineData[efov];

                final int pixelIndex = 2 * efov;
                longitudeLine_0[pixelIndex] = efovData[3][LON];
                longitudeLine_1[pixelIndex] = efovData[2][LON];
                longitudeLine_0[pixelIndex + 1] = efovData[0][LON];
                longitudeLine_1[pixelIndex + 1] = efovData[1][LON];

                latitudeLine_0[pixelIndex] = efovData[3][LAT];
                latitudeLine_1[pixelIndex] = efovData[2][LAT];
                latitudeLine_0[pixelIndex + 1] = efovData[0][LAT];
                latitudeLine_1[pixelIndex + 1] = efovData[1][LAT];
            }
        }

        final GeolocationData geolocationData = new GeolocationData();
        geolocationData.longitudes = Array.factory(longitudes);
        geolocationData.latitudes = Array.factory(latitudes);
        return geolocationData;
    }
}
