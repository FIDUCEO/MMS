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

package com.bc.fiduceo.util;

import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.netcdf3.N3iosp;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;

public class NetCDFUtils {

    public static Number getDefaultFillValue(Array array) {
        final Class type = array.getDataType().getPrimitiveClassType();
        return getDefaultFillValue(type);
    }

    public static Number getDefaultFillValue(Class type) {
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

    public static Number getFillValue(Variable variable) {
        final Attribute fillValueAttribute = variable.findAttribute("_FillValue");
        if (fillValueAttribute != null) {
            return fillValueAttribute.getNumericValue();
        }

        final DataType dataType = variable.getDataType();
        return getDefaultFillValue(dataType.getClassType());
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
     * @return the NetCDF equivalent to the given dataType or {@code null} if not {@code dataType} is
     * not one of {@code ProductData.TYPE_*}
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

    public static String getGlobalAttributeString(String attributeName, NetcdfFile netcdfFile) throws IOException {
        final Attribute startDateAttribute = netcdfFile.findGlobalAttribute(attributeName);
        if (startDateAttribute == null) {
            throw new IOException("Required global attribute not found: " + attributeName);
        }
        return startDateAttribute.getStringValue();
    }

    public static Variable getVariable(NetcdfFile reader, String name) {
        final String escapedName = NetcdfFile.makeValidCDLName(name);
        final Variable variable = reader.findVariable(null, escapedName);
        if (variable == null) {
            throw new RuntimeException("Input Variable '" + name + "' not present in input file");
        }
        return variable;
    }

    /**
     * Method to open NetcdfFile using a read only RandomAccessFile.
     * This is needed because opening a netcdf file with NetcdfFile.open(<String>) changes the file size.
     *
     * @param absFileLocation absolute path to file
     * @return unmodifieable NetcdfFile instance
     * @throws IOException on IO errors
     */
    public static NetcdfFile openReadOnly(final String absFileLocation) throws IOException {
        final RandomAccessFile raf = new RandomAccessFile(absFileLocation, "r");
        return NetcdfFile.open(raf, absFileLocation, null, null);
    }

    public static int getDimensionLength(String dimensionName, NetcdfFile netcdfFile) {
        final Dimension dimension = netcdfFile.findDimension(dimensionName);
        if (dimension == null) {
            throw new RuntimeException("Dimension not present in file: " + dimensionName);
        }
        return dimension.getLength();
    }
}
