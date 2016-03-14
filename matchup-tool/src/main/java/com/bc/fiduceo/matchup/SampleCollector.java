package com.bc.fiduceo.matchup;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.tool.ToolContext;

import java.awt.geom.Point2D;
import java.util.List;

public class SampleCollector {

    private final PixelLocator pixelLocator;
    private final Range xRange;
    private final Range yRange;
    private final ToolContext context;

    public SampleCollector(ToolContext context, PixelLocator pixelLocator) {
        this.context = context;
        this.pixelLocator = pixelLocator;
        xRange = new Range();
        yRange = new Range();
    }

    public void addSecondarySamples(List<SampleSet> sampleSets) {
        Point2D geopos = new Point2D.Double();
        for (final SampleSet sourceSample : sampleSets) {
            final Sample primary = sourceSample.getPrimary();
            final Point2D[] pixelLocations = pixelLocator.getPixelLocation(primary.lon, primary.lat);
            for (Point2D pixelLocation : pixelLocations) {
                final int x = (int) pixelLocation.getX();
                final int y = (int) pixelLocation.getY();
                geopos = pixelLocator.getGeoLocation(x + 0.5, y + 0.5, geopos);
                final Sample sample = new Sample(x, y, geopos.getX(), geopos.getY(), null);
                sourceSample.setSecondary(sample);
            }
        }
    }

    public void addPrimarySamples(Polygon polygon, MatchupSet matchupSet) {
        final Point[] coordinates = polygon.getCoordinates();
        for (Point coordinate : coordinates) {
            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(coordinate.getLon(), coordinate.getLat());
            for (Point2D point2D : pixelLocation) {
                xRange.aggregate(point2D.getX());
                yRange.aggregate(point2D.getY());
            }
        }

        final Point2D.Double geoPos = new Point2D.Double();
        final GeometryFactory factory = context.getGeometryFactory();

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
                    final Sample sample = new Sample(x, y, lon, lat, null);
                    matchupSet.addPrimary(sample);
                }
            }
        }
    }
}
