package com.bc.fiduceo.reader.fiduceo_fcdr;

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
public class AVHRR_FCDR_Reader_IO_Test {

    private File testDataDirectory;
    private AVHRR_FCDR_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new AVHRR_FCDR_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo_NOAA12() throws IOException {
        final File file = createAvhrrNOAA12File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);
            
            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1992, 4, 14, 14, 14, 12, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1992, 4, 14, 15, 55, 32, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(145, coordinates.length);
            assertEquals(-113.10220638290049, coordinates[0].getLon(), 1e-8);
            assertEquals(1.9309060927480464, coordinates[0].getLat(), 1e-8);

            assertEquals(-159.40000595524907, coordinates[25].getLon(), 1e-8);
            assertEquals(-62.739341352134936, coordinates[25].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(145, coordinates.length);
            assertEquals(79.80712294578552, coordinates[0].getLon(), 1e-8);
            assertEquals(1.593066193163395, coordinates[0].getLat(), 1e-8);

            assertEquals(81.58696241676807, coordinates[26].getLon(), 1e-8);
            assertEquals(76.53035058639944, coordinates[26].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1992, 4, 14, 14, 14, 26, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1992, 4, 14, 15, 4, 52, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_METOPA() throws IOException {
        final File file = createAvhrrMetopAFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2016, 11, 8, 7, 37, 29, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2016, 11, 8, 8, 28, 17, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Polygon boundingPolygon = (Polygon) boundingGeometry;

            Point[] coordinates = boundingPolygon.getCoordinates();
            assertEquals(145, coordinates.length);
            assertEquals(-95.06759840995073, coordinates[0].getLon(), 1e-8);
            assertEquals(-63.95336765795946, coordinates[0].getLat(), 1e-8);

            assertEquals(-145.73259668424726, coordinates[27].getLon(), 1e-8);
            assertEquals(6.561784716323017, coordinates[27].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            coordinates = boundingPolygon.getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2016, 11, 8, 7, 37, 29, time);
        } finally {
            reader.close();
        }
    }

//    @Test
//    public void testGetTimeLocatorNOAA12() throws IOException {
//        final File file = createAvhrrNOAA12File();
//
//        try {
//            reader.open(file);
//            final TimeLocator timeLocator = reader.getTimeLocator();
//            assertNotNull(timeLocator);
//
//            final long referenceTime = 448042841000L;
//            assertEquals(referenceTime, timeLocator.getTimeFor(169, 0));
//            assertEquals(referenceTime + 494, timeLocator.getTimeFor(168, 1));
//            assertEquals(referenceTime + 6997, timeLocator.getTimeFor(169, 14));
//            assertEquals(referenceTime + 507493, timeLocator.getTimeFor(170, 1015));
//            assertEquals(referenceTime + 1007996, timeLocator.getTimeFor(171, 2016));
//            assertEquals(referenceTime + 6122997, timeLocator.getTimeFor(172, 12246));
//        } finally {
//            reader.close();
//        }
//    }

    private File createAvhrrNOAA12File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n12-fcdr", "vBeta", "1992", "04", "14", "FIDUCEO_FCDR_L1C_AVHRR_NOAA12_19920414141412_19920414155532_EASY_vBeta_fv2.0.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createAvhrrMetopAFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-ma-fcdr", "vBeta", "2016", "11", "08", "FIDUCEO_FCDR_L1C_AVHRR_METOPA_20161108073729_20161108082817_EASY_vBeta_fv2.0.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

}
