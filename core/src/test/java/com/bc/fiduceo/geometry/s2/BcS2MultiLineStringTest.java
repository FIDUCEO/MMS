package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Point;
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2Polyline;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author muhammad.bc
 */
public class BcS2MultiLineStringTest {

    @Test
    public void testGetCoordinateS2MultiLineString() {
        BcS2MultiLineString bcS2MultiLineString = createS2Polyline("MULTILINESTRING((10 18, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
        Point[] coordinates = bcS2MultiLineString.getCoordinates();
        assertEquals(coordinates.length, 7);
        assertEquals(coordinates[0].getLon(), 9.999999999999998, 1e-8);
        assertEquals(coordinates[0].getLat(), 18.0, 1e-8);

        assertEquals(coordinates[6].getLon(), 29.999999999999993, 1e-8);
        assertEquals(coordinates[6].getLat(), 10.0, 1e-8);
    }

    @Test
    public void testCreateFrom_listOfLineStrings() {
        final BcS2GeometryFactory bcS2GeometryFactory = new BcS2GeometryFactory();
        final BcS2LineString lineString_1 = (BcS2LineString) bcS2GeometryFactory.parse("LINESTRING(10 10, 10 11, 11 12)");
        final BcS2LineString lineString_2 = (BcS2LineString) bcS2GeometryFactory.parse("LINESTRING(2 3, 3 5, 3.5 7)");
        final List<BcS2LineString> lineStringList = new ArrayList<>();
        lineStringList.add(lineString_1);
        lineStringList.add(lineString_2);

        final BcS2MultiLineString multiLineString = BcS2MultiLineString.createFrom(lineStringList);
        assertNotNull(multiLineString);
        final Point[] coordinates = multiLineString.getCoordinates();
        assertEquals(6, coordinates.length);

        assertEquals(10.0, coordinates[0].getLon(), 1e-8);
        assertEquals(10.0, coordinates[0].getLat(), 1e-8);

        assertEquals(3.0, coordinates[4].getLon(), 1e-8);
        assertEquals(5.0, coordinates[4].getLat(), 1e-8);

    }

    private BcS2MultiLineString createS2Polyline(String wkt) {
        S2WKTReader reader = new S2WKTReader();
        List<S2Polyline> read = (List<S2Polyline>) reader.read(wkt);
        return new BcS2MultiLineString(read);
    }
}
