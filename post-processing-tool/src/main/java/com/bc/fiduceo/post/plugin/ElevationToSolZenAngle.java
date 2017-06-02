package com.bc.fiduceo.post.plugin;


import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.util.JDomUtils;
import org.esa.snap.core.util.StringUtils;
import org.jdom.Element;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ElevationToSolZenAngle extends PostProcessing {

    ElevationToSolZenAngle(Configuration configuration) {

    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    static class Configuration {
        List<Conversion> conversions = new ArrayList<>();
    }

    static class Conversion {
        String sourceName;
        String targetName;
        boolean removeSource;
    }
}
