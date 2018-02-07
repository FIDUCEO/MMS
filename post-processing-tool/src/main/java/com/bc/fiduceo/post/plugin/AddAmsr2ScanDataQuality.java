package com.bc.fiduceo.post.plugin;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AddAmsr2ScanDataQuality extends PostProcessing {

    private Configuration configuration;

    void configure(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Dimension matchupDimension = reader.findDimension("matchup_count");

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(matchupDimension);
        dimensions.add(new Dimension("scan_data_quality", 512));
        final Variable variable = writer.addVariable(null, configuration.targetVariableName, DataType.BYTE, dimensions);
        variable.addAttribute(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class).byteValue()));
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    static class Configuration {
        String referenceVariableName;
        String targetVariableName;
    }
}
