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
import org.esa.snap.core.util.math.MathUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AngularCosineProportionScreening implements Screening {

    private final Interval singlePixel = new Interval(1, 1);

    private Configuration configuration;

    @Override
    public void apply(MatchupSet matchupSet, Reader primaryReader, Map<String, Reader> secondaryReader, ScreeningContext context) throws IOException, InvalidRangeException {
        final List<SampleSet> resultSet = new ArrayList<>();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();

        for (final SampleSet sampleSet : sampleSets) {
            final Sample primaryPixel = sampleSet.getPrimary();
            final Array szaPrimaryArray = primaryReader.readScaled(primaryPixel.x, primaryPixel.y, singlePixel, configuration.primaryVariableName);
            final double primaryVZA = szaPrimaryArray.getDouble(0);

            final Sample secondaryPixel = sampleSet.getSecondary(SampleSet.getOnlyOneSecondaryKey());
            final Reader reader = secondaryReader.get(SampleSet.getOnlyOneSecondaryKey());
            final Array szaSecondaryArray = reader.readScaled(secondaryPixel.x, secondaryPixel.y, singlePixel, configuration.secondaryVariableName);
            final double secondaryVZA = szaSecondaryArray.getDouble(0);

            final double primaryCosine = Math.cos(primaryVZA * MathUtils.DTOR);
            final double secondaryCosine = Math.cos(secondaryVZA * MathUtils.DTOR);
            final double cosineRelation = Math.abs(primaryCosine/secondaryCosine - 1.0);
            if (cosineRelation < configuration.threshold) {
                resultSet.add(sampleSet);
            }
        }

        matchupSet.setSampleSets(resultSet);
        sampleSets.clear();
    }

    void configure(Configuration configuration) {
        this.configuration = configuration;
    }

    static class Configuration {
        String primaryVariableName;
        String secondaryVariableName;

        double threshold;
    }
}
