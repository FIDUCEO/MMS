package com.bc.fiduceo.reader.netcdf;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.*;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

public abstract class NetCDFReader implements Reader {

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

    protected double getScaleFactor(String variableName, String attributeName) throws IOException {
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

    protected void addLayered3DVariables(List<Variable> result, Variable variable, int numChannels, String variable_base_name, LayerExtension layerExtension) throws InvalidRangeException {
        final int[] origin = {0, 0, 0};
        addChannelVariables(result, variable, numChannels, 0, origin, variable_base_name, layerExtension);
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
        final StandardLayerExtension layerExtension = new StandardLayerExtension();
        addChannelVariables(result, variable, numChannels, channel_dimension_index, origin, variableName, layerExtension);
    }

    private void addChannelVariables(List<Variable> result, Variable variable, int numChannels, int channel_dimension_index, int[] origin, String variableName, LayerExtension layerExtension) throws InvalidRangeException {
        final int[] shape = variable.getShape();
        shape[channel_dimension_index] = 1;

        final String variableBaseName = variableName + "_ch";
        for (int channel = 0; channel < numChannels; channel++) {
            final Section section = new Section(origin, shape);
            final Variable channelVariable = variable.section(section);
            final String channelVariableName = variableBaseName + layerExtension.getExtension(channel);
            channelVariable.setName(channelVariableName);
            result.add(channelVariable);
            origin[channel_dimension_index]++;
        }
    }

    protected Array acquisitionTimeFromTimeLocator(int y, Interval interval) throws IOException {
        final int height = interval.getY();
        final int width = interval.getX();
        final int y_offset = y - height / 2;
        int[] shape = new int[]{height, width};

        final TimeLocator timeLocator = getTimeLocator();
        final int pHeight = getProductSize().getNy();

        final Array acquisitionTime = Array.factory(DataType.INT, shape);
        final Index index = acquisitionTime.getIndex();

        final int acquisitionTimeFillValue = getDefaultFillValue(int.class).intValue();

        for (int ya = 0; ya < height; ya++) {
            final int yRead = y_offset + ya;
            final int lineTimeInSeconds;
            if (yRead < 0 || yRead >= pHeight) {
                lineTimeInSeconds = acquisitionTimeFillValue;
            } else {
                final long lineTime = timeLocator.getTimeFor(0, yRead);
                lineTimeInSeconds = (int) (lineTime / 1000);
            }

            for (int xa = 0; xa < width; xa++) {
                index.set(ya, xa);
                acquisitionTime.setInt(index, lineTimeInSeconds);
            }
        }
        return acquisitionTime;
    }
}
