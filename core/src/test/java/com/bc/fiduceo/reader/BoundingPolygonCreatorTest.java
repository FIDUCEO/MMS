package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.NodeType;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class BoundingPolygonCreatorTest {

    private BoundingPolygonCreator boundingPolygonCreator;

    @Before
    public void setUp() throws IOException {
        boundingPolygonCreator = new BoundingPolygonCreator(8, 8);
        assertNotNull(boundingPolygonCreator);
    }

    @Test
    public void testCreatePixelCodedBoundingPolygon_ascendingNode() {
        final ArrayDouble.D2 longitudes = (ArrayDouble.D2) Array.factory(AIRS_LONGITUDES);
        final ArrayDouble.D2 latitudes = (ArrayDouble.D2) Array.factory(AIRS_LATITUDES);

        final AcquisitionInfo acquisitionInfo = boundingPolygonCreator.createPixelCodedBoundingPolygon(latitudes, longitudes, NodeType.ASCENDING);
        assertNotNull(acquisitionInfo);

        final List<Coordinate> coordinates = acquisitionInfo.getCoordinates();
        assertEquals(5, coordinates.size());
        assertEquals(1, acquisitionInfo.getTimeAxisStartIndices()[0]);
        assertEquals(2, acquisitionInfo.getTimeAxisEndIndices()[0]);

    }

    @Test
    public void testClosePolygon_emptyList() {
        final ArrayList<Coordinate> coordinates = new ArrayList<>();

        BoundingPolygonCreator.closePolygon(coordinates);

        assertEquals(0, coordinates.size());
    }

    @Test
    public void testClosePolygon() {
        final ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(0, 0));
        coordinates.add(new Coordinate(1, 1));
        coordinates.add(new Coordinate(1, 3));

        BoundingPolygonCreator.closePolygon(coordinates);

        assertEquals(4, coordinates.size());
        final Coordinate closingCoordinate = coordinates.get(3);
        assertEquals(0, closingCoordinate.x, 1e-8);
        assertEquals(0, closingCoordinate.y, 1e-8);
    }

    private static final double[][] AIRS_LONGITUDES = new double[][] {{138.19514475348302, 138.77287682180165, 139.3232587268979, 139.86561480588978},
            {137.7680766938059, 138.34196788102574, 138.888842745419, 139.43625059118625},
            {137.32780413935305, 137.90682957068157, 138.4586123358709, 138.9939729311918},
            {136.90199908664985, 137.46778019306842, 138.01571817610454, 138.53923435004424}};

    private static final double[][] AIRS_LATITUDES = new double[][] {{71.15288152754994, 71.4359164390965, 71.69661607793569, 71.9452820772289},
            {71.23974580787146, 71.52412094894252, 71.78608894421787, 72.03976926305718},
            {71.32088787959934, 71.61122828082071, 71.87850964766172, 72.12942839534938},
            {71.41032171663477, 71.69739504897453, 71.96597011172345, 72.21432551071354}};
}
