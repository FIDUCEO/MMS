package com.bc.fiduceo.reader.avhrr_frac;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.MultiPolygon;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class AVHRR_FRAC_Reader_IO_Test {

    private File dataDirectory;
    private AVHRR_FRAC_Reader reader;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new AVHRR_FRAC_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = getAvhrrFRACFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2018, 5, 11, 14, 4, 15, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2018, 5, 11, 15, 44, 35, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(67, coordinates.length);
            assertEquals(-112.33999633789062, coordinates[0].getLon(), 1e-8);
            assertEquals(84.6032943725586, coordinates[0].getLat(), 1e-8);

            assertEquals(170.42459106445312, coordinates[28].getLon(), 1e-8);
            assertEquals(-81.03530120849611, coordinates[28].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(67, coordinates.length);
            assertEquals(179.9835968017578, coordinates[0].getLon(), 1e-8);
            assertEquals(-68.8104019165039, coordinates[0].getLat(), 1e-8);

            assertEquals(-8.743000030517578, coordinates[29].getLon(), 1e-8);
            assertEquals(79.99319458007814, coordinates[29].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);

            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2018, 5, 11, 14, 4, 16, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2018, 5, 11, 14, 54, 25, time);

        } finally {
            reader.close();
        }
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    @Test
    public void testGetTimeLocator() throws IOException {
        final File file = getAvhrrFRACFile();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            final long referenceTime = 1526047455000L;
            assertEquals(1526047455000L, timeLocator.getTimeFor(169, 0));
            assertEquals(1526047455167L, timeLocator.getTimeFor(168, 1));
            assertEquals(1526047457333L, timeLocator.getTimeFor(169, 14));
            assertEquals(1526047624167L, timeLocator.getTimeFor(170, 1015));
            assertEquals(1526047791000L, timeLocator.getTimeFor(171, 2016));
            assertEquals(1526053475000L, timeLocator.getTimeFor(172, 36120));
        } finally {
            reader.close();
        }
    }

    private File getAvhrrFRACFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-frac-ma", "v1", "2018", "05", "11", "NSS.FRAC.M2.D18131.S1404.E1544.B5998081.SV"}, false);

        final File file = new File(dataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}
