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

package com.bc.fiduceo.matchup.plot;

import com.bc.fiduceo.core.SamplingPoint;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class MapPlotTool {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        NetcdfFile netcdfFile = null;
        try {
            netcdfFile = NetcdfFile.open(args[0]);
            final Variable lonVariable = netcdfFile.findVariable("avhrr-n18_lon");
            final Variable latVariable = netcdfFile.findVariable("avhrr-n18_lat");
            final Variable timeVariable = netcdfFile.findVariable("avhrr-n18_acquisition_time");

            final int[] shape = lonVariable.getShape();
            shape[1] = 1;
            shape[2] = 1;
            final int[] offsets = new int[shape.length];
            offsets[0] = 0;
            offsets[1] = 2;
            offsets[2] = 2;

            final Array lonArray = lonVariable.read(offsets, shape);
            final Array latArray = latVariable.read(offsets, shape);
            final Array timeArray = timeVariable.read(offsets, shape);

            final ArrayList<SamplingPoint> samplingPoints = new ArrayList<>();
            for (int i = 0; i < shape[0] ; i++){
                final double lon = lonArray.getDouble(i);
                final double lat = latArray.getDouble(i);
                final int time = timeArray.getInt(i);
                samplingPoints.add(new SamplingPoint(lon, lat, time));
            }

            final BufferedImage plot = new SamplingPointPlotter().samples(samplingPoints).filePath("D:\\Satellite\\Fiduceo\\MMD02\\blabla.png").plot();

        } finally {
            if (netcdfFile != null) {
                netcdfFile.close();
            }
        }
    }
}
