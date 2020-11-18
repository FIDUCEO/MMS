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

    static Rectangle getEra5RasterPosition(GeoRect geoRect) {
        final float normLonMin = geoRect.getLonMin() + 180.f;
        final float normLonMax = geoRect.getLonMax() + 180.f;

        final double scaledLonMin = Math.floor(normLonMin * 4) / 4;
        final double scaledLonMax = Math.ceil(normLonMax * 4) / 4;
        final int xMin = (int)(scaledLonMin * 4);
        final int xMax = (int)(scaledLonMax * 4);

        final double scaledLatMin = Math.floor(geoRect.getLatMin() * 4) / 4;
        final double scaledLatMax = Math.ceil(geoRect.getLatMax() * 4) / 4;

        // remember: y axis runs top->down so we need to invert the coordinates tb 2020-11-17
        final int yMax = (int)((90.0 - scaledLatMin) * 4.0);
        final int yMin = (int)((90.0 - scaledLatMax) * 4.0);

        return new Rectangle(xMin, yMin, xMax - xMin + 1, yMax - yMin + 1);
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

        // @todo 1 tb/tb implement 2020-11-11
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        if (satelliteFields != null) {
            satelliteFields.compute();
        }

        if (matchupFields != null) {
            matchupFields.compute();
        }
        // @todo 1 tb/tb implement 2020-11-11
    }

    @Override
    protected void dispose() {
        satelliteFields = null;
        matchupFields = null;

        super.dispose();
    }
}
