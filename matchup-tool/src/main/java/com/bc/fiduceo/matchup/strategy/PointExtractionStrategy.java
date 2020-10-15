/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.matchup.strategy;

import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.tool.ToolContext;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class PointExtractionStrategy extends AbstractMatchupStrategy {

    PointExtractionStrategy(Logger logger) {
        super(logger);
    }

    @Override
    public MatchupCollection createMatchupCollection(ToolContext context) throws SQLException, IOException {
        final List<SatelliteObservation> primaryObservations = getPrimaryObservations(context);
        if (primaryObservations.size() == 0) {
            logger.warning("No satellite data in time interval:" + context.getStartDate() + " - " + context.getEndDate());
            return new MatchupCollection();
        }

        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final GeometryFactory geometryFactory = context.getGeometryFactory();
        final ReaderFactory readerFactory = ReaderFactory.get();

        final double lon = useCaseConfig.getLon();
        final double lat = useCaseConfig.getLat();
        final Point point = geometryFactory.createPoint(lon, lat);
        final Sample referenceSample = new Sample(-1, -1, lon, lat, -1);

        final MatchupCollection matchupCollection = new MatchupCollection();
        
        for (final SatelliteObservation primaryObservation : primaryObservations) {
            final Geometry geoBounds = primaryObservation.getGeoBounds();
            if (geoBounds.getIntersection(point).isValid()) {
                final MatchupSet matchupSet = new MatchupSet();
                matchupSet.setPrimaryObservationPath(primaryObservation.getDataFilePath());
                matchupSet.setPrimaryProcessingVersion(primaryObservation.getVersion());

                try (final Reader primaryReader = readerFactory.getReader(primaryObservation.getSensor().getName())) {
                    primaryReader.open(primaryObservation.getDataFilePath().toFile());

                    final PixelLocator pixelLocator = primaryReader.getPixelLocator();
                    final Point2D[] pixelLocations = pixelLocator.getPixelLocation(lon, lat);
                    if (pixelLocations.length == 0) {
                        continue;
                    }

                    final TimeLocator timeLocator = primaryReader.getTimeLocator();
                    Point2D geopos = new Point2D.Double();

                    final List<SampleSet> sampleSets = new ArrayList<>();
                    for (final Point2D pixelLocation : pixelLocations) {
                        final int x = (int) pixelLocation.getX();
                        final int y = (int) pixelLocation.getY();
                        geopos = pixelLocator.getGeoLocation(x + 0.5, y + 0.5, geopos);
                        if (geopos == null) {
                            continue;
                        }

                        final long time = timeLocator.getTimeFor(x, y);
                        final SampleSet sampleSet = new SampleSet();
                        final Sample sample = new Sample(x, y, geopos.getX(), geopos.getY(), time);
                        sampleSet.setPrimary(sample);
                        sampleSet.setSecondary("location", referenceSample);
                        sampleSets.add(sampleSet);
                    }

                    matchupSet.setSampleSets(sampleSets);

                }
                matchupCollection.add(matchupSet);
            }
        }

        return matchupCollection;
    }
}
