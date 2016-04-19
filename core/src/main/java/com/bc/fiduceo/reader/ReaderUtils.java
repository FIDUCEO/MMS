/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader;

import ucar.ma2.Array;
import ucar.nc2.Variable;


public class ReaderUtils {

    public static Number getDefaultFillValue(Variable variable) {
        final Class type = variable.getDataType().getPrimitiveClassType();
        return getDefaultFillValue(type);
    }

    public static Number getDefaultFillValue(Array array) {
        final Class type = array.getDataType().getPrimitiveClassType();
        return getDefaultFillValue(type);
    }

    private static Number getDefaultFillValue(Class type) {
        if (double.class == type) {
            return Double.MIN_VALUE;
        } else if (float.class == type) {
            return Float.MIN_VALUE;
        } else if (long.class == type) {
            return Long.MIN_VALUE;
        } else if (int.class == type) {
            return Integer.MIN_VALUE;
        } else if (short.class == type) {
            return Short.MIN_VALUE;
        } else if (byte.class == type) {
            return Byte.MIN_VALUE;
        } else {
            throw new RuntimeException("not implemented for type " + type.getTypeName());
        }
    }
}
