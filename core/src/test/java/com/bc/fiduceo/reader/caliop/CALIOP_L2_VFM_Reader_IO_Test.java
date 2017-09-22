package com.bc.fiduceo.reader.caliop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.PixelLocatorX1Yn;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.TimeLocator_TAI1993Vector;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(IOTestRunner.class)
public class CALIOP_L2_VFM_Reader_IO_Test {

    private Path testDataDirectory;
    private CALIOP_L2_VFM_Reader reader;
    private File caliopFile;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory().toPath();
        reader = new CALIOP_L2_VFM_Reader(new GeometryFactory(GeometryFactory.Type.S2), new CaliopUtils());
        caliopFile = getCaliopFile();
        reader.open(caliopFile);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        //execution
        final AcquisitionInfo acquisitionInfo = reader.read();

        //verification
        assertNotNull(acquisitionInfo);

        final Date sensingStart = acquisitionInfo.getSensingStart();
        TestUtil.assertCorrectUTCDate(2011, 1, 2, 23, 37, 1, sensingStart);

        final Date sensingStop = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2011, 1, 3, 0, 29, 33, sensingStop);

        final NodeType nodeType = acquisitionInfo.getNodeType();
        assertEquals(NodeType.UNDEFINED, nodeType);

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);

        Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(125, coordinates.length);

        assertEquals(16.202174, coordinates[0].getLon(), 1e-6);
        assertEquals(16.202174, coordinates[124].getLon(), 1e-6);

        assertEquals(-61.959280, coordinates[0].getLat(), 1e-6);
        assertEquals(-61.959280, coordinates[124].getLat(), 1e-6);

        assertEquals(173.04454, coordinates[61].getLon(), 1e-5);
        assertEquals(71.76522, coordinates[61].getLat(), 1e-5);

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertEquals(1, timeAxes.length);

        final TimeAxis timeAxis = timeAxes[0];
        final Geometry taGeometry = timeAxis.getGeometry();
        coordinates = taGeometry.getCoordinates();
        final Date time = timeAxes[0].getTime(coordinates[0]);
        TestUtil.assertCorrectUTCDate(2011, 1, 2, 23, 37, 1, time);

        final Geometry intersection = boundingGeometry.getIntersection(taGeometry);
        assertNotNull(intersection);
        final Point[] coordinates2 = intersection.getCoordinates();
        for (int i = 0; i < coordinates.length; i++) {
            Point p1 = coordinates[i];
            Point p2 = coordinates2[i];
            assertEquals("pos: " + i, p1.toString(), p2.toString());
        }
    }

    @Test
    public void getProductSize() throws Exception {
        final Dimension productSize = reader.getProductSize();
        assertEquals(1, productSize.getNx());
        assertEquals(4224, productSize.getNy());
    }

    @Test
    public void getTimeLocator() throws Exception {
        //execution
        final TimeLocator timeLocator = reader.getTimeLocator();

        //verification
        assertNotNull(timeLocator);
        assertTrue(timeLocator instanceof TimeLocator_TAI1993Vector);

        final Dimension productSize = reader.getProductSize();

        long time = timeLocator.getTimeFor(0, 0);
        assertEquals(1294011394826L, time);
        TestUtil.assertCorrectUTCDate(2011, 1, 2, 23, 36, 34, new Date(time));

        time = timeLocator.getTimeFor(0, productSize.getNy() / 2);
        assertEquals(1294012966052L, time);

        time = timeLocator.getTimeFor(0, productSize.getNy() - 1);
        assertEquals(1294014536597L, time);
        TestUtil.assertCorrectUTCDate(2011, 1, 3, 0, 28, 56, new Date(time));
    }

    @Test
    public void getVariables() throws Exception {
        final List<Variable> variables = reader.getVariables();
        assertNotNull(variables);
        assertEquals(10, variables.size());

        final Expectation[] expectations = getVariables_Expectations();
        for (int i = 0; i < variables.size(); i++) {
            final String pos = "Problem at position: " + i;

            Variable variable = variables.get(i);
            assertEquals(pos, expectations[i].name, variable.getShortName());
            assertEquals(pos, expectations[i].dataType, variable.getDataType());
            assertEquals(pos, 2, variable.getRank());
            assertArrayEquals(pos, new int[]{4224, 1}, variable.getShape());
            assertEquals(pos, expectations[i].attributes, variable.getAttributes());
        }
    }

    @Test
    public void getRegEx() throws Exception {

        final String YYYY = "(19[7-9]\\d|20[0-7]\\d)";
        final String MM = "(0[1-9]|1[0-2])";
        final String DD = "(0[1-9]|[12]\\d|3[01])";
        final String hh = "([01]\\d|2[0-3])";
        final String mm = "[0-5]\\d";
        final String ss = mm;
        final String prefix = "CAL_LID_L2_VFM-Standard-V4-10\\.";
        final String expected = prefix + YYYY + "-" + MM + "-" + DD + "T" + hh + "-" + mm + "-" + ss + "Z[DN]\\.hdf";
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher;
        // valid day                                                                       ⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-04ZD.hdf");
        assertEquals(true, matcher.matches());
        // valid night                                                                     ⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-04ZN.hdf");
        assertEquals(true, matcher.matches());

        // invalid year                                            ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2080-01-02T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid month                                              ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-00-02T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid month                                              ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-13-02T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid day                                                   ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-00T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid day                                                   ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-32T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid hour                                                     ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T24-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid minute                                                      ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-60-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid second                                                         ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-60ZD.hdf");
        assertThat(matcher.matches(), is(false));

        // invalid UTC "T" character                                       ⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02_23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid UTC "Z" character                                                ⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-04UD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid Day/Naight "D"/"N" character                                      ⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02_23-37-04ZI.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid dot character                                     ⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011.01-02T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid beginn          ⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓
        matcher = pattern.matcher("C_L_L_D_L-_V-M-S-a-d-r---4-1-.2011-01-02T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid end                                                                 ⇓⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-04ZD.kkk");
        assertThat(matcher.matches(), is(false));
    }

    @Test
    public void getPixelLocator() throws Exception {
        final PixelLocator pixelLocator = reader.getPixelLocator();

        assertThat(pixelLocator, is(instanceOf(PixelLocatorX1Yn.class)));

        final Point2D geoLocation = pixelLocator.getGeoLocation(0, 18, null);
        assertThat(geoLocation.getX(), is(closeTo(15.64188, 1e-6)));
        assertThat(geoLocation.getY(), is(closeTo(-62.756924, 1e-6)));

        final Point2D[] pixelLocation = pixelLocator.getPixelLocation(geoLocation.getX(), geoLocation.getY());
        assertThat(pixelLocation, is(notNullValue()));
        assertThat(pixelLocation.length, is(1));
        assertThat(pixelLocation[0].getX(), is(0.5));
        assertThat(pixelLocation[0].getY(), is(18.5));
    }

    @Test
    public void getPixelLocator_sameInstance() throws Exception {
        final PixelLocator pixelLocator1 = reader.getPixelLocator();
        final PixelLocator pixelLocator2 = reader.getPixelLocator();
        final PixelLocator pixelLocator3 = reader.getPixelLocator();
        final PixelLocator pixelLocator4 = reader.getSubScenePixelLocator(null);

        assertThat(pixelLocator1, is(sameInstance(pixelLocator2)));
        assertThat(pixelLocator1, is(sameInstance(pixelLocator3)));
        assertThat(pixelLocator1, is(sameInstance(pixelLocator4)));
    }

    @Test
    public void getPixelLocator_isNotSameInstance_afterCloseAndReopenTheSameFile() throws Exception {
        final PixelLocator pixelLocator1 = reader.getPixelLocator();
        reader.close();
        reader.open(caliopFile);
        final PixelLocator pixelLocator2 = reader.getPixelLocator();

        assertThat(pixelLocator1, is(not(sameInstance(pixelLocator2))));
    }

    @Test
    public void ensureValidInterval() {
        final int invalidX = 2;
        final int validY = 12;
        final Interval invalidInterval = new Interval(invalidX, validY);
        try {
            reader.readRaw(0, 44, invalidInterval, "");
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(RuntimeException.class)));
            assertThat(expected.getMessage(), is("An interval with x > 1 is not allowed."));
        }
        try {
            reader.readScaled(0, 44, invalidInterval, "");
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(RuntimeException.class)));
            assertThat(expected.getMessage(), is("An interval with x > 1 is not allowed."));
        }
        try {
            reader.readAcquisitionTime(0, 44, invalidInterval);
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(RuntimeException.class)));
            assertThat(expected.getMessage(), is("An interval with x > 1 is not allowed."));
        }
    }

    @Test
    public void readRawAndScaled_() throws Exception {
        final Interval interval = new Interval(1, 5);
        Array a1;
        Array a2;
        Object expected;

        a1 = reader.readRaw(0, 40, interval, "Profile_ID");
        a2 = reader.readScaled(0, 40, interval, "Profile_ID");
        assertThat(a1.getDataType(), is(equalTo(DataType.INT)));
        expected = new int[]{
                    81502, 81517, 81532, 81547, 81562
        };
        assertThat(a1.get1DJavaArray(int.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Latitude");
        a2 = reader.readScaled(0, 40, interval, "Latitude");
        assertThat(a1.getDataType(), is(equalTo(DataType.FLOAT)));
        expected = new float[]{
                    -63.609745f, -63.651947f, -63.694492f, -63.736717f, -63.778946f
        };
        assertThat(a1.get1DJavaArray(float.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Longitude");
        a2 = reader.readScaled(0, 40, interval, "Longitude");
        assertThat(a1.getDataType(), is(equalTo(DataType.FLOAT)));
        expected = new float[]{
                    14.952398f, 14.917107f, 14.881485f, 14.84628f, 14.811039f
        };
        assertThat(a1.get1DJavaArray(float.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Profile_Time");
        a2 = reader.readScaled(0, 40, interval, "Profile_Time");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                    5.681650570962E8, 5.681650578392E8, 5.681650585832E8, 5.681650593271999E8, 5.681650600711999E8
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Profile_UTC_Time");
        a2 = reader.readScaled(0, 40, interval, "Profile_UTC_Time");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                    110102.98437611343, 110102.98438471297, 110102.98439332408, 110102.98440193519, 110102.9844105463
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Day_Night_Flag");
        a2 = reader.readScaled(0, 40, interval, "Day_Night_Flag");
        assertThat(a1.getDataType(), is(equalTo(DataType.SHORT)));
        expected = new short[]{
                    0, 0, 0, 0, 0
        };
        assertThat(a1.get1DJavaArray(short.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 192, new Interval(1, 11), "Land_Water_Mask");
        a2 = reader.readScaled(0, 192, new Interval(1, 11), "Land_Water_Mask");
        assertThat(a1.getDataType(), is(equalTo(DataType.BYTE)));
        expected = new byte[]{
                    7, 7, 2, 2, 2, 2, 2, 2, 2, 1, 1
        };
        assertThat(a1.get1DJavaArray(byte.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Spacecraft_Position_x");
        a2 = reader.readScaled(0, 40, interval, "Spacecraft_Position_x");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                    3090.2231291304124, 3086.1839169722384, 3082.137038848976, 3078.087931358017, 3074.0365973714943
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Spacecraft_Position_y");
        a2 = reader.readScaled(0, 40, interval, "Spacecraft_Position_y");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                    839.2475400800969, 836.1470677137711, 833.0423484852521, 829.937556835804, 826.8326952132286
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Spacecraft_Position_z");
        a2 = reader.readScaled(0, 40, interval, "Spacecraft_Position_z");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                    -6324.418475639274, -6326.80178776186, -6329.1843969925485, -6331.563091989386, -6333.9378710620185
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));
    }

    @Test
    public void readAcquisitionTime() throws Exception {
        final int x = 0;
        final int y = 2;
        final Interval interval = new Interval(1, 7);
        final Array pt = reader.readRaw(x, y, interval, "Profile_Time");
        final int[] expectedShape = {7, 1};
        assertThat(pt.getShape(), is(equalTo(expectedShape)));
        final List<Variable> variables = reader.getVariables();
        Number fillValue = null;
        for (Variable variable : variables) {
            if (variable.getShortName().equals("Profile_Time")) {
                fillValue = variable.findAttribute(NetCDFUtils.CF_FILL_VALUE_NAME).getNumericValue();
            }
        }
        assertThat(fillValue, is(notNullValue()));

        //execution
        final ArrayInt.D2 at = reader.readAcquisitionTime(x, y, interval);

        //verification
        assertThat(at.getDataType(), is(equalTo(DataType.INT)));
        assertThat(at.getShape(), is(equalTo(expectedShape)));
        int fillValueCount = 0;
        for (int i = 0; i < at.getSize(); i++) {
            final double ptVal = pt.getDouble(i);
            final int expected;
            if (fillValue.equals(ptVal)) {
                expected = NetCDFUtils.getDefaultFillValue(int.class).intValue();
                fillValueCount++;
            } else {
                expected = (int) Math.round(TimeUtils.tai1993ToUtcInstantSeconds(ptVal));
            }
            assertThat("Loop number " + i, at.getInt(i), is(expected));
        }
        assertThat(fillValueCount, is(1));
    }

    private File getCaliopFile() {
        final Path relPath = Paths.get("caliop_vfm-cal", "v4", "2011", "01", "02", "CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-04ZD.hdf");
        final Path testFilePath = testDataDirectory.resolve(relPath);
        assertThat(Files.exists(testFilePath), is(true));
        return testFilePath.toFile();
    }

    private Expectation[] getVariables_Expectations() {
        return new Expectation[]{
                    new Expectation("Profile_ID", DataType.INT, Arrays.asList(
                                new Attribute("units", "NoUnits"),
                                new Attribute("format", "Int_32"),
                                new Attribute("valid_range", "1...3153600000"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.INT, false)))),
                    new Expectation("Latitude", DataType.FLOAT, Arrays.asList(
                                new Attribute("units", "degrees"),
                                new Attribute("format", "Float_32"),
                                new Attribute("valid_range", "-90.0...90.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false)))),
                    new Expectation("Longitude", DataType.FLOAT, Arrays.asList(
                                new Attribute("units", "degrees"),
                                new Attribute("format", "Float_32"),
                                new Attribute("valid_range", "-180.0...180.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false)))),
                    new Expectation("Profile_Time", DataType.DOUBLE, Arrays.asList(
                                new Attribute("units", "seconds"),
                                new Attribute("format", "Float_64"),
                                new Attribute("valid_range", "4.204E8...1.072E9"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                    new Expectation("Profile_UTC_Time", DataType.DOUBLE, Arrays.asList(
                                new Attribute("units", "NoUnits"),
                                new Attribute("format", "Float_64"),
                                new Attribute("valid_range", "60426.0...261231.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                    new Expectation("Day_Night_Flag", DataType.SHORT, Arrays.asList(
                                new Attribute("_Unsigned", "true"),
                                new Attribute("units", "NoUnits"),
                                new Attribute("format", "UInt_16"),
                                new Attribute("valid_range", "0...1"),
                                new Attribute(NetCDFUtils.CF_FLAG_VALUES_NAME, Array.factory(new short[]{0, 1})),
                                new Attribute(NetCDFUtils.CF_FLAG_MEANINGS_NAME, "Day Night"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.SHORT, true)))),
                    new Expectation("Land_Water_Mask", DataType.BYTE, Arrays.asList(
                                new Attribute("units", "NoUnits"),
                                new Attribute("format", "Int_8"),
                                new Attribute("valid_range", "0...7"),
                                new Attribute("fillvalue", (byte) -9),
                                new Attribute(NetCDFUtils.CF_FLAG_VALUES_NAME, Array.factory(new byte[]{0, 1, 2, 3, 4, 5, 6, 7})),
                                new Attribute(NetCDFUtils.CF_FLAG_MEANINGS_NAME, "shallow_ocean land coastlines shallow_inland_water intermittent_water deep_inland_water continental_ocean deep_ocean"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (byte) -9))),
                    new Expectation("Spacecraft_Position_x", DataType.DOUBLE, Arrays.asList(
                                new Attribute("units", "kilometers"),
                                new Attribute("format", "Float_64"),
                                new Attribute("valid_range", "-8000.0...8000.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                    new Expectation("Spacecraft_Position_y", DataType.DOUBLE, Arrays.asList(
                                new Attribute("units", "kilometers"),
                                new Attribute("format", "Float_64"),
                                new Attribute("valid_range", "-8000.0...8000.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                    new Expectation("Spacecraft_Position_z", DataType.DOUBLE, Arrays.asList(
                                new Attribute("units", "kilometers"),
                                new Attribute("format", "Float_64"),
                                new Attribute("valid_range", "-8000.0...8000.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false))))
        };
    }

    static class Expectation {

        final String name;
        final DataType dataType;
        final List<Attribute> attributes;

        public Expectation(String name, DataType dataType, List<Attribute> attributes) {
            this.name = name;
            this.dataType = dataType;
            this.attributes = attributes;
        }
    }

}