package com.bc.fiduceo.location;

import static java.lang.Math.max;
import static java.lang.Math.min;

import org.esa.snap.core.datamodel.GeoApproximation;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.ImageUtils;
import org.esa.snap.core.util.math.CosineDistance;
import org.esa.snap.core.util.math.DistanceMeasure;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;

import javax.media.jai.PlanarImage;
import java.awt.geom.Point2D;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;

/**
 * @author Ralf Quast
 */
public class SwathPixelLocator extends AbstractPixelLocator {

    private final PixelLocationEstimator estimator;
    private final PixelLocationSearcher searcher;

    private SwathPixelLocator(PlanarImage lonSource, PlanarImage latSource,
                              PixelLocationEstimator estimator, PixelLocationSearcher searcher) {
        super(lonSource, latSource);
        this.estimator = estimator;
        this.searcher = searcher;
    }

    public static PixelLocator create(ArrayFloat lonSource,
                                      ArrayFloat latSource,
                                      int width,
                                      int height,
                                      int wobbly) {

        final ProductData.Float lonData = new ProductData.Float((float[]) lonSource.getStorage());
        final ProductData.Float latData = new ProductData.Float((float[]) latSource.getStorage());


        final RenderedImage lonImage = ImageUtils.createRenderedImage(width, height, lonData);
        final RenderedImage latImage = ImageUtils.createRenderedImage(width, height, latData);
        PlanarImage lonPi = PlanarImage.wrapRenderedImage(lonImage);
        PlanarImage latPi = PlanarImage.wrapRenderedImage(latImage);

        final GeoApproximation[] approximations = createApproximations(lonPi, latPi, null);
        final PixelLocationEstimator estimator = new PixelLocationEstimator(approximations);

        final PixelLocationSearcher searcher = new PixelLocationSearcher(lonPi,
                                                                         latPi,
                                                                         null,
                                                                         wobbly);

        return new SwathPixelLocator(lonPi, latPi, estimator, searcher);
    }

    private static GeoApproximation[] createApproximations(PlanarImage lonImage,
                                                           PlanarImage latImage,
                                                           PlanarImage maskImage) {
        return GeoApproximation.createApproximations(lonImage, latImage, maskImage, 0.5);
    }

    @Override
    public boolean getPixelLocation(double lon, double lat, Point2D p) {
//        return estimator.estimatePixelLocation(lon, lat, p);
//        return searcher.searchPixelLocation(lon, lat, p);
        return estimator.estimatePixelLocation(lon, lat, p) && searcher.searchPixelLocation(lon, lat, p);
    }

    public static final class PixelLocationEstimator {

        private final GeoApproximation[] approximations;

        public PixelLocationEstimator(GeoApproximation[] approximations) {
            this.approximations = approximations;
        }

        private static GeoApproximation findMostSuitable(GeoApproximation[] approximations, double lat, double lon) {
            GeoApproximation bestApproximation = null;
            if (approximations.length == 1) {
                GeoApproximation a = approximations[0];
                final double distance = a.getDistance(lat, lon);
                if (distance < a.getMaxDistance()) {
                    bestApproximation = a;
                }
            } else {
                double minDistance = Double.MAX_VALUE;
                for (final GeoApproximation a : approximations) {
                    final double distance = a.getDistance(lat, lon);
                    if (distance < minDistance && distance < a.getMaxDistance()) {
                        minDistance = distance;
                        bestApproximation = a;
                    }
                }
            }
            return bestApproximation;
        }

        private static void g2p(GeoApproximation geoApproximation, Point2D g) {
            geoApproximation.getRotator().transform(g);
            final double lon = g.getX();
            final double lat = g.getY();
            final double x = geoApproximation.getFX().getValue(lat, lon);
            final double y = geoApproximation.getFY().getValue(lat, lon);
            g.setLocation(x, y);
        }

        public boolean estimatePixelLocation(double lon, double lat, Point2D p) {
            GeoApproximation approximation;
            if (approximations != null) {
                approximation = findMostSuitable(approximations, lat, lon);
                if (approximation != null) {
                    p.setLocation(lon, lat);
                    g2p(approximation, p);
                } else {
                    return false;
                }
            }
            return true;
        }
    }

    private static final class PixelLocationSearcher {

        private static final int R = 128;

        private final SampleSource maskSource;
        private final int wobbly;
        private final int sourceW;
        private final int sourceH;
        private final Raster lonData;
        private final Raster latData;

