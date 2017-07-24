/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.matchup.screening;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.Reader;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AtsrAngularScreening implements Screening {

    private final Interval singlePixel = new Interval(1, 1);

    private Configuration configuration;

    AtsrAngularScreening() {
        this.configuration = new Configuration();
    }

    @Override
    public void apply(MatchupSet matchupSet, Reader primaryReader, Map<String, Reader> secondaryReader, ScreeningContext context) throws IOException, InvalidRangeException {
        final List<SampleSet> resultSet = new ArrayList<>();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();

        for (final SampleSet sampleSet : sampleSets) {
            final Sample primary = sampleSet.getPrimary();

            final Array nadirElevArray = primaryReader.readScaled(primary.x, primary.y, singlePixel, "view_elev_nadir");
            final Array fwardElevArray = primaryReader.readScaled(primary.x, primary.y, singlePixel, "view_elev_fward");

            double nadirViewZenith = 90.0 - nadirElevArray.getDouble(0);
            double fwardViewZenith = 90.0 - fwardElevArray.getDouble(0);
            if (primary.x > 256) {
                nadirViewZenith *= -1.0;
                fwardViewZenith *= -1.0;
            }

            final Sample secondary = sampleSet.getSecondary(SampleSet.getOnlyOneSecondaryKey());
            final Reader reader = secondaryReader.get(SampleSet.getOnlyOneSecondaryKey());
            final Array satelliteZenithAngleArray = reader.readScaled(secondary.x, secondary.y, singlePixel, "satellite_zenith_angle");
            double satZenithAngle = satelliteZenithAngleArray.getDouble(0);
            if (secondary.x > 204) {
                satZenithAngle *= -1.0;
            }

            final double nadirDelta = Math.abs(satZenithAngle - nadirViewZenith);
            final double fwardDelta = Math.abs(satZenithAngle - fwardViewZenith);

            if (nadirDelta <= configuration.angleDeltaNadir || fwardDelta <= configuration.angleDeltaFward) {
                resultSet.add(sampleSet);
            }
        }

        matchupSet.setSampleSets(resultSet);
    }

    void configure(Configuration configuration) {
        this.configuration = configuration;
    }

    static class Configuration {
        double angleDeltaNadir;
        double angleDeltaFward;
    }
}
