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
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.Reader;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class HIRS_LZADeltaScreening implements Screening {

    private final Interval singlePixel = new Interval(1, 1);

    private Configuration configuration;

    @Override
    public void apply(MatchupSet matchupSet, Reader primaryReader, Reader secondaryReader, ScreeningContext context) throws IOException, InvalidRangeException {
        final List<SampleSet> resultSet = new ArrayList<>();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();

        for(final SampleSet sampleSet : sampleSets) {
            final Sample primary = sampleSet.getPrimary();
            final Array primaryLzaArray = primaryReader.readScaled(primary.x, primary.y, singlePixel, "lza");
            final Array primaryScanposArray = primaryReader.readScaled(primary.x, primary.y, singlePixel, "scanpos");

            final Sample secondary = sampleSet.getSecondary();
            final Array secondLzaArray = secondaryReader.readScaled(secondary.x, secondary.y, singlePixel, "lza");
            final Array secondScanPosArray = secondaryReader.readScaled(secondary.x, secondary.y, singlePixel, "scanpos");

            double primaryLza = primaryLzaArray.getDouble(0);
            final int primaryScanpos = primaryScanposArray.getInt(0);
            if (primaryScanpos < 28) {
                primaryLza *= -1.0;
            }

            double secondLza = secondLzaArray.getDouble(0);
            final int secondScanPos = secondScanPosArray.getInt(0);
            if (secondScanPos < 28) {
                secondLza *= -1.0;
            }

            final double lzaDelta = Math.abs(primaryLza - secondLza);
            if (lzaDelta < configuration.maxLzaDelta) {
                resultSet.add(sampleSet);
            }
        }

        matchupSet.setSampleSets(resultSet);
    }

    void configure(Configuration configuration) {
        this.configuration = configuration;
    }

    static class Configuration {
        double maxLzaDelta;
    }
}
