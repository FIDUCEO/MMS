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
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class GeoFile_IO_Test {

    @Test
    public void testCreate() throws IOException {
        final GeoFile geoFile = new GeoFile(1208);

        File file = null;
        NetcdfFile geoFileNC = null;
        try {
            geoFile.create();
            file = geoFile.getFile();
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
            if (file != null) {
                if (!file.delete()) {
                    fail("unable to delete test file");
                }
            }
        }
    }

    @Test
    public void testWrite() throws IOException, InvalidRangeException {
        final GeoFile geoFile = new GeoFile(14);
        final float[] longitudes = {115.39925f, 108.04573f, 108.04573f, 107.99058f, 97.13357f, 97.13357f, 97.13357f, 89.25099f, 89.25099f, 89.25099f, 98.02876f, 98.02876f, 99.472626f, 99.472626f};
        final float[] latitudes = {-46.212715f, -47.560722f, -47.560722f, -47.57288f, -49.25582f, -49.25582f, -49.25582f, -49.821648f, -49.821648f, -49.821648f, -48.872658f, -48.872658f, -48.64003f, -48.64003f};

        final Array longitudesArray = Array.factory(longitudes);
        final Array latitudesArray = Array.factory(latitudes);

        File file = null;
        NetcdfFile geoFileNC = null;
        try {
            geoFile.create();
            file = geoFile.getFile();
            assertTrue(file.isFile());

            geoFile.write(longitudesArray, latitudesArray);

            geoFile.close();

            geoFileNC = NetcdfFile.open(file.getAbsolutePath());
            NCTestUtils.assertScalarVariable("grid_dims", 0, 1, geoFileNC);
            NCTestUtils.assertScalarVariable("grid_dims", 1, 14, geoFileNC);

            NCTestUtils.assertScalarVariable("grid_center_lat", 0, -46.21271514892578, geoFileNC);
            NCTestUtils.assertScalarVariable("grid_center_lat", 1, -47.56072235107422, geoFileNC);

            NCTestUtils.assertScalarVariable("grid_center_lon", 2, 108.04573059082031, geoFileNC);
            NCTestUtils.assertScalarVariable("grid_center_lon", 3, 107.9905776977539, geoFileNC);

            NCTestUtils.assertScalarVariable("grid_imask", 4, 1, geoFileNC);
            NCTestUtils.assertScalarVariable("grid_imask", 5, 1, geoFileNC);
        } finally {
            geoFile.close();
            if (geoFileNC != null) {
                geoFileNC.close();
            }
            if (file != null) {
                if (!file.delete()) {
                    fail("unable to delete test file");
                }
            }
        }
    }
}
