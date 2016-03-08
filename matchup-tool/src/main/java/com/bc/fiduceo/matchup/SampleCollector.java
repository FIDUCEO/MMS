package com.bc.fiduceo.matchup;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;

import java.awt.geom.Point2D;
import java.util.LinkedList;

public class SampleCollector {

    private final PixelLocator pixelLocator;
    private final Range xRange;
    private final Range yRange;
    private final MatchupToolContext context;

    public SampleCollector(MatchupToolContext context, PixelLocator pixelLocator) {
        this.context = context;
        this.pixelLocator = pixelLocator;
        xRange = new Range();
        yRange = new Range();
    }

    public LinkedList<Sample> getSamplesFor(Polygon polygon) {
        final Point[] coordinates = polygon.getCoordinates();
        for (Point coordinate : coordinates) {
            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(coordinate.getLon(), coordinate.getLat());
            for (int i = 0; i < pixelLocation.length; i++) {
                Point2D point2D = pixelLocation[i];
                xRange.aggregate(point2D.getX());
                yRange.aggregate(point2D.getY());
            }
        }

        final Point2D.Double geoPos = new Point2D.Double();
        final GeometryFactory factory = context.getGeometryFactory();

        final LinkedList<Sample> samples = new LinkedList<>();

        final int startY = (int) yRange.getMin();
        final int endY = (int) yRange.getMax();
        final int startX = (int) xRange.getMin();
        final int endX = (int) xRange.getMax();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                final Point2D geoLocation = pixelLocator.getGeoLocation(x + .5, y + .5, geoPos);
                final double lon = geoLocation.getX();
                final double lat = geoLocation.getY();
                final Point geoPoint = factory.createPoint(lon, lat);
                if (polygon.contains(geoPoint)) {
                    samples.add(new Sample(x, y, lon, lat, null));
                }
            }
        }
        return samples;
    }
}
