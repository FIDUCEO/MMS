/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

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
import com.bc.fiduceo.reader.*;
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
public class CALIOP_SST_WP100_CLay_Reader_IO_Test {

    private Path testDataDirectory;
    private CALIOP_SST_WP100_CLay_Reader reader;
    private File caliopFile;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory().toPath();

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new CALIOP_SST_WP100_CLay_Reader(readerContext, new CaliopUtils());
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
        TestUtil.assertCorrectUTCDate(2008, 5, 30, 11, 0, 50, sensingStart);

        final Date sensingStop = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2008, 5, 30, 11, 47, 22, sensingStop);

        final NodeType nodeType = acquisitionInfo.getNodeType();
        assertEquals(NodeType.UNDEFINED, nodeType);

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);

        Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(125, coordinates.length);

        assertEquals(-126.55035355587347, coordinates[0].getLon(), 1e-6);
        assertEquals(-126.55035355587347, coordinates[124].getLon(), 1e-6);

        assertEquals(56.50688283056632, coordinates[0].getLat(), 1e-6);
        assertEquals(56.50688283056632, coordinates[124].getLat(), 1e-6);

        assertEquals(49.23701016344526, coordinates[61].getLon(), 1e-5);
        assertEquals(-67.03246729901173, coordinates[61].getLat(), 1e-5);

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertEquals(1, timeAxes.length);

        final TimeAxis timeAxis = timeAxes[0];
        coordinates = timeAxis.getGeometry().getCoordinates();
        final Date time = timeAxes[0].getTime(coordinates[0]);
        TestUtil.assertCorrectUTCDate(2008, 5, 30, 11, 0, 50, time);
    }

    @Test
    public void getProductSize()  {
        final Dimension productSize = reader.getProductSize();
        assertEquals(1, productSize.getNx());
        assertEquals(3744, productSize.getNy());
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
        assertEquals(1212145223799L, time);
        TestUtil.assertCorrectUTCDate(2008, 5, 30, 11, 0, 23, new Date(time));

        time = timeLocator.getTimeFor(0, productSize.getNy() / 2);
        assertEquals(1212146616474L, time);
        TestUtil.assertCorrectUTCDate(2008, 5, 30, 11, 23, 36, new Date(time));

        time = timeLocator.getTimeFor(0, productSize.getNy() - 1);
        assertEquals(1212148008367L, time);
        TestUtil.assertCorrectUTCDate(2008, 5, 30, 11, 46, 48, new Date(time));
    }

    @Test
    public void getVariables() throws Exception {
        final List<Variable> variables = reader.getVariables();
        assertNotNull(variables);
        assertEquals(22, variables.size());

        final Expectation[] expectations = getVariables_Expectations();
        assertEquals(variables.size(), expectations.length);
        for (int i = 0; i < variables.size(); i++) {
            final String pos = "Problem at position: " + i;

            Variable variable = variables.get(i);
            assertEquals(pos, expectations[i].name, variable.getShortName());
            assertEquals(pos, expectations[i].dataType, variable.getDataType());
            assertEquals(pos, 2, variable.getRank());
            assertArrayEquals(pos, expectations[i].shape, variable.getShape());
            assertEquals(pos, expectations[i].attributes, variable.getAttributes());
        }

        Array profile_id = reader.readRaw(0, 5, new Interval(1, 5), "Profile_ID");
        int[] actuals = (int[]) profile_id.get1DJavaArray(Integer.TYPE);
        int[] expecteds = {37200 + 7, 37215 + 7, 37230 + 7, 37245 + 7, 37260 + 7};
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void getRegEx() {
        final String YYYY = "(19[7-9]\\d|20[0-7]\\d)";
        final String MM = "(0[1-9]|1[0-2])";
        final String DD = "(0[1-9]|[12]\\d|3[01])";
        final String hh = "([01]\\d|2[0-3])";
        final String mm = "[0-5]\\d";
        final String ss = mm;
        final String prefix = "CAL_LID_L2_05kmCLay-Standard-V4-10\\.";
        final String expected = prefix + YYYY + "-" + MM + "-" + DD + "T" + hh + "-" + mm + "-" + ss + "Z[DN]\\.hdf";
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher;
        // valid day                                                                      ⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-02T23-37-04ZD.hdf");
        assertTrue(matcher.matches());
        // valid night                                                                    ⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-02T23-37-04ZN.hdf");
        assertTrue(matcher.matches());

        // invalid year                                                 ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2080-01-02T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid month                                                   ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-00-02T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid month                                                   ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-13-02T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid day                                                        ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-00T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid day                                                        ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-32T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid hour                                                          ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-02T24-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid minute                                                           ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-02T23-60-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid second                                                              ⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-02T23-37-60ZD.hdf");
        assertThat(matcher.matches(), is(false));

        // invalid UTC "T" character                                            ⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-02_23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid UTC "Z" character                                                     ⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-02T23-37-04UD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid Day/Naight "D"/"N" character                                           ⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-02_23-37-04ZI.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid dot character                                          ⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011.01-02T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid beginn          ⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓⇓
        matcher = pattern.matcher("C_L_L_D_L-_0_k_C_ay-S-a-d-r---4-1-.2011-01-02T23-37-04ZD.hdf");
        assertThat(matcher.matches(), is(false));
        // invalid end                                                                      ⇓⇓⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-02T23-37-04ZD.kkk");
        assertThat(matcher.matches(), is(false));
    }

    @Test
    public void getPixelLocator() throws Exception {
        final PixelLocator pixelLocator = reader.getPixelLocator();

        assertThat(pixelLocator, is(instanceOf(PixelLocatorX1Yn.class)));

        final Point2D geoLocation = pixelLocator.getGeoLocation(0, 18, null);
        assertThat(geoLocation.getX(), is(closeTo(-126.950264, 1e-6)));
        assertThat(geoLocation.getY(), is(closeTo(55.694767, 1e-6)));

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

//    @Test
//    public void ensureValidInterval() {
//        final int invalidX = 2;
//        final int validY = 12;
//        final Interval invalidInterval = new Interval(invalidX, validY);
//        try {
//            reader.readRaw(0, 44, invalidInterval, "");
//            fail("Exception expected");
//        } catch (Exception expected) {
//            assertThat(expected, is(instanceOf(RuntimeException.class)));
//            assertThat(expected.getMessage(), is("An interval with x > 1 is not allowed."));
//        }
//        try {
//            reader.readScaled(0, 44, invalidInterval, "");
//            fail("Exception expected");
//        } catch (Exception expected) {
//            assertThat(expected, is(instanceOf(RuntimeException.class)));
//            assertThat(expected.getMessage(), is("An interval with x > 1 is not allowed."));
//        }
//        try {
//            reader.readAcquisitionTime(0, 44, invalidInterval);
//            fail("Exception expected");
//        } catch (Exception expected) {
//            assertThat(expected, is(instanceOf(RuntimeException.class)));
//            assertThat(expected.getMessage(), is("An interval with x > 1 is not allowed."));
//        }
//    }

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
                37732, 37747, 37762, 37777, 37792
        };
        assertThat(a1.get1DJavaArray(int.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Latitude");
        a2 = reader.readScaled(0, 40, interval, "Latitude");
        assertThat(a1.getDataType(), is(equalTo(DataType.FLOAT)));
        expected = new float[]{
                54.82056F, 54.776577F, 54.732635F, 54.68885F, 54.64514F
        };
        assertThat(a1.get1DJavaArray(float.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Longitude");
        a2 = reader.readScaled(0, 40, interval, "Longitude");
        assertThat(a1.getDataType(), is(equalTo(DataType.FLOAT)));
        expected = new float[]{
                -127.40832F, -127.43086F, -127.453285F, -127.475716F, -127.49809F
        };
        assertThat(a1.get1DJavaArray(float.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Profile_Time");
        a2 = reader.readScaled(0, 40, interval, "Profile_Time");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                4.8629888506920004E8, 4.862988858132E8, 4.8629888655719995E8, 4.8629888730120003E8, 4.8629888804420006E8
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Profile_UTC_Time");
        a2 = reader.readScaled(0, 40, interval, "Profile_UTC_Time");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                80530.4592484861, 80530.45925709722, 80530.45926570833, 80530.45927431945, 80530.45928291898
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Day_Night_Flag");
        a2 = reader.readScaled(0, 40, interval, "Day_Night_Flag");
        assertThat(a1.getDataType(), is(equalTo(DataType.BYTE)));
        expected = new short[]{
                1, 1, 1, 1, 1
        };
        assertThat(a1.get1DJavaArray(short.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Spacecraft_Position_x");
        a2 = reader.readScaled(0, 40, interval, "Spacecraft_Position_x");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                -2458.3919067555635, -2462.359185976937, -2466.3252194802126, -2470.2900046437567, -2474.248212170256
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Spacecraft_Position_y");
        a2 = reader.readScaled(0, 40, interval, "Spacecraft_Position_y");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                -3231.1852607264345, -3233.7341416710115, -3236.2805931692683, -3238.824613848884, -3241.362787738696
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Spacecraft_Position_z");
        a2 = reader.readScaled(0, 40, interval, "Spacecraft_Position_z");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                5793.67685540881, 5790.576515539656, 5787.472569623185, 5784.365019547965, 5781.2580514207675
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));
    }

    @Test
    public void readAcquisitionTime() throws Exception {
        //preparation
        final int x = 0;
        final int y = 4;
        final Interval interval = new Interval(1, 13);
        final Array pt = reader.readRaw(x, y, interval, "Profile_Time");
        final int[] expectedShape = {13, 1};
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
        assertThat(fillValueCount, is(2));
    }

    @Test
    public void readDataPortionFromEachVariable() throws Exception {
        Array actual;
        Array expected;

        // ---------------------------------------------------------
        // Number_Layers_Found
        actual = reader.readRaw(0, 244, new Interval(1, 5), "Number_Layers_Found").copy();
        expected = Array.factory(DataType.BYTE, new int[]{5}, new byte[]{
                4, 5, 3, 1, 3
        });
        compare(expected, actual);
        assertArrayEquals((byte[]) expected.getStorage(), (byte[]) actual.getStorage());

        // ---------------------------------------------------------
        // Column_Feature_Fraction
        actual = reader.readRaw(0, 244, new Interval(1, 5), "Column_Feature_Fraction").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5}, new float[]{
                0.10399269F, 0.102987945F, 0.09243798F, 0.096457034F, 0.099471316F

        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // FeatureFinderQC
        actual = reader.readRaw(0, 244, new Interval(1, 5), "FeatureFinderQC").copy();
        expected = Array.factory(DataType.SHORT, new int[]{5}, new short[]{
                0, 0, 0, 0, 0
        });
        compare(expected, actual);
        assertArrayEquals((short[]) expected.getStorage(), (short[]) actual.getStorage());

        // ---------------------------------------------------------
        // Feature_Classification_Flags
        actual = reader.readRaw(5, 244, new Interval(10, 5), "Feature_Classification_Flags").copy();
        expected = Array.factory(DataType.SHORT, new int[]{5, 10}, toShorts(new int[]{
                36266, 27098, 34818, 25626, 1, 1, 1, 1, 1, 1,
                36266, 28090, 27098, 34818, 25626, 1, 1, 1, 1, 1,
                27098, 34818, 25602, 1, 1, 1, 1, 1, 1, 1,
                27098, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                27098, 34818, 26066, 1, 1, 1, 1, 1, 1, 1
        }));
        compare(expected, actual);
        assertArrayEquals((short[]) expected.getStorage(), (short[]) actual.getStorage());

        // ---------------------------------------------------------
        // ExtinctionQC_532
        actual = reader.readRaw(5, 244, new Interval(10, 5), "ExtinctionQC_532").copy();
        expected = Array.factory(DataType.SHORT, new int[]{5, 10}, toShorts(new int[]{
                0, 2, 0, 18, 32768, 32768, 32768, 32768, 32768, 32768,
                0, 0, 0, 0, 18, 32768, 32768, 32768, 32768, 32768,
                0, 0, 18, 32768, 32768, 32768, 32768, 32768, 32768, 32768,
                0, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768,
                0, 0, 82, 32768, 32768, 32768, 32768, 32768, 32768, 32768
        }));
        compare(expected, actual);
        assertArrayEquals((short[]) expected.getStorage(), (short[]) actual.getStorage());

        // ---------------------------------------------------------
        // CAD_Score
        actual = reader.readRaw(5, 244, new Interval(10, 5), "CAD_Score").copy();
        expected = Array.factory(DataType.BYTE, new int[]{5, 10}, new byte[]{
                21, 99, 2, 5, -127, -127, -127, -127, -127, -127,
                21, 99, 100, 2, 3, -127, -127, -127, -127, -127,
                100, 0, 3, -127, -127, -127, -127, -127, -127, -127,
                99, -127, -127, -127, -127, -127, -127, -127, -127, -127,
                93, 0, 62, -127, -127, -127, -127, -127, -127, -127

        });
        compare(expected, actual);
        assertArrayEquals((byte[]) expected.getStorage(), (byte[]) actual.getStorage());

        // ---------------------------------------------------------
        // Layer_IAB_QA_Factor
        actual = reader.readRaw(5, 244, new Interval(10, 5), "Layer_IAB_QA_Factor").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                0.9954777F, 0.9826256F, 0.3578414F, 0.25575027F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.9954777F, 0.99504F, 0.9587685F, 0.3578414F, 0.36825225F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.9775629F, 0.5977695F, 0.363961F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.98636895F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.98466104F, 0.5977695F, 0.6317817F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Opacity_Flag
        actual = reader.readRaw(5, 244, new Interval(10, 5), "Opacity_Flag").copy();
        expected = Array.factory(DataType.BYTE, new int[]{5, 10}, new byte[]{
                0, 0, 0, 1, 99, 99, 99, 99, 99, 99,
                0, 0, 0, 0, 1, 99, 99, 99, 99, 99,
                0, 0, 1, 99, 99, 99, 99, 99, 99, 99,
                0, 99, 99, 99, 99, 99, 99, 99, 99, 99,
                0, 0, 1, 99, 99, 99, 99, 99, 99, 99

        });
        compare(expected, actual);
        assertArrayEquals((byte[]) expected.getStorage(), (byte[]) actual.getStorage());

        // ---------------------------------------------------------
        // Ice_Water_Path
        actual = reader.readRaw(5, 244, new Interval(10, 5), "Ice_Water_Path").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                1.5912719f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                1.5912719f, 6.8650675f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Ice_Water_Path_Uncertainty
        actual = reader.readRaw(5, 244, new Interval(10, 5), "Ice_Water_Path_Uncertainty").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                0.22120453F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.22120453F, 0.73038733F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Feature_Optical_Depth_532
        actual = reader.readRaw(5, 244, new Interval(10, 5), "Feature_Optical_Depth_532").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                0.030404849F, 3.8703005F, 0.34442773F, 4.399083F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.030404849F, 0.1299083F, 2.5036433F, 0.34442773F, 4.181742F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                1.704152F, 0.009845099F, 4.1139874F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.49172032F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.29203787F, 0.009845099F, 16.28734F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Feature_Optical_Depth_Uncertainty_532
        actual = reader.readRaw(5, 244, new Interval(10, 5), "Feature_Optical_Depth_Uncertainty_532").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                0.008633511F, 0.674406F, 0.12225014F, 4.768365F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.008633511F, 0.034799386F, 0.40647134F, 0.12225014F, 10.782989F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.2678689F, 0.006549329F, 1.0313892F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.07666045F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                0.046314657F, 0.006549329F, -29.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Layer_Top_Altitude
        actual = reader.readRaw(5, 244, new Interval(10, 5), "Layer_Top_Altitude").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                8.3605995F, 4.9626184F, 4.244103F, 1.4299157F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                8.3605995F, 7.9564347F, 4.842866F, 4.244103F, 1.4299157F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                4.842866F, 3.9746592F, 1.4299157F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                4.7829895F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                4.872804F, 3.9746592F, 1.4598538F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Layer_Base_Altitude
        actual = reader.readRaw(5, 244, new Interval(10, 5), "Layer_Base_Altitude").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                7.267857F, 4.1542883F, 3.6752777F, 1.0107814F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                7.267857F, 7.2977953F, 4.094412F, 3.6752777F, 0.92096686F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                3.914783F, 3.735154F, 0.8012142F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                3.705216F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F,
                3.8249686F, 3.735154F, 0.6515234F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F, -9999.0F
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Profile_ID
        actual = reader.readRaw(0, 244, new Interval(1, 5), "Profile_ID").copy();
        expected = Array.factory(DataType.INT, new int[]{5}, new int[]{
                40792, 40807, 40822, 40837, 40852
        });
        compare(expected, actual);
        assertArrayEquals((int[]) expected.getStorage(), (int[]) actual.getStorage());

        // ---------------------------------------------------------
        // Latitude
        actual = reader.readRaw(0, 244, new Interval(1, 5), "Latitude").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5}, new float[]{
                45.843784F, 45.799698F, 45.755344F, 45.711246F, 45.666977F
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Longitude
        actual = reader.readRaw(0, 244, new Interval(1, 5), "Longitude").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5}, new float[]{
                -131.29697F, -131.31317F, -131.32945F, -131.3457F, -131.36206F
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Profile_Time
        actual = reader.readRaw(0, 244, new Interval(1, 5), "Profile_Time").copy();
        expected = Array.factory(DataType.DOUBLE, new int[]{5}, new double[]{
                4.862990368342E8, 4.862990375782E8, 4.8629903832220006E8, 4.862990390662E8, 4.862990398102E8
        });
        compare(expected, actual);
        assertArrayEquals((double[]) expected.getStorage(), (double[]) actual.getStorage(), 1e-12);

        // ---------------------------------------------------------
        // Profile_UTC_Time
        actual = reader.readRaw(0, 244, new Interval(1, 5), "Profile_UTC_Time").copy();
        expected = Array.factory(DataType.DOUBLE, new int[]{5}, new double[]{
                80530.46100502546, 80530.46101363658, 80530.46102224769, 80530.4610308588, 80530.46103946991
        });
        compare(expected, actual);
        assertArrayEquals((double[]) expected.getStorage(), (double[]) actual.getStorage(), 1e-12);

        // ---------------------------------------------------------
        // Spacecraft_Position_x
        actual = reader.readRaw(0, 244, new Interval(1, 5), "Spacecraft_Position_x").copy();
        expected = Array.factory(DataType.DOUBLE, new int[]{5}, new double[]{
                -3238.1232287620387, -3241.7810586296328, -3245.4371029142653, -3249.0913590425157, -3252.7438244406785
        });
        compare(expected, actual);
        assertArrayEquals((double[]) expected.getStorage(), (double[]) actual.getStorage(), 1e-12);

        // ---------------------------------------------------------
        // Spacecraft_Position_y
        actual = reader.readRaw(0, 244, new Interval(1, 5), "Spacecraft_Position_y").copy();
        expected = Array.factory(DataType.DOUBLE, new int[]{5}, new double[]{
                -3698.935030599059, -3700.9609798573824, -3702.9842430029626, -3705.0048189428885, -3707.02270658471
        });
        compare(expected, actual);
        assertArrayEquals((double[]) expected.getStorage(), (double[]) actual.getStorage(), 1e-12);

        // ---------------------------------------------------------
        // Spacecraft_Position_z
        actual = reader.readRaw(0, 244, new Interval(1, 5), "Spacecraft_Position_z").copy();
        expected = Array.factory(DataType.DOUBLE, new int[]{5}, new double[]{
                5089.42880100277, 5085.635729484134, 5081.839488903023, 5078.040081538584, 5074.237509669909
        });
        compare(expected, actual);
        assertArrayEquals((double[]) expected.getStorage(), (double[]) actual.getStorage(), 1e-12);
    }

    private short[] toShorts(int[] st) {
        short[] shorts = new short[st.length];
        for (int i = 0; i < st.length; i++) {
            shorts[i] = (short) st[i];
        }
        return shorts;
    }

    private void compare(Array expected, Array actual) {
        assertEquals(expected.getDataType(), actual.getDataType());
        assertEquals(expected.getSize(), actual.getSize());
        assertArrayEquals(expected.getShape(), actual.getShape());
    }

    private File getCaliopFile() {
        final Path relPath = Paths.get("caliop_clay-cal", "4.10", "2008", "05", "30", "CAL_LID_L2_05kmCLay-Standard-V4-10.2008-05-30T11-00-51ZN.hdf");
        final Path testFilePath = testDataDirectory.resolve(relPath);
        assertThat(Files.exists(testFilePath), is(true));
        return testFilePath.toFile();
    }

    private Expectation[] getVariables_Expectations() {
        return new Expectation[]{
                /* idx: 0 */
                new Expectation("Number_Layers_Found", DataType.BYTE, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Int_8"),
                        new Attribute("valid_range", "0...10"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (byte) -127))),
                /* idx: 1 */
                new Expectation("Column_Feature_Fraction", DataType.FLOAT, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...1.0"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 2 */
                new Expectation("FeatureFinderQC", DataType.SHORT, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("_Unsigned", "true"),
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "UInt_16"),
                        new Attribute("valid_range", "0...32767"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (short) -1))),
                /* idx: 3 */
                new Expectation("Feature_Classification_Flags", DataType.SHORT, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("_Unsigned", "true"),
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "UInt_16"),
                        new Attribute("valid_range", "1...49146"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (short) -1))),
                /* idx: 4 */
                new Expectation("ExtinctionQC_532", DataType.SHORT, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("_Unsigned", "true"),
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "UInt_16"),
                        new Attribute("valid_range", "0...32768"),
                        new Attribute("fillvalue", (short) -32768),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (short) -32768))),
                /* idx: 5 */
                new Expectation("CAD_Score", DataType.BYTE, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Int_8"),
                        new Attribute("valid_range", "-101...106"),
                        new Attribute("fillvalue", (byte) -127),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (byte) -127))),
                /* idx: 6 */
                new Expectation("Layer_IAB_QA_Factor", DataType.FLOAT, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...1.0"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 7 */
                new Expectation("Opacity_Flag", DataType.BYTE, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Int_8"),
                        new Attribute("valid_range", "0...1"),
                        new Attribute("fillvalue", (byte) 99),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (byte) 99))),
                /* idx: 8 */
                new Expectation("Ice_Water_Path", DataType.FLOAT, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("units", "gram per square meter"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...200.0"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 9 */
                new Expectation("Ice_Water_Path_Uncertainty", DataType.FLOAT, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("units", "gram per square meter"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...99.99"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 10 */
                new Expectation("Feature_Optical_Depth_532", DataType.FLOAT, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...5.0"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 11 */
                new Expectation("Feature_Optical_Depth_Uncertainty_532", DataType.FLOAT, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...TBD"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 12 */
                new Expectation("Layer_Top_Altitude", DataType.FLOAT, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("units", "kilometers"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "-0.5...30.1"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 13 */
                new Expectation("Layer_Base_Altitude", DataType.FLOAT, new int[]{3744, 10}, Arrays.asList(
                        new Attribute("units", "kilometers"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "-0.5...30.1"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 14 */
                new Expectation("Profile_ID", DataType.INT, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Int_32"),
                        new Attribute("valid_range", "1...3153600000"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.INT, false)))),
                /* idx: 15 */
                new Expectation("Latitude", DataType.FLOAT, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("units", "degrees"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "-90.0...90.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false)))),
                /* idx: 16 */
                new Expectation("Longitude", DataType.FLOAT, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("units", "degrees"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "-180.0...180.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false)))),
                /* idx: 17 */
                new Expectation("Profile_Time", DataType.DOUBLE, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("units", "seconds"),
                        new Attribute("format", "Float_64"),
                        new Attribute("valid_range", "4.204E8...1.072E9"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                /* idx: 18 */
                new Expectation("Profile_UTC_Time", DataType.DOUBLE, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Float_64"),
                        new Attribute("valid_range", "60426.0...261231.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                /* idx: 19 */
                new Expectation("Spacecraft_Position_x", DataType.DOUBLE, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("units", "kilometers"),
                        new Attribute("format", "Float_64"),
                        new Attribute("valid_range", "-8000.0...8000.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                /* idx: 20 */
                new Expectation("Spacecraft_Position_y", DataType.DOUBLE, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("units", "kilometers"),
                        new Attribute("format", "Float_64"),
                        new Attribute("valid_range", "-8000.0...8000.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                /* idx: 21 */
                new Expectation("Spacecraft_Position_z", DataType.DOUBLE, new int[]{3744, 1}, Arrays.asList(
                        new Attribute("units", "kilometers"),
                        new Attribute("format", "Float_64"),
                        new Attribute("valid_range", "-8000.0...8000.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                };
    }

    static class Expectation {

        final String name;
        final DataType dataType;
        final List<Attribute> attributes;
        final int[] shape;

        public Expectation(String name, DataType dataType, int[] shape, List<Attribute> attributes) {
            this.name = name;
            this.dataType = dataType;
            this.shape = shape;
            this.attributes = attributes;
        }
    }

}