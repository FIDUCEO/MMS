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
import com.bc.fiduceo.matchup.screening.expression.ReaderEvalEnv;
import com.bc.fiduceo.matchup.screening.expression.ReaderNamespace;
import com.bc.fiduceo.reader.Reader;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.impl.ParserImpl;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WindowValueScreening implements Screening {

    private final Configuration configuration;

    public WindowValueScreening(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void apply(MatchupSet matchupSet, Reader primaryReader, Reader secondaryReader, ScreeningContext context) throws IOException, InvalidRangeException {
        List<SampleSet> sampleSets = matchupSet.getSampleSets();

        final String primaryExpression = configuration.primaryExpression;
        if (StringUtils.isNotNullAndNotEmpty(primaryExpression)) {
            sampleSets = getKeptSampleSets(sampleSets, primaryExpression, primaryReader, context.getPrimaryDimension(), SampleSet::getPrimary);
        }

        final String secondaryExpression = configuration.secondaryExpression;
        if (StringUtils.isNotNullAndNotEmpty(secondaryExpression)) {
            sampleSets = getKeptSampleSets(sampleSets, secondaryExpression, secondaryReader, context.getSecondaryDimension(), SampleSet::getSecondary);
        }

        matchupSet.setSampleSets(sampleSets);
    }

    static List<SampleSet> getKeptSampleSets(List<SampleSet> sampleSets, String expression, Reader reader, Dimension dimension, SampleFetcher sampleFetcher) throws InvalidRangeException, IOException {
        List<SampleSet> keptSets = new ArrayList<>();
        final ReaderNamespace readerNamespace = new ReaderNamespace(reader);
        final ParserImpl parser = new ParserImpl(readerNamespace);
        final ReaderEvalEnv readerEvalEnv = new ReaderEvalEnv(reader);
        try {
            final Term term = parser.parse(expression);
            final int width = dimension.getNx();
            final int height = dimension.getNy();
            final int offsX = 0 - width/2;
            final int offsY = 0 - height/2;
            for (final SampleSet sampleSet : sampleSets) {
                final Sample sample = sampleFetcher.getSample(sampleSet);
                final int yTop = sample.y + offsY;
                final int xLeft = sample.x + offsX;
                int trueCount = 0;
                for (int y = 0; y < height; y++) {
                    final int yLoc = yTop + y;
                    for (int x = 0; x < width; x++) {
                        final int xLoc = xLeft + x;
                        readerEvalEnv.setLocation(xLoc, yLoc);
                        if (term.evalB(readerEvalEnv)) {
                            trueCount++;
                        }
                    }
                }

                final boolean keep = trueCount == width*height;
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

    static class Configuration {

        public String primaryExpression;
        public Double primaryPercentage;
        public String secondaryExpression;
        public Double secondaryPercentage;
    }
}
