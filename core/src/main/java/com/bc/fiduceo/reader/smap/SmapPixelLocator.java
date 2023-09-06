package com.bc.fiduceo.reader.smap;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.math.SphericalDistance;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

class SmapPixelLocator implements PixelLocator {

    private static final String YDIM_GRID = "ydim_grid";
    private static final String XDIM_GRID = "xdim_grid";
    private static final String LOOK = "look";
    private static final int CELL_COUNT_360_DEGREE = 360 * 4;
    private static final String CF_FillValue = NetCDFUtils.CF_FILL_VALUE_NAME;

    private final Array lons;
    private final Array lats;
    private final int fullHeight;
    private final int fullWidth;
    private final float fillValue;
    private final int xIdx;
    private final int yIdx;
    private final double degree360ColIdx;

    SmapPixelLocator(Variable cellonVar, Variable cellatVar, int lookVal) throws IOException {

        yIdx = cellonVar.findDimensionIndex(YDIM_GRID);
        fullHeight = cellonVar.getDimension(yIdx).getLength();
        xIdx = cellonVar.findDimensionIndex(XDIM_GRID);
        fullWidth = cellonVar.getDimension(xIdx).getLength();

        fillValue = cellonVar.findAttribute(CF_FillValue).getNumericValue().floatValue();

        final int lookIdx = cellonVar.findDimensionIndex(LOOK);
        final int[] shape = cellonVar.getShape();
        shape[lookIdx] = 1;
        final int[] origin = new int[shape.length];
        origin[lookIdx] = lookVal;

        try {
            lons = cellonVar.read(origin, shape).reduce();
            lats = cellatVar.read(origin, shape).reduce();
            degree360ColIdx = find360degreeIndex() + 0.5;
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }

    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D g) {
        if (x<0 || x>=fullWidth|| y<0||y>=fullHeight) {
            return null;
        }
        final Index index = lons.getIndex();
        index.set((int) Math.floor(y), (int) Math.floor(x));
        final float lon = lons.getFloat(index);
        final float lat = lats.getFloat(index);
        if (lon != fillValue && lat != fillValue) {
            if (g != null) {
                g.setLocation(lon, lat);
                return g;
            }
            return new Point2D.Float(lon, lat);
        }
        return null;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        if (lat > 90 || lat < -90) {
            return new Point2D[0];
        }
        lon = ensureLonRange360(lon);
        double lonIDX = degree360ColIdx - 1 - Math.floor(lon * 4);
        while (lonIDX < 0) {
            lonIDX += CELL_COUNT_360_DEGREE;
        }
        final double latIDX = Math.floor((lat + 90) * 4) + 0.5;

        final ArrayList<Point2D.Double> validPixels = new ArrayList<>();
        addClosestValidPixel(lonIDX, latIDX, lon, lat, validPixels);

        final double secondLonIDX = lonIDX + CELL_COUNT_360_DEGREE;
        if (secondLonIDX < fullWidth) {
            addClosestValidPixel(secondLonIDX, latIDX, lon, lat, validPixels);
        }
        return validPixels.toArray(new Point2D[0]);
    }

    private void addClosestValidPixel(double lonIDX, double latIDX, double lon, double lat, ArrayList<Point2D.Double> validPixels) {
        try {
            final Dimension productSize = new Dimension("size", fullWidth, fullHeight);
            final Interval readSize = new Interval(3, 3);
            final int centerX = (int) lonIDX;
            final int centerY = (int) latIDX;
            final float[] lon3x3 = (float[]) RawDataReader.read(centerX, centerY, readSize, fillValue, lons, productSize).copyTo1DJavaArray();
            final float[] lat3x3 = (float[]) RawDataReader.read(centerX, centerY, readSize, fillValue, lats, productSize).copyTo1DJavaArray();
            double min = Double.MAX_VALUE;
            int minIdx = -1;
            final SphericalDistance sd = new SphericalDistance(lon, lat);
            for (int i = 0; i < lon3x3.length; i++) {
                float lon2 = lon3x3[i];
                float lat2 = lat3x3[i];
                if (lon2 == fillValue || lat2 == fillValue) {
                    continue;
                }
                final double dist = sd.distance(lon2, lat2);
                if (dist < min) {
                    min = dist;
                    minIdx = i;
                }
            }
            if (minIdx > -1) {
                final int corrLonIDX = minIdx % 3 - 1;
                final int corrLatIDX = minIdx / 3 - 1;
                validPixels.add(new Point2D.Double(lonIDX + corrLonIDX, latIDX + corrLatIDX));
            }
        } catch (IOException e) {
            // This should never happen, because the data is already loaded.
            throw new RuntimeException(e);
        }
    }

    private static double ensureLonRange360(double lon) {
        while (lon < 0) {
            lon += 360;
        }
        while (lon > 360) {
            lon -= 360;
        }
        if (lon == 360.0) {
            lon = 0.0;
        }
        return lon;
    }

    private int find360degreeIndex() throws InvalidRangeException {
        final int[] verticalShape = new int[]{fullHeight, 1};
        final int[] origin = new int[2];
        SectionInfo mostValuesSection = new SectionInfo(0, 0, -1);
        int mostValuesIdx = 0;
        for (int x = 0; x < fullWidth; x++) {
            origin[xIdx] = x;
            final Array sectArr = lons.section(origin, verticalShape);
            final SectionInfo si = getSectionInfo((float[]) sectArr.copyTo1DJavaArray());
//            if (Double.MAX_VALUE==si.min) {
//                System.out.printf("%4d  ---,---  ---,---  ---,--- \n", x, si.min, si.max + ((si.max - si.min) / 2), si.max);
//            } else {
//                System.out.printf("%4d  %7.3f  %7.3f  %7.3f \n", x, si.min, si.min + ((si.max - si.min) / 2), si.max);
//            }
            if (si.validCount > mostValuesSection.validCount) {
                mostValuesSection = si;
                mostValuesIdx = x;
            }
        }
        final double min = mostValuesSection.min;
        final int quarterDegree0To360CellIndex = (int) Math.floor(min * 4);
        final int offsetTo360DegreeCell = CELL_COUNT_360_DEGREE - 1 - quarterDegree0To360CellIndex;
        return mostValuesIdx - offsetTo360DegreeCell;
    }

    private SectionInfo getSectionInfo(float[] slice) {
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        int count = 0;
        for (float val : slice) {
            if (val == fillValue) {
                continue;
            }
            count++;
            if (val > max) {
                max = val;
            }
            if (val < min) {
                min = val;
            }
        }
        return new SectionInfo(min, max, count);
    }

    static class SectionInfo {
        final double min;
        final double max;
        final int validCount;

        private int x;

        SectionInfo(double min, double max, int validCount) {
            this.min = min;
            this.max = max;
            this.validCount = validCount;
        }
    }
}
