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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.math.TimeInterval;
import ucar.ma2.Array;
import ucar.ma2.MAMath;
import ucar.nc2.Variable;

import java.util.Date;


public class ReaderUtils {

    public static Number getDefaultFillValue(Variable variable) {
        final Class type = variable.getDataType().getPrimitiveClassType();
        return getDefaultFillValue(type);
    }

    public static Number getDefaultFillValue(Array array) {
        final Class type = array.getDataType().getPrimitiveClassType();
        return getDefaultFillValue(type);
    }

    public static boolean mustScale(double scaleFactor, double offset) {
        return scaleFactor != 1.0 || offset != 0.0;
    }

    public static void setTimeAxes(AcquisitionInfo acquisitionInfo, Geometries geometries, GeometryFactory geometryFactory) {
        final Date sensingStart = acquisitionInfo.getSensingStart();
        final Date sensingStop = acquisitionInfo.getSensingStop();
        final Geometry timeAxesGeometry = geometries.getTimeAxesGeometry();
        if (timeAxesGeometry instanceof GeometryCollection) {
            final GeometryCollection axesCollection = (GeometryCollection) timeAxesGeometry;
            final Geometry[] axesGeometries = axesCollection.getGeometries();
            final TimeAxis[] timeAxes = new TimeAxis[axesGeometries.length];
            final TimeInterval timeInterval = new TimeInterval(sensingStart, sensingStop);
            final TimeInterval[] timeSplits = timeInterval.split(axesGeometries.length);
            for (int i = 0; i < axesGeometries.length; i++) {
                final LineString axisGeometry = (LineString) axesGeometries[i];
                final TimeInterval currentTimeInterval = timeSplits[i];
                timeAxes[i] = geometryFactory.createTimeAxis(axisGeometry, currentTimeInterval.getStartTime(), currentTimeInterval.getStopTime());
            }
            acquisitionInfo.setTimeAxes(timeAxes);
        } else {
            final TimeAxis timeAxis = geometryFactory.createTimeAxis((LineString) timeAxesGeometry, sensingStart, sensingStop);
            acquisitionInfo.setTimeAxes(new TimeAxis[]{timeAxis});
        }
    }

    public static String stripChannelSuffix(String fullVariableName) {
        final int splitIndex = fullVariableName.indexOf("_ch");
        if (splitIndex > 0) {
            return fullVariableName.substring(0, splitIndex);
        }
        return fullVariableName;
    }

    public static int getChannelIndex(String variableName) {
        final int splitIndex = variableName.lastIndexOf("_ch");
        if (splitIndex < 0) {
            return 0;
        }
        final String channelNumber = variableName.substring(splitIndex + 3);

        int channelIndex;
        try {
            channelIndex = Integer.parseInt(channelNumber) - 1;
        } catch (NumberFormatException e) {
            channelIndex = 0;   // fallback for names that contain an "_ch" but are not assembled like "bla_ch08"tb 2016-08-03
        }
        return channelIndex;
    }

    public static Array toFloat(Array original) {
        final Array floatArray = Array.factory(Float.class, original.getShape());
        MAMath.copyFloat(floatArray, original);
        return floatArray;
    }

    private static Number getDefaultFillValue(Class type) {
        if (double.class == type) {
            return Double.MIN_VALUE;
        } else if (float.class == type) {
            return Float.MIN_VALUE;
        } else if (long.class == type) {
            return Long.MIN_VALUE;
        } else if (int.class == type) {
            return Integer.MIN_VALUE;
        } else if (short.class == type) {
            return Short.MIN_VALUE;
        } else if (byte.class == type) {
            return Byte.MIN_VALUE;
        } else {
            throw new RuntimeException("not implemented for type " + type.getTypeName());
        }
    }
}
