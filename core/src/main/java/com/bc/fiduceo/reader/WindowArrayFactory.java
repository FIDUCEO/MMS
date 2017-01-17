/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.reader;

import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayShort;

class WindowArrayFactory {

    static ArrayDouble.D2 createDoubleArray(int width, int height) {
        return new ArrayDouble.D2(height, width);
    }

    static ArrayFloat.D2 createFloatArray(int width, int height) {
        return new ArrayFloat.D2(height, width);
    }

    static ArrayLong.D2 createLongArray(int width, int height) {
        return new ArrayLong.D2(height, width);
    }

    static ArrayInt.D2 createIntArray(int width, int height) {
        return new ArrayInt.D2(height, width);
    }

    static ArrayShort.D2 createShortArray(int width, int height) {
        return new ArrayShort.D2(height, width);
    }

    static ArrayByte.D2 createByteArray(int width, int height) {
        return new ArrayByte.D2(height, width);
    }
}
