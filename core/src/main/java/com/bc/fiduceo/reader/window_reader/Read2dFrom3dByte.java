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

package com.bc.fiduceo.reader.window_reader;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.WindowArrayFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.Index3D;

import java.io.IOException;

public class Read2dFrom3dByte extends Read2dFrom3d {

    private final ArraySourceByte arraySource;
    private ArrayByte.D2 windowArray;
    private ArrayByte.D3 sourceArray;

    public Read2dFrom3dByte(ArraySourceByte arraySource, String[] offsetMapping, Number fillValue) {
        super(fillValue, offsetMapping);
        this.arraySource = arraySource;
    }

    @Override
    public Array read(int centerX, int centerY, Interval interval) throws IOException {
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        sourceArray = arraySource.getSource();
        windowArray = WindowArrayFactory.createByteArray(windowWidth, windowHeight);

        final int[] shape = sourceArray.getShape();
        final Index3D index3D = new Index3D(shape);
        index3D.set(initialIndexPos);

        fillArray(centerX - windowWidth / 2,
                  centerY - windowHeight / 2,
                  windowWidth,
                  windowHeight,
                  shape[xIndex],
                  shape[yIndex],
                  (y1, x1) -> windowArray.set(y1, x1, fillValue.byteValue()),
                  (y, x, yRaw, xRaw) -> {
                      index3D.setDim(xIndex, xRaw);
                      index3D.setDim(yIndex, yRaw);
                      windowArray.set(y, x, sourceArray.get(index3D));
                  }
        );
        return windowArray;
    }

    public interface ArraySourceByte {

        ArrayByte.D3 getSource() throws IOException;
    }
}

