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

import java.awt.*;

/**
 * @author muhammad.bc
 */
public class WindowArrayFactory {

    private final Array rawArray;

    public WindowArrayFactory(Array array) {
        this.rawArray = array;
    }

    final private Array getArray(int x, int y, float fillValue, Interval interval, Array rawArray) throws InvalidRangeException {
        final int[] shape = rawArray.getShape();
        final int rawHeight = shape[0];
        final int rawWidth = shape[1];
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        if (windowWidth % 2 == 0 || windowHeight % 2 == 0) {
            throw new IllegalArgumentException("The windowSize X, Y must me odd numbers");
        }
        final int windowX = x - windowWidth / 2;
        final int windowY = y - windowHeight / 2;

        if (rawArray.getRank() > 2) {
            throw new RuntimeException("Can not create window with more then 2 dimension.");
        }

        boolean windowInside = isWindowInside(windowX, windowY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            return ((ArrayFloat.D2) rawArray).section(new int[]{windowY, windowX}, new int[]{windowHeight, windowWidth});
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

    final private Array getArray(int x, int y, double fillValue, Interval interval, Array rawArray) throws InvalidRangeException {

        final int[] shape = rawArray.getShape();
        final int rawHeight = shape[0];
        final int rawWidth = shape[1];
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();
        if (windowWidth % 2 == 0 || windowHeight % 2 == 0) {
            throw new IllegalArgumentException("The windowSize X, Y must me odd numbers");
        }
        final int windowX = x - windowWidth / 2;
        final int windowY = y - windowHeight / 2;

        if (rawArray.getRank() > 2) {
            throw new RuntimeException("Can not create window with more then 2 dimension.");
        }

        boolean windowInside = isWindowInside(windowX, windowY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            return ((ArrayDouble.D2) rawArray).section(new int[]{windowY, windowX}, new int[]{windowHeight, windowWidth});
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

    final private Array getArray(int x, int y, int fillValue, Interval interval, Array rawArray) throws InvalidRangeException {

        final int[] shape = rawArray.getShape();
        final int rawHeight = shape[0];
        final int rawWidth = shape[1];
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();
        if (windowWidth % 2 == 0 || windowHeight % 2 == 0) {
            throw new IllegalArgumentException("The windowSize X, Y must me odd numbers");
        }
        final int windowX = x - windowWidth / 2;
        final int windowY = y - windowHeight / 2;

        boolean windowInside = isWindowInside(windowX, windowY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            return ((ArrayInt.D2)rawArray).section(new int[]{windowY, windowX}, new int[]{windowHeight, windowWidth});
        }
        ArrayInt.D2 windowArray = new ArrayInt.D2(windowHeight, windowWidth);

        for (int iy = 0; iy < windowHeight; iy++) {
            for (int ix = 0; ix < windowWidth; ix++) {
                int iYRaw = iy + windowY;
                int iXRaw = ix + windowX;
                if (iYRaw >= 0 && iYRaw < rawHeight && iXRaw >= 0 && iXRaw < rawWidth) {
                    windowArray.set(iy, ix, ((ArrayInt.D2)rawArray).get(iYRaw, iXRaw));
                } else {
                    windowArray.set(iy, ix, fillValue);
                }
            }
        }
        return windowArray;
    }

    final private Array getArray(int x, int y, byte fillValue, Interval interval, Array rawArray) throws InvalidRangeException {
        final int[] shape = rawArray.getShape();
        final int rawHeight = shape[0];
        final int rawWidth = shape[1];
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();
        if (windowWidth % 2 == 0 || windowHeight % 2 == 0) {
            throw new IllegalArgumentException("The windowSize X, Y must me odd numbers");
        }

        if (rawArray.getRank() > 2) {
            throw new RuntimeException("Can not create window with Array having more then 2 dimension.");
        }

        final int windowX = x - windowWidth / 2;
        final int windowY = y - windowHeight / 2;


        boolean windowInside = isWindowInside(windowX, windowY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            return ((ArrayByte.D2) rawArray).section(new int[]{windowY, windowX}, new int[]{windowHeight, windowWidth});
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

    boolean isWindowInside(int winOffSetX, int winOffSetY, int windowWidth, int windowHeight, int rawWidth, int rawHeight) {
        final Rectangle windowRec = new Rectangle(winOffSetX, winOffSetY, windowWidth, windowHeight);
        final Rectangle arrayRectangle = new Rectangle(0, 0, rawWidth, rawHeight);
        return arrayRectangle.contains(windowRec);
    }

    public Array get(int x, int y, Interval interval, Number fillValue) throws InvalidRangeException {
        if (rawArray.getElementType().getName().contains("float")) {
            return getArray(x, y, (float) fillValue, interval, (ArrayFloat.D2) rawArray);
        } else if (rawArray.getElementType().getName().contains("double")) {
            return getArray(x, y, (double) fillValue, interval, (ArrayDouble.D2) rawArray);
        } else if (rawArray.getElementType().getName().contains("byte")) {
            return getArray(x, y, (byte) fillValue, interval, rawArray);
        }
        return null;
    }


}

