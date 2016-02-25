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
import com.bc.fiduceo.geometry.Point;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.ArrayDouble;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(IOTestRunner.class)
public class AMSU_MHS_L1B_Reader_IO_Test {

    private AMSU_MHS_L1B_Reader reader;

    @Before
    public void setUp() throws IOException {
        reader = new AMSU_MHS_L1B_Reader();
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File file = new File(testDataDirectory, "L8912163.NSS.AMBX.NK.D08001.S0000.E0155.B5008586.GC.gz.l1c.h5");
        reader.open(file);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void testAcqusitionInfo() throws IOException, ParseException {
        final AcquisitionInfo acquisitionInfo = reader.read();

        final List<Point> coordinates = acquisitionInfo.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(118, coordinates.size());
        assertCoordinate(-108.08909726943968, -72.57329816664424, coordinates.get(0));
        assertCoordinate(-96.7779975551821, -7.57889980854088, coordinates.get(10));

        assertNotNull(acquisitionInfo.getSensingStart());
        assertNotNull(acquisitionInfo.getSensingStop());
        TestUtil.assertCorrectUTCDate(2008, 1, 1, 0, 1, 8, 476, acquisitionInfo.getSensingStart());
        TestUtil.assertCorrectUTCDate(2008, 1, 1, 1, 55, 8, 475, acquisitionInfo.getSensingStop());
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

    @Test
    public void testGetSensorTypes_CheckSensorType() {
        HashMap<String, String> sensorTypes = reader.getSensorTypes();
        assertEquals("NK", sensorTypes.get("amsub-n15"));
        assertEquals("NN", sensorTypes.get("amsub-n18"));

        assertEquals(null, sensorTypes.get(""));
        assertEquals(null, sensorTypes.get("not exist"));


        assertTrue(reader.checkSensorTypeName("amsub-n18"));
        assertEquals("'?[A-Z].+[AMBX|MHSX].+[NK|M1].D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[GC|WI].h5", reader.getRegEx());
    }

    private ArrayDouble rescaleCoordinate(ArrayDouble coordinate) {
        double[] h = (double[]) coordinate.copyTo1DJavaArray();
        for (int i = 0; i < h.length; i++) {
            coordinate.setDouble(i, (h[i] / 10));
        }
        return coordinate;
    }


}
