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
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.TimeLocator;
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

    private static final int SNOT = 30;

    private static final int G_EPS_DAT_IASI_OFFSET = 9122;

    private ImageInputStream iis;
    private GenericRecordHeader mphrHeader;
    private MainProductHeaderRecord mainProductHeaderRecord;
    private GiadrScaleFactors giadrScaleFactors;

    private long firstMdrOffset;
    private long mdrSize;
    private int mdrCount;


    IASI_Reader(GeometryFactory geometryFactory) {
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

        return acquisitionInfo;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneIndex) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        final long[][] timeArray = readGEPSDatIasi();
        return new IASI_TimeLocator(timeArray);
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
        size.setNx(60); // @todo 3 tb/tb extract constant 2017-04-27
        size.setNy(2 * mdrCount);
        return size;
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

        determineMdrCount(iis);
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

    private void determineMdrCount(ImageInputStream iis) throws IOException {
        iis.seek(firstMdrOffset);
        final GenericRecordHeader mdrHeader = GenericRecordHeader.readGenericRecordHeader(iis);

        mdrSize = mdrHeader.recordSize;
        mdrCount = (int) ((iis.length() - firstMdrOffset) / mdrSize);
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

    private long getMdrOffset(int i) {
        return firstMdrOffset + (i * mdrSize);
    }
}
