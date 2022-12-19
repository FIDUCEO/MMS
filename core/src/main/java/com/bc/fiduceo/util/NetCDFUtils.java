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

import com.bc.fiduceo.reader.ReaderUtils;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.*;
import ucar.nc2.iosp.netcdf3.N3iosp;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;
import java.util.ArrayList;

public class NetCDFUtils {

    public static final String CF_FILL_VALUE_NAME = "_FillValue";
    public static final String CF_UNSIGNED = "_Unsigned";
    public static final String CF_SCALE_FACTOR_NAME = "scale_factor";
    public static final String CF_ADD_OFFSET_NAME = "add_offset";
    public static final String CF_VALID_RANGE_NAME = "valid_range";
    public static final String CF_UNITS_NAME = "units";
    public static final String CF_STANDARD_NAME = "standard_name";
    public static final String CF_LONG_NAME = "long_name";
    public static final String CF_FLAG_MEANINGS_NAME = "flag_meanings";
    public static final String CF_FLAG_MASKS_NAME = "flag_masks";
    public static final String CF_FLAG_VALUES_NAME = "flag_values";

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
            return N3iosp.NC_FILL_INT64;
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

    public static Number getDefaultFillValue(DataType type, boolean unsigned) {
        if (DataType.DOUBLE == type) {
            return N3iosp.NC_FILL_DOUBLE;
        } else if (DataType.FLOAT == type) {
            return N3iosp.NC_FILL_FLOAT;
        } else if (DataType.LONG == type) {
            return N3iosp.NC_FILL_INT64;
        } else if (DataType.INT == type) {
            if (unsigned) {
                return N3iosp.NC_FILL_UINT;
            }
            return N3iosp.NC_FILL_INT;
        } else if (DataType.SHORT == type) {
            if (unsigned) {
                return N3iosp.NC_FILL_USHORT;
            }
            return N3iosp.NC_FILL_SHORT;
        } else if (DataType.USHORT == type) {
            return N3iosp.NC_FILL_USHORT;
        } else if (DataType.BYTE == type) {
            if (unsigned) {
                return N3iosp.NC_FILL_UBYTE;
            }
            return N3iosp.NC_FILL_BYTE;
        } else {
            throw new RuntimeException("not implemented for type " + type.name());
        }
    }

    public static Number getFillValue(Variable variable) {
        final Attribute fillValueAttribute = variable.findAttribute(CF_FILL_VALUE_NAME);
        if (fillValueAttribute != null) {
            return fillValueAttribute.getNumericValue();
        }

        final DataType dataType = variable.getDataType();
        return getDefaultFillValue(dataType.getPrimitiveClassType());
    }

