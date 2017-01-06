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


import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;

class GeoFile {

    private File tempFile;
    private NetcdfFileWriter writer;
    private final int numMatchups;

    GeoFile(int numMatchups) {
        this.numMatchups = numMatchups;
    }

    void create() throws IOException {
        tempFile = File.createTempFile("geo", ".nc");

        writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, tempFile.getAbsolutePath());
        writer.setLargeFile(true);

        writer.addDimension(null, "grid_size", numMatchups);
        writer.addDimension(null, "grid_matchup", numMatchups);
        writer.addDimension(null, "grid_ny", 1);
        writer.addDimension(null, "grid_nx", 1);
        writer.addDimension(null, "grid_corners", 4);
        writer.addDimension(null, "grid_rank", 2);

        final Variable gridDims = writer.addVariable(null, "grid_dims", DataType.INT, "grid_rank");

        final Variable gridCenterLat = writer.addVariable(null, "grid_center_lat", DataType.FLOAT, "grid_size");
        gridCenterLat.addAttribute(new Attribute("units", "degrees"));

        final Variable gridCenterLon = writer.addVariable(null, "grid_center_lon", DataType.FLOAT, "grid_size");
        gridCenterLon.addAttribute(new Attribute("units", "degrees"));

        final Variable gridMask = writer.addVariable(null, "grid_imask", DataType.INT, "grid_size");
        writer.addVariable(null, "grid_corner_lat", DataType.FLOAT, "grid_size grid_corners");
        writer.addVariable(null, "grid_corner_lon", DataType.FLOAT, "grid_size grid_corners");
        writer.addGroupAttribute(null, new Attribute("title", "MMD geo-location in SCRIP format"));

        writer.create();
        writer.flush();
    }

    void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    File getFile() {
        return tempFile;
    }
}
