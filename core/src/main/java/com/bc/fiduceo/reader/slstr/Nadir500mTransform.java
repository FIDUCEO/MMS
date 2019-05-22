package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;

class Nadir500mTransform implements Transform {

    private final int rasterWidth;
    private final int rasterHeight;

    Nadir500mTransform(int rasterWidth, int rasterHeight) {
        this.rasterWidth = rasterWidth;
        this.rasterHeight = rasterHeight;
    }

    @Override
    public Dimension getRasterSize() {
        return new Dimension("raster", rasterWidth, rasterHeight);
    }

    @Override
    public int mapCoordinate(int coordinate) {
        return 2 * coordinate;
    }

    @Override
    public Interval mapInterval(Interval interval) {
        final int width = interval.getX() * 2;
        final int height = interval.getY() * 2;
        return new Interval(width, height);
    }

    @Override
    public Array process(Array array, double noDataValue) {
        final Array resultArray = createTargetArray(array);
        final Index writeIndex = resultArray.getIndex();
        final Index readIndex = array.getIndex();

        int count = 4;
        final double[] averagingArray = new double[count];

        final int[] shape = resultArray.getShape();
        for (int y = 0; y < shape[1]; y++) {
            final int twoY = y * 2;
            for (int x = 0; x < shape[0]; x++) {
                final int twoX = x * 2;

                readIndex.set(twoY, twoX);
                double value = array.getDouble(readIndex);
                if (Math.abs(value - noDataValue) < 1e-8) {
                    value = 0.0;
                    --count;
                }
                averagingArray[0] = value;

                readIndex.set(twoY, twoX + 1);
                value = array.getDouble(readIndex);
                if (Math.abs(value - noDataValue) < 1e-8) {
                    value = 0.0;
                    --count;
                }
                averagingArray[1] = value;

                readIndex.set(twoY + 1, twoX);
                value = array.getDouble(readIndex);
                if (Math.abs(value - noDataValue) < 1e-8) {
                    value = 0.0;
                    --count;
                }
                averagingArray[2] = value;

                readIndex.set(twoY + 1, twoX + 1);
                value = array.getDouble(readIndex);
                if (Math.abs(value - noDataValue) < 1e-8) {
                    value = 0.0;
                    --count;
                }
                averagingArray[3] = value;

                double result = noDataValue;
                if (count > 0) {
                    double sum = 0.0;
                    for (double v : averagingArray) {
                        sum += v;
                    }
                    result = sum / count;
                }

                writeIndex.set(y, x);
                resultArray.setDouble(writeIndex, result);
            }
        }

        return resultArray;
    }

    // @todo 2 tb/tb add tests 2019-05-22
    static Array createTargetArray(Array array) {
        final int[] shape = array.getShape();
        final DataType dataType = array.getDataType();
        for (int i = 0; i < shape.length; i++) {
            shape[i] = shape[i]/2;
        }

        return Array.factory(dataType, shape);
    }
}
