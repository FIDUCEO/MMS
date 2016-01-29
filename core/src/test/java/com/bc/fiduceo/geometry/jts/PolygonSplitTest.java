package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.reader.AMSU_MHS_L1B_Reader;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
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
import java.util.ArrayList;
import java.util.List;


public class PolygonSplitTest {

    private AMSU_MHS_L1B_Reader reader;
    private NetcdfFile netcdfFile;
    private com.bc.fiduceo.geometry.GeometryFactory factoryS2;

    public static Polygon[] halfBoundaryPolygon(ArrayDouble.D2 arrayLatitude, ArrayDouble.D2 arrayLongitude, NodeType nodeType) {
        final int[] shape = arrayLatitude.getShape();
        int width = shape[1] - 1;
        int height = (shape[0] - 1);
        int intervalX = 50;
        int intervalY = 50;
        JtsGeometryFactory geometryFactory = new JtsGeometryFactory();
        com.vividsolutions.jts.geom.GeometryFactory factory = new com.vividsolutions.jts.geom.GeometryFactory();
        List<Point> coordinatesFirst = new ArrayList<>();
        List<Point> coordinatesSecond = new ArrayList<>();
        List<com.bc.fiduceo.geometry.Polygon> myPolygon = new ArrayList<>();


        int[] timeAxisStart = new int[2];
        int[] timeAxisEnd = new int[2];
        if (nodeType == NodeType.ASCENDING) {
            for (int x = 0; x < width; x += intervalX) {
                final double lon = arrayLongitude.get(0, x);
                final double lat = arrayLatitude.get(0, x);
                coordinatesFirst.add(geometryFactory.createPoint(lon, lat));
            }

            timeAxisStart[0] = coordinatesFirst.size();
            timeAxisEnd[0] = timeAxisStart[0];
            // First Half
            int firstHalf = height / 2;
            for (int y = 0; y < firstHalf; y += intervalY) {
                final double lon = arrayLongitude.get(y, width);
                final double lat = arrayLatitude.get(y, width);
                coordinatesFirst.add(geometryFactory.createPoint(lon, lat));
                ++timeAxisEnd[0];
            }

            for (int x = width; x > 0; x -= intervalX) {
                final double lon = arrayLongitude.get(firstHalf, x);
                final double lat = arrayLatitude.get(firstHalf, x);
                coordinatesFirst.add(geometryFactory.createPoint(lon, lat));
            }

            for (int y = firstHalf; y > 0; y -= intervalY) {
                final double lon = arrayLongitude.get(y, 0);
                final double lat = arrayLatitude.get(y, 0);
                coordinatesFirst.add(geometryFactory.createPoint(lon, lat));
            }
            coordinatesFirst.add(coordinatesFirst.get(0));


            //------ Second half
            for (int x = 0; x < width; x += intervalX) {
                final double lon = arrayLongitude.get(firstHalf, x);
                final double lat = arrayLatitude.get(firstHalf, x);
                coordinatesSecond.add(geometryFactory.createPoint(lon, lat));
            }

            for (int y = firstHalf; y < height; y += intervalY) {
                final double lon = arrayLongitude.get(y, width);
                final double lat = arrayLatitude.get(y, width);
                coordinatesSecond.add(geometryFactory.createPoint(lon, lat));
            }


            for (int x = width; x > 0; x -= intervalX) {
                final double lon = arrayLongitude.get(height, x);
                final double lat = arrayLatitude.get(height, x);
                coordinatesSecond.add(geometryFactory.createPoint(lon, lat));
            }


            for (int y = height; y > firstHalf; y -= intervalY) {
                final double lon = arrayLongitude.get(y, 0);
                final double lat = arrayLatitude.get(y, 0);
                coordinatesSecond.add(geometryFactory.createPoint(lon, lat));
            }
            coordinatesSecond.add(coordinatesSecond.get(0));
            //---
        }
        Polygon polygon_1 = factory.createPolygon(JtsGeometryFactory.extractCoordinates(coordinatesFirst));
        Polygon polygon_2 = factory.createPolygon(JtsGeometryFactory.extractCoordinates(coordinatesSecond));

        return myPolygon.toArray(new Polygon[myPolygon.size()]);
    }