        public PixelLocationSearcher(PlanarImage lonSource, PlanarImage latSource, SampleSource maskSource,
                                     int wobbly) {
            lonData = lonSource.getData();
            latData = latSource.getData();
            this.maskSource = maskSource;
            this.wobbly = wobbly;


            sourceW = lonSource.getWidth();
            sourceH = lonSource.getHeight();
        }

        public boolean searchPixelLocation(double lon, double lat, Point2D p) {
            int x = (int) Math.floor(p.getX());
            int y = (int) Math.floor(p.getY());

            if (x < 0) {
                x = 0;
            } else if (x >= sourceW) {
                x = sourceW - 1;
            }
            if (y < 0) {
                y = 0;
            } else if (y >= sourceH) {
                y = sourceH - 1;
            }

            final int minX = max(x - R, 0);
            final int minY = max(y - R, 0);
            final int maxX = min(x + R, sourceW - 1);
            final int maxY = min(y + R, sourceH - 1);

            final DistanceMeasure d = new CosineDistance(lon, lat);
            final Result result = new Result(d, x, y, 2.0).invoke(x, y);

            for (int r = R; r > wobbly; r >>= 1) {
                final int midX = result.getX();
                final int midY = result.getY();

                final int outerMinX = max(minX, midX - r);
                final int outerMaxX = min(maxX, midX + r);
                final int outerMinY = max(minY, midY - r);
                final int outerMaxY = min(maxY, midY + r);

                // consider outer points in the N, S, E, and W
                result.invoke(outerMinX, midY);
                result.invoke(outerMaxX, midY);
                result.invoke(midX, outerMaxY);
                result.invoke(midX, outerMinY);
                // consider outer points in the NW, SW, SE, and NE
                result.invoke(outerMinX, outerMinY);
                result.invoke(outerMinX, outerMaxY);
                result.invoke(outerMaxX, outerMaxY);
                result.invoke(outerMaxX, outerMinY);

                if (r >> 1 > wobbly) {
                    final int innerMinX = max(minX, midX - (r >> 1));
                    final int innerMaxX = min(maxX, midX + (r >> 1));
                    final int innerMinY = max(minY, midY - (r >> 1));
                    final int innerMaxY = min(maxY, midY + (r >> 1));

                    // consider inner points in the NW, SW, SE, and NE
                    result.invoke(innerMinX, innerMinY);
                    result.invoke(innerMinX, innerMaxY);
                    result.invoke(innerMaxX, innerMaxY);
                    result.invoke(innerMaxX, innerMinY);

                    if (wobbly > 0) {
                        // consider inner points in the N, S, E, and W
                        result.invoke(innerMinX, midY);
                        result.invoke(innerMaxX, midY);
                        result.invoke(midX, innerMaxY);
                        result.invoke(midX, innerMinY);
                    }
                }
                if (wobbly > 0) {
                    final int minX1 = max(outerMinX, midX - wobbly);
                    final int maxX1 = min(outerMaxX, midX + wobbly);
                    final int minY1 = max(outerMinY, midY - wobbly);
                    final int maxY1 = min(outerMaxY, midY + wobbly);

                    for (int y1 = minY1; y1 <= maxY1; y1++) {
                        for (int x1 = minX1; x1 <= maxX1; x1++) {
                            if (x1 != midX || y1 != midY) {
                                result.invoke(x1, y1);
                            }
                        }
                    }
                }
            }

            final boolean found = result.getX() > minX && result.getX() < maxX && result.getY() > minY && result.getY() < maxY;
            if (found) {
                p.setLocation(result.getX() + 0.5, result.getY() + 0.5);
            }

            return found;
        }

        private final class Result {

            private final DistanceMeasure distanceMeasure;

            private int x;
            private int y;
            private double distance;

            public Result(DistanceMeasure distanceMeasure, int x, int y, double distance) {
                this.distanceMeasure = distanceMeasure;
                this.x = x;
                this.y = y;
                this.distance = distance;
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            public Result invoke(int otherX, int otherY) {
                if (maskSource == null || maskSource.getSample(otherX, otherY) != 0.0) {
                    final double lon = lonData.getSampleDouble(otherX, otherY, 0);
                    final double lat = latData.getSampleDouble(otherX, otherY, 0);
                    final double d = distanceMeasure.distance(lon, lat);

                    if (d < distance) {
                        x = otherX;
                        y = otherY;
                        distance = d;
                    }
                }
                return this;
            }
        }

    }

}
