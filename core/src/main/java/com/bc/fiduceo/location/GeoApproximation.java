package com.bc.fiduceo.location;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.RationalFunctionModel;
import org.esa.snap.core.util.jai.JAIUtils;
import org.esa.snap.core.util.math.CosineDistance;
import org.esa.snap.core.util.math.DistanceMeasure;

import javax.media.jai.PlanarImage;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public final class GeoApproximation {
    private static final int LAT = 0;
    private static final int LON = 1;
    private static final int X = 2;
    private static final int Y = 3;
    private static final int MAX_POINT_COUNT_PER_TILE = 1000;
    private final RationalFunctionModel fX;
    private final RationalFunctionModel fY;
    private final RationalFunctionModel fLon;
    private final RationalFunctionModel fLat;
    private final double maxDistance;
    private final Rotator rotator;
    private final DistanceMeasure calculator;
    private final Rectangle range;

    public static GeoApproximation[] createApproximations(PlanarImage lonImage,
                                                          PlanarImage latImage,
                                                          PlanarImage maskImage,
                                                          double accuracy) {
        final SampleSource lonSamples = new PlanarImageSampleSource(lonImage);
        final SampleSource latSamples = new PlanarImageSampleSource(latImage);
        final SampleSource maskSamples;
        if (maskImage != null) {
            maskSamples = new PlanarImageSampleSource(maskImage);
        } else {
            maskSamples = new SampleSource() {
                @Override
                public int getSample(int x, int y) {
                    return 1;
                }

                @Override
                public double getSampleDouble(int x, int y) {
                    return 1.0;
                }
            };
        }
        Tiling tiling = new Tiling(lonImage.getWidth(), lonImage.getHeight());
        final ArrayList<Rectangle> rectangleList = new ArrayList<>(tiling.getNumXTiles() * tiling.getNumYTiles());
        for (int tileY = 0; tileY < tiling.getNumYTiles(); tileY++) {
            for (int tileX = 0; tileX < tiling.getNumXTiles(); tileX++) {
                rectangleList.add(tiling.getTileRect(tileX, tileY));
            }
        }
        final Rectangle[] rectangles = rectangleList.toArray(new Rectangle[rectangleList.size()]);
        return createApproximations(lonSamples, latSamples, maskSamples, accuracy, rectangles,
                                    new DefaultSteppingFactory());
    }

    private static GeoApproximation create(SampleSource lonSamples, SampleSource latSamples, SampleSource maskSamples, double accuracy, Rectangle range, SteppingFactory steppingFactory) {
        Stepping stepping = steppingFactory.createStepping(range, 1000);
        double[][] data = extractWarpPoints(lonSamples, latSamples, maskSamples, stepping);
        return create(data, accuracy, range);
    }

    static GeoApproximation create(double[][] data, double accuracy, Rectangle range) {
        Point2D centerPoint = org.esa.snap.core.datamodel.Rotator.calculateCenter(data, 1, 0);
        double centerLon = centerPoint.getX();
        double centerLat = centerPoint.getY();
        double maxDistance = 1.0D - Math.cos(1.1D * Math.acos(1.0D - maxDistance(data, centerLon, centerLat)));
        Rotator rotator = new Rotator(centerLon, centerLat);
        rotator.transform(data, 1, 0);
        int[] xIndices = new int[]{0, 1, 2};
        int[] yIndices = new int[]{0, 1, 3};
        RationalFunctionModel fX = findBestModel(data, xIndices, accuracy);
        RationalFunctionModel fY = findBestModel(data, yIndices, accuracy);
        if(fX != null && fY != null) {
            int[] lonIndices = new int[]{2, 3, 1};
            int[] latIndices = new int[]{2, 3, 0};
            RationalFunctionModel fLon = findBestModel(data, lonIndices, 0.01D);
            RationalFunctionModel fLat = findBestModel(data, latIndices, 0.01D);
            return new GeoApproximation(fX, fY, fLon, fLat, maxDistance, rotator, new CosineDistance(centerLon, centerLat), range);
        } else {
            return null;
        }
    }

    static GeoApproximation findMostSuitable(GeoApproximation[] approximations, double lat, double lon) {
        GeoApproximation bestApproximation = null;
        if(approximations.length == 1) {
            GeoApproximation minDistance = approximations[0];
            double distance = minDistance.getDistance(lat, lon);
            if(distance < minDistance.getMaxDistance()) {
                bestApproximation = minDistance;
            }
        } else {
            double var14 = 1.7976931348623157E308D;
            GeoApproximation[] var8 = approximations;
            int var9 = approximations.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                GeoApproximation a = var8[var10];
                double distance1 = a.getDistance(lat, lon);
                if(distance1 < var14 && distance1 < a.getMaxDistance()) {
                    var14 = distance1;
                    bestApproximation = a;
                }
            }
        }

        return bestApproximation;
    }

    static GeoApproximation findSuitable(GeoApproximation[] approximations, PixelPos p) {
        GeoApproximation[] var2 = approximations;
        int var3 = approximations.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            GeoApproximation a = var2[var4];
            if(a.getRange().contains(p)) {
                return a;
            }
        }

        return null;
    }

    static GeoApproximation[] createApproximations(SampleSource lonSamples, SampleSource latSamples, SampleSource maskSamples, double accuracy, Rectangle[] rectangles, SteppingFactory steppingFactory) {
        ArrayList approximations = new ArrayList(rectangles.length);
        Rectangle[] var8 = rectangles;
        int var9 = rectangles.length;

        for(int var10 = 0; var10 < var9; ++var10) {
            Rectangle rectangle = var8[var10];
            GeoApproximation approximation = create(lonSamples, latSamples, maskSamples, accuracy, rectangle, steppingFactory);
            if(approximation == null) {
                return null;
            }

            approximations.add(approximation);
        }

        return (GeoApproximation[])approximations.toArray(new GeoApproximation[approximations.size()]);
    }

    public RationalFunctionModel getFX() {
        return this.fX;
    }

    public RationalFunctionModel getFY() {
        return this.fY;
    }

    public double getMaxDistance() {
        return this.maxDistance;
    }

    public double getDistance(double lat, double lon) {
        return this.calculator.distance(lon, lat);
    }

    public Rotator getRotator() {
        return this.rotator;
    }

    public Rectangle getRange() {
        return this.range;
    }

    void g2p(Point2D g) {
        this.rotator.transform(g);
        double lon = g.getX();
        double lat = g.getY();
        double x = this.fX.getValue(lat, lon);
        double y = this.fY.getValue(lat, lon);
        g.setLocation(x, y);
    }

    void p2g(Point2D p) {
        double x = p.getX();
        double y = p.getY();
        double lon = this.fLon.getValue(x, y);
        double lat = this.fLat.getValue(x, y);
        p.setLocation(lon, lat);
        this.rotator.transformInversely(p);
    }

    GeoApproximation(RationalFunctionModel fX, RationalFunctionModel fY, RationalFunctionModel fLon, RationalFunctionModel fLat, double maxDistance, Rotator rotator, DistanceMeasure calculator, Rectangle range) {
        this.fX = fX;
        this.fY = fY;
        this.fLon = fLon;
        this.fLat = fLat;
        this.maxDistance = maxDistance;
        this.rotator = rotator;
        this.calculator = calculator;
        this.range = range;
    }

    private static double maxDistance(double[][] data, double centerLon, double centerLat) {
        CosineDistance distanceMeasure = new CosineDistance(centerLon, centerLat);
        double maxDistance = 0.0D;
        double[][] var8 = data;
        int var9 = data.length;

        for(int var10 = 0; var10 < var9; ++var10) {
            double[] p = var8[var10];
            double d = distanceMeasure.distance(p[1], p[0]);
            if(d > maxDistance) {
                maxDistance = d;
            }
        }

        return maxDistance;
    }

    private static RationalFunctionModel findBestModel(double[][] data, int[] indexes, double accuracy) {
        RationalFunctionModel bestModel = null;

        for(int degreeP = 0; degreeP <= 4; ++degreeP) {
            for(int degreeQ = 0; degreeQ <= degreeP; ++degreeQ) {
                int termCountP = RationalFunctionModel.getTermCountP(degreeP);
                int termCountQ = RationalFunctionModel.getTermCountQ(degreeQ);
                if(data.length >= termCountP + termCountQ) {
                    RationalFunctionModel model = createModel(degreeP, degreeQ, data, indexes);
                    if(bestModel == null || model.getRmse() < bestModel.getRmse()) {
                        bestModel = model;
                    }

                    if(bestModel.getRmse() < accuracy) {
                        return bestModel;
                    }
                }
            }
        }

        return bestModel;
    }

    private static RationalFunctionModel createModel(int degreeP, int degreeQ, double[][] data, int[] indexes) {
        int ix = indexes[0];
        int iy = indexes[1];
        int iz = indexes[2];
        double[] x = new double[data.length];
        double[] y = new double[data.length];
        double[] g = new double[data.length];

        for(int i = 0; i < data.length; ++i) {
            x[i] = data[i][ix];
            y[i] = data[i][iy];
            g[i] = data[i][iz];
        }

        return new RationalFunctionModel(degreeP, degreeQ, x, y, g);
    }

    static double[][] extractWarpPoints(SampleSource lonSamples, SampleSource latSamples, SampleSource maskSamples, Stepping stepping) {
        int minX = stepping.getMinX();
        int maxX = stepping.getMaxX();
        int minY = stepping.getMinY();
        int maxY = stepping.getMaxY();
        int pointCountX = stepping.getPointCountX();
        int pointCountY = stepping.getPointCountY();
        int stepX = stepping.getStepX();
        int stepY = stepping.getStepY();
        int pointCount = stepping.getPointCount();
        ArrayList pointList = new ArrayList(pointCount);
        int j = 0;

        for(int k = 0; j < pointCountY; ++j) {
            int y = minY + j * stepY;
            if(y > maxY) {
                y = maxY;
            }

            for(int i = 0; i < pointCountX; ++k) {
                int x = minX + i * stepX;
                if(x > maxX) {
                    x = maxX;
                }

                int mask = maskSamples.getSample(x, y);
                if(mask != 0) {
                    double lat = latSamples.getSampleDouble(x, y);
                    double lon = lonSamples.getSampleDouble(x, y);
                    if(!Double.isNaN(lon) && lat >= -90.0D && lat <= 90.0D) {
                        double[] point = new double[]{lat, normalizeLon(lon), (double)x + 0.5D, (double)y + 0.5D};
                        pointList.add(point);
                    }
                }

                ++i;
            }
        }

        return (double[][])pointList.toArray(new double[pointList.size()][4]);
    }

    static double normalizeLon(double lon) {
        if(lon < -360.0D || lon > 360.0D) {
            lon %= 360.0D;
        }

        if(lon < -180.0D) {
            lon += 360.0D;
        } else if(lon > 180.0D) {
            lon -= 360.0D;
        }

        return lon;
    }

    private static class Tiling {
        private final int width;
        private final int height;
        private final int tileWidth;
        private final int tileHeight;
        private final Rectangle bounds;

        public Tiling(int width, int height) {
            this.width = width;
            this.height = height;
            this.bounds = new Rectangle(width, height);
            Dimension tileSize = JAIUtils.computePreferredTileSize(width, height, 1);
            this.tileWidth = tileSize.width;
            this.tileHeight = tileSize.height;
        }

        public int getNumXTiles() {
            return this.XToTileX(this.width - 1) - this.XToTileX(0) + 1;
        }

        public int getNumYTiles() {
            return this.YToTileY(this.height - 1) - this.YToTileY(0) + 1;
        }

        public Rectangle getTileRect(int tileX, int tileY) {
            return this.bounds.intersection(this.createRectangle(tileX, tileY));
        }

        private Rectangle createRectangle(int tileX, int tileY) {
            return new Rectangle(this.tileXToX(tileX), this.tileYToY(tileY), this.tileWidth, this.tileHeight);
        }

        private int tileXToX(int tx) {
            return tx * this.tileWidth;
        }

        private int tileYToY(int ty) {
            return ty * this.tileHeight;
        }

        private int XToTileX(int x) {
            return x / this.tileWidth;
        }

        private int YToTileY(int y) {
            return y / this.tileHeight;
        }
    }
}
