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
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.MAMath;
import ucar.nc2.Variable;
import ucar.nc2.iosp.netcdf3.N3iosp;
import ucar.nc2.jni.netcdf.Nc4Iosp;

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

    public static Number getDefaultFillValue(int productDataType) {
        switch (productDataType) {
            case ProductData.TYPE_FLOAT64:
                return getDefaultFillValue(double.class);

            case ProductData.TYPE_FLOAT32:
                return getDefaultFillValue(float.class);

            case ProductData.TYPE_INT32:
                return getDefaultFillValue(int.class);

            case ProductData.TYPE_INT16:
                return getDefaultFillValue(short.class);

            case ProductData.TYPE_INT8:
                return getDefaultFillValue(byte.class);
        }
        throw new RuntimeException("getDefaultFillValue not implemented for type: " + productDataType);
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

    /**
     * Return the NetCDF equivalent to the given dataType.
     *
     * @param dataType must be one of {@code ProductData.TYPE_*}
     *
     * @return the NetCDF equivalent to the given dataType or {@code null} if not {@code dataType} is
     *         not one of {@code ProductData.TYPE_*}
     *
     * @see ProductData
     */
    public static DataType getNetcdfDataType(int dataType) {
        if (dataType == ProductData.TYPE_INT8 || dataType == ProductData.TYPE_UINT8) {
            return DataType.BYTE;
        } else if (dataType == ProductData.TYPE_INT16 || dataType == ProductData.TYPE_UINT16) {
            return DataType.SHORT;
        } else if (dataType == ProductData.TYPE_INT32 || dataType == ProductData.TYPE_UINT32) {
            return DataType.INT;
        } else if (dataType == ProductData.TYPE_FLOAT32) {
            return DataType.FLOAT;
        } else if (dataType == ProductData.TYPE_FLOAT64) {
            return DataType.DOUBLE;
        } else if (dataType == ProductData.TYPE_ASCII) {
            return DataType.STRING;
        } else if (dataType == ProductData.TYPE_UTC) {
            return DataType.STRING;
        } else {
            throw new RuntimeException("Data type not supported: " + dataType);
        }

        // @todo 2 tb/tb this method is copied from SNAP snap-netcdf org.esa.snap.dataio.netcdf.util.DataTypeUtils to avoid version
        // conflicts. Snap uses netcdf version 4.3.22, fiduceo is at version 4.6.5 2016-08-08
    }

    private static Number getDefaultFillValue(Class type) {
        if (double.class == type) {
            return N3iosp.NC_FILL_DOUBLE;
        } else if (float.class == type) {
            return N3iosp.NC_FILL_FLOAT;
        } else if (long.class == type) {
            return N3iosp.NC_FILL_LONG;
        } else if (int.class == type) {
            return N3iosp.NC_FILL_INT;
        } else if (short.class == type) {
            return N3iosp.NC_FILL_SHORT;
        } else if (byte.class == type) {
            return N3iosp.NC_FILL_BYTE;
        } else {
            throw new RuntimeException("not implemented for type " + type.getTypeName());
        }
    }
}
