package com.bc.fiduceo.reader.iasi;


import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.math.SphericalDistance;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class IASI_PixelLocator implements PixelLocator {

    private final Array longitudes;
    private final Array latitudes;
    private final int width;
    private final int height;
    private final Index longitudesIndex;
    private final Index latitudesIndex;
    private final Polygon upperPolygon;
    private final Polygon lowerPolygon;
    private final GeometryFactory geometryFactory;

    IASI_PixelLocator(GeolocationData geolocationData, GeometryFactory geometryFactory) {
        longitudes = geolocationData.longitudes;
        latitudes = geolocationData.latitudes;
        this.geometryFactory = geometryFactory;

        final int[] shape = longitudes.getShape();
        width = shape[1];
        height = shape[0];

        longitudesIndex = longitudes.getIndex();
        latitudesIndex = latitudes.getIndex();

        final BoundingPolygonCreator polygonCreator = new BoundingPolygonCreator(new Interval(6, 24), geometryFactory);
        final Geometry boundingGeometry = polygonCreator.createBoundingGeometrySplitted(geolocationData.longitudes, geolocationData.latitudes, 2, true);
        if (!boundingGeometry.isValid()) {
            throw new RuntimeException("Unable to extract valid bounding geometry");
        }

        final GeometryCollection collection = (GeometryCollection) boundingGeometry;
        upperPolygon = (Polygon) collection.getGeometries()[0];
        lowerPolygon = (Polygon) collection.getGeometries()[1];
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D point) {
        final int x_pos = (int) Math.floor(x);
        final int y_pos = (int) Math.floor(y);
        if (x_pos < 0 || x_pos >= width || y_pos < 0 || y_pos >= height) {
            return null;
        }

        if (point == null) {
            point = new Point2D.Double();
        }
        latitudesIndex.set(y_pos, x_pos);
        longitudesIndex.set(y_pos, x_pos);
        point.setLocation(longitudes.getDouble(longitudesIndex), latitudes.getDouble(latitudesIndex));
        return point;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {


        final Point searchLocation = geometryFactory.createPoint(lon, lat);
        boolean upperContains = false;
        boolean lowerContains = false;
        if (upperPolygon.contains(searchLocation)) {
            upperContains = true;
        }
        if (lowerPolygon.contains(searchLocation)) {
            lowerContains = true;
        }
        if (!(upperContains || lowerContains)) {
            // pixel is not inside the swath th 2017-05-02
            return new Point2D[0];
        }

        final SphericalDistance sphericalDistance = new SphericalDistance(lon, lat);
        final int center = width / 2;
        final int halfHeight = height / 2 + 1;
        final List<Point2D> resultList = new ArrayList<>();
        if (upperContains) {
            final int lineIndex = getMinDistanceLine(sphericalDistance, 0, halfHeight, center);
            final int pixelIndex = getPixelIndex(sphericalDistance, lineIndex);
            resultList.add(new Point2D.Float(pixelIndex + 0.5f, lineIndex + 0.5f));
        }

        if (lowerContains) {
            final int lineIndex = getMinDistanceLine(sphericalDistance, halfHeight + 1, height, center);
            final int pixelIndex = getPixelIndex(sphericalDistance, lineIndex);
            resultList.add(new Point2D.Float(pixelIndex + 0.5f, lineIndex + 0.5f));
        }


        return resultList.toArray(new Point2D[resultList.size()]);
    }

    private int getMinDistanceLine(SphericalDistance sphericalDistance, int start, int stop, int center) {
        double minDistance = Double.MAX_VALUE;
        final int offsetLeft = center - 14;
        final int offsetRight = center + 14;

        int lineIndex = 0;
        for (int i = start; i < stop; i++) {
            longitudesIndex.set(i, offsetLeft);
            double lonCenter = longitudes.getDouble(longitudesIndex);
            latitudesIndex.set(i, offsetLeft);
            double latCenter = latitudes.getDouble(latitudesIndex);
            final double distance_1 = sphericalDistance.distance(lonCenter, latCenter);

            longitudesIndex.set(i, center);
            lonCenter = longitudes.getDouble(longitudesIndex);
            latitudesIndex.set(i, center);
            latCenter = latitudes.getDouble(latitudesIndex);
            final double distance_2 = sphericalDistance.distance(lonCenter, latCenter);

            longitudesIndex.set(i, offsetRight);
            lonCenter = longitudes.getDouble(longitudesIndex);
            latitudesIndex.set(i, offsetRight);
            latCenter = latitudes.getDouble(latitudesIndex);
            final double distance_3 = sphericalDistance.distance(lonCenter, latCenter);

            double distance = Math.min(distance_1, distance_2);
            distance = Math.min(distance, distance_3);
            if (distance < minDistance) {
                minDistance = distance;
                lineIndex = i;
            }
        }
        return lineIndex;
    }

    private int getPixelIndex(SphericalDistance sphericalDistance, int lineNumber) {
        double minDistance = Double.MAX_VALUE;

        int pixelIndex = 0;
        for (int i = 0; i < width; i++) {
            longitudesIndex.set(lineNumber, i);
            double lon = longitudes.getDouble(longitudesIndex);

            latitudesIndex.set(lineNumber, i);
            double lat = latitudes.getDouble(latitudesIndex);

            final double distance = sphericalDistance.distance(lon, lat);
            if (distance < minDistance) {
                minDistance = distance;
                pixelIndex = i;
            }
        }

        return pixelIndex;
    }
}
