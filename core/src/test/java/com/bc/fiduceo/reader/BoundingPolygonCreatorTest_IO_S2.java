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
import com.bc.fiduceo.geometry.Polygon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.ArrayDouble;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class BoundingPolygonCreatorTest_IO_S2 {

    private NetcdfFile netcdfFile;
    private AMSU_MHS_L1B_Reader reader;
    private BoundingPolygonCreator boundingPolygonCreator;


    @Before
    public void setUp() throws IOException {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
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
    public void createValidMultiplePolygon_AMSU_Reader() throws IOException {
        List<Polygon> polygonList = new ArrayList<>();
        List<ArrayDouble.D2> long_lat = AMSU_MHS_L1B_Reader.getLat_Long(netcdfFile);

        ArrayDouble.D2 arrayLong = long_lat.get(0);
        ArrayDouble.D2 arrayLat = long_lat.get(1);

        final int[] shape = arrayLat.getShape();
        int width = shape[1] - 1;
        int height = (shape[0] - 1);

        for (int i = 1; i <= 4; i++) {
            polygonList = boundingPolygonCreator.createPolygonsBounding(arrayLat, arrayLong, width, height, i);
            if (BoundingPolygonCreator.arePolygonsValid(polygonList)) {
                break;
            }
        }

        assertEquals(polygonList.get(0).getCoordinates()[0].getLon(), -97.86539752771206, 1e-8);
        assertEquals(polygonList.get(0).getCoordinates()[0].getLat(), 21.40989945914043, 1e-8);
        assertTrue(BoundingPolygonCreator.arePolygonsValid(polygonList));
    }
}
