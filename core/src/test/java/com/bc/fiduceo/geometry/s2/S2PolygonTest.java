package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AMSU_MHS_L1B_Reader;
import com.bc.geometry.s2.S2WKTReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @tom.bc
 */
public class S2PolygonTest {

    private S2WKTReader s2WKTReader;
    private NetcdfFile netcdfFile;
    private AMSU_MHS_L1B_Reader reader;

    @Before
    public void setUp() throws IOException {
        s2WKTReader = new S2WKTReader();

        File testDataDirectory = TestUtil.getTestDataDirectory();
        File file = new File(testDataDirectory, "NSS.AMBX.NK.D15348.S0057.E0250.B9144748.GC.h5");
        netcdfFile = NetcdfFile.open(file.getPath());
        reader = new AMSU_MHS_L1B_Reader();
        reader.open(file);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
        netcdfFile.close();
    }

    @Test
    public void testIsEmpty() {
        com.google.common.geometry.S2Polygon googlePolygon = new com.google.common.geometry.S2Polygon();
        final S2Polygon s2Polygon = new S2Polygon(googlePolygon);

        assertTrue(s2Polygon.isEmpty());
    }

    @Test
    public void testIntersect_noIntersection() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-5 0, -5 1, -4 1, -4 0, -5 0))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((5 0, 5 1, 4 1, 4 0, 5 0))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testIntersect_intersectionWest() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-5 0, -5 1, -4 1, -4 0, -5 0))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((-5.5 0, -5.5 1, -4.5 1, -4.5 0, -5.5 0))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        Point[] coordinates = intersection.getCoordinates();
        assertEquals(5, coordinates.length);
        assertEquals(-4.500000000000001, coordinates[0].getLon(), 1e-8);
        assertEquals(1.0, coordinates[0].getLat(), 1e-8);

        assertEquals(-5.0, coordinates[2].getLon(), 1e-8);
        assertEquals(0.9999999999999998, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testIntersact_intersactionNorth() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((-8 -10,-8 12,9 12,9 -10,-8 -10))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
        Point[] coordinates = intersection.getCoordinates();
        assertEquals(9.000000000000002, coordinates[0].getLon(), 1e-8);
        assertEquals(10.028657322246222, coordinates[0].getLat(), 1e-8);
        assertEquals(-7.999999999999998, coordinates[2].getLon(), 1e-8);
        assertEquals(-10.0, coordinates[2].getLat(), 1e-8);

    }

    @Test
    public void testIntersact_intersactionSouth() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((-8 -12,-8 10,9 10,9 -12,-8 -12))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
    }

    @Test
    public void testIntersact_intersactionEast() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((-10 -8,12 -8,12 9,-10 9,-10 -8))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
    }

    @Test
    public void testSamePolygon() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((10 10,-10 10,-10 -10,10 -10,10 10))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        // This is do to the egdes are the same
        assertEquals(intersection.toString(), "Polygon: (0) loops:\n");
    }

    @Test
    public void testGetCoordinates() {
        final S2Polygon s2Polygon = createS2Polygon("POLYGON((5 -1, 5 0, 4 0, 4 -1, 5 -1))");

        final Point[] coordinates = s2Polygon.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(4, coordinates.length);
        assertEquals(5.0, coordinates[0].getLon(), 1e-8);
        assertEquals(-1.0, coordinates[0].getLat(), 1e-8);

        assertEquals(4.0, coordinates[2].getLon(), 1e-8);
        assertEquals(0.0, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testPolygonIntersectFromFile() throws IOException {
        Polygon filePolygon = getPolygonFromFile();
//        com.bc.fiduceo.geometry.Polygon filePolygon = getPolygonFromFile();
        String s1 = TestUtil.plotPolygon(filePolygon.getCoordinates());
        assertNotNull(filePolygon);

        Geometry simplePolygon = createS2Polygon("POLYGON ((-100 50, 0 50, 90 50, 90 -50, 0 -50, -100 -50, -100 50))");
        assertNotNull(simplePolygon);

        final Geometry intersection = simplePolygon.intersection(filePolygon);
        assertNotNull(intersection);
        Point[] coordinates = intersection.getCoordinates();
        assertEquals(86.1444141912498, coordinates[0].getLon(), 1e-8);
        assertEquals(51.76532548048519, coordinates[0].getLat(), 1e-8);
    }

    private Polygon getPolygonFromFile() throws IOException {
        Array latitude = null;
        Array longitude = null;
        float latScale = 1;
        float longScale = 1;
        List<Variable> geolocation = netcdfFile.findGroup("Geolocation").getVariables();
        for (Variable geo : geolocation) {
            if (geo.getShortName().equals("Latitude")) {
                latitude = geo.read();
                latScale = (float) geo.findAttribute("Scale").getNumericValue();
            } else if (geo.getShortName().equals("Longitude")) {
                longitude = geo.read();
                longScale = (float) geo.findAttribute("Scale").getNumericValue();
            }
        }
        ArrayDouble.D2 arrayLong = TestUtil.rescaleCoordinate((ArrayInt.D2) longitude, longScale);
        ArrayDouble.D2 arrayLat = TestUtil.rescaleCoordinate((ArrayInt.D2) latitude, latScale);

        return TestUtil.allBoundingPolygon(arrayLong, arrayLat, NodeType.ASCENDING, GeometryFactory.Type.S2);
    }

    private S2Polygon createS2Polygon(String wellKnownText) {
        com.google.common.geometry.S2Polygon polygon_1 = (com.google.common.geometry.S2Polygon) s2WKTReader.read(wellKnownText);
        return new S2Polygon(polygon_1);
    }
}
