package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestData;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(IOTestRunner.class)
public class SlstrRegriddedReader_nadir_IOTest {

    private SlstrRegriddedSubsetReader reader;
    private ReaderContext readerContext;

    @Before
    public void setUp() throws IOException {
        readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));
        reader = new SlstrRegriddedSubsetReader(readerContext, true);
    }

    @Test
    public void testReadAcquisitionInfo_S3A() throws IOException {
        final File input = getS3AFile_unpacked();

        try {
            reader.open(input);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 12, 2, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 15, 2, sensingStop);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertThat(boundingGeometry, is(notNullValue()));
            assertThat(acquisitionInfo.getTimeAxes(), is(notNullValue()));

            assertThat(boundingGeometry, is(instanceOf(Polygon.class)));
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertThat(coordinates.length, is(23));
            final Point cornerUpperLeft = coordinates[0];
            final Point cornerLowerRight = coordinates[11];
            assertThat(cornerUpperLeft.getLon(), is(-3.6059465890532283));
            assertThat(cornerUpperLeft.getLat(), is(-25.83171070667771));
            assertThat(cornerLowerRight.getLon(), is(-20.57531176627247));
            assertThat(cornerLowerRight.getLat(), is(-18.60994342420135));
            assertThat(readerContext.getGeometryFactory().format(boundingGeometry), is(TestData.SLSTR_S3A_SUBSET_GEOMETRY_NADIR));

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            final TimeAxis timeAxis = timeAxes[0];

            Date time = timeAxis.getTime(cornerUpperLeft);
            TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 12, 2, time);

            time = timeAxis.getTime(cornerLowerRight);
            TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 15, 1, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_S3A_zip() throws IOException {
        final File file = getS3AFile_zip();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1590189141910L, timeLocator.getTimeFor(15, 10));
            assertEquals(1590189141910L, timeLocator.getTimeFor(16, 10));
            assertEquals(1590189155409L, timeLocator.getTimeFor(17, 100));
            assertEquals(1590189290403L, timeLocator.getTimeFor(1190, 1000));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_S3B() throws IOException {
        final File file = getS3BFile_unpacked();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1574032701906L, timeLocator.getTimeFor(15, 20));
            assertEquals(1574032701906L, timeLocator.getTimeFor(16, 20));
            assertEquals(1574032728905L, timeLocator.getTimeFor(17, 200));
            assertEquals(1574032850401L, timeLocator.getTimeFor(1190, 1010));
        } finally {
            reader.close();
        }
    }

    private File getS3AFile_unpacked() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a-uor-n", "1.0", "2020", "05", "22", "S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.SEN3"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getS3AFile_zip() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a-uor-n", "1.0", "2020", "05", "22", "S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.zip"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getS3BFile_unpacked() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3b-uor-o", "1.0", "2019", "11", "17", "S3B_SL_1_RBT____20191117T231801_20191117T232101_20191119T035119_0180_032_172_5400_LN2_O_NT_003.SEN3"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
