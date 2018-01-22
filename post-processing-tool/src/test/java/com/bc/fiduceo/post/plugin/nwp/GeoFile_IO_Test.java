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

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.util.TempFileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class GeoFile_IO_Test {

    private TempFileUtils tempFileUtils;

    @Before
    public void setUp() {
        tempFileUtils = new TempFileUtils();
    }

    @After
    public void tearDown() {
        tempFileUtils.cleanup();
    }

    @Test
    public void testCreateTimeSeries() throws IOException {
        final GeoFile geoFile = new GeoFile(1208);

        NetcdfFile geoFileNC = null;
        try {
            geoFile.createTimeSeries(tempFileUtils);
            final File file = geoFile.getFile();
            assertTrue(file.isFile());

            geoFileNC = NetcdfFile.open(file.getAbsolutePath());
            NCTestUtils.assertDimension("grid_size", 1208, geoFileNC);
            NCTestUtils.assertDimension("grid_matchup", 1208, geoFileNC);
            NCTestUtils.assertDimension("grid_ny", 1, geoFileNC);
            NCTestUtils.assertDimension("grid_nx", 1, geoFileNC);
            NCTestUtils.assertDimension("grid_corners", 4, geoFileNC);
            NCTestUtils.assertDimension("grid_rank", 2, geoFileNC);

            NCTestUtils.assertVariablePresent("grid_dims", DataType.INT, "grid_rank", geoFileNC);
            NCTestUtils.assertVariablePresent("grid_center_lat", DataType.FLOAT, "grid_size", geoFileNC);
            NCTestUtils.assertVariablePresent("grid_center_lon", DataType.FLOAT, "grid_size", geoFileNC);
            NCTestUtils.assertVariablePresent("grid_imask", DataType.INT, "grid_size", geoFileNC);
            NCTestUtils.assertVariablePresent("grid_corner_lat", DataType.FLOAT, "grid_size grid_corners", geoFileNC);
            NCTestUtils.assertVariablePresent("grid_corner_lon", DataType.FLOAT, "grid_size grid_corners", geoFileNC);

        } finally {
            geoFile.close();
            if (geoFileNC != null) {
                geoFileNC.close();
            }
        }
    }

    @Test
    public void testWriteTimeSeries() throws IOException, InvalidRangeException {
        final GeoFile geoFile = new GeoFile(14);
        final float[] longitudes = {115.39925f, 108.04573f, 108.04573f, 107.99058f, 97.13357f, 97.13357f, 97.13357f, 89.25099f, 89.25099f, 89.25099f, 98.02876f, 98.02876f, 99.472626f, 99.472626f};
        final float[] latitudes = {-46.212715f, -47.560722f, -47.560722f, -47.57288f, -49.25582f, -49.25582f, -49.25582f, -49.821648f, -49.821648f, -49.821648f, -48.872658f, -48.872658f, -48.64003f, -48.64003f};

        final Array longitudesArray = Array.factory(longitudes);
        final Array latitudesArray = Array.factory(latitudes);

        NetcdfFile geoFileNC = null;
        try {
            geoFile.createTimeSeries(tempFileUtils);
            final File file = geoFile.getFile();
            assertTrue(file.isFile());

            geoFile.writeTimeSeries(longitudesArray, latitudesArray);

            geoFile.close();

            geoFileNC = NetcdfFile.open(file.getAbsolutePath());
            NCTestUtils.assertVectorVariable("grid_dims", 0, 1, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_dims", 1, 14, geoFileNC);

            NCTestUtils.assertVectorVariable("grid_center_lat", 0, -46.21271514892578, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_center_lat", 1, -47.56072235107422, geoFileNC);

            NCTestUtils.assertVectorVariable("grid_center_lon", 2, 108.04573059082031, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_center_lon", 3, 107.9905776977539, geoFileNC);

            NCTestUtils.assertVectorVariable("grid_imask", 4, 1, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_imask", 5, 1, geoFileNC);
        } finally {
            geoFile.close();
            if (geoFileNC != null) {
                geoFileNC.close();
            }
        }
    }

    @Test
    public void testWriteTimeSeries_3dLonLats() throws IOException, InvalidRangeException {
        final GeoFile geoFile = new GeoFile(2);
        final float[] longitudes = {-156.35783f, -156.38275f, -156.40768f, -156.38489f, -156.4098f, -156.43472f, -156.4121f, -156.437f, -156.4619f, -158.20874f, -158.23456f, -158.26039f, -158.23567f, -158.26147f, -158.2873f, -158.26251f, -158.2883f, -158.3141f};
        final float[] latitudes = {71.39814f, 71.384605f, 71.37107f, 71.403175f, 71.38964f, 71.3761f, 71.40825f, 71.3947f, 71.38115f, 70.86857f, 70.85323f, 70.8379f, 70.87337f, 70.85803f, 70.8427f, 70.87809f, 70.86274f, 70.8474f};

        final Array longitudesArray = Array.factory(DataType.FLOAT, new int[]{2, 3, 3}, longitudes);
        final Array latitudesArray = Array.factory(DataType.FLOAT, new int[]{2, 3, 3}, latitudes);

        NetcdfFile geoFileNC = null;
        try {
            geoFile.createTimeSeries(tempFileUtils);
            final File file = geoFile.getFile();
            assertTrue(file.isFile());

            geoFile.writeTimeSeries(longitudesArray, latitudesArray);

            geoFile.close();

            geoFileNC = NetcdfFile.open(file.getAbsolutePath());
            NCTestUtils.assertVectorVariable("grid_dims", 0, 1, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_dims", 1, 2, geoFileNC);

            NCTestUtils.assertVectorVariable("grid_center_lat", 0, 71.38964080810547, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_center_lat", 1, 70.8580322265625, geoFileNC);

            NCTestUtils.assertVectorVariable("grid_center_lon", 0, -156.40980529785156, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_center_lon", 1, -158.261474609375, geoFileNC);

            NCTestUtils.assertVectorVariable("grid_imask", 0, 1, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_imask", 1, 1, geoFileNC);
        } finally {
            geoFile.close();
            if (geoFileNC != null) {
                geoFileNC.close();
            }
        }
    }

    @Test
    public void testCreateSensorExtract() throws IOException {
        final GeoFile geoFile = new GeoFile(714);
        final SensorExtractConfiguration sensorExtractConfiguration = new SensorExtractConfiguration();
        sensorExtractConfiguration.setX_Dimension(3);
        sensorExtractConfiguration.setY_Dimension(5);

        NetcdfFile geoFileNC = null;
        try {
            geoFile.createSensorExtract(tempFileUtils, sensorExtractConfiguration);
            final File file = geoFile.getFile();
            assertTrue(file.isFile());

            geoFileNC = NetcdfFile.open(file.getAbsolutePath());
            NCTestUtils.assertDimension("grid_size", 714 * 3 * 5, geoFileNC);
            NCTestUtils.assertDimension("grid_matchup", 714, geoFileNC);
            NCTestUtils.assertDimension("grid_nx", 3, geoFileNC);
            NCTestUtils.assertDimension("grid_ny", 5, geoFileNC);
            NCTestUtils.assertDimension("grid_corners", 4, geoFileNC);
            NCTestUtils.assertDimension("grid_rank", 2, geoFileNC);

            NCTestUtils.assertVariablePresent("grid_dims", DataType.INT, "grid_rank", geoFileNC);
            NCTestUtils.assertVariablePresent("grid_center_lat", DataType.FLOAT, "grid_size", geoFileNC);
            NCTestUtils.assertVariablePresent("grid_center_lon", DataType.FLOAT, "grid_size", geoFileNC);
            NCTestUtils.assertVariablePresent("grid_imask", DataType.INT, "grid_size", geoFileNC);
            NCTestUtils.assertVariablePresent("grid_corner_lat", DataType.FLOAT, "grid_size grid_corners", geoFileNC);
            NCTestUtils.assertVariablePresent("grid_corner_lon", DataType.FLOAT, "grid_size grid_corners", geoFileNC);

        } finally {
            geoFile.close();
            if (geoFileNC != null) {
                geoFileNC.close();
            }
        }
    }

    @Test
    public void testWriteSensorExtract() throws IOException, InvalidRangeException {
        final GeoFile geoFile = new GeoFile(2);
        final float[] longitudes = {-156.35783f, -156.38275f, -156.40768f, -156.38489f, -156.4098f, -156.43472f, -156.4121f, -156.437f, -156.4619f, -158.20874f, -158.23456f, -158.26039f, -158.23567f, -158.26147f, -158.2873f, -158.26251f, -158.2883f, -158.3141f};
        final float[] latitudes = {71.39814f, 71.384605f, 71.37107f, 71.403175f, 71.38964f, 71.3761f, 71.40825f, 71.3947f, 71.38115f, 70.86857f, 70.85323f, 70.8379f, 70.87337f, 70.85803f, 70.8427f, 70.87809f, 70.86274f, 70.8474f};

        final Array longitudesArray = Array.factory(DataType.FLOAT, new int[]{2, 3, 3}, longitudes);
        final Array latitudesArray = Array.factory(DataType.FLOAT, new int[]{2, 3, 3}, latitudes);


        final SensorExtractConfiguration sensorExtractConfiguration = new SensorExtractConfiguration();
        sensorExtractConfiguration.setX_Dimension(3);
        sensorExtractConfiguration.setY_Dimension(3);

        NetcdfFile geoFileNC = null;
        try {
            geoFile.createSensorExtract(tempFileUtils, sensorExtractConfiguration);
            final File file = geoFile.getFile();
            assertTrue(file.isFile());

            geoFile.writeSensorExtract(longitudesArray, latitudesArray, 1, 1, sensorExtractConfiguration);

            geoFile.close();

            geoFileNC = NetcdfFile.open(file.getAbsolutePath());
            NCTestUtils.assertVectorVariable("grid_dims", 0, 3, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_dims", 1, 6, geoFileNC);

            NCTestUtils.assertVectorVariable("grid_center_lat", 0, 71.39813995361328, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_center_lat", 1, 71.38460540771484, geoFileNC);

            NCTestUtils.assertVectorVariable("grid_center_lon", 2, -156.40768432617188, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_center_lon", 3, -156.3848876953125, geoFileNC);

            NCTestUtils.assertVectorVariable("grid_imask", 4, 1, geoFileNC);
            NCTestUtils.assertVectorVariable("grid_imask", 5, 1, geoFileNC);
        } finally {
            geoFile.close();
            if (geoFileNC != null) {
                geoFileNC.close();
            }
        }
    }
}
