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
import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MapPlotTool {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        NetcdfFile netcdfFile = null;
        try {
            final String filePath = args[0];
            netcdfFile = NetcdfFile.open(filePath);

            final Variable lonVariable = netcdfFile.findVariable(escape(args[1]));
            final Variable latVariable = netcdfFile.findVariable(escape(args[2]));
            final Variable timeVariable = netcdfFile.findVariable(escape(args[3]));

            final int[] shape = lonVariable.getShape();
            final int[] offsets = new int[shape.length];
            offsets[0] = 0;
            offsets[1] = shape[1]/2;
            offsets[2] = shape[2]/2;
            shape[1] = 1;
            shape[2] = 1;

            Array lonArray = readScaledIfRequired(lonVariable,offsets, shape);
            lonArray = shiftIfRequired(lonArray);
            final Array latArray = readScaledIfRequired(latVariable, offsets, shape);
            final Array timeArray = timeVariable.read(offsets, shape);

            final ArrayList<SamplingPoint> samplingPoints = new ArrayList<>();
            for (int i = 0; i < shape[0]; i++) {
                final double lon = lonArray.getDouble(i);
                final double lat = latArray.getDouble(i);
                final int time = timeArray.getInt(i);
                samplingPoints.add(new SamplingPoint(lon, lat, time));
            }

            final String fileName = FileUtils.getFileNameFromPath(filePath);
            final String pngFileName = FileUtils.exchangeExtension(fileName, ".png");
            final String outputDir = new File(filePath).getParent();
            //final File targetFile = new File(outputDir, args[4]);
            final File targetFile = new File(outputDir, pngFileName);
            final BufferedImage plot = new SamplingPointPlotter()
                    .mapStrategyName(SamplingPointPlotter.LON_LAT)
                    .samples(samplingPoints)
                    .filePath(targetFile.getAbsolutePath())
                    .plot();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("usage: map-plot-tool <filepath> <lon-variable> <lat-variable> <time-variable>");
        } finally {
            if (netcdfFile != null) {
                netcdfFile.close();
            }
        }
    }

    private static String escape(String vname) {
        return NetcdfFile.makeValidCDLName(vname);
    }

    private static Array shiftIfRequired(Array lonArray) {
        final IndexIterator indexIterator = lonArray.getIndexIterator();

        boolean mustShift = false ;
        while(indexIterator.hasNext())  {
            final float lonVal = indexIterator.getFloatNext();
            if (lonVal > 180.0) {
                mustShift = true;
                break;
            }
        }

        if (mustShift) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(1.0, -180.0);
            return MAMath.convert2Unpacked(lonArray, scaleOffset);
        } else {
            return lonArray;
        }
    }

    private static Array readScaledIfRequired(Variable variable, int[] offsets, int[] shape) throws IOException, InvalidRangeException {
        final Array array = variable.read(offsets, shape);

        double scale = 1.0;
        double offset = 0.0;
        boolean mustScale = false;

        final Attribute scaleAttribute = variable.findAttribute("Scale");
        if (scaleAttribute != null) {
            scale = scaleAttribute.getNumericValue().doubleValue();
            mustScale = true;
        }

        if (mustScale) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scale, offset);
            return MAMath.convert2Unpacked(array, scaleOffset);
        } else {
            return array;
        }
    }
}
