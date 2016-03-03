package com.bc.fiduceo.location;

import java.awt.Rectangle;

/**
* @author Ralf Quast
*/
interface SteppingFactory {

    Stepping createStepping(Rectangle rectangle, int maxPointCount);
}
