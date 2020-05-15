package com.bc.fiduceo.reader.netcdf;

import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

public abstract class NetCDFReader implements Reader {

    private static final NumberFormat CHANNEL_INDEX_FORMAT = new DecimalFormat("00");

    protected ArrayCache arrayCache;
    protected NetcdfFile netcdfFile;

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFiles.open(file.getPath());
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

    private double getScaleFactor(String variableName, String attributeName) throws IOException {
        return getScaleFactor(variableName, attributeName, true);
    }

    protected double getScaleFactor(String variableName, String attributeName, boolean escapeName) throws IOException {
        if (attributeName == null) {
            attributeName = CF_SCALE_FACTOR_NAME;
        }
        final String escapedName;
        if (escapeName) {
            escapedName = NetCDFUtils.escapeVariableName(variableName);
        } else {
            escapedName = variableName;
        }
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
            escapedName = NetCDFUtils.escapeVariableName(variableName);
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

    protected void addLayered3DVariables(List<Variable> result, Variable variable, int numChannels, int channel_dimension_index) throws InvalidRangeException {
        final int[] origin = {0, 0, 0};
        addChannelVariables(result, variable, numChannels, channel_dimension_index, origin);
    }

    protected void addLayered3DVariables(List<Variable> result, Variable variable, int numChannels, int channel_dimension_index, String variable_base_name) throws InvalidRangeException {
        final int[] origin = {0, 0, 0};
        addChannelVariables(result, variable, numChannels, channel_dimension_index, origin, variable_base_name);
    }

    protected void addChannelVectorVariables(List<Variable> result, Variable variable, int numChannels, int channel_dimension_index) throws InvalidRangeException {
        final int[] origin = {0, 0};
        addChannelVariables(result, variable, numChannels, channel_dimension_index, origin);
    }

    private void addChannelVariables(List<Variable> result, Variable variable, int numChannels, int channel_dimension_index, int[] origin) throws InvalidRangeException {
        final String variableName = variable.getFullName();
        addChannelVariables(result, variable, numChannels, channel_dimension_index, origin, variableName);
    }

    private void addChannelVariables(List<Variable> result, Variable variable, int numChannels, int channel_dimension_index, int[] origin, String variableName) throws InvalidRangeException {
        final int[] shape = variable.getShape();
        shape[channel_dimension_index] = 1;

        final String variableBaseName = variableName + "_ch";
        for (int channel = 0; channel < numChannels; channel++) {
            final Section section = new Section(origin, shape);
            final Variable channelVariable = variable.section(section);
            final String channelVariableName = variableBaseName + CHANNEL_INDEX_FORMAT.format(channel + 1);
            channelVariable.setName(channelVariableName);
            result.add(channelVariable);
            origin[channel_dimension_index]++;
        }
    }
}
