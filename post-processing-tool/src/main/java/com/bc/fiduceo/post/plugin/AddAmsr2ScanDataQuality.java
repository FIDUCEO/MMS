package com.bc.fiduceo.post.plugin;

import com.bc.fiduceo.post.PostProcessing;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;

public class AddAmsr2ScanDataQuality extends PostProcessing {

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    static class Configuration {
        String targetVariableName;
    }
}
