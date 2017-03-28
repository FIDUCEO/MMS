package com.bc.fiduceo.post.plugin.nwp;

import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;

abstract class Strategy {

    abstract void prepare(Context context);

    abstract File writeGeoFile(Context context) throws IOException, InvalidRangeException;

}
