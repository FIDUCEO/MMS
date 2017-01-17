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
 */

package com.bc.fiduceo.matchup.screening.expression;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.Reader;
import org.esa.snap.core.jexp.EvalEnv;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.HashMap;

public class WindowReaderEvalEnv implements EvalEnv, WindowVariableSymbol.NoDataListener {

    private final HashMap<String, Array> windowArrayMap = new HashMap<>();
    private final int[] pixelShape = new int[]{1, 1};

    private final Reader reader;

    private int x;
    private int y;

    private Interval interval;
    private int centerX;
    private int centerY;
    private boolean noData = false;

    WindowReaderEvalEnv(Reader reader) {
        this.reader = reader;
    }

    public void setLocationInWindow(int x, int y) {
        resetNoData();
        this.x = x;
        this.y = y;
    }

    public void setWindow(int centerX, int centerY, int width, int height) {
        resetNoData();
        this.centerX = centerX;
        this.centerY = centerY;
        interval = new Interval(width, height);
        windowArrayMap.clear();
    }

    @Override
    public void fireNoData() {
        noData = true;
    }

    public boolean isNoData() {
        return noData;
    }

    Array fetchPixel(String name) throws IOException, InvalidRangeException {
        if (!windowArrayMap.containsKey(name)) {
            readWindow(name);
        }
        final Array array = windowArrayMap.get(name);
        return array.section(new int[]{y, x}, pixelShape);
    }

    private void resetNoData() {
        noData = false;
    }


    private void readWindow(String name) throws IOException, InvalidRangeException {
        final Array array = reader.readScaled(centerX, centerY, interval, name);
        windowArrayMap.put(name, array);
    }
}
