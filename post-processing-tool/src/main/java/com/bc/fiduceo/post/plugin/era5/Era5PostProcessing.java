package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.core.GeoRect;
import com.bc.fiduceo.post.PostProcessing;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.awt.*;
import java.io.IOException;

class Era5PostProcessing extends PostProcessing {

    private static final double EPS = 0.00000001;

    private final Configuration configuration;

    private SatelliteFields satelliteFields;
    private MatchupFields matchupFields;

    Era5PostProcessing(Configuration configuration) {
        super();

        this.configuration = configuration;
        satelliteFields = null;
        matchupFields = null;
    }

    // package access for testing only tb 2020-11-17
    static GeoRect getGeoRegion(Array lonArray, Array latArray) {
        // @todo 1 tb/tb check for anti-meridian and handle 2020-11-17
        final float[] lonMinMax = getMinMax(lonArray);
        final float[] latMinMax = getMinMax(latArray);

        return new GeoRect(lonMinMax[0], lonMinMax[1], latMinMax[0], latMinMax[1]);
    }

    // package access for testing only tb 2020-11-17
    static Rectangle getEra5RasterPosition(GeoRect geoRect) {
        final float lonMin = geoRect.getLonMin();
        final int xMin = getEra5LonMin(lonMin);

        final float lonMax = geoRect.getLonMax();
        final int xMax = getEra5LonMax(lonMax);

        // remember: y axis runs top->down so we need to invert the coordinates tb 2020-11-17
        final int yMax = getEra5LatMax(geoRect.getLatMin());
        final int yMin = getEra5LatMin(geoRect.getLatMax());

        return new Rectangle(xMin, yMin, xMax - xMin + 1, yMax - yMin + 1);
    }

    private static int getEra5LatMin(float latMax) {
        final double shiftedLat = latMax + EPS;
        final double scaledLatMax = Math.ceil(shiftedLat * 4) / 4;
        return (int) ((90.0 - scaledLatMax) * 4.0);
    }

    private static int getEra5LatMax(float latMin) {
        final double shiftedLat = latMin - EPS;
        final double scaledLatMin = Math.floor(shiftedLat * 4) / 4;
        return (int) ((90.0 - scaledLatMin) * 4.0);
    }

    private static int getEra5LonMax(float lonMax) {
        final double shiftedLon = lonMax + EPS;
        final double normLonMax = shiftedLon + 180.0;
        final double scaledLonMax = Math.ceil(normLonMax * 4) / 4;
        return (int) (scaledLonMax * 4);
    }

    private static int getEra5LonMin(float lonMin) {
        final double shiftedLon = lonMin - EPS;
        final double normLonMin = shiftedLon + 180.0;
        final double scaledLonMin = Math.floor(normLonMin * 4) / 4;
        return (int) (scaledLonMin * 4);
    }

    // package access for testing only tb 2020-11-20
    static InterpolationContext getInterpolationContext(Array lonArray, Array latArray) {
        // todo 2 tb/tb check shape 2020-11-20
        final int[] shape = lonArray.getShape();
        final InterpolationContext context = new InterpolationContext(shape[1], shape[0]);

        final Index lonIdx = lonArray.getIndex();
        final Index latIdx = latArray.getIndex();
        for (int y = 0; y < shape[0]; y++) {
            for (int x = 0; x < shape[1]; x++) {
                lonIdx.set(y, x);
                latIdx.set(y, x);

                final float lon = lonArray.getFloat(lonIdx);
                final float lat = latArray.getFloat(latIdx);

                // + detect four era5 corner-points for interpolation
                // + calculate longitude delta -> a
                // + calculate latitude delta -> b
                // + create BilinearInterpolator(a, b)
                // + store to context at (x, y)
                final double era5LonMin = getEra5LonMin(lon) * 0.25 - 180.0;
                final double era5LatMin = 90.0 - getEra5LatMin(lat) * 0.25;

                // we have a quarter degree raster and need to normalize the distance tb 2020-11-20
                final double lonDelta = (lon - era5LonMin) * 4.0;
                final double latDelta = (era5LatMin - lat) * 4.0;

                final BilinearInterpolator interpolator = new BilinearInterpolator(lonDelta, latDelta);
                context.set(x, y, interpolator);
            }
        }
        return context;
    }

    private static float[] getMinMax(Array floatArray) {
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        final Index index = floatArray.getIndex();
        final int[] shape = floatArray.getShape();
        for (int y = 0; y < shape[0]; y++) {
            for (int x = 0; x < shape[1]; x++) {
                index.set(y, x);
                final float value = floatArray.getFloat(index);
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
        }
        return new float[]{min, max};
    }


    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Dimension matchupCountDimension = reader.findDimension(FiduceoConstants.MATCHUP_COUNT);
        if (matchupCountDimension == null) {
            throw new RuntimeException("Expected dimension not present in file: " + FiduceoConstants.MATCHUP_COUNT);
        }

        final SatelliteFieldsConfiguration satFieldsConfig = configuration.getSatelliteFields();
        if (satFieldsConfig != null) {
            satelliteFields = new SatelliteFields();
            satelliteFields.prepare(satFieldsConfig, reader, writer);
        }

        final MatchupFieldsConfiguration matchupFieldsConfig = configuration.getMatchupFields();
        if (matchupFieldsConfig != null) {
            matchupFields = new MatchupFields();
            matchupFields.prepare();
        }
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        if (satelliteFields != null) {
            satelliteFields.compute(configuration, reader, writer);
        }

        if (matchupFields != null) {
            matchupFields.compute();
        }
    }

    @Override
    protected void dispose() {
        satelliteFields = null;
        matchupFields = null;

        super.dispose();
    }
}
