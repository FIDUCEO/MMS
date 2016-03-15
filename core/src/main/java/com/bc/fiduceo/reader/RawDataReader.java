/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayShort;
import ucar.ma2.InvalidRangeException;

import java.awt.Rectangle;

/**
 * @author muhammad.bc
 * @author sabine.bc
 */
public class RawDataReader {

    public static Array read(int centerX, int centerY, Interval interval, Number fillValue, Array rawArray) throws InvalidRangeException {
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        final int[] shape = rawArray.getShape();
        final int rank = rawArray.getRank();
        final int rawHeight;
        final int rawWidth;
        if (rank == 3 && shape[0] == 1) {
            rawHeight = shape[1];
            rawWidth = shape[2];
        } else if(rank == 2){
            rawHeight = shape[0];
            rawWidth = shape[1];
        } else {
            throw new RuntimeException("not supported case");
        }
        final int offsetX = centerX - windowWidth / 2;
        final int offsetY = centerY - windowHeight / 2;

        boolean windowInside = isWindowInside(offsetX, offsetY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            if (rank == 3) {
                return rawArray.section(new int[]{0, offsetY, offsetX}, new int[]{1, windowHeight, windowWidth});
            } else {
                return rawArray.section(new int[]{offsetY, offsetX}, new int[]{windowHeight, windowWidth});
            }
        }

        if (rank == 2) {
            return readFrom2DArray(offsetX, offsetY, windowWidth, windowHeight, fillValue, rawArray, rawWidth, rawHeight);
        } else if (rank == 3) {
            return readFrom3DArray(offsetX, offsetY, windowWidth, windowHeight, fillValue, rawArray, rawWidth, rawHeight);
        } else {
            throw new RuntimeException("not implemented");
        }
    }

    static Array readFrom2DArray(int offsetX, int offsetY, int windowWidth, int windowHeight, Number fillValue, Array rawArray, int rawWidth, int rawHeight) {
        final Class elementType = rawArray.getElementType();
        if (elementType == double.class) {
            return WindowReader2dFrom2d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.doubleValue(), (ArrayDouble.D2) rawArray, rawWidth, rawHeight);
        } else if (elementType == float.class) {
            return WindowReader2dFrom2d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.floatValue(), (ArrayFloat.D2) rawArray, rawWidth, rawHeight);
        } else if (elementType == long.class) {
            return WindowReader2dFrom2d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.longValue(), (ArrayLong.D2) rawArray, rawWidth, rawHeight);
        } else if (elementType == int.class) {
            return WindowReader2dFrom2d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.intValue(), (ArrayInt.D2) rawArray, rawWidth, rawHeight);
        } else if (elementType == short.class) {
            return WindowReader2dFrom2d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.shortValue(), (ArrayShort.D2) rawArray, rawWidth, rawHeight);
        } else if (elementType == byte.class) {
            return WindowReader2dFrom2d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.byteValue(), (ArrayByte.D2) rawArray, rawWidth, rawHeight);
        } else {
            throw new RuntimeException("Datatype not implemented");
        }
    }

    static Array readFrom3DArray(int offsetX, int offsetY, int windowWidth, int windowHeight, Number fillValue, Array rawArray, int rawWidth, int rawHeight) {
        final Class elementType = rawArray.getElementType();
        if (elementType == double.class) {
            return WindowReader2dFrom3d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.doubleValue(), (ArrayDouble.D3) rawArray, rawWidth, rawHeight);
        } else if (elementType == float.class) {
            return WindowReader2dFrom3d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.floatValue(), (ArrayFloat.D3) rawArray, rawWidth, rawHeight);
        } else if (elementType == long.class) {
            return WindowReader2dFrom3d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.longValue(), (ArrayLong.D3) rawArray, rawWidth, rawHeight);
        } else if (elementType == int.class) {
            return WindowReader2dFrom3d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.intValue(), (ArrayInt.D3) rawArray, rawWidth, rawHeight);
        } else if (elementType == short.class) {
            return WindowReader2dFrom3d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.shortValue(), (ArrayShort.D3) rawArray, rawWidth, rawHeight);
        } else if (elementType == byte.class) {
            return WindowReader2dFrom3d.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.byteValue(), (ArrayByte.D3) rawArray, rawWidth, rawHeight);
        } else {
            throw new RuntimeException("Datatype not implemented");
        }
    }

    private static boolean isWindowInside(int winOffSetX, int winOffSetY, int windowWidth, int windowHeight, int rawWidth, int rawHeight) {
        final Rectangle windowRec = new Rectangle(winOffSetX, winOffSetY, windowWidth, windowHeight);
        final Rectangle arrayRectangle = new Rectangle(0, 0, rawWidth, rawHeight);
        return arrayRectangle.contains(windowRec);
    }
}

