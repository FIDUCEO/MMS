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
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class BuehlerCloudScreening implements Screening {

    private final Interval singlePixel = new Interval(1, 1);

    private Configuration configuration;
    private boolean usePrimary;
    private boolean useSecondary;

    @Override
    public void apply(MatchupSet matchupSet, Reader primaryReader, Reader[] secondaryReader, ScreeningContext context) throws IOException, InvalidRangeException {
        if (usePrimary) {
            runScreening(matchupSet, primaryReader, true);
        }

        if (useSecondary) {
            // todo se multisensor
            runScreening(matchupSet, secondaryReader[0], false);
        }
    }

    // package access for testing only tb 2016-05-10
    double calculateThreshold(double szaDegrees) {
        double accumulate = 0.0;

        double power = 1.0;
        accumulate += power * 240.0307051931515;

        power *= szaDegrees;
        accumulate += power * 0.072461406038773;

        power *= szaDegrees;
        accumulate += power * (-0.015418271153414);

        power *= szaDegrees;
        accumulate += power * 0.001002246603123;

        power *= szaDegrees;
        accumulate += power * (-3.340724245004187e-5);

        power *= szaDegrees;
        accumulate += power *  5.185745040106524e-7;

        power *= szaDegrees;
        accumulate += power * (-3.063798878759070e-9);

        return accumulate;
    }

    void configure(Configuration configuration) {
        this.configuration = configuration;

        usePrimary = false;
        useSecondary = false;

        if (StringUtils.isNotNullAndNotEmpty(configuration.primaryNarrowChannelName) &&
                StringUtils.isNotNullAndNotEmpty(configuration.primaryWideChannelName) &&
                StringUtils.isNotNullAndNotEmpty(configuration.primaryVZAVariableName)) {
            usePrimary = true;
        }

        if (StringUtils.isNotNullAndNotEmpty(configuration.secondaryNarrowChannelName) &&
                StringUtils.isNotNullAndNotEmpty(configuration.secondaryWideChannelName) &&
                StringUtils.isNotNullAndNotEmpty(configuration.secondaryVZAVariableName)) {
            useSecondary = true;
        }
    }

    // @todo 3 tb/** this method needs refactoring in a concentrated and quiet moment 2016-05-10
    private void runScreening(MatchupSet matchupSet, Reader reader, boolean primary) throws IOException, InvalidRangeException {
        final List<SampleSet> resultSet = new ArrayList<>();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();

        String narrowChannelName;
        String wideChannelName;
        String vzaVariableName;
        if (primary) {
            narrowChannelName = configuration.primaryNarrowChannelName;
            wideChannelName = configuration.primaryWideChannelName;
            vzaVariableName = configuration.primaryVZAVariableName;
        } else {
            narrowChannelName = configuration.secondaryNarrowChannelName;
            wideChannelName = configuration.secondaryWideChannelName;
            vzaVariableName = configuration.secondaryVZAVariableName;
        }

        for (final SampleSet sampleSet : sampleSets) {
            final Sample pixel;
            if (primary) {
                pixel = sampleSet.getPrimary();
            } else {
                pixel = sampleSet.getSecondary(SampleSet.getOnlyOneSecondaryKey());
            }
            final Array narrowChannelArray = reader.readScaled(pixel.x, pixel.y, singlePixel, narrowChannelName);
            final Array wideChannelArray = reader.readScaled(pixel.x, pixel.y, singlePixel, wideChannelName);

            final double narrowChannelBTemp = narrowChannelArray.getDouble(0);
            final double wideChannelBTemp = wideChannelArray.getDouble(0);

            if (wideChannelBTemp > narrowChannelBTemp) {
                continue;
            }

            final Array vzaArray = reader.readScaled(pixel.x, pixel.y, singlePixel, vzaVariableName);
            final double vza = vzaArray.getDouble(0);
            final double threshold = calculateThreshold(vza);
            if (narrowChannelBTemp < threshold) {
                continue;
            }

            resultSet.add(sampleSet);
        }

        matchupSet.setSampleSets(resultSet);
        sampleSets.clear();
    }

    static class Configuration {
        String primaryNarrowChannelName;
        String primaryWideChannelName;
        String primaryVZAVariableName;

        String secondaryNarrowChannelName;
        String secondaryWideChannelName;
        String secondaryVZAVariableName;
    }
}
