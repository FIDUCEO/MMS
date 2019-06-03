package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.*;

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
    public int getOffset_X() {
        return 1;
    }

    @Override
    public int getOffset_Y() {
        return 1;
    }

    @Override
    public Interval mapInterval(Interval interval) {
        final int width = interval.getX() * 2;
        final int height = interval.getY() * 2;
        return new Interval(width, height);
    }

    @Override
    public Array process(Array array, double noDataValue) throws InvalidRangeException {
        final Array resultArray = createTargetArray(array);
        final boolean isInteger = !resultArray.getDataType().isFloatingPoint();
        final Index writeIndex = resultArray.getIndex();

        int count = 4;
        final double[] averagingArray = new double[count];
        final int[] section_shape = new int[]{2, 2};
        final int[] offset = new int[2];

        final int[] shape = resultArray.getShape();
        for (int y = 0; y < shape[0]; y++) {
            final int twoY = y * 2;
            offset[0] = twoY;
            for (int x = 0; x < shape[1]; x++) {
                count = 4;
                final int twoX = x * 2;
                offset[1] = twoX;

                final Array section = array.section(offset, section_shape);
                final IndexIterator indexIterator = section.getIndexIterator();
                for (int i = 0; i < averagingArray.length; i++) {
                    final double value = indexIterator.getDoubleNext();
                    if (Math.abs(value - noDataValue) < 0.01) {
                        averagingArray[i] = 0.0;
                        --count;
                    } else {
                        averagingArray[i] = value;
                    }
                }

                double result = noDataValue;
                if (count > 0) {
                    double sum = 0.0;
                    for (double v : averagingArray) {
                        sum += v;
                    }
                    result = sum / count;
                    if (isInteger) {
                        result = Math.round(result);
                    }
                }

                writeIndex.set(y, x);
                resultArray.setDouble(writeIndex, result);
            }
        }

        return resultArray;
    }

    @Override
    public Array processFlags(Array array, int noDataValue) throws InvalidRangeException {
        final Array resultArray = createTargetArray(array);
        final Index writeIndex = resultArray.getIndex();

        int targetVal;
        int count;
        final int[] section_shape = new int[]{2, 2};
        final int[] offset = new int[2];

        final int[] shape = resultArray.getShape();
        for (int y = 0; y < shape[0]; y++) {
            final int twoY = y * 2;
            offset[0] = twoY;
            for (int x = 0; x < shape[1]; x++) {
                targetVal = 0;
                count = 4;
                final int twoX = x * 2;
                offset[1] = twoX;

                final Array section = array.section(offset, section_shape);
                final IndexIterator indexIterator = section.getIndexIterator();
                for (int i = 0; i < 4; i++) {
                    final int value = indexIterator.getIntNext();
                    if (value != noDataValue) {
                        targetVal |= value;
                    } else {
                        --count;
                    }
                }

                writeIndex.set(y, x);
                if (count > 0) {
                    resultArray.setInt(writeIndex, targetVal);
                } else {
                    resultArray.setInt(writeIndex, noDataValue);
                }
            }
        }

        return resultArray;
    }

    // package access for testing only tb 2019-05-27
    static Array createTargetArray(Array array) {
        final int[] shape = array.getShape();
        final DataType dataType = array.getDataType();
        for (int i = 0; i < shape.length; i++) {
            shape[i] = shape[i] / 2;
        }

        return Array.factory(dataType, shape);
    }
}
