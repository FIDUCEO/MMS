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


    PixelLocatorSegmented() {
        segments = new ArrayList<>();
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D g) {
        for(final GeoCodingSegment segment : segments) {
            if (y >= segment.interval.getX() && y <= segment.interval.getY()) {
                final PixelPos pixelPos = new PixelPos(x, y);
                final GeoPos geoPos = segment.geoCoding.getGeoPos(pixelPos, null);
                return  new Point2D.Double(geoPos.lon, geoPos.lat);
            }
        }
        return null;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        final ArrayList<Point2D> pointList = new ArrayList<>();

        final GeoPos geoPos = new GeoPos(lat, lon);
        for(final GeoCodingSegment segment : segments) {
            final PixelPos pixelPos = segment.geoCoding.getPixelPos(geoPos, null);
            if (pixelPos != null) {
                pointList.add(new Point2D.Double(pixelPos.x, pixelPos.y));
            }
        }
        return pointList.toArray(new Point2D[]{});
    }

    void addGeoCoding(GeoCoding geoCoding, Interval interval) {
        final GeoCodingSegment segment = new GeoCodingSegment();
        segment.geoCoding = geoCoding;
        segment.interval = interval;

        segments.add(segment);
    }

    static class GeoCodingSegment {
        GeoCoding geoCoding;
        Interval interval;
    }
}
