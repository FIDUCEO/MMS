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

package com.bc.fiduceo.reader.ssmt2;

import com.bc.fiduceo.util.VariablePrototype;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ZenithAngleVariable extends VariablePrototype {

    enum SensorType {
        F11,
        F12,
        F14,
        F15;

        public static SensorType fromString(String value) {
            return SensorType.valueOf(value);
        }
    }

    private static final float[] F11_ANGLES = {47.43f, 43.65f, 39.96f, 36.33f, 32.75f, 29.22f, 25.72f, 22.24f, 18.79f, 15.35f, 11.93f, 8.51f, 5.10f, 1.70f, 1.70f, 5.10f, 8.51f, 11.93f, 15.35f, 18.79f, 22.24f, 25.72f, 29.22f, 32.75f, 36.33f, 39.96f, 43.65f, 47.43f};
    private static final float[] F12_ANGLES = {47.40f, 43.63f, 39.94f, 36.31f, 32.74f, 29.21f, 25.71f, 22.23f, 18.78f, 15.34f, 11.92f, 8.51f, 5.10f, 1.70f, 1.70f, 5.10f, 8.51f, 11.92f, 15.34f, 18.78f, 22.23f, 25.71f, 29.21f, 32.74f, 36.31f, 39.94f, 43.63f, 47.40f};
    private static final float[] F14_ANGLES = {47.42f, 43.64f, 39.95f, 36.33f, 32.75f, 29.21f, 25.71f, 22.24f, 18.78f, 15.35f, 11.92f, 8.51f, 5.10f, 1.70f, 1.70f, 5.10f, 8.51f, 11.92f, 15.35f, 18.78f, 22.24f, 25.71f, 29.21f, 32.75f, 36.33f, 39.95f, 43.64f, 47.42f};
    private static final float[] F15_ANGLES = {47.40f, 43.63f, 39.94f, 36.31f, 32.74f, 29.21f, 25.71f, 22.23f, 18.78f, 15.34f, 11.92f, 8.51f, 5.10f, 1.70f, 1.70f, 5.10f, 8.51f, 11.92f, 15.34f, 18.78f, 22.23f, 25.71f, 29.21f, 32.74f, 36.31f, 39.94f, 43.63f, 47.40f};
    private static final int WIDTH = 28;

    private SensorType sensorType;
    private int height;
    private Array dataArray;

    ZenithAngleVariable(SensorType sensorType, int height) {
        this.sensorType = sensorType;
        this.height = height;
        dataArray = null;
    }

    @Override
    public DataType getDataType() {
        return DataType.FLOAT;
    }

    @Override
    public int[] getShape() {
        return new int[]{height, WIDTH};
    }

    @Override
    public int getShape(int index) {
        if (index == 0) {
            return height;
        } else if (index == 1) {
            return WIDTH;
        }

        throw new RuntimeException("Invalid shape index: " + index);
    }

    @Override
    public String getShortName() {
        return "Satellite_zenith_angle";
    }

    @Override
    public List<Attribute> getAttributes() {
        final List<Attribute> attributeList = new ArrayList<>();
        attributeList.add(new Attribute("units", "degrees"));

        return attributeList;
    }

    @Override
    public Array read() throws IOException {
        if (dataArray == null) {
            dataArray = Array.factory(DataType.FLOAT, getShape());

            float[] lineData = getLineData();

            final Index index = dataArray.getIndex();
            for (int x = 0; x < 28; x++) {
                for (int y = 0; y < height; y++) {
                    index.set(y, x);
                    dataArray.setFloat(index, lineData[x]);
                }
            }
        }

        return dataArray;
    }

    private float[] getLineData() {
        if (sensorType == SensorType.F11) {
            return F11_ANGLES;
        } else if (sensorType == SensorType.F12) {
            return F12_ANGLES;
        } else if (sensorType == SensorType.F14) {
            return F14_ANGLES;
        } else if (sensorType == SensorType.F15) {
            return F15_ANGLES;
        }

        throw new RuntimeException("Invalid Line data requested");
    }
}