    public static Array toFloat(Array original) {
        if (original.getDataType() == DataType.FLOAT) {
            return original;
        }
        final Array floatArray = Array.factory(DataType.FLOAT, original.getShape());
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

    public static DataType escalateUnsignedType(DataType unsignedType) {
        if (unsignedType == DataType.BYTE || unsignedType == DataType.UBYTE) {
            return DataType.SHORT;
        } else if(unsignedType == DataType.SHORT || unsignedType == DataType.USHORT) {
            return DataType.INT;
        }else if(unsignedType == DataType.INT || unsignedType == DataType.UINT) {
            return DataType.LONG;
        } else {
            throw new RuntimeException("Data type not supported: " + unsignedType);
        }
    }

    public static String getGlobalAttributeString(String attributeName, NetcdfFile netcdfFile) {
        final Attribute globalAttribute = getGlobalAttributeSafe(attributeName, netcdfFile);
        return globalAttribute.getStringValue();
    }

    public static int getGlobalAttributeInt(String attributeName, NetcdfFile netcdfFile) {
        final Attribute attribute = getGlobalAttributeSafe(attributeName, netcdfFile);
        return attribute.getNumericValue().intValue();
    }

    public static double getGlobalAttributeDouble(String attributeName, NetcdfFile netcdfFile) {
        final Attribute attribute = getGlobalAttributeSafe(attributeName, netcdfFile);
        return attribute.getNumericValue().doubleValue();
    }

    public static int getAttributeInt(Variable variable, String name, int defaultValue) {
        final Attribute attribute = variable.findAttribute(name);
        if (attribute == null) {
            return defaultValue;
        }
        return attribute.getNumericValue().intValue();
    }

    public static float getAttributeFloat(Variable variable, String name, float defaultValue) {
        final Attribute attribute = variable.findAttribute(name);
        if (attribute == null) {
            return defaultValue;
        }
        return attribute.getNumericValue().floatValue();
    }

    public static double getAttributeDouble(Variable variable, String name, double defaultValue) {
        final Attribute attribute = variable.findAttribute(name);
        if (attribute == null) {
            return defaultValue;
        }
        return attribute.getNumericValue().doubleValue();
    }

    public static String getAttributeString(Variable variable, String name, String defaultValue) {
        final Attribute attribute = variable.findAttribute(name);
        if (attribute == null) {
            return defaultValue;
        }
        return attribute.getStringValue();
    }

    public static Variable getVariable(NetcdfFile reader, String name) {
        return getVariable(reader, name, true);
    }

    public static Variable getVariable(NetcdfFile reader, String name, boolean escapeName) {
        String escapedName;
        if (escapeName) {
            escapedName = escapeVariableName(name);
        } else {
            escapedName = name;
        }
        final Variable variable = reader.findVariable(null, escapedName);
        if (variable == null) {
            throw new RuntimeException("Input Variable '" + name + "' not present in input file");
        }
        return variable;
    }

    public static String escapeVariableName(String name) {
        String escapedName;
        escapedName = NetcdfFiles.makeValidCDLName(name);
        if (escapedName.contains(".")) {
            escapedName = escapedName.replace(".", "\\.");
        }
        return escapedName;
    }

    public static Variable getVariable(NetcdfFileWriter fileWriter, String name) {
        return getVariable(fileWriter, name, true);
    }

    public static Variable getVariable(NetcdfFileWriter fileWriter, String name, boolean escapeName) {
        String escapedName;
        if (escapeName) {
            escapedName = escapeVariableName(name);
        } else {
            escapedName = name;
        }
        final Variable variable = fileWriter.findVariable(escapedName);
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
     * @return unmodifiable NetcdfFile instance
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

    public static void ensureFillValue(Variable variable) {
        final Attribute attribute = variable.findAttribute(CF_FILL_VALUE_NAME);
        if (attribute != null) {
            return;
        }

        final DataType dataType = variable.getDataType();
        if (dataType.isNumeric()) {
            final Number fillValue = getDefaultFillValue(dataType.getPrimitiveClassType());
            variable.addAttribute(new Attribute(CF_FILL_VALUE_NAME, fillValue));
        }
    }

    public static double getDoubleValueFromAttribute(Variable variable, String attrName, final double defaultValue) {
        if (attrName != null) {
            final Attribute attribute = variable.findAttribute(attrName);
            if (attribute == null) {
                throw new RuntimeException("No attribute with name '" + attrName + "'.");
            }
            final Number number = attribute.getNumericValue();
            if (number == null) {
                throw new RuntimeException("Attribute '" + attrName + "' does not own a number value.");
            }
            return number.doubleValue();
        }
        return defaultValue;
    }

    public static float getFloatValueFromAttribute(Variable variable, String attrName, final float defaultValue) {
        if (attrName != null) {
            final Attribute attribute = variable.findAttribute(attrName);
            if (attribute == null) {
                throw new RuntimeException("No attribute with name '" + attrName + "'.");
            }
            final Number number = attribute.getNumericValue();
            if (number == null) {
                throw new RuntimeException("Attribute '" + attrName + "' does not own a number value.");
            }
            return number.floatValue();
        }
        return defaultValue;
    }

    public static String readString(Variable variable, int offset, int stringLength) throws IOException, InvalidRangeException {
        final Array singleStringArray = variable.read(new int[]{offset, 0}, new int[]{1, stringLength});
        return String.valueOf((char[]) singleStringArray.getStorage()).trim();
    }

    public static Array getCenterPosArrayFromMMDFile(NetcdfFile netcdfFile, String varShortName, String scaleAttrName, String offsetAttrName, String matchupCountDimName) throws IOException, InvalidRangeException {
        final Variable variable = getVariable(netcdfFile, varShortName);

        final int countIdx = variable.findDimensionIndex(matchupCountDimName);

        final int[] shape = variable.getShape();
        final int[] index = new int[shape.length];
        for (int i = 0; i < shape.length; i++) {
            int dimWith = shape[i];
            if (i != countIdx) {
                index[i] = dimWith / 2;
                shape[i] = 1;
            }
        }

        final Array array = variable.read(index, shape).reduce();

        double scaleFactor = getDoubleValueFromAttribute(variable, scaleAttrName, 1);
        double offset = getDoubleValueFromAttribute(variable, offsetAttrName, 0);
        if (scaleFactor != 1d || offset != 0d) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            return MAMath.convert2Unpacked(array, scaleOffset);
        }
        return array;
    }

    public static Array readAndScaleIfNecessary(Variable variable) throws IOException {
        final Array dataArray = variable.read();
        return scaleIfNecessary(variable, dataArray);
    }

    public static Array scaleIfNecessary(Variable variable, Array array) {
        final double scaleFactor = NetCDFUtils.getScaleFactor(variable);
        final double offset = NetCDFUtils.getOffset(variable);
        if (ReaderUtils.mustScale(scaleFactor, offset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            array = MAMath.convert2Unpacked(array, scaleOffset);
        }
        return array;
    }

    public static Attribute getGlobalAttributeSafe(String attributeName, NetcdfFile netcdfFile) {
        final Attribute globalAttribute = netcdfFile.findGlobalAttribute(attributeName);
        if (globalAttribute == null) {
            throw new RuntimeException("Required global attribute not found: " + attributeName);
        }
        return globalAttribute;
    }

    // @todo 2 tb/** write tests for this method 2022-07-18
    public static ArrayList<Attribute> getAttributes(Variable variable) {
        final AttributeContainer inAtts = variable.attributes();
        final double scaling = inAtts.findAttributeDouble(CF_SCALE_FACTOR_NAME, 1.0);
        final double offset = inAtts.findAttributeDouble(CF_ADD_OFFSET_NAME, 0.0);
        final Attribute fillAtt = inAtts.findAttribute(CF_FILL_VALUE_NAME);
        final Number newFill;
        if (fillAtt != null && (scaling != 1.0 || offset != 0.0)) {
            final Number value = fillAtt.getNumericValue();
            newFill = value.doubleValue() * scaling + offset;
        } else {
            newFill = null;
        }

        final ArrayList<Attribute> attributes = new ArrayList<>();
        inAtts.forEach(attribute -> {
            final String name = attribute.getShortName();
            if (CF_SCALE_FACTOR_NAME.equals(name)) {
                attributes.add(new Attribute(CF_SCALE_FACTOR_NAME, 1.0F));
            } else if (CF_ADD_OFFSET_NAME.equals(name)) {
                attributes.add(new Attribute(CF_ADD_OFFSET_NAME, 0.0F));
            } else if (newFill != null && CF_FILL_VALUE_NAME.equals(name)) {
                attributes.add(new Attribute(CF_FILL_VALUE_NAME, newFill.floatValue()));
            } else if (!(name.contains("_Swath_Sampling") || CF_VALID_RANGE_NAME.equals(name))) {
                attributes.add(attribute);
            }
        });
        return attributes;
    }


    public static double getScaleFactor(Variable variable) {
        double scaleFactor = NetCDFUtils.getAttributeDouble(variable, NetCDFUtils.CF_SCALE_FACTOR_NAME, Double.NaN);
        if (Double.isNaN(scaleFactor)) {
            scaleFactor = NetCDFUtils.getAttributeFloat(variable, "Scale", Float.NaN);
        }
        if (Double.isNaN(scaleFactor)) {
            return 1.f;
        }
        return scaleFactor;
    }

    public static double getOffset(Variable variable) {
        double scaleFactor = NetCDFUtils.getAttributeDouble(variable, NetCDFUtils.CF_ADD_OFFSET_NAME, Double.NaN);
        if (Double.isNaN(scaleFactor)) {
            return 0.f;
        }
        return scaleFactor;
    }

    public static Array section(Array array, int[] offsets, int[] shape) throws IOException {
        try {
            return array.section(offsets, shape);
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static Array create(byte[] data) {
        final int[] shape = new int[]{data.length};
        return Array.factory(DataType.BYTE, shape, data);
    }

    public static Array create(byte[][] data) {
        final int lineLength = data[0].length;
        final byte[] flatData = new byte[data.length * lineLength];
        for (int n = 0; n < data.length; n++) {
            System.arraycopy(data[n], 0, flatData, n * lineLength, lineLength);
        }
        final int[] shape = new int[]{data.length, lineLength};
        return Array.factory(DataType.BYTE, shape, flatData);
    }

    public static Array create(char[] data) {
        final int[] shape = new int[]{data.length};
        return Array.factory(DataType.CHAR, shape, data);
    }

    public static Array create(char[][] data) {
        return Array.makeFromJavaArray(data);
    }

    public static Array create(short[] data) {
        final int[] shape = new int[]{data.length};
        return Array.factory(DataType.SHORT, shape, data);
    }

    public static Array create(short[][] data) {
        return Array.makeFromJavaArray(data);
    }

    public static Array create(int[] data) {
        final int[] shape = new int[]{data.length};
        return Array.factory(DataType.INT, shape, data);
    }

    public static Array create(int[][] data) {
        return Array.makeFromJavaArray(data);
    }

    public static Array create(long[] data) {
        final int[] shape = new int[]{data.length};
        return Array.factory(DataType.LONG, shape, data);
    }

    public static Array create(long[][] data) {
        return Array.makeFromJavaArray(data);
    }

    public static Array create(float[] data) {
        final int[] shape = new int[]{data.length};
        return Array.factory(DataType.FLOAT, shape, data);
    }

    public static Array create(float[][] data) {
        return Array.makeFromJavaArray(data);
    }

    public static Array create(double[] data) {
        final int[] shape = new int[]{data.length};
        return Array.factory(DataType.DOUBLE, shape, data);
    }

    public static Array create(double[][] data) {
        final int linelength = data[0].length;
        double[] flatData = new double[data.length * linelength];
        for (int n = 0; n < data.length; n++) {
            System.arraycopy(data[n], 0, flatData, n * linelength, linelength);
        }
        final int[] shape = new int[]{data.length, linelength};
        return Array.factory(DataType.DOUBLE, shape, flatData);
    }

    public static Array create(DataType dataType, int[] shape, Number fillValue) {
        final Array array = Array.factory(dataType, shape);
        final int size = (int) array.getSize();
        for (int i = 0; i < size; i++) {
            array.setObject(i, fillValue);
        }

        return array;
    }
}
