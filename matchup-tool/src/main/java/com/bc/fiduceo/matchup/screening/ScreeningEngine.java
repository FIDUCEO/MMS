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

import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.reader.Reader;
import org.jdom.Element;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// @todo se write tests

public class ScreeningEngine {

    private final List<Screening> screeningList;

    public ScreeningEngine() {
        screeningList = new ArrayList<>();
    }

    public void process(MatchupSet matchupSet, final Reader primaryReader, final Reader secondaryReader) throws IOException, InvalidRangeException {
        for (final Screening screening : screeningList) {
            screening.apply(matchupSet, primaryReader, secondaryReader);
        }
    }

    @SuppressWarnings("unchecked")
    public void configure(UseCaseConfig useCaseConfig) {
        final Element screeningsElem = useCaseConfig.getDomElement("screenings");
        if (screeningsElem != null) {
            final List<Element> children = screeningsElem.getChildren();
            for (Element child : children) {
                final Screening screening = ScreeningFactory.get().getScreening(child);
                if (screening != null) {
                    screeningList.add(screening);
                }
            }
        }
    }
}
