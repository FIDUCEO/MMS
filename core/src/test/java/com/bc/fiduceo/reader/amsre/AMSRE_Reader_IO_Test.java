/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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

package com.bc.fiduceo.reader.amsre;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class AMSRE_Reader_IO_Test {

    private File testDataDirectory;
    private AMSRE_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        reader = new AMSRE_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = createAmsreFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 36, 6, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 56, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.DESCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(67, coordinates.length);

            assertEquals(111.96620178222658, coordinates[0].getLon(), 1e-8);
            assertEquals(83.54039001464844, coordinates[0].getLat(), 1e-8);

            assertEquals(-159.9243621826172, coordinates[23].getLon(), 1e-8);
            assertEquals(-77.13606262207031, coordinates[23].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);

            final TimeAxis timeAxis = timeAxes[0];
            coordinates = timeAxis.getGeometry().getCoordinates();
            final Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 36, 6, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File file = createAmsreFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);
            assertTrue(timeLocator instanceof AMSRE_TimeLocator);

            long time = timeLocator.getTimeFor(67, 0);
            assertEquals(1108618539107L, time);
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 35, 39, new Date(time));

            time = timeLocator.getTimeFor(68, 1000);
            assertEquals(1108620038998L, time);

            time = timeLocator.getTimeFor(68, 1994);
            assertEquals(1108621529890L, time);
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 29, new Date(time));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws IOException, InvalidRangeException {
        final File file = createAmsreFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(42, variables.size());

            Variable variable = variables.get(0);
            assertEquals("Time", variable.getShortName());
            assertEquals(DataType.DOUBLE, variable.getDataType());

            variable = variables.get(3);
            assertEquals("Earth_Incidence", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(6);
            assertEquals("Sun_Azimuth", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(7);
            assertEquals("Land_Ocean_Flag_6", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(8);
            assertEquals("6.9V_Res.1_TB", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(11);
            assertEquals("10.7H_Res.1_TB", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(14);
            assertEquals("23.8V_Res.1_TB", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(17);
            assertEquals("36.5H_Res.1_TB", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(20);
            assertEquals("Scan_Quality_Flag", variable.getShortName());
            assertEquals(DataType.INT, variable.getDataType());

            variable = variables.get(21);
            assertEquals("Channel_Quality_Flag_6V", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(24);
            assertEquals("Channel_Quality_Flag_10H", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(27);
            assertEquals("Channel_Quality_Flag_23V", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(30);
            assertEquals("Channel_Quality_Flag_36H", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            // @todo 1 tb/tb continue here 2016-09-05
//            variable = variables.get(33);
//            assertEquals("Channel_Quality_Flag_36H", variable.getShortName());
//            assertEquals(DataType.SHORT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    private File createAmsreFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsre-aq", "v12", "2005", "02", "17", "AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}
