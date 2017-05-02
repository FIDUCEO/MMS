package com.bc.fiduceo.reader.iasi;


import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import org.esa.snap.core.util.math.DistanceMeasure;
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

    IASI_PixelLocator(IASI_Reader.GeolocationData geolocationData, GeometryFactory geometryFactory) {
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
        int lineIndex = 0;
        for (int i = start; i < stop; i++) {
            longitudesIndex.set(i, center - 8);
            double lonCenter = longitudes.getDouble(longitudesIndex);
            latitudesIndex.set(i, center - 8);
            double latCenter = latitudes.getDouble(latitudesIndex);
            final double distance_1 = sphericalDistance.distance(lonCenter, latCenter);

            longitudesIndex.set(i, center + 8);
            lonCenter = longitudes.getDouble(longitudesIndex);
            latitudesIndex.set(i, center + 8);
            latCenter = latitudes.getDouble(latitudesIndex);
            final double distance_2 = sphericalDistance.distance(lonCenter, latCenter);

            final double distance = Math.min(distance_1, distance_2);
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

    // @todo 1 tb/tb discuss with Sabine and eventually migrate to our own class, discuss with SNAP team. 2017-05-02
    private final class SphericalDistance implements DistanceMeasure {

        private final double lon;
        private final double lat;
        private final double si;
        private final double co;

        /**
         * Creates a new instance of this class.
         *
         * @param lon The reference longitude of this distance calculator.
         * @param lat The reference latitude of this distance calculator.
         */
        public SphericalDistance(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
            this.si = Math.sin(Math.toRadians(lat));
            this.co = Math.cos(Math.toRadians(lat));
        }

        /**
         * Returns the spherical distance (in Radian) of a given (lon, lat) point to
         * the reference (lon, lat) point.
         *
         * @param lon The longitude.
         * @param lat The latitude.
         *
         * @return the spherical distance (in Radian) of the given (lon, lat) point
         * to the reference (lon, lat) point.
         */
        @Override
        public double distance(double lon, double lat) {
            if (this.lon == lon && this.lat == lat) {
                return 0.0;
            }
            final double phi = Math.toRadians(lat);
            return Math.acos(si * Math.sin(phi) + co * Math.cos(phi) * Math.cos(Math.toRadians(lon - this.lon)));
        }
    }
}
