package com.bc.fiduceo.reader.iasi;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.TiePointGeoCoding;
import org.esa.snap.core.datamodel.TiePointGrid;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

class IASI_TP_PixelLocator implements PixelLocator {

    private final Array longitudes;
    private final Array latitudes;
    private final GeometryFactory geometryFactory;

    private final Index longitudesIndex;
    private final Index latitudesIndex;

    private final int width;
    private final int height;

    private final Polygon upperPolygon;
    private final Polygon lowerPolygon;
    private final TiePointGeoCoding upperGeocoding;
    private final TiePointGeoCoding lowerGeocoding;
    private final int split_height;

    IASI_TP_PixelLocator(GeolocationData geolocationData, GeometryFactory geometryFactory) {
        longitudes = geolocationData.longitudes;
        latitudes = geolocationData.latitudes;
        this.geometryFactory = geometryFactory;

        longitudesIndex = longitudes.getIndex();
        latitudesIndex = latitudes.getIndex();

        final int[] shape = longitudes.getShape();
        width = shape[1];
        height = shape[0];

        final BoundingPolygonCreator polygonCreator = new BoundingPolygonCreator(new Interval(6, 24), geometryFactory);
        final Geometry boundingGeometry = polygonCreator.createBoundingGeometrySplitted(geolocationData.longitudes, geolocationData.latitudes, 2, true);
        if (!boundingGeometry.isValid()) {
            throw new RuntimeException("Unable to extract valid bounding geometry");
        }

        final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        upperPolygon = polygons.get(0);
        lowerPolygon = polygons.get(1);

        try {
            split_height = height / 2 + 1;
            Array latSubset = latitudes.section(new int[]{0, 0}, new int[]{split_height, width}, new int[]{1, 1}).copy();
            Array lonSubset = longitudes.section(new int[]{0, 0}, new int[]{split_height, width}, new int[]{1, 1}).copy();

            final TiePointGrid lon_upper = new TiePointGrid("lon_upper", width, split_height, 0.5, 0.5, 1, 1, (float[]) lonSubset.copyTo1DJavaArray());
            final TiePointGrid lat_upper = new TiePointGrid("lat_upper", width, split_height, 0.5, 0.5, 1, 1, (float[]) latSubset.copyTo1DJavaArray());
            upperGeocoding = new TiePointGeoCoding(lat_upper, lon_upper);

            final int remainingHeight = height - split_height;
            latSubset = latitudes.section(new int[]{split_height, 0}, new int[]{remainingHeight, width}, new int[]{1, 1}).copy();
            lonSubset = longitudes.section(new int[]{split_height, 0}, new int[]{remainingHeight, width}, new int[]{1, 1}).copy();
            final TiePointGrid lon_lower = new TiePointGrid("lon_lower", width, remainingHeight, 0.5, 0.5, 1, 1, (float[]) lonSubset.copyTo1DJavaArray());
            final TiePointGrid lat_lower = new TiePointGrid("lat_lower", width, remainingHeight, 0.5, 0.5, 1, 1, (float[]) latSubset.copyTo1DJavaArray());
            lowerGeocoding = new TiePointGeoCoding(lat_lower, lon_lower);
        } catch (InvalidRangeException e) {
            throw new RuntimeException(e.getMessage());
        }
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
            // pixel is not inside the swath tb 2018-04-05
            return new Point2D[0];
        }

        final List<Point2D> resultList = new ArrayList<>();
        final GeoPos searchGeoPos = new GeoPos(lat, lon);
        PixelPos pixelPos = new PixelPos();
        if (upperContains) {
            pixelPos = upperGeocoding.getPixelPos(searchGeoPos, pixelPos);
            if (pixelPos.isValid()) {
                resultList.add(new Point2D.Float((float) (Math.floor(pixelPos.x) + 0.5), (float) (Math.floor(pixelPos.y) + 0.5)));
            }
        }

        if (lowerContains) {
            pixelPos = lowerGeocoding.getPixelPos(searchGeoPos, pixelPos);
            if (pixelPos.isValid()) {
                resultList.add(new Point2D.Float((float) (Math.floor(pixelPos.x) + 0.5), (float) (Math.floor(pixelPos.y + split_height) + 0.5)));
            }
        }

        return resultList.toArray(new Point2D[0]);
    }
}
