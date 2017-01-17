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
import ucar.ma2.Index;

import java.io.IOException;

public class Read1dFrom3dAndExpandTo2d extends WindowReader {

    protected final Number fillValue;
    private final int[] initialIndexPos;
    private final ArraySource arraySource;
    private int yIndex;
    private Array windowArray;
    private Array sourceArray;

    public Read1dFrom3dAndExpandTo2d(ArraySource arraySource, String[] offsetMapping, Number fillValue) {
        this.fillValue = fillValue;
        initialIndexPos = new int[3];
        initializeIndex(offsetMapping);
        this.arraySource = arraySource;
    }

    @Override
    public Array read(int centerX, int centerY, Interval interval) throws IOException {
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        sourceArray = arraySource.getSource();
        windowArray = Array.factory(sourceArray.getDataType(), new int[]{windowWidth, windowHeight});
        final Index tarIndex = windowArray.getIndex();

        final int[] shape = sourceArray.getShape();
        final Index srcIndex = sourceArray.getIndex();
        srcIndex.set(initialIndexPos);

        fillArray(centerX,
                centerY - windowHeight / 2,
                windowWidth,
                windowHeight,
                Integer.MAX_VALUE,
                shape[yIndex],
                (y1, x1) -> {
                    tarIndex.set(y1, x1);
                    windowArray.setObject(tarIndex, fillValue);
                },
                (y, x, yRaw, xRaw) -> {
                    srcIndex.setDim(yIndex, yRaw);
                    tarIndex.set(y, x);
                    windowArray.setObject(tarIndex, sourceArray.getObject(srcIndex));
                }
        );
        return windowArray;
    }

    private void initializeIndex(String[] offsetMapping) {
        for (int i = 0; i < offsetMapping.length; i++) {
            String s = offsetMapping[i];
            if ("y".equalsIgnoreCase(s)) {
                yIndex = i;
            } else {
                initialIndexPos[i] = Integer.parseInt(s);
            }
        }
    }

    public interface ArraySource {

        Array getSource() throws IOException;
    }
}

