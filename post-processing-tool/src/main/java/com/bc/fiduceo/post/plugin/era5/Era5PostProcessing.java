package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.post.PostProcessing;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

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