    public static com.bc.fiduceo.geometry.Polygon allBoundingPolygon(ArrayDouble.D2 arrayLatitude, ArrayDouble.D2 arrayLongitude, NodeType nodeType) {
        final int[] shape = arrayLatitude.getShape();
        int width = shape[1] - 1;
        int height = shape[0] - 1;

        int intervalX = 50;
        int intervalY = 50;

        com.bc.fiduceo.geometry.GeometryFactory geometryFactory = new com.bc.fiduceo.geometry.GeometryFactory(com.bc.fiduceo.geometry.GeometryFactory.Type.S2);
        List<Point> coordinates = new ArrayList<>();

        int[] timeAxisStart = new int[2];
        int[] timeAxisEnd = new int[2];
        if (nodeType == NodeType.ASCENDING) {
            for (int x = 0; x < width; x += intervalX) {
                final double lon = arrayLongitude.get(0, x);
                final double lat = arrayLatitude.get(0, x);
                coordinates.add(geometryFactory.createPoint(lon, lat));
            }

            timeAxisStart[0] = coordinates.size();
            timeAxisEnd[0] = timeAxisStart[0];
            for (int y = 0; y < height; y += intervalY) {
                final double lon = arrayLongitude.get(y, width);
                final double lat = arrayLatitude.get(y, width);
                coordinates.add(geometryFactory.createPoint(lon, lat));
                ++timeAxisEnd[0];
            }

            for (int x = width; x > 0; x -= intervalX) {
                final double lon = arrayLongitude.get(height, x);
                final double lat = arrayLatitude.get(height, x);
                coordinates.add(geometryFactory.createPoint(lon, lat));
            }

            for (int y = height; y > 0; y -= intervalY) {
                final double lon = arrayLongitude.get(y, 0);
                final double lat = arrayLatitude.get(y, 0);
                coordinates.add(geometryFactory.createPoint(lon, lat));
            }
        } else {
            timeAxisStart[0] = 0;
            timeAxisEnd[0] = 0;
            for (int y = 0; y < height; y += intervalY) {
                final double lon = arrayLongitude.get(y, width);
                final double lat = arrayLatitude.get(y, width);
                coordinates.add(geometryFactory.createPoint(lon, lat));
                ++timeAxisEnd[0];
            }

            for (int x = width; x > 0; x -= intervalX) {
                final double lon = arrayLongitude.get(height, x);
                final double lat = arrayLatitude.get(height, x);
                coordinates.add(geometryFactory.createPoint(lon, lat));
            }

            for (int y = height; y > 0; y -= intervalY) {
                final double lon = arrayLongitude.get(y, 0);
                final double lat = arrayLatitude.get(y, 0);
                coordinates.add(geometryFactory.createPoint(lon, lat));
            }

            for (int x = 0; x < width; x += intervalX) {
                final double lon = arrayLongitude.get(0, x);
                final double lat = arrayLatitude.get(0, x);
                coordinates.add(geometryFactory.createPoint(lon, lat));
            }
        }
        return geometryFactory.createPolygon(coordinates);
    }

    public static Coordinate[] getCoordinates(List<Point> points) {
        final Coordinate[] coordinates = new Coordinate[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            coordinates[i] = new Coordinate(point.getLon(), point.getLat());
        }
        return coordinates;
    }

    private static S2Point createS2Point(double lon, double lat) {
        return S2LatLng.fromDegrees(lat, lon).toPoint();
    }

    private static String plotMultipoint(Coordinate[] coordinates) {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("MULTIPOINT(");

        for (int i = 0; i < coordinates.length; i++) {
            Coordinate coordinate = coordinates[i];
            stringBuffer.append(coordinate.x);
            stringBuffer.append(" ");
            stringBuffer.append(coordinate.y);
            if (i < coordinates.length - 1) {
                stringBuffer.append(",");
            }
        }

        stringBuffer.append(")");

        System.out.println(stringBuffer.toString());
        return stringBuffer.toString();
    }

    @Before
    public void setUp() throws IOException {
        File testDataDirectory = TestUtil.getTestDataDirectory();
        File file = new File(testDataDirectory, "NSS.AMBX.NK.D15348.S0057.E0250.B9144748.GC.h5");
        netcdfFile = NetcdfFile.open(file.getPath());

        reader = new AMSU_MHS_L1B_Reader();
        reader.open(file);


        factoryS2 = new com.bc.fiduceo.geometry.GeometryFactory(com.bc.fiduceo.geometry.GeometryFactory.Type.S2);

    }

    @After
    public void tearDown() throws IOException {
        reader.close();
        netcdfFile.close();
    }

    private ArrayDouble.D2 rescaleCoordinate(ArrayInt.D2 coodinate, double scale) {
        int[] coordinates = (int[]) coodinate.copyTo1DJavaArray();
        int[] shape = coodinate.getShape();
        ArrayDouble arrayDouble = new ArrayDouble(shape);

        for (int i = 0; i < coordinates.length; i++) {
            arrayDouble.setDouble(i, ((coordinates[i] * scale)));
        }
        return (ArrayDouble.D2) arrayDouble.copy();
    }

    @Test
    public void testPolygon() throws IOException {
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
        ArrayDouble.D2 arrayLong = rescaleCoordinate((ArrayInt.D2) longitude, longScale);
        ArrayDouble.D2 arrayLat = rescaleCoordinate((ArrayInt.D2) latitude, latScale);
        Polygon[] polygons = halfBoundaryPolygon(arrayLong, arrayLat, NodeType.ASCENDING);

        Geometry simplePolygon = factoryS2.parse("Polygon((-140 10, 0  10, 140  10, 140 -10, 0 -10, -140 -10, -140 10))");
    }

}
