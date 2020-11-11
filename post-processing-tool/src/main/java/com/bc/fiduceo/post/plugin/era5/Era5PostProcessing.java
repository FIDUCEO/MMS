package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.post.PostProcessing;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;

class Era5PostProcessing extends PostProcessing {
    Era5PostProcessing(Configuration configuration) {
        super();
    }


    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        // @todo 1 tb/tb implement 2020-11-11
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        // @todo 1 tb/tb implement 2020-11-11
    }
}
