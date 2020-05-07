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

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.*;

import java.awt.*;
import java.io.IOException;

/**
 * @author muhammad.bc
 * @author sabine.bc
 */
public class RawDataReader {

    public static Array read(int centerX, int centerY, Interval interval, Number fillValue, Array rawArray, com.bc.fiduceo.core.Dimension productSize) throws IOException {
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        final int defaultWidth = productSize.getNx();
        int[] shape = rawArray.getShape();
        int rank = rawArray.getRank();
        final InputDimension inputDimension = getInputDimension(rank, shape);
        if (inputDimension == InputDimension.THREE_D_FALSE_DIMENSION) {
            rawArray = rawArray.reduce();
            shape = rawArray.getShape();
        } else if (inputDimension == InputDimension.TWO_D_FALSE_DIMENSION) {
            shape[0] = shape[1];
            shape[1] = defaultWidth;
            rawArray = rawArray.reduce();
        } else if (inputDimension == InputDimension.ONE_D) {
            shape = new int[]{shape[0], defaultWidth};
        } else if (inputDimension == InputDimension.SKALAR) {
            shape = new int[]{1, 1};
        }
        // no specific handling for the pure 2d data case tb 2016-04-18

        final int rawHeight = shape[0];
        final int rawWidth = shape[1];
        final int offsetX = centerX - windowWidth / 2;
        final int offsetY = centerY - windowHeight / 2;

        if (inputDimension == InputDimension.ONE_D
                || inputDimension == InputDimension.TWO_D_FALSE_DIMENSION) {
            return readFrom1DArray(offsetX, offsetY, windowWidth, windowHeight, fillValue, rawArray, rawWidth, rawHeight);
        } else if (inputDimension == InputDimension.SKALAR) {
            return readFromSkalarArray(offsetX, offsetY, windowWidth, windowHeight, fillValue, rawArray, productSize);
        } else {
            boolean windowInside = isWindowInside(offsetX, offsetY, windowWidth, windowHeight, rawWidth, rawHeight);
            if (windowInside) {
                return NetCDFUtils.section(rawArray, new int[]{offsetY, offsetX}, new int[]{windowHeight, windowWidth});
            }
            return readFrom2DArray(offsetX, offsetY, windowWidth, windowHeight, fillValue, rawArray, rawWidth, rawHeight);
        }
    }

    private static Array readFrom2DArray(int offsetX, int offsetY, int windowWidth, int windowHeight, Number fillValue, Array rawArray, int rawWidth, int rawHeight) {
        final Class elementType = rawArray.getElementType();
        if (elementType == double.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.doubleValue(), (ArrayDouble.D2) rawArray, rawWidth, rawHeight);
        } else if (elementType == float.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.floatValue(), (ArrayFloat.D2) rawArray, rawWidth, rawHeight);
        } else if (elementType == long.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.longValue(), (ArrayLong.D2) rawArray, rawWidth, rawHeight);
        } else if (elementType == int.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.intValue(), (ArrayInt.D2) rawArray, rawWidth, rawHeight);
        } else if (elementType == short.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.shortValue(), (ArrayShort.D2) rawArray, rawWidth, rawHeight);
        } else if (elementType == byte.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.byteValue(), (ArrayByte.D2) rawArray, rawWidth, rawHeight);
        } else {
            throw new RuntimeException("Datatype not implemented");
        }
    }

    private static Array readFromSkalarArray(int offsetX, int offsetY, int windowWidth, int windowHeight, Number fillValue, Array rawArray, Dimension productSize) {
        final Number value = (Number) rawArray.getObject(0);
        final Array targetArray = Array.factory(rawArray.getDataType(), new int[]{windowHeight, windowWidth});
        final Index index = targetArray.getIndex();
        for (int y = 0; y < windowHeight; y++) {
            final int readY = offsetY + y;
            for (int x = 0; x < windowWidth; x++) {
                final int readx = offsetX + x;
                index.set(y, x);
                if (readY >= 0 && readY < productSize.getNy() && readx >= 0 && readx < productSize.getNx()) {
                    targetArray.setObject(index, value);
                } else {
                    targetArray.setObject(index, fillValue);
                }
            }
        }
        return targetArray;
    }

    private static Array readFrom1DArray(int offsetX, int offsetY, int windowWidth, int windowHeight, Number fillValue, Array rawArray, int rawWidth, int rawHeight) {
        final Class elementType = rawArray.getElementType();
        if (elementType == double.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.doubleValue(), (ArrayDouble.D1) rawArray, rawWidth, rawHeight);
        } else if (elementType == float.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.floatValue(), (ArrayFloat.D1) rawArray, rawWidth, rawHeight);
        } else if (elementType == long.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.longValue(), (ArrayLong.D1) rawArray, rawWidth, rawHeight);
        } else if (elementType == int.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.intValue(), (ArrayInt.D1) rawArray, rawWidth, rawHeight);
        } else if (elementType == short.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.shortValue(), (ArrayShort.D1) rawArray, rawWidth, rawHeight);
        } else if (elementType == byte.class) {
            return WindowReader.readWindow(offsetX, offsetY, windowWidth, windowHeight, fillValue.byteValue(), (ArrayByte.D1) rawArray, rawWidth, rawHeight);
        } else {
            throw new RuntimeException("Datatype not implemented");
        }
    }

    private static boolean isWindowInside(int winOffSetX, int winOffSetY, int windowWidth, int windowHeight, int rawWidth, int rawHeight) {
        final Rectangle windowRec = new Rectangle(winOffSetX, winOffSetY, windowWidth, windowHeight);
        final Rectangle arrayRectangle = new Rectangle(0, 0, rawWidth, rawHeight);
        return arrayRectangle.contains(windowRec);
    }

    // package access for testing only tb 2016-04-18
    static InputDimension getInputDimension(int rank, int[] shape) {
        if (rank == 1 && shape[0] == 1) {
            return InputDimension.SKALAR;
        } else if (rank == 1 && shape[0] > 1) {
            return InputDimension.ONE_D;
        } else if (rank == 2 && shape[0] == 1) {
            return InputDimension.TWO_D_FALSE_DIMENSION;
        } else //noinspection ConstantConditions - to clarify the code intention tb 2016-04-18
            if (rank == 2 && shape[0] != 1) {
                return InputDimension.TWO_D;
            } else if (rank == 3 && shape[0] == 1) {
                return InputDimension.THREE_D_FALSE_DIMENSION;
            }

        throw new RuntimeException("Unsupported input dimensionality");
    }

    enum InputDimension {
        THREE_D_FALSE_DIMENSION,
        TWO_D,
        TWO_D_FALSE_DIMENSION,
        ONE_D,
        SKALAR
    }

}

