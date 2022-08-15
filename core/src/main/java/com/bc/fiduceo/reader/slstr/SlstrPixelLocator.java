package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.reader.slstr.utility.Transform;
import com.bc.fiduceo.reader.snap.SNAP_PixelLocator;
import org.esa.snap.core.datamodel.GeoCoding;

import java.awt.geom.Point2D;

public class SlstrPixelLocator extends SNAP_PixelLocator {

    private final Transform transform;

    SlstrPixelLocator(GeoCoding geoCoding, Transform transform) {
        super(geoCoding);

        this.transform = transform;
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D point) {
        final double mappedX = transform.mapCoordinate_X(x - 0.5);
        final double mappedy = transform.mapCoordinate_Y(y - 0.5);
        return super.getGeoLocation(mappedX, mappedy, point);
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        final Point2D[] pixelLocations = super.getPixelLocation(lon, lat);
        for (final Point2D point : pixelLocations) {
            final double mappedX = transform.inverseCoordinate_X(point.getX());
            final double mappedY = transform.inverseCoordinate_Y(point.getY());

            point.setLocation(mappedX + 0.5, mappedY + 0.5);
        }
        return pixelLocations;
    }
}
