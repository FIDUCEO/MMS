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

package com.bc.fiduceo.reader.atsr;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.snap.SNAP_Reader;
import com.bc.fiduceo.reader.snap.SNAP_TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.datamodel.TimeCoding;
import org.esa.snap.dataio.envisat.EnvisatConstants;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;

import java.io.File;
import java.io.IOException;

import static ucar.ma2.DataType.INT;

class ATSR_L1B_Reader extends SNAP_Reader {

    private static final Interval INTERVAL = new Interval(5, 20);
    private static final int NUM_SPLITS = 2;
    private static final String REG_EX = "AT([12S])_TOA_1P[A-Z0-9]{4}\\d{8}_\\d{6}_\\d{12}_\\d{5}_\\d{5}_\\d{4}.([NE])([12])";

    ATSR_L1B_Reader(ReaderContext readerContext) {
        super(readerContext);
    }

    @Override
    public void open(File file) throws IOException {
        open(file, EnvisatConstants.ENVISAT_FORMAT_NAME);
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        return read(INTERVAL, NUM_SPLITS);
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude";
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) {
        // subscene is only relevant for segmented geometries which we do not have tb 2016-08-11
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() {
        return new SNAP_TimeLocator(product);
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String yearString = fileName.substring(14, 18);
        final String monthString = fileName.substring(18, 20);
        final String dayString = fileName.substring(20, 22);

        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(yearString);
        ymd[1] = Integer.parseInt(monthString);
        ymd[2] = Integer.parseInt(dayString);
        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        if (product.containsTiePointGrid(variableName)) {
            // we do not want raw data access on tie-point grids tb 2016-08-11
            return readScaled(centerX, centerY, interval, variableName);
        }

        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();


        final RasterDataNode dataNode = getRasterDataNode(variableName);

        final double noDataValue = getNoDataValue(dataNode);
        final DataType targetDataType = NetCDFUtils.getNetcdfDataType(dataNode.getDataType());
        final int[] shape = getShape(interval);
        final Array readArray = Array.factory(targetDataType, shape);
        final Array targetArray = Array.factory(targetDataType, shape);

        final int width = interval.getX();
        final int height = interval.getY();

        final int xOffset = centerX - width / 2;
        final int yOffset = centerY - height / 2;

        readRawProductData(dataNode, readArray, width, height, xOffset, yOffset);

        final Index index = targetArray.getIndex();
        int readIndex = 0;
        for (int y = 0; y < width; y++) {
            final int currentY = yOffset + y;
            for (int x = 0; x < height; x++) {
                final int currentX = xOffset + x;
                index.set(y, x);
                if (currentX >= 0 && currentX < sceneRasterWidth && currentY >= 0 && currentY < sceneRasterHeight) {
                    targetArray.setObject(index, readArray.getObject(readIndex));
                    ++readIndex;
                } else {
                    targetArray.setObject(index, noDataValue);
                }
            }
        }

        return targetArray;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) {
        // @todo 3 tb/** this method should be combined with the functionality implemented in WindowReader classes. 2016-08-10
        final int width = interval.getX();
        final int height = interval.getY();
        final int[] timeArray = new int[width * height];

        final PixelPos pixelPos = new PixelPos();
        final TimeCoding sceneTimeCoding = product.getSceneTimeCoding();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int halfHeight = height / 2;
        final int halfWidth = width / 2;
        int writeOffset = 0;
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        for (int yRead = y - halfHeight; yRead <= y + halfHeight; yRead++) {
            int lineTimeSeconds = fillValue;
            if (yRead >= 0 && yRead < sceneRasterHeight) {
                pixelPos.setLocation(x, yRead + 0.5);
                final double lineMjd = sceneTimeCoding.getMJD(pixelPos);
                final long lineTime = TimeUtils.mjd2000ToDate(lineMjd).getTime();
                lineTimeSeconds = (int) Math.round(lineTime * 0.001);
            }

            for (int xRead = x - halfWidth; xRead <= x + halfWidth; xRead++) {
                if (xRead >= 0 && xRead < sceneRasterWidth) {
                    timeArray[writeOffset] = lineTimeSeconds;
                } else {
                    timeArray[writeOffset] = fillValue;
                }
                ++writeOffset;
            }
        }

        final int[] shape = getShape(interval);
        return (ArrayInt.D2) Array.factory(INT, shape, timeArray);
    }

    protected void readProductData(RasterDataNode dataNode, Array targetArray, int width, int height, int xOffset, int yOffset) throws IOException {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();

        readSubsetData(dataNode, targetArray, width, height, xOffset, yOffset, sceneRasterWidth, sceneRasterHeight);
    }
}
