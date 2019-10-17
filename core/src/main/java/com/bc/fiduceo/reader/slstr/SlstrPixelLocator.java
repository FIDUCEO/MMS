package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.reader.snap.SNAP_PixelLocator;
import org.esa.snap.core.datamodel.GeoCoding;

import java.awt.geom.Point2D;

public class SlstrPixelLocator extends SNAP_PixelLocator {

    private final Transform transform;

    public SlstrPixelLocator(GeoCoding geoCoding, Transform transform) {
        super(geoCoding);

        this.transform = transform;
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D point) {
        final int mappedX = transform.mapCoordinate_X((int) x);
        final int mappedy = transform.mapCoordinate_Y((int) y);
        return super.getGeoLocation(mappedX, mappedy, point);
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        final Point2D[] pixelLocations = super.getPixelLocation(lon, lat);
        for (final Point2D point : pixelLocations) {
            final int mappedX = transform.inverseCoordinate_X((int) point.getX());
            final int mappedy = transform.inverseCoordinate_Y((int) point.getY());

            point.setLocation(mappedX + 0.5, mappedy + 0.5);
        }
        return pixelLocations;
    }
}
