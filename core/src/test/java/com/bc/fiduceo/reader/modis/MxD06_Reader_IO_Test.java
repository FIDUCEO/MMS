/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class MxD06_Reader_IO_Test {

    private File dataDirectory;
    private MxD06_Reader reader;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();
        reader = new MxD06_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo_Terra() throws IOException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2013, 2, 6, 14, 35, 0, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2013, 2, 6, 14, 40, 0, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(31, coordinates.length);
            assertEquals(-67.80709838867188, coordinates[0].getLon(), 1e-8);
            assertEquals(48.376953125, coordinates[0].getLat(), 1e-8);

            assertEquals(-38.39603042602539, coordinates[24].getLon(), 1e-8);
            assertEquals(44.151187896728516, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Point[] locations = coordinates[0].getCoordinates();
            Date time = timeAxes[0].getTime(locations[0]);
            TestUtil.assertCorrectUTCDate(2013, 2, 6, 14, 35, 7, 198, time);

            locations = coordinates[9].getCoordinates();
            time = timeAxes[0].getTime(locations[0]);
            TestUtil.assertCorrectUTCDate(2013, 2, 6, 14, 40, 0, 0, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_Aqua() throws IOException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 35, 0, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 40, 0, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(31, coordinates.length);
            assertEquals(95.26064300537111, coordinates[0].getLon(), 1e-8);
            assertEquals(-65.07981872558594, coordinates[0].getLat(), 1e-8);

            assertEquals(36.58618927001953, coordinates[24].getLon(), 1e-8);
            assertEquals(-73.6755599975586, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Point[] locations = coordinates[0].getCoordinates();
            Date time = timeAxes[0].getTime(locations[0]);
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 35, 0, 0, time);

            locations = coordinates[9].getCoordinates();
            time = timeAxes[0].getTime(locations[0]);
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 39, 54, 17, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_Terra() throws IOException {
        final File file = getTerraFile();

        try {
            reader.open(file);
            final Dimension productSize = reader.getProductSize();
            assertEquals(270, productSize.getNx());
            assertEquals(406, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_Aqua() throws IOException {
        final File file = getAquaFile();

        try {
            reader.open(file);
            final Dimension productSize = reader.getProductSize();
            assertEquals(270, productSize.getNx());
            assertEquals(406, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_Terra() throws IOException {
        final File file = getTerraFile();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1360161273778L, timeLocator.getTimeFor(0, 0));
            assertEquals(1360161273778L, timeLocator.getTimeFor(269, 0));

            assertEquals(1360161422969L, timeLocator.getTimeFor(76, 203));
            assertEquals(1360161572161L, timeLocator.getTimeFor(145, 405));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_Aqua() throws IOException {
        final File file = getAquaFile();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1242210874206L, timeLocator.getTimeFor(4, 0));
            assertEquals(1242210874206L, timeLocator.getTimeFor(223, 0));

            assertEquals(1242211023395L, timeLocator.getTimeFor(21, 202));
            assertEquals(1242211172583L, timeLocator.getTimeFor(147, 405));
        } finally {
            reader.close();
        }
    }

    // @todo 1 tb/tb continue here 2017-05-31
//    @Test
//    public void testGetVariables_Terra() throws IOException, InvalidRangeException {
//        final File file = getTerraFile();
//
//        try {
//            reader.open(file);
//            final List<Variable> variables = reader.getVariables();
//            assertEquals();
//        } finally {
//            reader.close();
//        }
//    }


    private File getTerraFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mod06-te", "v006", "2013", "037", "MOD06_L2.A2013037.1435.006.2015066015540.hdf"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getAquaFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"myd06-aq", "v006", "2009", "133", "MYD06_L2.A2009133.1035.006.2014062050327.hdf"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getFileAsserted(String testFilePath) {
        final File file = new File(dataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}
