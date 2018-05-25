/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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

package com.bc.fiduceo.reader.airs;

import static org.junit.Assert.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.*;
import org.junit.runner.*;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(IOTestRunner.class)
public class AIRS_L1B_Reader_IO_Test {

    private static Path airsDataPath;
    private static DateFormat dateFormat;

    private AIRS_L1B_Reader airsL1bReader;

    @BeforeClass
    public static void setUpClass() throws IOException {
        airsDataPath = TestUtil.getTestDataDirectory().toPath().resolve("airs-aq");
        dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    @Before
    public void setUp() throws IOException {
        airsL1bReader = new AIRS_L1B_Reader(null);
    }

    @After
    public void tearDown() throws Exception {
        airsL1bReader.close();
    }

    @Test
    public void testRead_FirstOfTheDay() throws IOException, ParseException {
        airsL1bReader.open(airsDataPath.resolve("AIRS.2010.01.07.001.L1B.AIRS_Rad.v5.0.0.0.G10007112420.hdf").toFile());

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        assertCorrectDate("2010-01-07 00:05:24.000", acquisitionInfo.getSensingStart());
        assertCorrectDate("2010-01-07 00:11:23.999", acquisitionInfo.getSensingStop());
        assertEquals(NodeType.ASCENDING, acquisitionInfo.getNodeType());

//        acquisitionInfo.getTimeAxes()
//        acquisitionInfo.getSensingStop()
//        acquisitionInfo.getSensingStart()
//        acquisitionInfo.getNodeType()
//        acquisitionInfo.getBoundingGeometry()

//        final List<Point> coordinates = Arrays.asList(acquisitionInfo.getBoundingGeometry().getCoordinates());
//        assertNotNull(coordinates);
//        assertEquals(40, coordinates.size());
//        assertCoordinate(-164.84726526737956, 78.65846904183893, coordinates.create(0));
//        assertCoordinate(14.132166700261118, 82.20332415748388, coordinates.create(10));
//        assertCoordinate(71.66176722582615, 70.98479982855267, coordinates.create(20));

//        assertEquals(0, acquisitionInfo.getTimeAxisStartIndices()[0]);
//        assertEquals(12, acquisitionInfo.getTimeAxisEndIndices()[0]);


    }

//        airsL1bReader.open(airsDataPath.resolve("AIRS.2010.01.07.005.L1B.AIRS_Rad.v5.0.0.0.G10007113615.hdf").toFile());
//        airsL1bReader.open(airsDataPath.resolve("AIRS.2010.01.07.236.L1B.AIRS_Rad.v5.0.0.0.G10008113144.hdf").toFile());
//        airsL1bReader.open(airsDataPath.resolve("AIRS.2010.01.07.240.L1B.AIRS_Rad.v5.0.0.0.G10008113232.hdf").toFile());

    private void assertCorrectDate(String expected, Date date) throws ParseException {
        assertNotNull(date);

        final Date expectedDate = dateFormat.parse(expected);
        assertEquals(expected, dateFormat.format(date));
//        assertEquals(expectedDate.getTime(), date.getTime());
    }

    private void assertCoordinate(double expectedX, double expectedY, Point coordinate) {
        assertEquals(expectedX, coordinate.getLon(), 1e-8);
        assertEquals(expectedY, coordinate.getLat(), 1e-8);
    }
}
