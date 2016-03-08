package com.bc.fiduceo.location;

import org.esa.snap.core.datamodel.SnapAvoidCodeDuplicationClass_SwathPixelLocator;
import ucar.ma2.Array;


public class SwathPixelLocator extends SnapAvoidCodeDuplicationClass_SwathPixelLocator {

    public SwathPixelLocator(Array lonArray, Array latArray, int width, int height) {
        super(lonArray, latArray, width, height);
    }
}
