package com.bc.fiduceo.post.plugin;


import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ElevationToSolZenAngle extends PostProcessing {

    private final Configuration configuration;

    ElevationToSolZenAngle(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected List<String> getVariableNamesToRemove() {
        final List<String> namesList = new ArrayList<>();
        for (final Conversion conversion : configuration.conversions) {
            if (conversion.removeSource) {
                namesList.add(conversion.sourceName);
            }
        }
        return namesList;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        for (final Conversion conversion : configuration.conversions) {
            final Variable variable = NetCDFUtils.getVariable(reader, conversion.sourceName);
            final List<Dimension> dimensions = variable.getDimensions();

            final Variable newVariable = writer.addVariable(null, conversion.targetName, DataType.FLOAT, dimensions);
            newVariable.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(float.class)));
        }
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        for (final Conversion conversion : configuration.conversions) {
            final Variable sourceVariable = NetCDFUtils.getVariable(reader, conversion.sourceName);
            final Variable targetVariable = NetCDFUtils.getVariable(writer, conversion.targetName);

            final Array sourceData = sourceVariable.read();
            final Array targetData = Array.factory(float.class, sourceData.getShape());

            final Number fillValue = NetCDFUtils.getFillValue(sourceVariable);

            calculateZenithAngle(sourceData, targetData, fillValue.floatValue());

            writer.write(targetVariable, targetData);
        }
    }

    // package access for testing only tb 2017-06-06
    static void calculateZenithAngle(Array sourceData, Array targetData, float fillValue) {
        final IndexIterator sourceIterator = sourceData.getIndexIterator();
        final IndexIterator targetIterator = targetData.getIndexIterator();
        while(sourceIterator.hasNext()) {
            final float elevation = sourceIterator.getFloatNext();
            if (elevation != fillValue) {
                final float zenithAngle = 90.f - elevation;
                targetIterator.setFloatNext(zenithAngle);
            } else {
                targetIterator.setFloatNext(NetCDFUtils.getDefaultFillValue(float.class).floatValue());
            }
        }
    }

    static class Configuration {
        List<Conversion> conversions = new ArrayList<>();
    }

    static class Conversion {
        String sourceName;
        String targetName;
        boolean removeSource;

        Conversion(String sourceName, String targetName, boolean removeSource) {
            this.sourceName = sourceName;
            this.targetName = targetName;
            this.removeSource = removeSource;
        }
    }
}
