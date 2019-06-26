package org.esa.snap.core.datamodel;

import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.util.ImageUtils;
import ucar.ma2.Array;

import javax.media.jai.PlanarImage;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.util.ArrayList;

// @todo se/** remove inheritance of this super class in SwathPixelLocator if the used dependencies from snap
// are changed to public and are usable in package com.bc.fiduceo.location. Move all the functionality to
// SwathPixelLocator and remove this class and this snap package.

/**
 * To avoid code duplication this class is placed in this snap style package.
 * The reason is, that some needed snap classes unfortunately are package local. :(
 * If the visibility of the needed snap classes are changed to public, this class should be
 * entirely integrated in SwathPixelLocator, and the inheritance should be removed.
 */
public class SnapAvoidCodeDuplicationClass_SwathPixelLocator implements PixelLocator {

    private final GeoPos internalUseGeoPos;
    private final GeoCoding gc;
    private final int width;
    private final int height;
    private final Point2D.Double internalUsePoint;
    private BestApproximations bestApproximations;
    private PixelPos internalUsePixelPos;

    public SnapAvoidCodeDuplicationClass_SwathPixelLocator(Array lonArray, Array latArray, int width, int height) {
        this.width = width;
        this.height = height;
        lonArray = NetCDFUtils.toFloat(lonArray);
        latArray = NetCDFUtils.toFloat(latArray);
        PlanarImage lonImg = getPlanarImage(lonArray, width, height);
        PlanarImage latImg = getPlanarImage(latArray, width, height);
        GeoApproximation[] approximations = GeoApproximation.createApproximations(lonImg, latImg, null, 0.1);
        bestApproximations = new BestApproximations(approximations);
        final float[] lats = (float[]) latArray.getStorage();
        final float[] lons = (float[]) lonArray.getStorage();
        final TiePointGrid latGrid = new TiePointGrid("lat", width, height, 0.5, 0.5, 1.0, 1.0, lats);
        final TiePointGrid lonGrid = new TiePointGrid("lon", width, height, 0.5, 0.5, 1.0, 1.0, lons);
        gc = new TiePointGeoCoding(latGrid, lonGrid);
        internalUseGeoPos = new GeoPos();
        internalUsePixelPos = new PixelPos();
        internalUsePoint = new Point2D.Double();
    }

    public GeoCoding getGc() {
        return gc;
    }

    private static PlanarImage getPlanarImage(Array data, int width, int height) {
        final float[] floats = (float[]) data.getStorage();
        final ProductData productData = ProductData.createInstance(floats);
        final RenderedImage lonImage = ImageUtils.createRenderedImage(width, height, productData);
        return PlanarImage.wrapRenderedImage(lonImage);
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D g) {
        internalUsePixelPos.setLocation(x, y);
        internalUseGeoPos.setInvalid();
        gc.getGeoPos(internalUsePixelPos, internalUseGeoPos);
        if (internalUseGeoPos.isValid()) {
            if (g == null) {
                g = new Point2D.Double();
            }
            g.setLocation(internalUseGeoPos.getLon(), internalUseGeoPos.getLat());
            return g;
        }
        return null;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        bestApproximations.findFor(lon, lat);
        if (!bestApproximations.hasApproximations()) {
            return new Point2D[0];
        }
        final ArrayList<Point2D> pipos = new ArrayList<>();
        final ArrayList<GeoApproximation> theBest = bestApproximations.getTheBest();
        for (GeoApproximation a : theBest) {
            internalUsePoint.setLocation(lon, lat);
            a.g2p(internalUsePoint);
            if (isValid(internalUsePoint)) {
                pipos.add(new Point2D.Double(internalUsePoint.getX(), internalUsePoint.getY()));
            }
        }
        return pipos.toArray(new Point2D[0]);
    }

    private boolean isValid(Point2D p) {
        final int iX = (int) Math.floor(p.getX());
        final int iY = (int) Math.floor(p.getY());
        return iX >= 0 && iX < width && iY >= 0 && iY < height;
    }

    protected static class BestApproximations {

        private final GeoApproximation[] approximations;
        private final ArrayList<GeoApproximation> best;
        private final ArrayList<Integer> bestIdx;
        private final ArrayList<Double> distances;

        BestApproximations(GeoApproximation[] approximations) {
            this.approximations = approximations;
            best = new ArrayList<>();
            bestIdx = new ArrayList<>();
            distances = new ArrayList<>();
        }

        void findFor(double lon, double lat) {
            emptyFields();
            for (int i = 0; i < approximations.length; i++) {
                GeoApproximation a = approximations[i];
                final double distance = a.getDistance(lat, lon);
                if (distance <= a.getMaxDistance()) {
                    best.add(a);
                    bestIdx.add(i);
                    distances.add(distance);
                }
            }
            findCouplesAndKeepTheBest();
        }

        private void findCouplesAndKeepTheBest() {
            for (int i = 1; i < best.size(); i++) {
                final int first = i - 1;
                final Integer i1 = bestIdx.get(first);
                final Integer i2 = bestIdx.get(i);
                final boolean isNeighbor = i2 - i1 == 1;
                if (isNeighbor) {
                    final double d1 = distances.get(first);
                    final double d2 = distances.get(i);
                    if (d1 < d2) {
                        remove(i);
                    } else {
                        remove(first);
                    }
                    i--;
                }
            }
        }

        private void remove(int i) {
            best.remove(i);
            bestIdx.remove(i);
            distances.remove(i);
        }

        private void emptyFields() {
            best.clear();
            bestIdx.clear();
            distances.clear();
        }

        public boolean hasApproximations() {
            return best.size() > 0;
        }

        public ArrayList<GeoApproximation> getTheBest() {
            return best;
        }
    }
}
