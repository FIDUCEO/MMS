package com.bc.fiduceo.location;

import org.esa.snap.core.datamodel.SnapAvoidCodeDuplicationClass_SwathPixelLocator;
import ucar.ma2.Array;


class SwathPixelLocator extends SnapAvoidCodeDuplicationClass_SwathPixelLocator {

    SwathPixelLocator(Array lonArray, Array latArray, int width, int height) {
        super(lonArray, latArray, width, height);
    }
}
