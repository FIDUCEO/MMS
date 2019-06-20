/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.post.util;

import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.bc.fiduceo.util.NetCDFUtils.CF_SCALE_FACTOR_NAME;

public class DistanceToLandMap {

    private static int instanceCount = 0;
    private final Path distancePath;
    private static NetcdfFile ncFile;
    private static Array array;
    private static Index index;
    private static double lonF;
    private static double latF;
    private static int maxLonIdx;
    private static int maxLatIdx;
    private static double scaleFactor;

    public DistanceToLandMap(Path path) {
        distancePath = path;
        validatePath();

        if (instanceCount == 0) {
            init();
        }
        instanceCount++;
    }

    public double getDistance(double longitude, double latitude) {
        int latIdx = getLatIdx(latitude);
        int lonIdx = getLonIdx(longitude);
        index.set(latIdx, lonIdx);
        return array.getDouble(index) * scaleFactor;
    }

    public void close() {
        instanceCount--;
        if (instanceCount == 0) {
            try {
                ncFile.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to close DistanceToLandMap.", e);
            }
            array = null;
            index = null;
        }
    }

    private int getLonIdx(double lon) {
        double shiftedLon = lon + 180;
        return Math.min(maxLonIdx, (int) Math.floor(shiftedLon * lonF));
    }

    private int getLatIdx(double lat) {
        double shiftedLat = lat * -1 + 90;
        return Math.min(maxLatIdx, (int) Math.floor(shiftedLat * latF));
    }

    private void init() {
        final String absolutePathString = distancePath.toAbsolutePath().toString();
        try {
            ncFile = NetCDFUtils.openReadOnly(absolutePathString);
            Variable distance_to_land = ncFile.findVariable(ncFile.getRootGroup(), "distance_to_land");
            scaleFactor = distance_to_land.findAttribute(CF_SCALE_FACTOR_NAME).getNumericValue().doubleValue();
            array = distance_to_land.read();
            index = array.getIndex();
            final int width = distance_to_land.getDimension(1).getLength();
            maxLonIdx = width - 1;
            lonF = width / 360.0;
            final int height = distance_to_land.getDimension(0).getLength();
            maxLatIdx = height - 1;
            latF = height / 180.0;
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize DistanceToLandMap.", e);
        }
    }

    private void validatePath() {
        final String absolutePathString = distancePath.toAbsolutePath().toString();
        if (!Files.isRegularFile(distancePath)) {
            throw new RuntimeException("Missing file: '" + absolutePathString + "'");
        }
        if (!Files.isReadable(distancePath)) {
            throw new RuntimeException("No read access to: '" + absolutePathString + "'");
        }
    }
}
