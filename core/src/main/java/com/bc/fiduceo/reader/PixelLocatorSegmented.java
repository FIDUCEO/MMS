package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.location.PixelLocator;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class PixelLocatorSegmented implements PixelLocator {

    private final ArrayList<GeoCodingSegment> segments;
    private final int swathWidth;


    public PixelLocatorSegmented(int swathWidth) {
        segments = new ArrayList<>();
        this.swathWidth = swathWidth;
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D g) {
        for (final GeoCodingSegment segment : segments) {
            if (y >= segment.interval.getX() && y < segment.interval.getY() && x >= 0 && x < swathWidth) {
                final PixelPos pixelPos = new PixelPos(x, y - segment.interval.getX());
                final GeoPos geoPos = segment.geoCoding.getGeoPos(pixelPos, null);
                return new Point2D.Double(geoPos.lon, geoPos.lat);
            }
        }
        return null;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        final ArrayList<Point2D> pointList = new ArrayList<>();

        final GeoPos geoPos = new GeoPos(lat, lon);
        for (final GeoCodingSegment segment : segments) {
            final PixelPos pixelPos = segment.geoCoding.getPixelPos(geoPos, null);
            if (pixelPos != null && isInSegment(pixelPos, segment.interval)) {
                pointList.add(new Point2D.Double(pixelPos.x, pixelPos.y + segment.interval.getX()));
            }
        }
        return pointList.toArray(new Point2D[]{});
    }

    public void addGeoCoding(GeoCoding geoCoding, Interval interval) {
        final GeoCodingSegment segment = new GeoCodingSegment();
        segment.geoCoding = geoCoding;
        segment.interval = interval;

        segments.add(segment);
    }

    boolean isInSegment(PixelPos pixelPos, Interval interval) {
        final int intervalLastLine = interval.getY() - interval.getX();
        return pixelPos.x >= 0 && pixelPos.x < this.swathWidth && pixelPos.y >= 0 && pixelPos.y < intervalLastLine;
    }

    static class GeoCodingSegment {
        GeoCoding geoCoding;
        Interval interval;
    }
}
