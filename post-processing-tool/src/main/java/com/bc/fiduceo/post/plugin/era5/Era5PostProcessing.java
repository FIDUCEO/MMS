package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.post.PostProcessing;
import org.esa.snap.core.util.StringUtils;
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

    private static int getEra5LatMin(float latMax) {
        final double shiftedLat = latMax + EPS;
        final double scaledLatMax = Math.ceil(shiftedLat * 4) / 4;
        return (int) ((90.0 - scaledLatMax) * 4.0);
    }

    private static int getEra5LonMin(float lonMin) {
        final double shiftedLon = lonMin - EPS;
        final double normLonMin = shiftedLon + 180.0;
        final double scaledLonMin = Math.floor(normLonMin * 4) / 4;
        return (int) (scaledLonMin * 4);
    }

    // package access for testing only tb 2020-11-20
    static InterpolationContext getInterpolationContext(Array lonArray, Array latArray) {
        final int[] shape = lonArray.getShape();
        if (shape.length == 2) {
            return createInterpolationContext_2D(lonArray, latArray, shape);
        } else if (shape.length == 0) {
            return createInterpolationContext_1D(lonArray, latArray);
        }

        throw new IllegalStateException("Unsupported dimensionality of geolocation data");
    }

    private static InterpolationContext createInterpolationContext_2D(Array lonArray, Array latArray, int[] shape) {
        final InterpolationContext context = new InterpolationContext(shape[1], shape[0]);

        final Index lonIdx = lonArray.getIndex();
        final Index latIdx = latArray.getIndex();
        int xMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;
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
                final int era5_X_min = getEra5LonMin(lon);
                final int era5_Y_min = getEra5LatMin(lat);
                if (era5_X_min < xMin) {
                    xMin = era5_X_min;
                }
                if (era5_X_min > xMax) {
                    xMax = era5_X_min;
                }
                if (era5_Y_min < yMin) {
                    yMin = era5_Y_min;
                }
                if (era5_Y_min > yMax) {
                    yMax = era5_Y_min;
                }

                final double era5LonMin = era5_X_min * 0.25 - 180.0;
                final double era5LatMin = 90.0 - era5_Y_min * 0.25;

                // we have a quarter degree raster and need to normalize the distance tb 2020-11-20
                final double lonDelta = (lon - era5LonMin) * 4.0;
                final double latDelta = (era5LatMin - lat) * 4.0;

                final BilinearInterpolator interpolator = new BilinearInterpolator(lonDelta, latDelta, era5_X_min, era5_Y_min);
                context.set(x, y, interpolator);
            }

            // add 2 to width and height to have always 4 points for the interpolation tb 2020-11-30
            final Rectangle era5Rect = new Rectangle(xMin, yMin, xMax - xMin + 2, yMax - yMin + 2);
            context.setEra5Region(era5Rect);
        }
        return context;
    }

    private static InterpolationContext createInterpolationContext_1D(Array lonArray, Array latArray) {
        final InterpolationContext context = new InterpolationContext(1, 1);

        final float lon = lonArray.getFloat(0);
        final float lat = latArray.getFloat(0);

        final int era5_X_min = getEra5LonMin(lon);
        final int era5_Y_min = getEra5LatMin(lat);

        final double era5LonMin = era5_X_min * 0.25 - 180.0;
        final double era5LatMin = 90.0 - era5_Y_min * 0.25;

        // we have a quarter degree raster and need to normalize the distance tb 2020-11-20
        final double lonDelta = (lon - era5LonMin) * 4.0;
        final double latDelta = (era5LatMin - lat) * 4.0;

        final BilinearInterpolator interpolator = new BilinearInterpolator(lonDelta, latDelta, era5_X_min, era5_Y_min);
        context.set(0, 0, interpolator);

        final Rectangle era5Rect = new Rectangle(era5_X_min, era5_Y_min, 2, 2);
        context.setEra5Region(era5Rect);

        return context;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) {
        final Dimension matchupCountDimension = reader.findDimension(FiduceoConstants.MATCHUP_COUNT);
        if (matchupCountDimension == null) {
            throw new RuntimeException("Expected dimension not present in file: " + FiduceoConstants.MATCHUP_COUNT);
        }

        // @todo 1 tb/tb add generic prepare for global meta
        // - ERA5 collection string

        prepare(writer);

        final SatelliteFieldsConfiguration satFieldsConfig = configuration.getSatelliteFields();
        if (satFieldsConfig != null) {
            satelliteFields = new SatelliteFields();
            satelliteFields.prepare(satFieldsConfig, reader, writer);
        }

        final MatchupFieldsConfiguration matchupFieldsConfig = configuration.getMatchupFields();
        if (matchupFieldsConfig != null) {
            matchupFields = new MatchupFields();
            matchupFields.prepare(matchupFieldsConfig, reader, writer);
        }
    }

    private void prepare(NetcdfFileWriter writer) {
        String collection = getEra5Collection(configuration);
        writer.addGlobalAttribute("era5-collection", collection);
    }

    // package access for testing only tb 2021-01-12
    static String getEra5Collection(Configuration configuration) {
        String collection = configuration.getEra5Collection();
        if (StringUtils.isNotNullAndNotEmpty(collection)) {
            return collection;
        }

        // we need to find the collection in the path-name
        final String nwpAuxDir = configuration.getNWPAuxDir();
        final String upperCaseAuxDir = nwpAuxDir.toUpperCase();
        collection = "UNKNOWN";
        if (upperCaseAuxDir.contains("ERA5T") || upperCaseAuxDir.contains("ERA-5T")) {
            collection = "ERA-5T";
        } else if (upperCaseAuxDir.contains("ERA51") || upperCaseAuxDir.contains("ERA-51")) {
            collection = "ERA-51";
        } else if (upperCaseAuxDir.contains("ERA5") || upperCaseAuxDir.contains("ERA-5")) {
            collection = "ERA-5";
        }
        return collection;
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        if (satelliteFields != null) {
            satelliteFields.compute(configuration, reader, writer);
        }

        if (matchupFields != null) {
            matchupFields.compute(configuration, reader, writer);
        }
    }

    @Override
    protected void dispose() {
        satelliteFields = null;
        matchupFields = null;

        super.dispose();
    }
}
