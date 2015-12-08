/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public abstract class BoundingPolygonCreatorTest {

    protected BoundingPolygonCreator boundingPolygonCreator;
    protected GeometryFactory geometryFactory;

    @Test
    public void testClosePolygon_emptyList() {
        final ArrayList<Point> coordinates = new ArrayList<>();

        BoundingPolygonCreator.closePolygon(coordinates);

        assertEquals(0, coordinates.size());
    }

    @Test
    public void testClosePolygon() {
        final ArrayList<Point> coordinates = new ArrayList<>();
        coordinates.add(geometryFactory.createPoint(0, 0));
        coordinates.add(geometryFactory.createPoint(1, 1));
        coordinates.add(geometryFactory.createPoint(1, 3));

        BoundingPolygonCreator.closePolygon(coordinates);

        assertEquals(4, coordinates.size());
        final Point closingCoordinate = coordinates.get(3);
        assertEquals(0, closingCoordinate.getLon(), 1e-8);
        assertEquals(0, closingCoordinate.getLat(), 1e-8);
    }

    @Test
    public void testThrowsOnInvalidInterval() {
        Interval interval = new Interval(0, 8);
        try {
            new BoundingPolygonCreator(interval, geometryFactory);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

        interval = new Interval(12, 0);
        try {
            new BoundingPolygonCreator(interval, geometryFactory);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

        interval = new Interval(-3, 0);
        try {
            new BoundingPolygonCreator(interval, geometryFactory);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreatePixelCodedBoundingPolygon_ascendingNode() {
        final ArrayDouble.D2 longitudes = (ArrayDouble.D2) Array.factory(AIRS_LONGITUDES);
        final ArrayDouble.D2 latitudes = (ArrayDouble.D2) Array.factory(AIRS_LATITUDES);

        final AcquisitionInfo acquisitionInfo = boundingPolygonCreator.createPixelCodedBoundingPolygon(latitudes, longitudes, NodeType.ASCENDING);
        assertNotNull(acquisitionInfo);

        final List<Point> coordinates = acquisitionInfo.getCoordinates();
        assertEquals(5, coordinates.size());
        assertEquals(138.19514475348302, coordinates.get(0).getLon(), 1e-8);
        assertEquals(71.15288152754994, coordinates.get(0).getLat(), 1e-8);
        assertEquals(136.90199908664985, coordinates.get(3).getLon(), 1e-8);
        assertEquals(71.41032171663477, coordinates.get(3).getLat(), 1e-8);

        assertEquals(1, acquisitionInfo.getTimeAxisStartIndices()[0]);
        assertEquals(2, acquisitionInfo.getTimeAxisEndIndices()[0]);
    }

    protected static final double[][] AIRS_LONGITUDES = new double[][]{{138.19514475348302, 138.77287682180165, 139.3232587268979, 139.86561480588978},
            {137.7680766938059, 138.34196788102574, 138.888842745419, 139.43625059118625},
            {137.32780413935305, 137.90682957068157, 138.4586123358709, 138.9939729311918},
            {136.90199908664985, 137.46778019306842, 138.01571817610454, 138.53923435004424}};

    protected static final double[][] AIRS_LATITUDES = new double[][]{{71.15288152754994, 71.4359164390965, 71.69661607793569, 71.9452820772289},
            {71.23974580787146, 71.52412094894252, 71.78608894421787, 72.03976926305718},
            {71.32088787959934, 71.61122828082071, 71.87850964766172, 72.12942839534938},
            {71.41032171663477, 71.69739504897453, 71.96597011172345, 72.21432551071354}};
}
