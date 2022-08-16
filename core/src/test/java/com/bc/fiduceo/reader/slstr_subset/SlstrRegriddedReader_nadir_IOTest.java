package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestData;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
            assertThat(cornerUpperLeft.getLon(), is(-3.605947));
            assertThat(cornerUpperLeft.getLat(), is(-25.831709));
            assertThat(cornerLowerRight.getLon(), is(-20.575312));
            assertThat(cornerLowerRight.getLat(), is(-18.609944000000002));
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

    @Test
    public void testGetVariables_S3A() throws IOException {
        final File file = getS3AFile_unpacked();

        try {
            reader.open(file);
            final List<Variable> variables = reader.getVariables();

            assertEquals(32, variables.size());
            @SuppressWarnings("Convert2MethodRef") final List<String> names = variables.stream().map(v -> v.getShortName()).collect(Collectors.toList());
            assertThat(names, Matchers.containsInAnyOrder(
                    "bayes_in", "bayes_orphan_in", "cloud_in", "confidence_in", "detector_in",
                    "latitude_in", "latitude_tx", "longitude_in", "longitude_tx",
                    "pixel_in", "pointing_in", "probability_cloud_dual_in", "probability_cloud_single_in",
                    "S1_radiance_in", "S2_radiance_in", "S3_radiance_in", "S4_radiance_in", "S5_radiance_in",
                    "S6_radiance_in", "S7_BT_in", "S7_exception_in", "S8_BT_in", "S8_exception_in", "S9_BT_in",
                    "S9_exception_in",
                    "sat_azimuth_tn", "sat_path_tn", "sat_zenith_tn",
                    "scan_in",
                    "solar_azimuth_tn", "solar_path_tn", "solar_zenith_tn"
            ));
        }finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_S3A_zip() throws IOException {
        final File file = getS3AFile_zip();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(1500, productSize.getNx());
            assertEquals(1200, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_S3B() throws IOException, InvalidRangeException {
        final File file = getS3BFile_unpacked();

        try {
            reader.open(file);

            final Interval interval = new Interval(3, 3);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(1057, 631, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(1574032793, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1574032793, 0, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1574032793, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1574032794, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1574032794, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1574032793, 2, 0, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_S3B_zip_upper_right() throws IOException, InvalidRangeException {
        final File file = getS3BFile_zip();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(1499, 1, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(-2147483647, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1574032699, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1574032699, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1574032699, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 3, 2, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 4, 2, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_S3A_bottom() throws IOException, InvalidRangeException {
        final File file = getS3AFile_unpacked();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(126, 1199, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(1590189320, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1590189320, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1590189320, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 2, 3, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 3, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_zip() throws IOException, InvalidRangeException {
        final File file = getS3AFile_zip();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(338, 811, interval, "cloud_in");
            NCTestUtils.assertValueAt(64, 0, 0, array);
            NCTestUtils.assertValueAt(64, 1, 0, array);
            NCTestUtils.assertValueAt(64, 2, 0, array);

            array = reader.readScaled(663, 618, interval, "detector_in");
            NCTestUtils.assertValueAt(1, 3, 0, array);
            NCTestUtils.assertValueAt(0, 4, 0, array);
            NCTestUtils.assertValueAt(1, 0, 1, array);

            array = reader.readScaled(1266, 245, interval, "S3_radiance_in");
            NCTestUtils.assertValueAt(-294.9119873046875, 1, 1, array);
            NCTestUtils.assertValueAt(-294.9119873046875, 2, 1, array);
            NCTestUtils.assertValueAt(-294.9119873046875, 3, 1, array);

            array = reader.readScaled(114, 956, interval, "S8_exception_in");
            NCTestUtils.assertValueAt(0, 4, 1, array);
            NCTestUtils.assertValueAt(0, 0, 2, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3B_nadir_bottom() throws IOException, InvalidRangeException {
        final File file = getS3BFile_unpacked();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final Array array = reader.readScaled(1473, 1199, interval, "S9_BT_in");
            NCTestUtils.assertValueAt(283.04, 2, 0, array);
            NCTestUtils.assertValueAt(274.66, 2, 1, array);
            NCTestUtils.assertValueAt(274.66, 2, 2, array);
            NCTestUtils.assertValueAt(-43.94999999999999, 2, 3, array);
            NCTestUtils.assertValueAt(-43.94999999999999, 2, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3B_zip() throws IOException, InvalidRangeException {
        final File file = getS3BFile_zip();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(339, 812, interval, "S8_BT_in");
            NCTestUtils.assertValueAt(386, 0, 0, array);
            NCTestUtils.assertValueAt(401, 1, 0, array);
            NCTestUtils.assertValueAt(396, 2, 0, array);

            array = reader.readRaw(664, 619, interval, "S9_exception_in");
            NCTestUtils.assertValueAt(0, 3, 0, array);
            NCTestUtils.assertValueAt(0, 4, 0, array);
            NCTestUtils.assertValueAt(0, 0, 1, array);

            array = reader.readRaw(1267, 246, interval, "solar_azimuth_tn");
            NCTestUtils.assertValueAt(204.96172681244948, 1, 1, array);
            NCTestUtils.assertValueAt(204.97304213239295, 2, 1, array);
            NCTestUtils.assertValueAt(204.98435744789836, 3, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_left() throws IOException, InvalidRangeException {
        final File file = getS3AFile_unpacked();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final Array array = reader.readRaw(0, 318, interval, "S8_exception_in");
            NCTestUtils.assertValueAt(-127, 0, 1, array);
            NCTestUtils.assertValueAt(-127, 1, 1, array);
            NCTestUtils.assertValueAt(-128, 2, 1, array);
            NCTestUtils.assertValueAt(-128, 3, 1, array);
            NCTestUtils.assertValueAt(-128, 4, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixel_S3A_zip() throws IOException {
        final File file = getS3AFile_zip();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(145.5, 1045.5, null);
            assertEquals(-7.804243, geoLocation.getX(), 1e-8);
            assertEquals(-17.144347, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(-7.804243, -17.144347);
            assertEquals(1, pixelLocation.length);
            assertEquals(145.5, pixelLocation[0].getX(), 1e-8);
            assertEquals(1045.5, pixelLocation[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(668.5, 805.5, null);
            assertEquals(-12.020273999999999, geoLocation.getX(), 1e-8);
            assertEquals(-20.451089, geoLocation.getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-12.020273999999999, -20.451089);
            assertEquals(1, pixelLocation.length);
            assertEquals(668.5, pixelLocation[0].getX(), 1e-8);
            assertEquals(805.5, pixelLocation[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(1000.5, 850.5, null);
            assertEquals(-15.228683, geoLocation.getX(), 1e-8);
            assertEquals(-20.745514, geoLocation.getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-15.228683, -20.745514);
            assertEquals(1, pixelLocation.length);
            assertEquals(1000.5, pixelLocation[0].getX(), 1e-8);
            assertEquals(850.5, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(1725, -90);
            assertEquals(0, pixelLocation.length);
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

    private File getS3BFile_zip() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3b-uor-o", "1.0", "2019", "11", "17", "S3B_SL_1_RBT____20191117T231801_20191117T232101_20191119T035119_0180_032_172_5400_LN2_O_NT_003.zip"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
