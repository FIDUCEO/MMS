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
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

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
}
