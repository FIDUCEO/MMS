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

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class BoundingPolygonCreatorTest_IO_S2 extends BoundingPolygonCreatorTest {

    private NetcdfFile netcdfFile;
    private AMSU_MHS_L1B_Reader reader;

    @Before
    public void setUp() throws IOException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Interval interval = new Interval(8, 8);

        boundingPolygonCreator = new BoundingPolygonCreator(interval, geometryFactory);

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
    public void testCreateBoundingPolygon() throws IOException {
        final AcquisitionInfo acquisitionInfo = reader.read();
        assertNotNull(acquisitionInfo);

        final List<Polygon> polygons = acquisitionInfo.getMultiPolygons();
        assertTrue(polygons.size() > 0);
        final Point[] points = polygons.get(0).getCoordinates();
        assertTrue(points.length == 52);
        assertEquals(points[0].getLon(), -97.86539752771206, 1e-8);
        assertEquals(points[0].getLat(), 21.40989945914043, 1e-8);
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
    public void createValidMultiplePolygon() throws IOException {
        Array latitude = null;
        Array longitude = null;
        float latScale = 1;
        float longScale = 1;
        List<Polygon> polygonList = new ArrayList<>();
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

        final int[] shape = arrayLat.getShape();
        int width = shape[1] - 1;
        int height = (shape[0] - 1);

        GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(new Interval(50, 50), geometryFactory);
        for (int i = 1; i <= 4; i++) {
            polygonList = boundingPolygonCreator.createPolygonsBounding(arrayLat, arrayLong, width, height, i);
            if (TestUtil.isPointValidation(polygonList)) {
                break;
            }
        }

        assertEquals(polygonList.get(0).getCoordinates()[0].getLon(), -97.86539752771206, 1e-8);
        assertEquals(polygonList.get(0).getCoordinates()[0].getLat(), 21.40989945914043, 1e-8);
        assertTrue(TestUtil.isPointValidation(polygonList));
    }
}
