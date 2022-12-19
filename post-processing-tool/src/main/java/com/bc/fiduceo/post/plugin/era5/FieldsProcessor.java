package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

import java.awt.*;
import java.io.IOException;

class FieldsProcessor {

    private static final int RASTER_WIDTH = 1440;

    TemplateVariable createTemplate(String name, String units, String longName, String standardName, boolean is3d) {
        return new TemplateVariable(name, units, longName, standardName, is3d);
    }

    static Array readSubset(int numLayers, Rectangle era5RasterPosition, VariableCache.CacheEntry cacheEntry) throws IOException, InvalidRangeException {
        Array subset;

        final int maxRequestedX = era5RasterPosition.x + era5RasterPosition.width - 1;
        if (era5RasterPosition.x < 0 || maxRequestedX >= RASTER_WIDTH) {
            subset = readVariableDataOverlapped(numLayers, era5RasterPosition, cacheEntry.array);
        } else {
            subset = readVariableData(numLayers, era5RasterPosition, cacheEntry.array);
        }

        return NetCDFUtils.scaleIfNecessary(cacheEntry.variable, subset);
    }

    private static Array readVariableDataOverlapped(int numLayers, Rectangle era5RasterPosition, Array array) throws IOException, InvalidRangeException {
        Array subset;
        int xMin = 0;
        int xMax;
        int leftWidth;
        int rightWidth;
        if (era5RasterPosition.x < 0) {
            xMax = RASTER_WIDTH + era5RasterPosition.x; // notabene: rasterposition is negative tb 2021-01-13
            leftWidth = era5RasterPosition.width + era5RasterPosition.x;
            rightWidth = -era5RasterPosition.x;
        } else {
            xMax = era5RasterPosition.x;
            rightWidth = RASTER_WIDTH - era5RasterPosition.x;
            leftWidth = era5RasterPosition.width - rightWidth;
        }
        final Rectangle leftEraPos = new Rectangle(xMin, era5RasterPosition.y, leftWidth, era5RasterPosition.height);
        final Array leftSubset = readVariableData(numLayers, leftEraPos, array);

        final Rectangle rightEraPos = new Rectangle(xMax, era5RasterPosition.y, rightWidth, era5RasterPosition.height);
        final Array rightSubset = readVariableData(numLayers, rightEraPos, array);

        subset = mergeData(leftSubset, rightSubset, numLayers, era5RasterPosition, array);
        return subset;
    }

    static Array mergeData(Array leftSubset, Array rightSubset, int numLayers, Rectangle era5RasterPosition, Array array) {
        final int rank = array.getRank();
        final Array mergedArray;
        if (rank == 4) {
            mergedArray = Array.factory(array.getDataType(), new int[]{numLayers, era5RasterPosition.height, era5RasterPosition.width});
            if (era5RasterPosition.x < 0) {
                final int xMax = era5RasterPosition.width + era5RasterPosition.x;
                mergeArrays_3D(leftSubset, rightSubset, era5RasterPosition, mergedArray, xMax);
            } else {
                final int xMax = RASTER_WIDTH - era5RasterPosition.x;
                mergeArrays_3D(rightSubset, leftSubset, era5RasterPosition, mergedArray, xMax);
            }
        } else {
            mergedArray = Array.factory(array.getDataType(), new int[]{era5RasterPosition.height, era5RasterPosition.width});
            if (era5RasterPosition.x < 0) {
                final int xMax = era5RasterPosition.width + era5RasterPosition.x;
                mergeArrays(leftSubset, rightSubset, era5RasterPosition, mergedArray, xMax);
            } else {
                final int xMax = RASTER_WIDTH - era5RasterPosition.x;
                mergeArrays(rightSubset, leftSubset, era5RasterPosition, mergedArray, xMax);
            }
        }

        return mergedArray;
    }

    private static void mergeArrays(Array leftSubset, Array rightSubset, Rectangle era5RasterPosition, Array mergedArray, int xMax) {
        final Index targetIndex = mergedArray.getIndex();
        Index srcIndex = leftSubset.getIndex();
        int srcX = 0;
        for (int x = 0; x < xMax; x++) {
            for (int y = 0; y < era5RasterPosition.height; y++) {
                targetIndex.set(y, x);
                srcIndex.set(y, srcX);
                mergedArray.setObject(targetIndex, leftSubset.getObject(srcIndex));
            }
            ++srcX;
        }
        srcIndex = rightSubset.getIndex();
        srcX = 0;
        for (int x = xMax; x < era5RasterPosition.width; x++) {
            for (int y = 0; y < era5RasterPosition.height; y++) {
                targetIndex.set(y, x);
                srcIndex.set(y, srcX);
                mergedArray.setObject(targetIndex, rightSubset.getObject(srcIndex));
            }
            ++srcX;
        }
    }

    private static void mergeArrays_3D(Array leftSubset, Array rightSubset, Rectangle era5RasterPosition, Array mergedArray, int xMax) {
        final Index targetIndex = mergedArray.getIndex();
        Index srcIndex = leftSubset.getIndex();
        int srcX = 0;
        final int numLayers = leftSubset.getShape()[0];
        for (int x = 0; x < xMax; x++) {
            for (int y = 0; y < era5RasterPosition.height; y++) {
                for (int z = 0; z < numLayers; z++) {
                    targetIndex.set(z, y, x);
                    srcIndex.set(z, y, srcX);
                    mergedArray.setObject(targetIndex, leftSubset.getObject(srcIndex));
                }
            }
            ++srcX;
        }
        srcIndex = rightSubset.getIndex();
        srcX = 0;
        for (int x = xMax; x < era5RasterPosition.width; x++) {
            for (int y = 0; y < era5RasterPosition.height; y++) {
                for (int z = 0; z < numLayers; z++) {
                    targetIndex.set(z, y, x);
                    srcIndex.set(z, y, srcX);
                    mergedArray.setObject(targetIndex, rightSubset.getObject(srcIndex));
                }
            }
            ++srcX;
        }
    }

    private static Array readVariableData(int numLayers, Rectangle era5RasterPosition, Array array) throws IOException, InvalidRangeException {
        final int rank = array.getRank();
        Array subset;
        if (rank == 2) {
            final int[] origin = new int[]{era5RasterPosition.y, era5RasterPosition.x};
            final int[] shape = new int[]{era5RasterPosition.height, era5RasterPosition.width};
            final int[] stride = new int[]{1, 1};
            subset = array.sectionNoReduce(origin, shape, stride).copy();
        } else if (rank == 3) {
            final int[] origin = new int[]{0, era5RasterPosition.y, era5RasterPosition.x};
            final int[] shape = new int[]{numLayers, era5RasterPosition.height, era5RasterPosition.width};
            final int[] stride = new int[]{1, 1, 1};
            subset = array.sectionNoReduce(origin, shape, stride).copy();
        } else {
            throw new IOException("variable rank invalid");
        }

        return subset;
    }
}
