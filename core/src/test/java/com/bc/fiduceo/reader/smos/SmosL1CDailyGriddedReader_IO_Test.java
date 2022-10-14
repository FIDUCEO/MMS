package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.TempFileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("NewClassNamingConvention")
@RunWith(IOTestRunner.class)
public class SmosL1CDailyGriddedReader_IO_Test {

    private SmosL1CDailyGriddedReader reader;

    @Before
    public void setUp() throws IOException {
        final ReaderContext readerContext = new ReaderContext();
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

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(5, coordinates.length);
            assertEquals(-179.8703155517578, coordinates[0].getLon(), 1e-8);
            assertEquals(-83.51713562011719, coordinates[0].getLat(), 1e-8);
            assertEquals(-179.8703155517578, coordinates[1].getLon(), 1e-8);
            assertEquals(83.51713562011719, coordinates[1].getLat(), 1e-8);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2016, 6, 10, 0, 0, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2016, 6, 10, 23, 59, 59, sensingStop);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            final TimeAxis timeAxis = timeAxes[0];
            assertTrue(timeAxis instanceof L3TimeAxis);
            final Geometry geometry = timeAxis.getGeometry();
            coordinates = geometry.getCoordinates();
            assertEquals(4, coordinates.length);
            assertEquals(-179.8703155517578, coordinates[0].getLon(), 1e-8);
            assertEquals(0.0, coordinates[0].getLat(), 1e-8);
            assertEquals(0.0, coordinates[3].getLon(), 1e-8);
            assertEquals(-83.51713562011719, coordinates[3].getLat(), 1e-8);

            TestUtil.assertCorrectUTCDate(2016, 6, 10, 0, 0, 0, timeAxis.getStartTime());
            TestUtil.assertCorrectUTCDate(2016, 6, 10, 23, 59, 59, timeAxis.getEndTime());

            assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_CDF3TD() throws IOException {
        final File file = getCDF3TDFile();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(1388, productSize.getNx());
            assertEquals(584, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_CDF3TA() throws IOException {
        final File file = getCDF3TAFile();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(-179.8703155517578, geoLocation.getX(), 1e-8);
            assertEquals(-83.51713562011719, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocations = pixelLocator.getPixelLocation(-179.8703155517578, -83.51713562011719);
            assertEquals(1, pixelLocations.length);
            assertEquals(0.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(0.5, pixelLocations[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(762.5, 404.5, null);
            assertEquals(17.766571044921875, geoLocation.getX(), 1e-8);
            assertEquals(22.638275146484375, geoLocation.getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(17.766571044921875, 22.638275146484375);
            assertEquals(1, pixelLocations.length);
            assertEquals(762.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(404.5, pixelLocations[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(1387.5, 583.5, null);
            assertEquals(179.8703155517578, geoLocation.getX(), 1e-8);
            assertEquals(83.51713562011719, geoLocation.getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(179.9, 83.6);
            assertEquals(1, pixelLocations.length);
            assertEquals(1387.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(583.5, pixelLocations[0].getY(), 1e-8);

            // check outside locations
            geoLocation = pixelLocator.getGeoLocation(-1, 0.5, null);
            assertNull(geoLocation);

            geoLocation = pixelLocator.getGeoLocation(22.5, 685.5, null);
            assertNull(geoLocation);

            pixelLocations = pixelLocator.getPixelLocation(116.7, -89.6);
            assertEquals(0, pixelLocations.length);

            pixelLocations = pixelLocator.getPixelLocation(116.7, 89.6);
            assertEquals(0, pixelLocations.length);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_CDF3TD() throws IOException, InvalidRangeException {
        // X_Swath, Grid_Point_Mask
        // BT_H(15), BT_V(15), BT_3(15), BT_4(15),
        // Pixel_Radiometric_Accuracy_H(15), Pixel_Radiometric_Accuracy_V(15), Pixel_Radiometric_Accuracy_3(15), Pixel_Radiometric_Accuracy_4(15)
        // Pixel_BT_Standard_Deviation_H(15), Pixel_BT_Standard_Deviation_V(15), Pixel_BT_Standard_Deviation_3(15), Pixel_BT_Standard_Deviation_4(15)
        // Incidence_Angle(15), Azimuth_Angle(15), Footprint_Axis1(15), Footprint_Axis2(15),
        // Xi(15), Eta(15), Nviews(15), Nb_RFI_Flags(15), Nb_SUN_Flags(15)
        // Days(15), UTC_Seconds(15), UTC_Microseconds(15)
        // 362 variables total

        final File file = getCDF3TAFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(338, variables.size());

            Variable variable = variables.get(0);
            assertEquals("X_Swath", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
            NCTestUtils.assertAttribute(variable, "_FillValue", "9.96921E36");
            NCTestUtils.assertAttribute(variable, "units", "m");
            NCTestUtils.assertAttribute(variable, "long_name", "Minimum distance of grid point to the sub satellite point track");

            variable = variables.get(3);
            assertEquals("BT_H_075", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());
            NCTestUtils.assertAttribute(variable, "_FillValue", "-32768");
            NCTestUtils.assertAttribute(variable, "units", "K");
            NCTestUtils.assertAttribute(variable, "long_name", "Angle class averaged Brightness temperature in H-pol over current Earth fixed grid point, obtained by polarisation rotation from L1c data");
            NCTestUtils.assertAttribute(variable, "add_offset", "200.0");
            NCTestUtils.assertAttribute(variable, "scale_factor", "0.0061037018951994385");

            variable = variables.get(38);
            assertEquals("BT_3_400", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());
            NCTestUtils.assertAttribute(variable, "_FillValue", "-32768");
            NCTestUtils.assertAttribute(variable, "units", "K");
            NCTestUtils.assertAttribute(variable, "long_name", "Angle class averaged Brightness temperature 3rd Stokes parameter over current Earth fixed grid point, obtained by polarisation rotation from L1c data");
            NCTestUtils.assertAttribute(variable, "add_offset", "0.0");
            NCTestUtils.assertAttribute(variable, "scale_factor", "0.0015259254737998596");

            variable = variables.get(165);
            assertEquals("Pixel_BT_Standard_Deviation_4_425", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());
            NCTestUtils.assertAttribute(variable, "_FillValue", "-32768");
            NCTestUtils.assertAttribute(variable, "units", "K");
            NCTestUtils.assertAttribute(variable, "long_name", "Angle class BT standard deviation in the Brightness Temperature presented in the previous field, extracted in the direction of the pixel ");
            NCTestUtils.assertAttribute(variable, "add_offset", "25.0");
            NCTestUtils.assertAttribute(variable, "scale_factor", "7.629627368999298E-4");

            variable = variables.get(210);
            assertEquals("Footprint_Axis1_575", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());
            NCTestUtils.assertAttribute(variable, "_FillValue", "-32768");
            NCTestUtils.assertAttribute(variable, "units", "km");
            NCTestUtils.assertAttribute(variable, "long_name", "Angle class averaged Elliptical footprint major semi-axis value");
            NCTestUtils.assertAttribute(variable, "add_offset", "50.0");
            NCTestUtils.assertAttribute(variable, "scale_factor", "0.0015259254737998596");

            variable = variables.get(290);
            assertEquals("Nb_SUN_Flags_400", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());
            NCTestUtils.assertAttribute(variable, "_FillValue", "-32768");
            NCTestUtils.assertAttribute(variable, "units", "NA");
            NCTestUtils.assertAttribute(variable, "long_name", "Number of views flagged as potentially contaminated by Sun used to compute Angle class averages");

            variable = variables.get(337);
            assertEquals("UTC_Microseconds_625", variable.getShortName());
            assertEquals(DataType.INT, variable.getDataType());
            NCTestUtils.assertAttribute(variable, "_FillValue", "-2147483647");
            NCTestUtils.assertAttribute(variable, "units", "10-6s");
            NCTestUtils.assertAttribute(variable, "long_name", "UTC Time at which the averaged BT was taken, in EE CFI transport time format. Microseconds");
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator_CDF3TD() throws IOException {
        final File file = getCDF3TDFile();

        try {
            reader.open(file);

            final PixelLocator subScenePixelLocator = reader.getSubScenePixelLocator(null);// geometry is not used here tb 2022-09-29
            final PixelLocator pixelLocator = reader.getPixelLocator();

            assertSame(pixelLocator, subScenePixelLocator);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_CDF3TA() throws IOException {
        final File file = getCDF3TAFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1465599546734L, timeLocator.getTimeFor(0, 0));
            assertEquals(1465576328309L, timeLocator.getTimeFor(100, 100));
            assertEquals(1465570699654L, timeLocator.getTimeFor(200, 200));
            assertEquals(-1L, timeLocator.getTimeFor(300, 300));
            assertEquals(-1L, timeLocator.getTimeFor(340, 300));
            assertEquals(-1L, timeLocator.getTimeFor(341, 300));
            assertEquals(1465559087142L, timeLocator.getTimeFor(342, 300));
            assertEquals(1465559079942L, timeLocator.getTimeFor(343, 300));
            assertEquals(1465559037942L, timeLocator.getTimeFor(350, 300));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_CDF3TD() throws IOException, InvalidRangeException {
        final File file = getCDF3TDFile();

        try {
            reader.open(file);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(166, 67, new Interval(5, 3));
            assertEquals(15, acquisitionTime.getSize());

            // run over a section that covers a swath border tb 2022-10-13
            NCTestUtils.assertValueAt(-2147483647, 0, 1, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1511142933, 2, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1511142929, 3, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1511142923, 4, 1, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_CDF3TA_outsideTop() throws IOException, InvalidRangeException {
        final File file = getCDF3TAFile();

        try {
            reader.open(file);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(416, 0, new Interval(3, 5));
            assertEquals(15, acquisitionTime.getSize());

            NCTestUtils.assertValueAt(-2147483647, 1, 0, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1465575514, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1465575510, 1, 3, acquisitionTime);
            NCTestUtils.assertValueAt(1465569571, 1, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_2D_CDF3TD() throws IOException, InvalidRangeException {
        final File file = getCDF3TDFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(962, 175, new Interval(3, 3), "X_Swath");
            NCTestUtils.assertValueAt(495649.46875, 0, 0, array);
            NCTestUtils.assertValueAt(521634.0625, 1, 0, array);
            NCTestUtils.assertValueAt(517200.75, 1, 1, array);
            NCTestUtils.assertValueAt(543231.25, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_3D_CDF3TD() throws IOException, InvalidRangeException {
        final File file = getCDF3TDFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(963, 284, new Interval(3, 3), "BT_H_025");
            NCTestUtils.assertValueAt(-16530, 0, 1, array);
            NCTestUtils.assertValueAt(-16831, 1, 1, array);
            NCTestUtils.assertValueAt(-32768, 2, 1, array);
            NCTestUtils.assertValueAt(-17140, 0, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_2D_CDF3TA_outsideRight() throws IOException, InvalidRangeException {
        final File file = getCDF3TAFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(1387, 112, new Interval(3, 3), "Grid_Point_Mask");
            NCTestUtils.assertValueAt(25, 0, 1, array);
            NCTestUtils.assertValueAt(9, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_3D_CDF3TA_outsideBottom() throws IOException, InvalidRangeException {
        final File file = getCDF3TAFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(849, 583, new Interval(3, 3), "Pixel_Radiometric_Accuracy_V_175");
            NCTestUtils.assertValueAt(-28288, 1, 0, array);
            NCTestUtils.assertValueAt(-26149, 1, 1, array);
            NCTestUtils.assertValueAt(-32768, 1, 2, array);
            NCTestUtils.assertValueAt(-28250, 2, 0, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_3D_CDF3TD() throws IOException, InvalidRangeException {
        final File file = getCDF3TDFile();

        try {
            reader.open(file);

            final Array array = reader.readScaled(673, 310, new Interval(3, 3), "Pixel_BT_Standard_Deviation_3_225");
            NCTestUtils.assertValueAt(4.222235786004212, 0, 1, array);
            NCTestUtils.assertValueAt(2.3056733909115863, 1, 1, array);
            NCTestUtils.assertValueAt(2.92062135685293, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_3D_CDF3TA_outsideLeft() throws IOException, InvalidRangeException {
        final File file = getCDF3TAFile();

        try {
            reader.open(file);

            final Array array = reader.readScaled(0, 225, new Interval(3, 3), "Incidence_Angle_275");
            NCTestUtils.assertValueAt(-0.0013733329264198346, 0, 1, array);
            NCTestUtils.assertValueAt(28.25907162694174, 1, 1, array);
            NCTestUtils.assertValueAt(29.65300454725791, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    private File getCDF3TAFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"miras-smos-CDF3TA", "re07", "2016", "162", "SM_RE07_MIR_CDF3TA_20160610T000000_20160610T235959_330_001_7.tgz"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getCDF3TDFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"miras-smos-CDF3TD", "re07", "2017", "324", "SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.tgz"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
