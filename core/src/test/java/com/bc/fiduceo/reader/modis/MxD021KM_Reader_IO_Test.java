package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.After;
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
public class MxD021KM_Reader_IO_Test {

    private MxD021KM_Reader reader;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() throws IOException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(geometryFactory);

        reader = new MxD021KM_Reader(readerContext);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final AcquisitionInfo acquisitionInfo = reader.read();
        assertNotNull(acquisitionInfo);

        final Date sensingStart = acquisitionInfo.getSensingStart();
        TestUtil.assertCorrectUTCDate(2003, 5, 22, 14, 45, 0, 0, sensingStart);

        final Date sensingStop = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2003, 5, 22, 14, 50, 0, 0, sensingStop);

        final NodeType nodeType = acquisitionInfo.getNodeType();
        assertEquals(NodeType.UNDEFINED, nodeType);

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);
        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(31, coordinates.length);
        assertEquals(-119.84687042236328, coordinates[0].getLon(), 1e-8);
        assertEquals(-67.26568603515625, coordinates[0].getLat(), 1e-8);

        assertEquals(-50.65733337402344, coordinates[24].getLon(), 1e-8);
        assertEquals(-77.25603485107422, coordinates[24].getLat(), 1e-8);
    }

    @Test
    public void testReadAcquisitionInfo_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final AcquisitionInfo acquisitionInfo = reader.read();
        assertNotNull(acquisitionInfo);

        final Date sensingStart = acquisitionInfo.getSensingStart();
        TestUtil.assertCorrectUTCDate(2011, 6, 17, 22, 10, 0, 0, sensingStart);

        final Date sensingStop = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2011, 6, 17, 22, 15, 0, 0, sensingStop);

        final NodeType nodeType = acquisitionInfo.getNodeType();
        assertEquals(NodeType.UNDEFINED, nodeType);

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);
        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(31, coordinates.length);
        assertEquals(-120.74213409423832, coordinates[0].getLon(), 1e-8);
        assertEquals(68.62024688720705, coordinates[0].getLat(), 1e-8);

        assertEquals(-169.29559326171875, coordinates[24].getLon(), 1e-8);
        assertEquals(61.602462768554695, coordinates[24].getLat(), 1e-8);
    }

    private File getTerraFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mod021km-te", "v61", "2003", "05", "22", "MOD021KM.A2003142.1445.061.2017194130122.hdf"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getAquaFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"myd021km-aq", "v61", "2011", "06", "17", "MYD021KM.A2011168.2210.061.2018032001033.hdf"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
