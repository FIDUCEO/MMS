package com.bc.fiduceo.reader.calipso;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import org.junit.*;
import org.junit.runner.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@RunWith(IOTestRunner.class)
public class CALIPSO_L2_VFM_Reader_IO_Test {

    private File testDataDirectory;
    private CALIPSO_L2_VFM_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        reader = new CALIPSO_L2_VFM_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = getCalipsoFile();

        final AcquisitionInfo acquisitionInfo;
        try (Reader r = reader){
            r.open(file);

            acquisitionInfo = r.read();
//        } finally {
//            reader.close();
        }
        assertNotNull(acquisitionInfo);

        final Date sensingStart = acquisitionInfo.getSensingStart();
        TestUtil.assertCorrectUTCDate(2011, 1, 2, 23, 37, 1, sensingStart);

        final Date sensingStop = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2011, 1, 3, 0, 29, 33, sensingStop);

        final NodeType nodeType = acquisitionInfo.getNodeType();
        assertEquals(NodeType.UNDEFINED, nodeType);

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof LineString);

        Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(61, coordinates.length);

        assertEquals(16.246477, coordinates[0].getLon(), 1e-6);
        assertEquals(-61.96789, coordinates[0].getLat(), 1e-5);

        assertEquals(172.98334, coordinates[60].getLon(), 1e-5);
        assertEquals(71.75316, coordinates[60].getLat(), 1e-5);

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertEquals(1, timeAxes.length);

        final TimeAxis timeAxis = timeAxes[0];
        coordinates = timeAxis.getGeometry().getCoordinates();
        final Date time = timeAxes[0].getTime(coordinates[0]);
        TestUtil.assertCorrectUTCDate(2011, 1, 2, 23, 37, 1, time);
    }

    private File getCalipsoFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"calipso-vfl", "CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-04ZD.hdf"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}