package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class SlstrReader_IO_Test {

    private File dataDirectory;
    private SlstrReader reader;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new SlstrReader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo_S3A() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 24, 36, 182, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 27, 36, 182, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.DESCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(29, coordinates.length);
            assertEquals(168.0067138671875, coordinates[0].getLon(), 1e-8);
            assertEquals(83.76530456542969, coordinates[0].getLat(), 1e-8);

            assertEquals(-141.01283264160156, coordinates[14].getLon(), 1e-8);
            assertEquals(65.22335052490234, coordinates[14].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 24, 36, 356, time);
            time = timeAxes[0].getTime(coordinates[15]);
            TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 27, 21, 312, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_S3A() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1542147896326L, timeLocator.getTimeFor(15, 0));
            assertEquals(1542147896626L, timeLocator.getTimeFor(16, 100));
            assertEquals(1542148072417L, timeLocator.getTimeFor(1189, 1000));
        } finally {
            reader.close();
        }
    }

    private File getS3AFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a", "1.0", "2018", "10", "13", "S3A_SL_1_RBT____20181013T222436_20181013T222736_20181015T035102_0179_037_001_1620_LN2_O_NT_003.SEN3", "xfdumanifest.xml"}, false);
        return getFileAsserted(testFilePath);
    }

    // @todo 3 tb/tb move this to a common location and refactor 2019-05-11
    private File getFileAsserted(String testFilePath) {
        final File file = new File(dataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}
