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
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.*;
import org.junit.runner.*;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

@RunWith(IOTestRunner.class)
public class AIRS_L1B_Reader_IO_Test {

    private final String FirstProductOfTheDay = "AIRS.2010.01.07.001.L1B.AIRS_Rad.v5.0.0.0.G10007112420.hdf";
    private final String CloseToFirstProductOfTheDay = "AIRS.2010.01.07.005.L1B.AIRS_Rad.v5.0.0.0.G10007113615.hdf";
    private final String CloseToLastProductOfTheDay = "AIRS.2010.01.07.236.L1B.AIRS_Rad.v5.0.0.0.G10008113144.hdf";
    private final String LastProductOfTheDay = "AIRS.2010.01.07.240.L1B.AIRS_Rad.v5.0.0.0.G10008113232.hdf";

    private Path airsDataPath;
    private DateFormat dateFormat;

    private AIRS_L1B_Reader airsL1bReader;

    @Before
    public void setUp() throws IOException {
        airsDataPath = TestUtil.getTestDataDirectory().toPath().resolve("airs-aq");
        dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));
        airsL1bReader = new AIRS_L1B_Reader(readerContext);
    }

    @After
    public void tearDown() throws Exception {
        airsL1bReader.close();
    }

    @Test
    public void testReadAcquisitionInfo_FirstProductOfTheDay() throws IOException, ParseException {
        airsL1bReader.open(airsDataPath.resolve(FirstProductOfTheDay).toFile());

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final String sensingStart = "2010-01-07 00:05:24.000";
        final String sensingStop = "2010-01-07 00:11:23.999";
        final NodeType nodeType = NodeType.ASCENDING;
        final int numBoundingCoordinates = 25;
        final IndexedPoint[] boundingPoints = {
                new IndexedPoint(0, -160.58036121827877, -41.004036486781956),
                new IndexedPoint(4, -161.05254137389082, -19.224336964357146),
                new IndexedPoint(9, -153.6135499287492, -18.145799200699678),
                new IndexedPoint(14, -142.23738370588939, -32.68196330611892),
                new IndexedPoint(19, -149.31553953668907, -39.80039622557189),
                new IndexedPoint(24, -160.58036121827877, -41.004036486781956)
        };
        final int timeAxisDuration = 359999;
        final int numTimeAxisPoints = 4;
        final IndexedPoint[] expTimeAxisPoints = {
                new IndexedPoint(0, -149.94851774135594, -39.900237141024554),
                new IndexedPoint(1, -152.26983032923866, -31.9310548433196),
                new IndexedPoint(2, -154.30902622054185, -23.925098294196466),
                new IndexedPoint(3, -155.5917138838205, -18.46488718833132)
        };


        assertAquisitionInfo(acquisitionInfo,
                             sensingStart, sensingStop, nodeType,
                             numBoundingCoordinates, boundingPoints,
                             timeAxisDuration, numTimeAxisPoints, expTimeAxisPoints);

        assertEquals("Latitude", airsL1bReader.getLatitudeVariableName());
        assertEquals("Longitude", airsL1bReader.getLongitudeVariableName());
        final PixelLocator pixelLocator = airsL1bReader.getPixelLocator();
        assertNotNull(pixelLocator);
    }

    @Test
    public void testReadAcquisitionInfo_CloseToFirstProductOfTheDay() throws IOException, ParseException {
        airsL1bReader.open(airsDataPath.resolve(CloseToFirstProductOfTheDay).toFile());

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final String sensingStart = "2010-01-07 00:29:24.000";
        final String sensingStop = "2010-01-07 00:35:23.999";
        final NodeType nodeType = NodeType.ASCENDING;

        final int numBoundingCoordinates = 25;
        final IndexedPoint[] boundingPoints = {
                new IndexedPoint(0, 177.3923460702796, 44.49116274166107),
                new IndexedPoint(4, 163.83469904779346, 65.15509282631304),
                new IndexedPoint(9, -179.79775976537454, 68.24818312168115),
                new IndexedPoint(14, -160.87279750459268, 53.32304100688955),
                new IndexedPoint(19, -170.88670253253346, 46.86273350972246),
                new IndexedPoint(24, 177.3923460702796, 44.49116274166107)
        };

        final int timeAxisDuration = 359999;
        final int numTimeAxisPoints = 4;
        final IndexedPoint[] expTimeAxisPoints = {
                new IndexedPoint(0, -171.58591251481585, 46.75805806021887),
                new IndexedPoint(1, -175.01312278072135, 54.646004393037586),
                new IndexedPoint(2, -179.81070704199072, 62.4201870893187),
                new IndexedPoint(3, 175.4312904888272, 67.59035616165096)
        };

        assertAquisitionInfo(acquisitionInfo,
                             sensingStart, sensingStop, nodeType,
                             numBoundingCoordinates, boundingPoints,
                             timeAxisDuration, numTimeAxisPoints, expTimeAxisPoints);
    }

    @Test
    public void testReadAcquisitionInfo_CloseToLastProductOfTheDay() throws IOException, ParseException {
        airsL1bReader.open(airsDataPath.resolve(CloseToLastProductOfTheDay).toFile());

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final String sensingStart = "2010-01-07 23:35:24.000";
        final String sensingStop = "2010-01-07 23:41:23.999";
        final NodeType nodeType = NodeType.ASCENDING;
        final int numBoundingCoordinates = 25;
        final IndexedPoint[] boundingPoints = {
                new IndexedPoint(0, -172.27133582792965, 49.9423509168333),
                new IndexedPoint(4, 168.11832902638176, 69.86166126288212),
                new IndexedPoint(9, -172.51500576157054, 73.7847658106488),
                new IndexedPoint(14, -147.8540238995572, 59.16644183567005),
                new IndexedPoint(19, -159.29997125412646, 52.646366874661226),
                new IndexedPoint(24, -172.27133582792965, 49.9423509168333)
        };
        final int timeAxisDuration = 359999;
        final int numTimeAxisPoints = 4;
        final IndexedPoint[] expTimeAxisPoints = {
                new IndexedPoint(0, -160.08426185741553, 52.52941926262613),
                new IndexedPoint(1, -164.4162005056497, 60.341837662286686),
                new IndexedPoint(2, -171.0924637408649, 67.96965235826016),
                new IndexedPoint(3, -178.49355932258163, 72.93192501547036)
        };


        assertAquisitionInfo(acquisitionInfo,
                             sensingStart, sensingStop, nodeType,
                             numBoundingCoordinates, boundingPoints,
                             timeAxisDuration, numTimeAxisPoints, expTimeAxisPoints);
    }

    @Test
    public void testReadAcquisitionInfo_LastProductOfTheDay() throws IOException, ParseException {
        airsL1bReader.open(airsDataPath.resolve(LastProductOfTheDay).toFile());

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final String sensingStart = "2010-01-07 23:59:24.000";
        final String sensingStop = "2010-01-08 00:05:23.999";
        final NodeType nodeType = NodeType.DESCENDING;
        final int numBoundingCoordinates = 25;
        final IndexedPoint[] boundingPoints = {
                new IndexedPoint(0, 41.13878818856406, 37.23502459334875),
                new IndexedPoint(4, 31.012294490703606, 16.774711614878182),
                new IndexedPoint(9, 23.678166447899063, 17.83152001447589),
                new IndexedPoint(14, 20.165963444715054, 34.569152009864204),
                new IndexedPoint(19, 30.580425601541442, 39.1540590819922),
                new IndexedPoint(24, 41.13878818856406, 37.23502459334875)
        };
        final int timeAxisDuration = 359999;
        final int numTimeAxisPoints = 4;
        final IndexedPoint[] expTimeAxisPoints = {
                new IndexedPoint(0, 31.19984799556, 39.07174966950319),
                new IndexedPoint(1, 28.925058120604955, 31.07533396419192),
                new IndexedPoint(2, 26.915686824838257, 23.04849053661578),
                new IndexedPoint(3, 25.646540679335928, 17.577617949148454)
        };


        assertAquisitionInfo(acquisitionInfo,
                             sensingStart, sensingStop, nodeType,
                             numBoundingCoordinates, boundingPoints,
                             timeAxisDuration, numTimeAxisPoints, expTimeAxisPoints);


    }

    private void assertAquisitionInfo(AcquisitionInfo acquisitionInfo, String sensingStart, String sensingStop, NodeType nodeType, int numBoundingCoordinates, IndexedPoint[] boundingPoints, int timeAxisDuration, int numTimeAxisPoints, IndexedPoint[] expTimeAxisPoints) throws ParseException {
        assertCorrectDate(sensingStart, acquisitionInfo.getSensingStart());
        assertCorrectDate(sensingStop, acquisitionInfo.getSensingStop());
        assertEquals(nodeType, acquisitionInfo.getNodeType());

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertEquals("class com.bc.fiduceo.geometry.s2.BcS2Polygon", boundingGeometry.getClass().toString());

        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(numBoundingCoordinates, coordinates.length);
        assertCoordinates(boundingPoints, coordinates);

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertNotNull(timeAxes);
        assertEquals(1, timeAxes.length);
        final TimeAxis timeAxis = timeAxes[0];
        assertNotNull(timeAxis);
        assertEquals(sensingStart, dateFormat.format(timeAxis.getStartTime()));
        assertEquals(sensingStop, dateFormat.format(timeAxis.getEndTime()));
        assertEquals(timeAxisDuration, timeAxis.getDurationInMillis());
        final Geometry axisGeometry = timeAxis.getGeometry();
        assertNotNull(axisGeometry);
        final Point[] axisCoordinates = axisGeometry.getCoordinates();
        assertNotNull(axisCoordinates);
        assertEquals(numTimeAxisPoints, axisCoordinates.length);
        assertCoordinates(expTimeAxisPoints, axisCoordinates);
    }

    private void assertCorrectDate(String expected, Date date) throws ParseException {
        assertNotNull(date);
        assertEquals(expected, dateFormat.format(date));
    }

    private void assertCoordinates(IndexedPoint[] expected, Point[] actual) {
        for (IndexedPoint ip : expected) {
            final String msg = "error at index " + ip.idx;
            final Point point = actual[ip.idx];
            assertEquals("Lon " + msg, ip.lon, point.getLon(), 1e-8);
            assertEquals("Lat " + msg, ip.lat, point.getLat(), 1e-8);
        }
    }

    private void assertCoordinate(String msg, double expectedLon, double expectedLat, Point coordinate) {
        assertEquals(expectedLon, coordinate.getLon(), 1e-8);
        assertEquals(expectedLat, coordinate.getLat(), 1e-8);
    }

    static class IndexedPoint {

        int idx;
        double lon, lat;

        public IndexedPoint(int idx, double lon, double lat) {
            this.idx = idx;
            this.lon = lon;
            this.lat = lat;
        }
    }
}
