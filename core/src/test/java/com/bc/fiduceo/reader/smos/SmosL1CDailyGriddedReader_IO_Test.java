package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.util.TempFileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class SmosL1CDailyGriddedReader_IO_Test {

    private SmosL1CDailyGriddedReader reader;
    private ReaderContext readerContext;

    @Before
    public void setUp() throws IOException {
        readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        final File testDir = TestUtil.getTestDir();
        if (!testDir.mkdirs()) {
            fail("unable to create test directory");
        }

        readerContext.setTempFileUtils(new TempFileUtils(testDir.getAbsolutePath()));
        reader = new SmosL1CDailyGriddedReader(readerContext);
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testReadAcquisitionInfo_CDF3TA() throws IOException {
        final File file = getCDF3TAFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertTrue(boundingGeometry instanceof Polygon);

            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(5, coordinates.length);
            assertEquals(-179.8703155517578, coordinates[0].getLon(), 1e-8);
            assertEquals(-83.51713562011719, coordinates[0].getLat(), 1e-8);
            assertEquals(-179.8703155517578, coordinates[1].getLon(), 1e-8);
            assertEquals(83.51713562011719, coordinates[1].getLat(), 1e-8);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2016, 6, 10, 0, 0, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2016, 6, 10, 23, 59, 59, sensingStop);
        } finally {
            reader.close();
        }
    }

    private File getCDF3TAFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"miras-smos-CDF3TA", "re07", "2016", "162", "SM_RE07_MIR_CDF3TA_20160610T000000_20160610T235959_330_001_7.tgz"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
