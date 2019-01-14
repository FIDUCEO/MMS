package com.bc.fiduceo.reader.netcdf;

import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_OFFSET_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_SCALE_FACTOR_NAME;

public abstract class NetCDFReader implements Reader {

    protected ArrayCache arrayCache;
    protected NetcdfFile netcdfFile;

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);
    }

    @Override
    public void close() throws IOException {
        arrayCache = null;

        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    protected Number getFillValue(String variableName) throws IOException {
        final Number fillValue = arrayCache.getNumberAttributeValue(CF_FILL_VALUE_NAME, variableName);
        if (fillValue != null) {
            return fillValue;
        }
        final Array array = arrayCache.get(variableName);
        return NetCDFUtils.getDefaultFillValue(array);
    }

    protected double getScaleFactor(String variableName, String attributeName) throws IOException {
        if (attributeName == null) {
            attributeName = CF_SCALE_FACTOR_NAME;
        }

        final String escapedName = NetcdfFile.makeValidCDLName(variableName);
        final Number scaleFactorValue = arrayCache.getNumberAttributeValue(attributeName, escapedName);
        if (scaleFactorValue != null) {
            return scaleFactorValue.doubleValue();
        }
        return 1.0;
    }

    protected double getScaleFactorCf(String variableName) throws IOException {
        return getScaleFactor(variableName, null);
    }

    protected double getScaleFactor(String groupName, String variableName, String attributeName, boolean escapeName) throws IOException {
        if (attributeName == null) {
            attributeName = CF_SCALE_FACTOR_NAME;
        }
        String escapedName;
        if (escapeName) {
            escapedName = NetcdfFile.makeValidCDLName(variableName);
        } else {
            escapedName = variableName;
        }
        final Number scaleFactorValue = arrayCache.getNumberAttributeValue(attributeName, groupName, escapedName);
        if (scaleFactorValue != null) {
            return scaleFactorValue.doubleValue();
        }
        return 1.0;
    }

    protected double getScaleFactorCf(String groupName, String variableName) throws IOException {
        return getScaleFactor(groupName, variableName, null, true);
    }

    protected double getOffset(String variableName) throws IOException {
        final Number offsetValue = arrayCache.getNumberAttributeValue(CF_OFFSET_NAME, variableName);
        if (offsetValue != null) {
            return offsetValue.doubleValue();
        }
        return 0.0;
    }

    protected double getOffset(String groupName, String variableName) throws IOException {
        return getOffset(groupName, variableName, null);
    }

    protected double getOffset(String groupName, String variableName, String attributeName) throws IOException {
        if (attributeName == null) {
            attributeName = CF_OFFSET_NAME;
        }
        final Number offsetValue = arrayCache.getNumberAttributeValue(attributeName, groupName, variableName);
        if (offsetValue != null) {
            return offsetValue.doubleValue();
        }
        return 0.0;
    }
}
