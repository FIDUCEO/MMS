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
public class RawDataReader {

    public static Array get(int centerX, int centerY, Interval interval, Number fillValue, Array rawArray) throws InvalidRangeException {
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        final int[] shape = rawArray.getShape();
        final int rawHeight = shape[0];
        final int rawWidth = shape[1];
        final int offsetX = centerX - windowWidth / 2;
        final int offsetY = centerY - windowHeight / 2;

        boolean windowInside = isWindowInside(offsetX, offsetY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            return rawArray.section(new int[]{offsetY, offsetX}, new int[]{windowHeight, windowWidth});
        }

        return readWindow(offsetX, offsetY, windowWidth, windowHeight, (double) fillValue, (ArrayDouble.D2) rawArray, rawWidth, rawHeight);
    }

    private static Array getArray(int centerX, int centerY, Interval interval, float fillValue, Array rawArray) throws InvalidRangeException {
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

    static Array readWindow(int offsetX, int offsetY, int width, int height, double fillValue, ArrayDouble.D2 rawArray, int rawWidth, int rawHeight) {
        final ArrayDouble.D2 windowArray = WindowArrayFactory.createDoubleArray(width, height);
        for (int y = 0; y < height; y++) {
            int yRaw = y + offsetY;
            for (int x = 0; x < width; x++) {
                int xRaw = x + offsetX;
                if (yRaw >= 0 && yRaw < rawHeight && xRaw >= 0 && xRaw < rawWidth) {
                    windowArray.set(y, x, rawArray.get(yRaw, xRaw));
                } else {
                    windowArray.set(y, x, fillValue);
                }
            }
        }
        return windowArray;
    }

    private static Array getArray(int centerX, int centerY, Interval interval, int fillValue, Array rawArray) throws InvalidRangeException {

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

    private static Array getArray(int centerX, int centerY, Interval interval, byte fillValue, Array rawArray) throws InvalidRangeException {
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

    private static boolean isWindowInside(int winOffSetX, int winOffSetY, int windowWidth, int windowHeight, int rawWidth, int rawHeight) {
        final Rectangle windowRec = new Rectangle(winOffSetX, winOffSetY, windowWidth, windowHeight);
        final Rectangle arrayRectangle = new Rectangle(0, 0, rawWidth, rawHeight);
        return arrayRectangle.contains(windowRec);
    }
}

