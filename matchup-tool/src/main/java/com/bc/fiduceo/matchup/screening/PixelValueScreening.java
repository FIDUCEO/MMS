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

class PixelValueScreening implements Screening {

    private Configuration configuration;

    @Override
    public void apply(MatchupSet matchupSet, Reader primaryReader, Reader secondaryReader, ScreeningContext context) throws IOException, InvalidRangeException {
        if (configuration == null) {
            return;
        }

        List<SampleSet> sampleSets = matchupSet.getSampleSets();

        if (StringUtils.isNotNullAndNotEmpty(configuration.primaryExpression)) {
            List<SampleSet> keptSets = new ArrayList<>();
            final ReaderNamespace readerNamespace = new ReaderNamespace(primaryReader);
            final ParserImpl parser = new ParserImpl(readerNamespace);
            final ReaderEvalEnv readerEvalEnv = new ReaderEvalEnv(primaryReader);

            try {
                final Term term = parser.parse(configuration.primaryExpression);
                for (final SampleSet sampleSet : sampleSets) {
                    final Sample primary = sampleSet.getPrimary();
                    readerEvalEnv.setLocation(primary.x, primary.y);
                    final boolean keep = term.evalB(readerEvalEnv);
                    if (!keep) {
                        continue;
                    }
                    keptSets.add(sampleSet);
                }

            } catch (ParseException e) {
                throw new IOException("Invalid expression: " + e.getMessage());
            }

            sampleSets = keptSets;
        }

        if (StringUtils.isNotNullAndNotEmpty(configuration.secondaryExpression)) {
            List<SampleSet> keptSets = new ArrayList<>();
            final ReaderNamespace readerNamespace = new ReaderNamespace(secondaryReader);
            final ParserImpl parser = new ParserImpl(readerNamespace);
            final ReaderEvalEnv readerEvalEnv = new ReaderEvalEnv(secondaryReader);

            try {
                final Term term = parser.parse(configuration.secondaryExpression);
                for (final SampleSet sampleSet : sampleSets) {
                    final Sample secondary = sampleSet.getSecondary();
                    readerEvalEnv.setLocation(secondary.x, secondary.y);
                    final boolean keep = term.evalB(readerEvalEnv);
                    if (!keep) {
                        continue;
                    }
                    keptSets.add(sampleSet);
                }

            } catch (ParseException e) {
                throw new IOException("Invalid expression: " + e.getMessage());
            }
            sampleSets = keptSets;
        }

        matchupSet.setSampleSets(sampleSets);
    }

    public void configure(Configuration configuration) {
        this.configuration = configuration;
    }

    static class Configuration {
        String primaryExpression;
        String secondaryExpression;
    }
}
