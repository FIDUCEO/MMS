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

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.tool.ToolContext;
import org.jdom.Element;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// @todo se write tests

public class ScreeningEngine {

    private final List<Screening> screeningList;
    private final ToolContext context;

    public ScreeningEngine(ToolContext context) {
        this.context = context;
        screeningList = new ArrayList<>();
        configure();
    }

    public void process(MatchupSet matchupSet, final Reader primaryReader, final Map<String,Reader> secondaryReader) throws IOException, InvalidRangeException {
        final Screening.ScreeningContext sc = createScreeningContext();
        for (final Screening screening : screeningList) {
            screening.apply(matchupSet, primaryReader, secondaryReader, sc);
        }
    }

    @SuppressWarnings("unchecked")
    private void configure() {
        UseCaseConfig useCaseConfig = context.getUseCaseConfig();
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

    private Screening.ScreeningContext createScreeningContext() {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        return new Screening.ScreeningContext() {
            @Override
            public Dimension getPrimaryDimension() {
                final String name = useCaseConfig.getPrimarySensor().getName();
                return useCaseConfig.getDimensionFor(name);
            }

            @Override
            public Dimension getSecondaryDimension(String sensorName) {
                return useCaseConfig.getDimensionFor(sensorName);
            }
        };
    }
}
