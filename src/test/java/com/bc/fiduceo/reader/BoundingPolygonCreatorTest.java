package com.bc.fiduceo.reader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class BoundingPolygonCreatorTest {

    private static final String POLYGONAIRS = "POLYGON ((-166.1730278143979 12.279422414280331, -161.50715746033566 13.057308306143742, -158.43923494622842 13.556043659558377, -154.74718921639902 14.083472777091306, -151.5331749119938 14.457717633993736, -152.24160661916514 18.30840676225526, -152.93041231406852 22.16010663394288, -153.59932438347053 26.01299578883174, -154.25857874349714 29.8657940266665, -154.90287384906094 33.71784589442994, -155.22100159384635 35.643407945336584, -156.4154886637448 35.564455266536505, -162.03767213761958 34.98975986081048, -165.67042484593026 34.43861715006958, -169.9464526366916 33.66017283151052, -173.5921218067924 32.90472542332997, -172.1983000207946 29.162140412110194, -170.92247683141954 25.39940941633738, -169.7457472101792 21.62002239734624, -168.65273530924344 17.826844811300536, -167.63148361076378 14.021764653386432, -166.1730278143979 12.279422414280331))";
    private BoundingPolygonCreator boundingPolygonCreator;
    private File productFile;

    @Before
    public void setUp() throws IOException {
        boundingPolygonCreator = new BoundingPolygonCreator(8, 8);
        assertNotNull(boundingPolygonCreator);

        final File testDataDirectory = TestUtil.getTestDataDirectory();
        productFile = new File(testDataDirectory,"AIRS.2015.08.03.001.L1B.AMSU_Rad.v5.0.14.0.R15214205337.hdf");
    }

    // @todo 1 tb/tb rewrite test to use mock-arrays 2015-09-07

//    @Test
//    public void testPolygonFromCoordinate() throws IOException, ParseException {
//        final NetcdfFile netcdfFile = NetcdfFile.open(productFile.getPath());
//        final Geometry geometry = boundingPolygonCreator.createPolygonForAIRS(netcdfFile);
//        assertNotNull(geometry);
//        final WKTReader wkbReader = new WKTReader(new GeometryFactory());
//        assertTrue(geometry.equals(wkbReader.read(POLYGONAIRS)));
//
//        netcdfFile.close();
//    }

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

    @Test
    public void testNormalizePolygon_tooSmallArray() {
        Coordinate[] coordinates = new Coordinate[0];
        BoundingPolygonCreator.normalizePolygon(coordinates);
        assertEquals(0, coordinates.length);

        coordinates = new Coordinate[1];
        BoundingPolygonCreator.normalizePolygon(coordinates);
        assertEquals(1, coordinates.length);
    }

    @Test
    public void testNormalizePolygon_noNormaization() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(10, 10);
        coordinates[1] = new Coordinate(10, 20);
        coordinates[2] = new Coordinate(20, 20);
        coordinates[3] = new Coordinate(20, 10);
        coordinates[4] = new Coordinate(10, 10);

        BoundingPolygonCreator.normalizePolygon(coordinates);
        assertEquals(10, coordinates[0].x, 1e-8);
        assertEquals(10, coordinates[1].x, 1e-8);
        assertEquals(20, coordinates[2].x, 1e-8);
        assertEquals(20, coordinates[3].x, 1e-8);
        assertEquals(10, coordinates[4].x, 1e-8);
    }

    @Test
    public void testNormalizePolygon_normalizeEast() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(170, 10);
        coordinates[1] = new Coordinate(170, 20);
        coordinates[2] = new Coordinate(-175, 20);
        coordinates[3] = new Coordinate(-175, 10);
        coordinates[4] = new Coordinate(170, 10);

        BoundingPolygonCreator.normalizePolygon(coordinates);
        assertEquals(170, coordinates[0].x, 1e-8);
        assertEquals(170, coordinates[1].x, 1e-8);
        assertEquals(185, coordinates[2].x, 1e-8);
        assertEquals(185, coordinates[3].x, 1e-8);
        assertEquals(170, coordinates[4].x, 1e-8);
    }

    @Test
    public void testNormalizePolygon_normalizeWest() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(-170, 10);
        coordinates[1] = new Coordinate(-170, 20);
        coordinates[2] = new Coordinate(175, 20);
        coordinates[3] = new Coordinate(175, 10);
        coordinates[4] = new Coordinate(-170, 10);

        BoundingPolygonCreator.normalizePolygon(coordinates);
        assertEquals(190, coordinates[0].x, 1e-8);
        assertEquals(190, coordinates[1].x, 1e-8);
        assertEquals(175, coordinates[2].x, 1e-8);
        assertEquals(175, coordinates[3].x, 1e-8);
        assertEquals(190, coordinates[4].x, 1e-8);
    }
}
