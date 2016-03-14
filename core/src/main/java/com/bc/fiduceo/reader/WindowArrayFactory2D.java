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
import ucar.ma2.InvalidRangeException;

import java.awt.Rectangle;

/**
 * @author muhammad.bc
 */
public class WindowArrayFactory2D {

    private final Array rawArray;

    public WindowArrayFactory2D(Array array) {
        this.rawArray = array;
    }

    public Array get(int centerX, int centerY, Interval interval, Number fillValue) throws InvalidRangeException {
        try {
            if (rawArray.getElementType() == float.class) {
                return getArray(centerX, centerY, interval, (float) fillValue, rawArray);
            } else if (rawArray.getElementType() == double.class) {
                return getArray(centerX, centerY, interval, (double) fillValue, rawArray);
            } else if (rawArray.getElementType() == byte.class) {
                return getArray(centerX, centerY, interval, (byte) fillValue, rawArray);
            } else if (rawArray.getElementType() == int.class) {
                return getArray(centerX, centerY, interval, (int) fillValue, rawArray);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Array getArray(int centerX, int centerY, Interval interval, float fillValue, Array rawArray) throws InvalidRangeException {
        final int[] shape = rawArray.getShape();
        final int rawHeight = shape[0];
        final int rawWidth = shape[1];
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        final int windowX = centerX - windowWidth / 2;
        final int windowY = centerY - windowHeight / 2;

        boolean windowInside = isWindowInside(windowX, windowY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            return rawArray.section(new int[]{windowY, windowX}, new int[]{windowHeight, windowWidth});
        }


        ArrayFloat.D2 windowArray = new ArrayFloat.D2(windowHeight, windowWidth);
        for (int iy = 0; iy < windowHeight; iy++) {
            for (int ix = 0; ix < windowWidth; ix++) {
                int iYRaw = iy + windowY;
                int iXRaw = ix + windowX;
                if (iYRaw >= 0 && iYRaw < rawHeight && iXRaw >= 0 && iXRaw < rawWidth) {
                    windowArray.set(iy, ix, ((ArrayFloat.D2) rawArray).get(iYRaw, iXRaw));
                } else {
                    windowArray.set(iy, ix, fillValue);
                }
            }
        }
        return windowArray;
    }

    private Array getArray(int centerX, int centerY, Interval interval, double fillValue, Array rawArray) throws InvalidRangeException {

        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        final int[] shape = rawArray.getShape();
        final int rawHeight = shape[0];
        final int rawWidth = shape[1];
        final int windowX = centerX - windowWidth / 2;
        final int windowY = centerY - windowHeight / 2;

        boolean windowInside = isWindowInside(windowX, windowY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            return rawArray.section(new int[]{windowY, windowX}, new int[]{windowHeight, windowWidth});
        }

        ArrayDouble.D2 windowArray = new ArrayDouble.D2(windowHeight, windowWidth);
        for (int iy = 0; iy < windowHeight; iy++) {
            for (int ix = 0; ix < windowWidth; ix++) {
                int iYRaw = iy + windowY;
                int iXRaw = ix + windowX;
                if (iYRaw >= 0 && iYRaw < rawHeight && iXRaw >= 0 && iXRaw < rawWidth) {
                    windowArray.set(iy, ix, ((ArrayDouble.D2) rawArray).get(iYRaw, iXRaw));
                } else {
                    windowArray.set(iy, ix, fillValue);
                }
            }
        }
        return windowArray;
    }

    private Array getArray(int centerX, int centerY, Interval interval, int fillValue, Array rawArray) throws InvalidRangeException {

        final int[] shape = rawArray.getShape();
        final int rawHeight = shape[0];
        final int rawWidth = shape[1];
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        final int windowX = centerX - windowWidth / 2;
        final int windowY = centerY - windowHeight / 2;

        boolean windowInside = isWindowInside(windowX, windowY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            return rawArray.section(new int[]{windowY, windowX}, new int[]{windowHeight, windowWidth});
        }
        ArrayInt.D2 windowArray = new ArrayInt.D2(windowHeight, windowWidth);

        for (int iy = 0; iy < windowHeight; iy++) {
            for (int ix = 0; ix < windowWidth; ix++) {
                int iYRaw = iy + windowY;
                int iXRaw = ix + windowX;
                if (iYRaw >= 0 && iYRaw < rawHeight && iXRaw >= 0 && iXRaw < rawWidth) {
                    windowArray.set(iy, ix, ((ArrayInt.D2) rawArray).get(iYRaw, iXRaw));
                } else {
                    windowArray.set(iy, ix, fillValue);
                }
            }
        }
        return windowArray;
    }

    private Array getArray(int centerX, int centerY, Interval interval, byte fillValue, Array rawArray) throws InvalidRangeException {
        final int[] shape = rawArray.getShape();
        final int rawHeight = shape[0];
        final int rawWidth = shape[1];
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        final int windowX = centerX - windowWidth / 2;
        final int windowY = centerY - windowHeight / 2;

        boolean windowInside = isWindowInside(windowX, windowY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            return rawArray.section(new int[]{windowY, windowX}, new int[]{windowHeight, windowWidth});
        }
        ArrayByte.D2 windowArray = new ArrayByte.D2(windowHeight, windowWidth);

        for (int iy = 0; iy < windowHeight; iy++) {
            for (int ix = 0; ix < windowWidth; ix++) {
                int iYRaw = iy + windowY;
                int iXRaw = ix + windowX;
                if (iYRaw >= 0 && iYRaw < rawHeight && iXRaw >= 0 && iXRaw < rawWidth) {
                    windowArray.set(iy, ix, ((ArrayByte.D2) rawArray).get(iYRaw, iXRaw));
                } else {
                    windowArray.set(iy, ix, fillValue);
                }
            }
        }
        return windowArray;
    }

    private boolean isWindowInside(int winOffSetX, int winOffSetY, int windowWidth, int windowHeight, int rawWidth, int rawHeight) {
        final Rectangle windowRec = new Rectangle(winOffSetX, winOffSetY, windowWidth, windowHeight);
        final Rectangle arrayRectangle = new Rectangle(0, 0, rawWidth, rawHeight);
        return arrayRectangle.contains(windowRec);
    }
}

