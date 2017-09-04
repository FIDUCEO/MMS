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
public class CALIOP_SST_WP100_CLay_Reader_IO_Test {

    private Path testDataDirectory;
    private CALIOP_SST_WP100_CLay_Reader reader;
    private File caliopFile;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory().toPath();
        reader = new CALIOP_SST_WP100_CLay_Reader(new GeometryFactory(GeometryFactory.Type.S2));
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
        TestUtil.assertCorrectUTCDate(2008, 5, 29, 4, 28, 30, sensingStart);

        final Date sensingStop = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2008, 5, 29, 5, 20, 52, sensingStop);

        final NodeType nodeType = acquisitionInfo.getNodeType();
        assertEquals(NodeType.UNDEFINED, nodeType);

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);

        Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(125, coordinates.length);

        assertEquals(158.772269, coordinates[0].getLon(), 1e-6);
        assertEquals(158.772269, coordinates[124].getLon(), 1e-6);

        assertEquals(-66.879679, coordinates[0].getLat(), 1e-6);
        assertEquals(-66.879679, coordinates[124].getLat(), 1e-6);

        assertEquals(-41.094352, coordinates[61].getLon(), 1e-5);
        assertEquals(57.353959, coordinates[61].getLat(), 1e-5);

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertEquals(1, timeAxes.length);

        final TimeAxis timeAxis = timeAxes[0];
        coordinates = timeAxis.getGeometry().getCoordinates();
        final Date time = timeAxes[0].getTime(coordinates[0]);
        TestUtil.assertCorrectUTCDate(2008, 5, 29, 4, 28, 30, time);
    }

    @Test
    public void getProductSize() throws Exception {
        final Dimension productSize = reader.getProductSize();
        assertEquals(1, productSize.getNx());
        assertEquals(4208, productSize.getNy());
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
        assertEquals(1212035283809L, time);
        TestUtil.assertCorrectUTCDate(2008, 5, 29, 4, 28, 3, new Date(time));

        time = timeLocator.getTimeFor(0, productSize.getNy() / 2);
        assertEquals(1212036849041L, time);
        TestUtil.assertCorrectUTCDate(2008, 5, 29, 4, 54, 9, new Date(time));

        time = timeLocator.getTimeFor(0, productSize.getNy() - 1);
        assertEquals(1212038413552L, time);
        TestUtil.assertCorrectUTCDate(2008, 5, 29, 5, 20, 13, new Date(time));
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
        int[] expecteds = {107055 + 7, 107070 + 7, 107085 + 7, 107100 + 7, 107115 + 7};
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void getRegEx() throws Exception {

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
        assertEquals(true, matcher.matches());
        // valid night                                                                    ⇓
        matcher = pattern.matcher("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-01-02T23-37-04ZN.hdf");
        assertEquals(true, matcher.matches());

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
        assertThat(geoLocation.getX(), is(closeTo(157.919036, 1e-6)));
        assertThat(geoLocation.getY(), is(closeTo(-66.113822, 1e-6)));

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
                107587, 107602, 107617, 107632, 107647
        };
        assertThat(a1.get1DJavaArray(int.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Latitude");
        a2 = reader.readScaled(0, 40, interval, "Latitude");
        assertThat(a1.getDataType(), is(equalTo(DataType.FLOAT)));
        expected = new float[]{
                -65.272064F, -65.22999F, -65.18795F, -65.14603F, -65.10381F
        };
        assertThat(a1.get1DJavaArray(float.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Longitude");
        a2 = reader.readScaled(0, 40, interval, "Longitude");
        assertThat(a1.getDataType(), is(equalTo(DataType.FLOAT)));
        expected = new float[]{
                157.10263F, 157.06293F, 157.02338F, 156.9844F, 156.9454F
        };
        assertThat(a1.get1DJavaArray(float.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Profile_Time");
        a2 = reader.readScaled(0, 40, interval, "Profile_Time");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                4.861889450782E8, 4.8618894582220006E8, 4.861889465662E8, 4.861889473102E8, 4.8618894805420005E8
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Profile_UTC_Time");
        a2 = reader.readScaled(0, 40, interval, "Profile_UTC_Time");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                80529.18679488657, 80529.18680349768, 80529.18681210879, 80529.1868207199, 80529.18682933103
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Day_Night_Flag");
        a2 = reader.readScaled(0, 40, interval, "Day_Night_Flag");
        assertThat(a1.getDataType(), is(equalTo(DataType.BYTE)));
        expected = new short[]{
                0, 0, 0, 0, 0
        };
        assertThat(a1.get1DJavaArray(short.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Spacecraft_Position_x");
        a2 = reader.readScaled(0, 40, interval, "Spacecraft_Position_x");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                -2717.5980916358153, -2721.188001157519, -2724.7758326051103, -2728.361583807504, -2731.9452526266537
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Spacecraft_Position_y");
        a2 = reader.readScaled(0, 40, interval, "Spacecraft_Position_y");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                1132.1603809497437, 1135.9114650985234, 1139.662241136329, 1143.412706530299, 1147.1628587629582
        };
        assertThat(a1.get1DJavaArray(double.class), is(equalTo(expected)));
        assertThat(a1.getDataType(), is(equalTo(a2.getDataType())));
        assertThat(a1.getStorage(), is(equalTo(a2.getStorage())));

        a1 = reader.readRaw(0, 40, interval, "Spacecraft_Position_z");
        a2 = reader.readScaled(0, 40, interval, "Spacecraft_Position_z");
        assertThat(a1.getDataType(), is(equalTo(DataType.DOUBLE)));
        expected = new double[]{
                -6448.450232948256, -6446.275181017335, -6444.096140882298, -6441.913113910224, -6439.726101528853
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
        for (int i = 0; i < at.getSize(); i++) {
            final double ptVal = pt.getDouble(i);
            final int expected;
            if (fillValue.equals(ptVal)) {
                expected = NetCDFUtils.getDefaultFillValue(int.class).intValue();
            } else {
                expected = (int) Math.round(TimeUtils.tai1993ToUtcInstantSeconds(ptVal));
            }
            assertThat("Loop number " + i, at.getInt(i), is(expected));
        }
    }

    @Test
    public void readDataPortionFromEachVariable() throws Exception {
        Array actual;
        Array expected;

        // ---------------------------------------------------------
        // Number_Layers_Found
        actual = reader.readRaw(0, 10, new Interval(1, 5), "Number_Layers_Found").copy();
        expected = Array.factory(DataType.BYTE, new int[]{5}, new byte[]{
                5, 5, 3, 2, 3
        });
        compare(expected, actual);
        assertArrayEquals((byte[]) expected.getStorage(), (byte[]) actual.getStorage());

        // ---------------------------------------------------------
        // Column_Feature_Fraction
        actual = reader.readRaw(0, 10, new Interval(1, 5), "Column_Feature_Fraction").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5}, new float[]{
                0.15573786f, 0.15674262f, 0.17583312f, 0.17784262f, 0.17482835f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // FeatureFinderQC
        actual = reader.readRaw(0, 10, new Interval(1, 5), "FeatureFinderQC").copy();
        expected = Array.factory(DataType.SHORT, new int[]{5}, new short[]{
                0, 0, 0, 0, 0
        });
        compare(expected, actual);
        assertArrayEquals((short[]) expected.getStorage(), (short[]) actual.getStorage());

        // ---------------------------------------------------------
        // Feature_Classification_Flags
        actual = reader.readRaw(5, 10, new Interval(10, 5), "Feature_Classification_Flags").copy();
        expected = Array.factory(DataType.SHORT, new int[]{5, 10}, toShorts(new int[]{
                36282, 28090, 35258, 43450, 35258, 1, 1, 1, 1, 1,
                36282, 28090, 35258, 27066, 26066, 1, 1, 1, 1, 1,
                36282, 28090, 26074, 1, 1, 1, 1, 1, 1, 1,
                36282, 28090, 1, 1, 1, 1, 1, 1, 1, 1,
                28090, 35258, 27066, 1, 1, 1, 1, 1, 1, 1
        }));
        compare(expected, actual);
        assertArrayEquals((short[]) expected.getStorage(), (short[]) actual.getStorage());

        // ---------------------------------------------------------
        // ExtinctionQC_532
        actual = reader.readRaw(5, 10, new Interval(10, 5), "ExtinctionQC_532").copy();
        expected = Array.factory(DataType.SHORT, new int[]{5, 10}, toShorts(new int[]{
                0, 0, 0, 0, 0, 32768, 32768, 32768, 32768, 32768,
                0, 0, 0, 0, 0, 32768, 32768, 32768, 32768, 32768,
                0, 0, 0, 32768, 32768, 32768, 32768, 32768, 32768, 32768,
                0, 0, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768,
                0, 0, 0, 32768, 32768, 32768, 32768, 32768, 32768, 32768

        }));
        compare(expected, actual);
        assertArrayEquals((short[]) expected.getStorage(), (short[]) actual.getStorage());

        // ---------------------------------------------------------
        // CAD_Score
        actual = reader.readRaw(5, 10, new Interval(10, 5), "CAD_Score").copy();
        expected = Array.factory(DataType.BYTE, new int[]{5, 10}, new byte[]{
                99, 99, 98, 98, 98, -127, -127, -127, -127, -127,
                99, 99, 98, 97, 53, -127, -127, -127, -127, -127,
                99, 99, 99, -127, -127, -127, -127, -127, -127, -127,
                99, 99, -127, -127, -127, -127, -127, -127, -127, -127,
                99, 99, 98, -127, -127, -127, -127, -127, -127, -127
        });
        compare(expected, actual);
        assertArrayEquals((byte[]) expected.getStorage(), (byte[]) actual.getStorage());

        // ---------------------------------------------------------
        // Layer_IAB_QA_Factor
        actual = reader.readRaw(5, 10, new Interval(10, 5), "Layer_IAB_QA_Factor").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                0.995821f, 0.99089766f, 0.6696708f, 0.6261782f, 0.6061473f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.995821f, 0.9893216f, 0.6696708f, 0.6049977f, 0.5541164f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.995821f, 0.98823327f, 0.54189193f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.995821f, 0.995319f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.9956598f, 0.6136023f, 0.61971337f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Opacity_Flag
        actual = reader.readRaw(5, 10, new Interval(10, 5), "Opacity_Flag").copy();
        expected = Array.factory(DataType.BYTE, new int[]{5, 10}, new byte[]{
                0, 0, 0, 0, 0, 99, 99, 99, 99, 99,
                0, 0, 0, 0, 0, 99, 99, 99, 99, 99,
                0, 0, 0, 99, 99, 99, 99, 99, 99, 99,
                0, 0, 99, 99, 99, 99, 99, 99, 99, 99,
                0, 0, 0, 99, 99, 99, 99, 99, 99, 99
        });
        compare(expected, actual);
        assertArrayEquals((byte[]) expected.getStorage(), (byte[]) actual.getStorage());

        // ---------------------------------------------------------
        // Ice_Water_Path
        actual = reader.readRaw(5, 10, new Interval(10, 5), "Ice_Water_Path").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                2.8114026f, 65.97041f, 8.561217f, 7.5695524f, 25.962954f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                2.8114026f, 57.745724f, 8.561217f, 44.144547f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                2.8114026f, 118.50471f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                2.8114026f, 121.064964f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                48.9806f, 31.439783f, 44.92013f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Ice_Water_Path_Uncertainty
        actual = reader.readRaw(5, 10, new Interval(10, 5), "Ice_Water_Path_Uncertainty").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                0.27301234f, 4.4200163f, 1.6050873f, 1.5351523f, 4.3847594f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.27301234f, 3.5729249f, 1.6050873f, 5.090071f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.27301234f, 7.1709604f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.27301234f, 7.3925066f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                2.768608f, 2.612421f, 4.7174597f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Feature_Optical_Depth_532
        actual = reader.readRaw(5, 10, new Interval(10, 5), "Feature_Optical_Depth_532").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                0.065577f, 1.3179158f, 0.15606333f, 0.123959616f, 0.40751576f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.065577f, 1.1333562f, 0.15606333f, 0.709296f, 0.48402625f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.065577f, 1.9696243f, 0.5130597f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.065577f, 2.0486019f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.9650542f, 0.49028724f, 0.6899106f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Feature_Optical_Depth_Uncertainty_532
        actual = reader.readRaw(5, 10, new Interval(10, 5), "Feature_Optical_Depth_Uncertainty_532").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                0.017420875f, 0.33772564f, 0.04859861f, 0.03992325f, 0.12237338f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.017420875f, 0.2896439f, 0.04859861f, 0.19464017f, 0.13815889f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.017420875f, 0.5042736f, 0.14395729f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.017420875f, 0.52427757f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                0.24632958f, 0.12870522f, 0.18619548f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Layer_Top_Altitude
        actual = reader.readRaw(5, 10, new Interval(10, 5), "Layer_Top_Altitude").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                7.5373006f, 6.72897f, 5.3218765f, 4.0644736f, 3.6453395f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                7.5373006f, 6.2499595f, 5.3218765f, 4.3937936f, 1.3700393f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                7.5373006f, 6.339774f, 1.3700393f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                7.5373006f, 7.477424f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                7.4175477f, 4.1542883f, 3.5854633f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Layer_Base_Altitude
        actual = reader.readRaw(5, 10, new Interval(10, 5), "Layer_Base_Altitude").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5, 10}, new float[]{
                6.72897f, 5.3518147f, 4.6033607f, 3.4956486f, 2.6873183f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                6.72897f, 5.022495f, 4.6033607f, 2.6274421f, 1.2502867f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                6.72897f, 2.2083077f, 1.1604722f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                6.72897f, 2.2681842f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                4.1842265f, 2.3579986f, 2.238246f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Profile_ID
        actual = reader.readRaw(0, 10, new Interval(1, 5), "Profile_ID").copy();
        expected = Array.factory(DataType.INT, new int[]{5}, new int[]{
                107137, 107152, 107167, 107182, 107197
        });
        compare(expected, actual);
        assertArrayEquals((int[]) expected.getStorage(), (int[]) actual.getStorage());

        // ---------------------------------------------------------
        // Latitude
        actual = reader.readRaw(0, 10, new Interval(1, 5), "Latitude").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5}, new float[]{
                -66.5331f, -66.49134f, -66.44939f, -66.40748f, -66.365486f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Longitude
        actual = reader.readRaw(0, 10, new Interval(1, 5), "Longitude").copy();
        expected = Array.factory(DataType.FLOAT, new int[]{5}, new float[]{
                158.3462f, 158.30307f, 158.25969f, 158.21677f, 158.17372f
        });
        compare(expected, actual);
        assertArrayEquals((float[]) expected.getStorage(), (float[]) actual.getStorage(), 1e-6f);

        // ---------------------------------------------------------
        // Profile_Time
        actual = reader.readRaw(0, 10, new Interval(1, 5), "Profile_Time").copy();
        expected = Array.factory(DataType.DOUBLE, new int[]{5}, new double[]{
                4.861889227602E8, 4.861889235042E8, 4.8618892424820006E8, 4.861889249922E8, 4.861889257362E8
        });
        compare(expected, actual);
        assertArrayEquals((double[]) expected.getStorage(), (double[]) actual.getStorage(), 1e-12);

        // ---------------------------------------------------------
        // Profile_UTC_Time
        actual = reader.readRaw(0, 10, new Interval(1, 5), "Profile_UTC_Time").copy();
        expected = Array.factory(DataType.DOUBLE, new int[]{5}, new double[]{
                80529.1865365764, 80529.1865451875, 80529.18655379862, 80529.18656240973, 80529.18657102084
        });
        compare(expected, actual);
        assertArrayEquals((double[]) expected.getStorage(), (double[]) actual.getStorage(), 1e-12);

        // ---------------------------------------------------------
        // Spacecraft_Position_x
        actual = reader.readRaw(0, 10, new Interval(1, 5), "Spacecraft_Position_x").copy();
        expected = Array.factory(DataType.DOUBLE, new int[]{5}, new double[]{
                -2608.955331565842, -2612.606545011657, -2616.2557470659044, -2619.9029355172183, -2623.548108174199
        });
        compare(expected, actual);
        assertArrayEquals((double[]) expected.getStorage(), (double[]) actual.getStorage(), 1e-12);

        // ---------------------------------------------------------
        // Spacecraft_Position_y
        actual = reader.readRaw(0, 10, new Interval(1, 5), "Spacecraft_Position_y").copy();
        expected = Array.factory(DataType.DOUBLE, new int[]{5}, new double[]{
                1019.507306015573, 1023.2664508586447, 1027.0253637421645, 1030.7840421544531, 1034.542483603354
        });
        compare(expected, actual);
        assertArrayEquals((double[]) expected.getStorage(), (double[]) actual.getStorage(), 1e-12);

        // ---------------------------------------------------------
        // Spacecraft_Position_z
        actual = reader.readRaw(0, 10, new Interval(1, 5), "Spacecraft_Position_z").copy();
        expected = Array.factory(DataType.DOUBLE, new int[]{5}, new double[]{
                -6511.835638911349, -6509.780795522994, -6507.721926987995, -6505.659034526308, -6503.592119346765
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
        final Path relPath = Paths.get("caliop_clay-cal", "4.10", "2008", "05", "29", "CAL_LID_L2_05kmCLay-Standard-V4-10.2008-05-29T04-28-32ZD.hdf");
        final Path testFilePath = testDataDirectory.resolve(relPath);
        assertThat(Files.exists(testFilePath), is(true));
        return testFilePath.toFile();
    }

    private Expectation[] getVariables_Expectations() {
        return new Expectation[]{
                /* idx: 0 */
                new Expectation("Number_Layers_Found", DataType.BYTE, new int[]{4208, 1}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Int_8"),
                        new Attribute("valid_range", "0...10"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (byte) -127))),
                /* idx: 1 */
                new Expectation("Column_Feature_Fraction", DataType.FLOAT, new int[]{4208, 1}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...1.0"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 2 */
                new Expectation("FeatureFinderQC", DataType.SHORT, new int[]{4208, 1}, Arrays.asList(
                        new Attribute("_Unsigned", "true"),
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "UInt_16"),
                        new Attribute("valid_range", "0...32767"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (short) -1))),
                /* idx: 3 */
                new Expectation("Feature_Classification_Flags", DataType.SHORT, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("_Unsigned", "true"),
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "UInt_16"),
                        new Attribute("valid_range", "1...49146"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (short) -1))),
                /* idx: 4 */
                new Expectation("ExtinctionQC_532", DataType.SHORT, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("_Unsigned", "true"),
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "UInt_16"),
                        new Attribute("valid_range", "0...32768"),
                        new Attribute("fillvalue", (short) -32768),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (short) -32768))),
                /* idx: 5 */
                new Expectation("CAD_Score", DataType.BYTE, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Int_8"),
                        new Attribute("valid_range", "-101...106"),
                        new Attribute("fillvalue", (byte) -127),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (byte) -127))),
                /* idx: 6 */
                new Expectation("Layer_IAB_QA_Factor", DataType.FLOAT, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...1.0"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 7 */
                new Expectation("Opacity_Flag", DataType.BYTE, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Int_8"),
                        new Attribute("valid_range", "0...1"),
                        new Attribute("fillvalue", (byte) 99),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (byte) 99))),
                /* idx: 8 */
                new Expectation("Ice_Water_Path", DataType.FLOAT, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("units", "gram per square meter"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...200.0"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 9 */
                new Expectation("Ice_Water_Path_Uncertainty", DataType.FLOAT, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("units", "gram per square meter"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...99.99"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 10 */
                new Expectation("Feature_Optical_Depth_532", DataType.FLOAT, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...5.0"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 11 */
                new Expectation("Feature_Optical_Depth_Uncertainty_532", DataType.FLOAT, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "0.0...TBD"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 12 */
                new Expectation("Layer_Top_Altitude", DataType.FLOAT, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("units", "kilometers"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "-0.5...30.1"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 13 */
                new Expectation("Layer_Base_Altitude", DataType.FLOAT, new int[]{4208, 10}, Arrays.asList(
                        new Attribute("units", "kilometers"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "-0.5...30.1"),
                        new Attribute("fillvalue", -9999f),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999f))),
                /* idx: 14 */
                new Expectation("Profile_ID", DataType.INT, new int[]{4208, 1}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Int_32"),
                        new Attribute("valid_range", "1...3153600000"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.INT, false)))),
                /* idx: 15 */
                new Expectation("Latitude", DataType.FLOAT, new int[]{4208, 1}, Arrays.asList(
                        new Attribute("units", "degrees"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "-90.0...90.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false)))),
                /* idx: 16 */
                new Expectation("Longitude", DataType.FLOAT, new int[]{4208, 1}, Arrays.asList(
                        new Attribute("units", "degrees"),
                        new Attribute("format", "Float_32"),
                        new Attribute("valid_range", "-180.0...180.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false)))),
                /* idx: 17 */
                new Expectation("Profile_Time", DataType.DOUBLE, new int[]{4208, 1}, Arrays.asList(
                        new Attribute("units", "seconds"),
                        new Attribute("format", "Float_64"),
                        new Attribute("valid_range", "4.204E8...1.072E9"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                /* idx: 18 */
                new Expectation("Profile_UTC_Time", DataType.DOUBLE, new int[]{4208, 1}, Arrays.asList(
                        new Attribute("units", "NoUnits"),
                        new Attribute("format", "Float_64"),
                        new Attribute("valid_range", "60426.0...261231.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                /* idx: 19 */
                new Expectation("Spacecraft_Position_x", DataType.DOUBLE, new int[]{4208, 1}, Arrays.asList(
                        new Attribute("units", "kilometers"),
                        new Attribute("format", "Float_64"),
                        new Attribute("valid_range", "-8000.0...8000.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                /* idx: 20 */
                new Expectation("Spacecraft_Position_y", DataType.DOUBLE, new int[]{4208, 1}, Arrays.asList(
                        new Attribute("units", "kilometers"),
                        new Attribute("format", "Float_64"),
                        new Attribute("valid_range", "-8000.0...8000.0"),
                        new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false)))),
                /* idx: 21 */
                new Expectation("Spacecraft_Position_z", DataType.DOUBLE, new int[]{4208, 1}, Arrays.asList(
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