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

package com.bc.fiduceo;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.s2.S2GeometryFactory;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.vividsolutions.jts.geom.Coordinate;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.ArrayDouble;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestUtil {

    private static final String SYSTEM_TEMP_PROPETY = "java.io.tmpdir";
    private static final String TEST_DIRECTORY = "fiduceo_test";

    public static File getTestDataDirectory() throws IOException {
        final InputStream resourceStream = TestUtil.class.getResourceAsStream("dataDirectory.properties");
        final Properties properties = new Properties();
        properties.load(resourceStream);
        final String dataDirectoryProperty = properties.getProperty("dataDirectory");
        if (dataDirectoryProperty == null) {
            fail("Property 'dataDirectory' is not set.");
        }
        final File dataDirectory = new File(dataDirectoryProperty);
        if (!dataDirectory.isDirectory()) {
            fail("Property 'dataDirectory' supplied does not exist: '" + dataDirectoryProperty + "'");
        }
        return dataDirectory;
    }

    public static void assertCorrectUTCDate(int year, int month, int day, int hour, int minute, int second, Date utcDate) {
        final Calendar calendar = ProductData.UTC.createCalendar();
        calendar.setTime(utcDate);

        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month - 1, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, calendar.get(Calendar.MINUTE));
        assertEquals(second, calendar.get(Calendar.SECOND));
    }

    public static void assertCorrectUTCDate(int year, int month, int day, int hour, int minute, int second, int millisecond, Date utcDate) {
        final Calendar calendar = ProductData.UTC.createCalendar();
        calendar.setTime(utcDate);

        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month - 1, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, calendar.get(Calendar.MINUTE));
        assertEquals(second, calendar.get(Calendar.SECOND));
        assertEquals(millisecond, calendar.get(Calendar.MILLISECOND));
    }

    public static File createTestDirectory() {
        final File testDir = getTestDir();
        if (!testDir.mkdirs()) {
            fail("unable to create test directory: " + testDir.getAbsolutePath());
        }
        return testDir;
    }

    public static void deleteTestDirectory() {
        final File testDir = getTestDir();
        if (testDir.isDirectory()) {
            final boolean deleted = FileUtils.deleteTree(testDir);
            if (!deleted) {
                fail("unable to delete test directory: " + testDir.getAbsolutePath());
            }
        }
    }

    public static File createFileInTestDir(String fileName) throws IOException {
        final File testDirectory = getTestDir();

        final File databaseConfigFile = new File(testDirectory, fileName);
        if (!databaseConfigFile.createNewFile()) {
            fail("Unable to create test file: " + databaseConfigFile.getAbsolutePath());
        }
        return databaseConfigFile;
    }

    private static File getTestDir() {
        final String tempDirPath = System.getProperty(SYSTEM_TEMP_PROPETY);
        return new File(tempDirPath, TEST_DIRECTORY);
    }

    public static List<com.bc.fiduceo.geometry.Polygon> halfBoundaryPoints(ArrayDouble.D2 arrayLatitude, ArrayDouble.D2 arrayLongitude, NodeType nodeType, GeometryFactory.Type type) {
        final int[] shape = arrayLatitude.getShape();
        int width = shape[1] - 1;
        int height = (shape[0] - 1);
        int intervalX = 10;
        int intervalY = 10;
        com.bc.fiduceo.geometry.GeometryFactory geometryFactory = new com.bc.fiduceo.geometry.GeometryFactory(type);
        List<Point> coordinatesFirst = new ArrayList<>();
        List<Point> coordinatesSecond = new ArrayList<>();
        List<com.bc.fiduceo.geometry.Polygon> polygonList = new ArrayList<>();


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
            if (GeometryFactory.Type.JTS == type) {
                coordinatesFirst.add(coordinatesFirst.get(0));
            }
            if (type == GeometryFactory.Type.S2) {
                coordinatesFirst.add(coordinatesFirst.get(0));
            }

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

            if (GeometryFactory.Type.JTS == type) {
                coordinatesSecond.add(coordinatesSecond.get(0));
            }
        }

        polygonList.add(geometryFactory.createPolygon(coordinatesFirst));
        polygonList.add(geometryFactory.createPolygon(coordinatesSecond));
        return polygonList;
    }

    public static Coordinate[] getCoordinates(List<Point> points) {
        final Coordinate[] coordinates = new Coordinate[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            coordinates[i] = new Coordinate(point.getLon(), point.getLat());
        }
        return coordinates;
    }

    public static boolean isPointValidation(List<Polygon> polygonList) {
        boolean valid = true;
        for (Polygon polygon : polygonList) {

            List<Point> points = Arrays.asList(polygon.getCoordinates());
            List<S2Point> s2Points = S2GeometryFactory.extractS2Points(points);
            S2Loop s2Loop = new S2Loop(s2Points);
            valid = s2Loop.isValid();
            if (!valid) {
                return false;
            }
        }
        return valid;
    }

}
