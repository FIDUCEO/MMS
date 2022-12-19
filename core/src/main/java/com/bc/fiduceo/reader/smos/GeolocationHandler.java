package com.bc.fiduceo.reader.smos;


import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.location.PixelLocator;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;

import java.awt.geom.Point2D;

class GeolocationHandler {

    static int LONGITUDE = 0;
    static int LATITUDE = 1;

    private final PixelLocator pixelLocator;

    GeolocationHandler(PixelLocator pixelLocator) {
        this.pixelLocator = pixelLocator;
    }

    public Array read(int x, int y, Interval interval, int type) {
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        final Array geoArray = Array.factory(DataType.FLOAT, new int[]{windowHeight, windowWidth});
        final Index index = geoArray.getIndex();

        final int offsetX = x - windowWidth / 2;
        final int offsetY = y - windowHeight / 2;

        int writeX = 0;
        int writeY = 0;
        Point2D geoPoint = new Point2D.Float();
        for (int j = offsetY; j < offsetY + windowHeight; j++) {
            for (int i = offsetX; i < offsetX + windowWidth; i++) {
                geoPoint = pixelLocator.getGeoLocation(i + 0.5, j + 0.5, geoPoint);

                index.set(writeY, writeX);
                if (geoPoint == null) {
                    geoArray.setFloat(index, Float.NaN);
                } else {
                    if (type == LATITUDE) {
                        geoArray.setFloat(index, (float) geoPoint.getY());
                    } else if (type == LONGITUDE) {
                        geoArray.setFloat(index, (float) geoPoint.getX());
                    } else {
                        throw new RuntimeException("illegal type");
                    }
                }

                ++writeX;
            }
            ++writeY;
            writeX = 0;
        }
        return geoArray;
    }
}
