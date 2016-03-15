/*
 * $Id$
 *
 * Copyright (C) 2015 by Brockmann Consult (info@brockmann-consult.de)
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

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayShort;

public class WindowReader {

    static Array readWindow(int offsetX, int offsetY, int width, int height, double fillValue, ArrayDouble.D2 rawArray, int rawWidth, int rawHeight) {
        final ArrayDouble.D2 windowArray = WindowArrayFactory.createDoubleArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y, x, yRaw, xRaw) -> windowArray.set(y, x, rawArray.get(yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, double fillValue, ArrayDouble.D3 rawArray, int rawWidth, int rawHeight) {
        final ArrayDouble.D2 windowArray = WindowArrayFactory.createDoubleArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y, x, yRaw, xRaw) -> windowArray.set(y, x, rawArray.get(0, yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, float fillValue, ArrayFloat.D2 rawArray, int rawWidth, int rawHeight) {
        final ArrayFloat.D2 windowArray = WindowArrayFactory.createFloatArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y1, x1, yRaw, xRaw) -> windowArray.set(y1, x1, rawArray.get(yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, float fillValue, ArrayFloat.D3 rawArray, int rawWidth, int rawHeight) {
        final ArrayFloat.D2 windowArray = WindowArrayFactory.createFloatArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y1, x1, yRaw, xRaw) -> windowArray.set(y1, x1, rawArray.get(0, yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, long fillValue, ArrayLong.D2 rawArray, int rawWidth, int rawHeight) {
        final ArrayLong.D2 windowArray = WindowArrayFactory.createLongArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y1, x1, yRaw, xRaw) -> windowArray.set(y1, x1, rawArray.get(yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, long fillValue, ArrayLong.D3 rawArray, int rawWidth, int rawHeight) {
        final ArrayLong.D2 windowArray = WindowArrayFactory.createLongArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y1, x1, yRaw, xRaw) -> windowArray.set(y1, x1, rawArray.get(0, yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, int fillValue, ArrayInt.D2 rawArray, int rawWidth, int rawHeight) {
        final ArrayInt.D2 windowArray = WindowArrayFactory.createIntArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y1, x1, yRaw, xRaw) -> windowArray.set(y1, x1, rawArray.get(yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, int fillValue, ArrayInt.D3 rawArray, int rawWidth, int rawHeight) {
        final ArrayInt.D2 windowArray = WindowArrayFactory.createIntArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y1, x1, yRaw, xRaw) -> windowArray.set(y1, x1, rawArray.get(0, yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, short fillValue, ArrayShort.D2 rawArray, int rawWidth, int rawHeight) {
        final ArrayShort.D2 windowArray = WindowArrayFactory.createShortArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y1, x1, yRaw, xRaw) -> windowArray.set(y1, x1, rawArray.get(yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, short fillValue, ArrayShort.D3 rawArray, int rawWidth, int rawHeight) {
        final ArrayShort.D2 windowArray = WindowArrayFactory.createShortArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y1, x1, yRaw, xRaw) -> windowArray.set(y1, x1, rawArray.get(0, yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, byte fillValue, ArrayByte.D2 rawArray, int rawWidth, int rawHeight) {
        final ArrayByte.D2 windowArray = WindowArrayFactory.createByteArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y1, x1, yRaw, xRaw) -> windowArray.set(y1, x1, rawArray.get(yRaw, xRaw)));
        return windowArray;
    }

    static Array readWindow(int offsetX, int offsetY, int width, int height, byte fillValue, ArrayByte.D3 rawArray, int rawWidth, int rawHeight) {
        final ArrayByte.D2 windowArray = WindowArrayFactory.createByteArray(width, height);
        fillArray(offsetX, offsetY, width, height, rawWidth, rawHeight,
                  (y, x) -> windowArray.set(y, x, fillValue),
                  (y1, x1, yRaw, xRaw) -> windowArray.set(y1, x1, rawArray.get(0, yRaw, xRaw)));
        return windowArray;
    }

    private static void fillArray(int offsetX, int offsetY, int width, int height, int rawWidth, int rawHeight, FillValueSetter fillSetter, RawValueSetter rawSetter) {
        for (int y = 0; y < height; y++) {
            int yRaw = y + offsetY;
            for (int x = 0; x < width; x++) {
                int xRaw = x + offsetX;
                if (yRaw >= 0 && yRaw < rawHeight && xRaw >= 0 && xRaw < rawWidth) {
                    rawSetter.set(y, x, yRaw, xRaw);
                } else {
                    fillSetter.set(y, x);
                }
            }
        }
    }

    private interface FillValueSetter {

        void set(int y, int x);
    }

    private interface RawValueSetter {

        void set(int y, int x, int yRaw, int xRaw);
    }
}
