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
 */
package com.bc.fiduceo.matchup.screening;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.matchup.screening.expression.WindowReaderEvalEnv;
import com.bc.fiduceo.matchup.screening.expression.WindowReaderNamespace;
import com.bc.fiduceo.reader.Reader;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.impl.ParserImpl;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bc.fiduceo.matchup.screening.WindowValueScreening.Evaluate.EntireWindow;


// todo se multisensor
public class WindowValueScreening implements Screening {

    private final Configuration configuration;

    WindowValueScreening(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void apply(MatchupSet matchupSet, Reader primaryReader, Map<String, Reader> secondaryReader, ScreeningContext context) throws IOException, InvalidRangeException {
        List<SampleSet> sampleSets = matchupSet.getSampleSets();

        final String primaryExpression = configuration.primaryExpression;
        if (StringUtils.isNotNullAndNotEmpty(primaryExpression)) {
            // todo se multisensor
            final Dimension primaryDimension = context.getPrimaryDimension();
            final SampleFetcher primarySampleFetcher = SampleSet::getPrimary;
            final double percentage = configuration.primaryPercentage;
            final Evaluate evaluate = configuration.primaryEvaluate;
            sampleSets = getKeptSampleSets(sampleSets, primaryExpression, primaryReader, primaryDimension, primarySampleFetcher, percentage, evaluate);
        }

        final String secondaryExpression = configuration.secondaryExpression;
        if (StringUtils.isNotNullAndNotEmpty(secondaryExpression)) {
            // todo se multisensor
            final String sensorName = SampleSet.getOnlyOneSecondaryKey();
            final Dimension secondaryDimension = context.getSecondaryDimension(sensorName);
            final SampleFetcher secondarySampleFetcher = (sampleSet) -> sampleSet.getSecondary(sensorName);
            final double percentage = configuration.secondaryPercentage;
            final Evaluate evaluate = configuration.secondaryEvaluate;
            // todo se multisensor
            sampleSets = getKeptSampleSets(sampleSets, secondaryExpression, secondaryReader.get(sensorName), secondaryDimension, secondarySampleFetcher, percentage, evaluate);
        }

        matchupSet.setSampleSets(sampleSets);
    }

    static List<SampleSet> getKeptSampleSets(List<SampleSet> sampleSets, String expression, Reader reader,
                                             Dimension dimension, SampleFetcher sampleFetcher,
                                             double percentage, Evaluate evaluate) throws InvalidRangeException, IOException {
        List<SampleSet> keptSets = new ArrayList<>();
        final WindowReaderNamespace readerNamespace = new WindowReaderNamespace(reader);
        final WindowReaderEvalEnv readerEvalEnv = readerNamespace.getEvalEnv();
        final ParserImpl parser = new ParserImpl(readerNamespace);
        try {
            final Term term = parser.parse(expression);
            final int width = dimension.getNx();
            final int height = dimension.getNy();
            for (final SampleSet sampleSet : sampleSets) {
                final Sample sample = sampleFetcher.getSample(sampleSet);
                readerEvalEnv.setWindow(sample.x, sample.y, width, height);
                int trueCount = 0;
                int noDataCount = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        readerEvalEnv.setLocationInWindow(x, y);
                        final boolean result = term.evalB(readerEvalEnv);
                        if (readerEvalEnv.isNoData()) {
                            noDataCount++;
                        } else if (result) {
                            trueCount++;
                        }
                    }
                }

                final int fullCount = width * height;
                final double minCount;
                if (EntireWindow.equals(evaluate)) {
                    minCount = fullCount * percentage * 0.01;
                } else {
                    final int validCount = fullCount - noDataCount;
                    minCount = validCount * percentage * 0.01;
                }
                final boolean keep = trueCount >= minCount;
                if (!keep) {
                    continue;
                }
                keptSets.add(sampleSet);
            }

        } catch (ParseException e) {
            throw new IOException("Invalid expression: " + e.getMessage());
        }

        return keptSets;
    }

    interface SampleFetcher {

        Sample getSample(SampleSet sampleSet);
    }

    // todo se multisensor
    static class Configuration {

        String primaryExpression;
        Double primaryPercentage;
        Evaluate primaryEvaluate;
        String secondaryExpression;
        Double secondaryPercentage;
        Evaluate secondaryEvaluate;
    }

    enum Evaluate {
        EntireWindow,
        IgnoreNoData
    }
}
