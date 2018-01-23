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

package com.bc.fiduceo.reader.airs;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.Point;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(IOTestRunner.class)
public class AIRS_L1B_Reader_IO_Test {

    private AIRS_L1B_Reader airsL1bReader;
    private File dataDirectory;
    private DateFormat dateFormat;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();
        airsL1bReader = new AIRS_L1B_Reader(null);
        dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.S");
    }

    @After
    public void endTest() throws IOException {
        airsL1bReader.close();
    }

    @Test
    public void dummy() throws Exception {
        // remove this when we have meaningful tests g´here tb 2016-04-11

    }

    //    @Test
//    public void testRead_closeToPole() throws IOException, ParseException {
//        final File airsL1bFile = new File(dataDirectory, "AIRS.2015.09.02.006.L1B.AIRS_Rad.v5.0.23.0.G15246014542.hdf");
//        airsL1bReader.open(airsL1bFile);
//
//        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
//        assertNotNull(acquisitionInfo);
//
//        final List<Point> coordinates = acquisitionInfo.getCoordinates();
//        assertNotNull(coordinates);
//        assertEquals(40, coordinates.size());
//        assertCoordinate(-164.84726526737956, 78.65846904183893, coordinates.create(0));
//        assertCoordinate(14.132166700261118, 82.20332415748388, coordinates.create(10));
//        assertCoordinate(71.66176722582615, 70.98479982855267, coordinates.create(20));
//
////        assertEquals(0, acquisitionInfo.getTimeAxisStartIndices()[0]);
////        assertEquals(12, acquisitionInfo.getTimeAxisEndIndices()[0]);
//
//        assertCorrectDate("2015-09-02 00:35:22.000Z", acquisitionInfo.getSensingStart());
//        assertCorrectDate("2015-09-02 00:41:21.999Z", acquisitionInfo.getSensingStop());
//
//        assertEquals(NodeType.DESCENDING, acquisitionInfo.getNodeType());
//    }

//    @Test
//    public void testRead_descendingNode() throws IOException, ParseException {
//        final File airsL1bFile = new File(dataDirectory, "AIRS.2015.09.02.023.L1B.AIRS_Rad.v5.0.23.0.G15246021652.hdf");
//        airsL1bReader.open(airsL1bFile);
//
//        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
//        assertNotNull(acquisitionInfo);
//
//        final List<Point> coordinates = acquisitionInfo.getCoordinates();
//        assertNotNull(coordinates);
//        assertEquals(40, coordinates.size());
//        assertCoordinate(-6.4170300611108315, 88.23613967607469, coordinates.create(1));
//        assertCoordinate(-13.02427652238762, 69.09764314472332, coordinates.create(11));
//        assertCoordinate(27.651190877552047, 64.80095475900657, coordinates.create(21));
//
//        //todo:mba to add time series.
////        assertEquals(0, acquisitionInfo.getTimeAxisStartIndices()[0]);
////        assertEquals(12, acquisitionInfo.getTimeAxisEndIndices()[0]);
//
//        assertCorrectDate("2015-09-02 02:17:22.000Z", acquisitionInfo.getSensingStart());
//        assertCorrectDate("2015-09-02 02:23:21.999Z", acquisitionInfo.getSensingStop());
//
//        assertEquals(NodeType.DESCENDING, acquisitionInfo.getNodeType());
//    }

//    @Test
//    public void testRead_ascendingNode() throws com.vividsolutions.jts.io.ParseException, IOException, ParseException {
//        final File airsL1bFile = new File(dataDirectory, "AIRS.2015.09.02.135.L1B.AIRS_Rad.v5.0.23.0.G15246114803.hdf");
//        airsL1bReader.open(airsL1bFile);
//
//        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
//        assertNotNull(acquisitionInfo);
//
//        final List<Point> coordinates = acquisitionInfo.getCoordinates();
//        assertNotNull(coordinates);
//        assertEquals(40, coordinates.size());
//        assertCoordinate(-3.5416849171828058, 15.6620445809086, coordinates.create(2));
//        assertCoordinate(5.98667051416274, 24.885289473168978, coordinates.create(12));
//        assertCoordinate(-2.9671231963127838, 38.0742812192399, coordinates.create(22));
//
//
//        assertCorrectDate("2015-09-02 13:29:22.000Z", acquisitionInfo.getSensingStart());
//        assertCorrectDate("2015-09-02 13:35:21.999Z", acquisitionInfo.getSensingStop());
//
//        assertEquals(NodeType.ASCENDING, acquisitionInfo.getNodeType());
//    }

    private void assertCorrectDate(String expected, Date date) throws ParseException {
        assertNotNull(date);

        final Date expectedDate = dateFormat.parse(expected);
        assertEquals(expectedDate.getTime(), date.getTime());
    }

    private void assertCoordinate(double expectedX, double expectedY, Point coordinate) {
        assertEquals(expectedX, coordinate.getLon(), 1e-8);
        assertEquals(expectedY, coordinate.getLat(), 1e-8);
    }
}
