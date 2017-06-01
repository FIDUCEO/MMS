package com.bc.fiduceo.post.plugin;


import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ElevationToSolZenAngle extends PostProcessing {

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    // package access for testing only tb 2017-06-01
    static Configuration createConfiguration(Element rootElement) {
        return new Configuration();
    }

    static class Configuration {
        List<Conversion> conversions = new ArrayList<>();
    }

    static class Conversion {

    }
}
