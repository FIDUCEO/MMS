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

package com.bc.fiduceo.matchup.strategy;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.tool.ToolContext;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

class SampleCollector {

    private final PixelLocator pixelLocator;
    private final Range xRange;
    private final Range yRange;
    private final ToolContext context;

    SampleCollector(ToolContext context, PixelLocator pixelLocator) {
        this.context = context;
        this.pixelLocator = pixelLocator;
        xRange = new Range();
        yRange = new Range();
    }

    /**
     * Adds the associated secondary sample location and time to the primary location.
     *
     * @param sampleSets  the input data - will be empty after the operation!
     * @param timeLocator the time locator for the sample locations
     *
     * @param secSensorName
     * @return the result list with the sampleSets that contain two observations
     */
    List<SampleSet> addSecondarySamples(List<SampleSet> sampleSets, TimeLocator timeLocator, final String secSensorName) {
        Point2D geopos = new Point2D.Double();
        final List<SampleSet> toKeep = new ArrayList<>();
        for (SampleSet sampleSet : sampleSets) {
            final Sample primary = sampleSet.getPrimary();
            final Point2D[] pixelLocations = pixelLocator.getPixelLocation(primary.lon, primary.lat);
            for (int i = 0; i < pixelLocations.length; i++) {
                Point2D pixelLocation = pixelLocations[i];
                final int x = (int) pixelLocation.getX();
                final int y = (int) pixelLocation.getY();
                geopos = pixelLocator.getGeoLocation(x + 0.5, y + 0.5, geopos);
                final long time = timeLocator.getTimeFor(x, y);
                final Sample sample = new Sample(x, y, geopos.getX(), geopos.getY(), time);
                if (i>0) {
                    sampleSet = new SampleSet();
                    sampleSet.setPrimary(primary);
                }
                sampleSet.setSecondary(secSensorName, sample);
                toKeep.add(sampleSet);
            }
        }

        return toKeep;
    }

    void addPrimarySamples(Polygon polygon, MatchupSet matchupSet, TimeLocator timeLocator) {
        final Point[] coordinates = polygon.getCoordinates();
        for (Point coordinate : coordinates) {
            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(coordinate.getLon(), coordinate.getLat());
            for (Point2D point2D : pixelLocation) {
                xRange.aggregate(point2D.getX());
                yRange.aggregate(point2D.getY());
            }
        }

        final Point2D.Double geoPos = new Point2D.Double();
        final GeometryFactory factory = context.getGeometryFactory();

        final int startY = (int) yRange.getMin();
        final int endY = (int) yRange.getMax();
        final int startX = (int) xRange.getMin();
        final int endX = (int) xRange.getMax();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                final Point2D geoLocation = pixelLocator.getGeoLocation(x + .5, y + .5, geoPos);
                final double lon = geoLocation.getX();
                final double lat = geoLocation.getY();
                final Point geoPoint = factory.createPoint(lon, lat);
                if (polygon.contains(geoPoint)) {
                    final long time = timeLocator.getTimeFor(x, y);
                    final Sample sample = new Sample(x, y, lon, lat, time);
                    matchupSet.addPrimary(sample);
                }
            }
        }
    }
}
