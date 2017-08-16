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
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AngularScreening implements Screening {

    private final Interval singlePixel = new Interval(1, 1);
    private Configuration configuration;
    private boolean hasPrimary;
    private boolean hasSecondary;

    AngularScreening() {
        this.configuration = new Configuration();
    }

    @Override
    public void apply(MatchupSet matchupSet, Reader primaryReader, Map<String, Reader> secondaryReader, ScreeningContext context) throws IOException, InvalidRangeException {
        final List<SampleSet> resultSet = new ArrayList<>();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();

        for (final SampleSet sampleSet : sampleSets) {
            final Reader reader = secondaryReader.get(SampleSet.getOnlyOneSecondaryKey());
            if (shouldBeKept(sampleSet, primaryReader, reader)) {
                resultSet.add(sampleSet);
            }
        }
        matchupSet.setSampleSets(resultSet);
        sampleSets.clear();
    }

    public void configure(Configuration configuration) {
        this.configuration = configuration;
        hasPrimary = StringUtils.isNotNullAndNotEmpty(configuration.primaryVariableName);
        hasSecondary = StringUtils.isNotNullAndNotEmpty(configuration.secondaryVariableName);
    }

    private boolean shouldBeKept(SampleSet sampleSet, Reader primaryReader, Reader secondaryReader) throws IOException, InvalidRangeException {
        double primaryVZA = Double.MAX_VALUE;
        double secondaryVZA = Double.MAX_VALUE;

        if (hasPrimary) {
            final Sample primaryPixel = sampleSet.getPrimary();
            final Array szaPrimaryArray = primaryReader.readScaled(primaryPixel.getX(), primaryPixel.getY(), singlePixel, configuration.primaryVariableName);
            final IndexIterator indexIterator = szaPrimaryArray.getIndexIterator();

            primaryVZA = indexIterator.getDoubleNext();
        }

        if (hasSecondary) {
            final Sample secondaryPixel = sampleSet.getSecondary(SampleSet.getOnlyOneSecondaryKey());
            final Array szaSecondaryArray = secondaryReader.readScaled(secondaryPixel.getX(), secondaryPixel.getY(), singlePixel, configuration.secondaryVariableName);

            final IndexIterator indexIterator = szaSecondaryArray.getIndexIterator();

            secondaryVZA = indexIterator.getDoubleNext();
        }

        if (configuration.usePrimary) {
            if (primaryVZA > configuration.maxPrimaryVZA) {
                return false;
            }
        }

        if (configuration.useSecondary) {
            if (secondaryVZA > configuration.maxSecondaryVZA) {
                return false;
            }
        }

        if (configuration.useDelta) {
            final double absDelta = Math.abs(primaryVZA - secondaryVZA);
            if (absDelta > configuration.maxAngleDelta) {
                return false;
            }
        }

        return true;
    }

    static class Configuration {

        String primaryVariableName;
        String secondaryVariableName;

        boolean usePrimary;
        double maxPrimaryVZA;

        boolean useSecondary;
        double maxSecondaryVZA;

        boolean useDelta;
        double maxAngleDelta;
    }
}
