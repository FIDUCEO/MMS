/*
 * Copyright (C) 2017 Brockmann Consult GmbH
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

package com.bc.fiduceo.post.plugin.nwp;


import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;

class GeoFile {

    private File tempFile;
    private NetcdfFileWriter writer;
    private final int numMatchups;
    private Variable gridDims;
    private Variable gridCenterLat;
    private Variable gridCenterLon;
    private Variable gridMask;

    GeoFile(int numMatchups) {
        this.numMatchups = numMatchups;
    }

    void createTimeSeries(TempFileManager tempFileManager) throws IOException {
        tempFile = tempFileManager.create("geo", "nc");

        createWriter();

        createDimensionsAndVariables(1, 1);

        createNetcdf();
    }


    void createSensorExtract(TempFileManager tempFileManager, SensorExtractConfiguration config) throws IOException {
        tempFile = tempFileManager.create("geo", "nc");

        createWriter();

        final int x_dimension = config.getX_Dimension();
        final int y_dimension = config.getY_Dimension();
        createDimensionsAndVariables(x_dimension, y_dimension);

        createNetcdf();
    }

    void writeTimeSeries(Array longitudesArray, Array latitudesArray) throws IOException, InvalidRangeException {
        writer.write(gridDims, Array.factory(new int[]{1, numMatchups}));

        int[] sourceStart;
        int[] sourceShape;
        final int rank = longitudesArray.getRank();
        if (rank == 1) {
            sourceStart = new int[]{0};
            sourceShape = new int[]{1};
        } else if (rank == 3) {
            final int[] shape = longitudesArray.getShape();
            sourceStart = new int[]{0, shape[1] / 2, shape[2] / 2};
            sourceShape = new int[]{1, 1, 1};
        } else {
            throw new RuntimeException("Unsupported geolocation array dimensionality");
        }
        final int[] targetStart = {0};
        final int[] targetShape = {1};

        final Array maskData = Array.factory(DataType.INT, targetShape);
        final Array lonWriteArray = Array.factory(new float[]{0.f});
        final Array latWriteArray = Array.factory(new float[]{0.f});

        for (int i = 0; i < numMatchups; i++) {
            targetStart[0] = i;
            sourceStart[0] = i;

            final Array lonSection = longitudesArray.section(sourceStart, sourceShape);
            final IndexIterator lonIterator = lonSection.getIndexIterator();
            final Array latSection = latitudesArray.section(sourceStart, sourceShape);
            final IndexIterator latIterator = latSection.getIndexIterator();
            final float lon = lonIterator.getFloatNext();
            final float lat = latIterator.getFloatNext();

            maskData.setInt(0, lat >= -90.0f && lat <= 90.0f && lon >= -180.0f && lat <= 180.0f ? 1 : 0);
            lonWriteArray.setFloat(0, lon);
            latWriteArray.setFloat(0, lat);

            writer.write(gridCenterLon, targetStart, lonWriteArray);
            writer.write(gridCenterLat, targetStart, latWriteArray);
            writer.write(gridMask, targetStart, maskData);
        }
    }

    void writeSensorExtract(Array longitudesArray, Array latitudesArray, int strideX, int strideY, SensorExtractConfiguration config) throws IOException, InvalidRangeException {
        final int x_dimension = config.getX_Dimension();
        final int y_dimension = config.getY_Dimension();

        writer.write(gridDims, Array.factory(new int[]{x_dimension, y_dimension * numMatchups}));

        final int[] shape = longitudesArray.getShape();
        final int nx = shape[2];
        final int ny = shape[1];

        final int[] sourceStart = {0, (ny >> 1) - (y_dimension >> 1) * strideY, (nx >> 1) - (x_dimension >> 1) * strideX};
        final int[] sourceShape = {1, y_dimension, x_dimension};
        final int[] sourceStride = {1, strideY, strideX};
        final int[] targetStart = {0};
        final int[] targetShape = {y_dimension * x_dimension};
        final Array maskData = Array.factory(DataType.INT, targetShape);

        for (int i = 0; i < numMatchups; i++) {
            sourceStart[0] = i;
            targetStart[0] = i * y_dimension * x_dimension;
            final Array latData = latitudesArray.section(sourceStart, sourceShape, sourceStride);
            final Array lonData = longitudesArray.section(sourceStart, sourceShape, sourceStride);
            for (int k = 0; k < targetShape[0]; k++) {
                final float lat = latData.getFloat(k);
                final float lon = lonData.getFloat(k);
                maskData.setInt(k, lat >= -90.0f && lat <= 90.0f && lon >= -180.0f && lat <= 180.0f ? 1 : 0);
            }
            writer.write(gridCenterLat, targetStart, latData.reshape(targetShape));
            writer.write(gridCenterLon, targetStart, lonData.reshape(targetShape));
            writer.write(gridMask, targetStart, maskData);
        }
    }

    void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    File getFile() {
        return tempFile;
    }


    private void createWriter() throws IOException {
        writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, tempFile.getAbsolutePath());
        writer.setLargeFile(true);
    }

    private void createNetcdf() throws IOException {
        writer.create();
        writer.flush();
    }

    private void createDimensionsAndVariables(int x_dimension, int y_dimension) {
        writer.addDimension(null, "grid_size", numMatchups * x_dimension * y_dimension);
        writer.addDimension(null, "grid_matchup", numMatchups);

        writer.addDimension(null, "grid_nx", x_dimension);
        writer.addDimension(null, "grid_ny", y_dimension);

        writer.addDimension(null, "grid_corners", 4);
        writer.addDimension(null, "grid_rank", 2);

        gridDims = writer.addVariable(null, "grid_dims", DataType.INT, "grid_rank");
        gridCenterLat = writer.addVariable(null, "grid_center_lat", DataType.FLOAT, "grid_size");
        gridCenterLat.addAttribute(new Attribute("units", "degrees"));

        gridCenterLon = writer.addVariable(null, "grid_center_lon", DataType.FLOAT, "grid_size");
        gridCenterLon.addAttribute(new Attribute("units", "degrees"));

        gridMask = writer.addVariable(null, "grid_imask", DataType.INT, "grid_size");
        // @todo 2 tb/tb why is this written? Can't we just skip these variables? 2017-03-30
        writer.addVariable(null, "grid_corner_lat", DataType.FLOAT, "grid_size grid_corners");
        writer.addVariable(null, "grid_corner_lon", DataType.FLOAT, "grid_size grid_corners");
        writer.addGroupAttribute(null, new Attribute("title", "MMD geo-location in SCRIP format"));
    }
}
