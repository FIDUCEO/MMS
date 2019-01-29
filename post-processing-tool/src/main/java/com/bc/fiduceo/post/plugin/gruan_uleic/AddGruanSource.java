package com.bc.fiduceo.post.plugin.gruan_uleic;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.post.PostProcessing;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AddGruanSource extends PostProcessing {

    private final Configuration configuration;

    AddGruanSource(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final List<Dimension> dimensions = reader.getDimensions();

        final ArrayList<Dimension> targetDimensions = extractTargetDimensions(dimensions);

        writer.addVariable(null, configuration.targetVariableName, DataType.CHAR, targetDimensions);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {

    }

    static ArrayList<Dimension> extractTargetDimensions(List<Dimension> dimensions) {
        final ArrayList<Dimension> resultList = new ArrayList<>();
        for (final Dimension dimension : dimensions) {
            final String name = dimension.getShortName();
            if (name.equals(FiduceoConstants.MATCHUP_COUNT) || name.equals("file_name")) {
                resultList.add(dimension);
            }
        }

        if (resultList.size() != 2) {
            throw new RuntimeException("Required dimensions not present");
        }

        return resultList;
    }

    static class Configuration {
        String targetVariableName;
        String yCoordinateName;
        String filenameVariableName;
        String processingVersionVariableName;
    }
}
