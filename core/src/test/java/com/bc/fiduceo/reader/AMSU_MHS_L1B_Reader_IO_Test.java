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
import com.bc.fiduceo.core.SatelliteGeometry;
import com.bc.fiduceo.geometry.Point;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.ArrayDouble;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(IOTestRunner.class)
public class AMSU_MHS_L1B_Reader_IO_Test {

    private AMSU_MHS_L1B_Reader reader;

    @Before
    public void setUp() throws IOException {
        reader = new AMSU_MHS_L1B_Reader();
        File testDataDirectory = TestUtil.getTestDataDirectory();

        File file = new File(testDataDirectory, "NSS.AMBX.NK.D15348.S0057.E0250.B9144748.GC.h5");
        reader.open(file);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void testAcqusitionInfo() throws IOException, ParseException {
        AcquisitionInfo read = reader.read();
        List<Point> coordinates = read.getCoordinates();

        assertNotNull(coordinates);
        assertEquals(101, coordinates.size());
        assertCoordinate(-9.786540247228802E9, 2.140990054085958E9, coordinates.get(0));
        assertCoordinate(-7.76048019604622E9, 8.695230219659992E9, coordinates.get(10));
        assertCoordinate(-9.786540247228802E9, 2.140990054085958E9, coordinates.get(coordinates.size() - 1));

        assertNotNull(read.getSensingStart());
        assertNotNull(read.getSensingStop());
        TestUtil.assertCorrectUTCDate(2015, 12, 13, 23, 15, 30, 128, read.getSensingStart());
        TestUtil.assertCorrectUTCDate(2015, 12, 14, 1, 1, 32, 787, read.getSensingStop());

        SatelliteGeometry satelliteGeometry = GeometryUtils.prepareForStorage(read);
//        Polygon[] polygons = GeometryUtils.mapToGlobe(satelliteGeometry.getGeometry());
    }

    private void assertCoordinate(double expectedX, double expectedY, Point coordinate) {
        assertEquals(expectedX, coordinate.getLon(), 1e-8);
        assertEquals(expectedY, coordinate.getLat(), 1e-8);
    }

    @Test
    public void testScale() {
        int[] next = {4, 6};
        ArrayDouble arrayDouble = new ArrayDouble(next);
        double[] h = (double[]) arrayDouble.copyTo1DJavaArray();
        for (int i = 0; i < h.length; i++) {
            arrayDouble.setDouble(i, i * 2);
        }
        ArrayDouble.D2 aDouble = (ArrayDouble.D2) rescaleCoordinate(arrayDouble).copy();
        int[] shape = aDouble.getShape();

        assertEquals(shape[0], 4);
        assertEquals(shape[1], 6);
        assertEquals(aDouble.get(0, 0), 0.0, 1e-8);
        assertEquals(aDouble.get(3, 5), 4.6, 1e-8);

    }


    private ArrayDouble rescaleCoordinate(ArrayDouble coordinate) {
        double[] h = (double[]) coordinate.copyTo1DJavaArray();
        for (int i = 0; i < h.length; i++) {
            coordinate.setDouble(i, (h[i] / 10));
        }
        return coordinate;
    }


}
