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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author muhammad.bc
 */
@RunWith(IOTestRunner.class)
public class AMSU_MHS_L1B_Reader_IO_Test {


    private AMSU_MHS_L1B_Reader reader;
    private File testDataDirectory;
    private File file;

    @Before
    public void setUp() throws IOException {
        reader = new AMSU_MHS_L1B_Reader();
        testDataDirectory = TestUtil.getTestDataDirectory();
    }

    @Test
    public void testOpenHDF5() throws IOException {
        file = new File(testDataDirectory, "NSS.AMBX.NK.D15348.S0057.E0250.B9144748.GC.h5");
        reader.open(file);
        reader.close();
    }

    @Test
    public void testAcqusitionInfo() throws IOException, ParseException {
        file = new File(testDataDirectory, "NSS.AMBX.NK.D15348.S0057.E0250.B9144748.GC.h5");
        reader.open(file);
        AcquisitionInfo read = reader.read();
        List<Point> coordinates = read.getCoordinates();
        Assert.assertNotNull(read.getSensingStop());
        Assert.assertNotNull(read.getSensingStart());
        assertNotNull(coordinates);

        assertEquals(497, coordinates.size());
        assertCoordinate(-978654.0, 214099.0, coordinates.get(0));
        assertCoordinate(-785613.0, 260409.0, coordinates.get(10));
        assertCoordinate(-978654.0, 214099.0, coordinates.get(coordinates.size() - 1));

        TestUtil.assertCorrectUTCDate(2015, 12, 13, 23, 15, 30, 128, read.getSensingStart());
        TestUtil.assertCorrectUTCDate(2015, 12, 14, 1, 1, 32, 787, read.getSensingStop());

    }

    private void assertCoordinate(double expectedX, double expectedY, Point coordinate) {
        assertEquals(expectedX, coordinate.getLon(), 1e-8);
        assertEquals(expectedY, coordinate.getLat(), 1e-8);
    }


    @After
    public void testCloseHDF5() throws IOException {
        file = new File(testDataDirectory, "NSS.AMBX.NK.D15348.S0057.E0250.B9144748.GC.h5");
        reader.open(file);
        reader.close();
    }
}
