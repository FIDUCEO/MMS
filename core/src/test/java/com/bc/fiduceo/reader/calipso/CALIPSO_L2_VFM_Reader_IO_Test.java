package com.bc.fiduceo.reader.calipso;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.TimeLocator_TAI1993Vector;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(IOTestRunner.class)
public class CALIPSO_L2_VFM_Reader_IO_Test {

    private File testDataDirectory;
    private CALIPSO_L2_VFM_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        reader = new CALIPSO_L2_VFM_Reader(new GeometryFactory(GeometryFactory.Type.S2));
        final File file = getCalipsoFile();
        reader.open(file);
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
        assertTrue(boundingGeometry instanceof LineString);

        Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(62, coordinates.length);

        assertEquals(16.231642, coordinates[0].getLon(), 1e-6);
        assertEquals(-61.987904, coordinates[0].getLat(), 1e-6);

        assertEquals(173.01501, coordinates[61].getLon(), 1e-5);
        assertEquals(71.7348, coordinates[61].getLat(), 1e-5);

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertEquals(1, timeAxes.length);

        final TimeAxis timeAxis = timeAxes[0];
        coordinates = timeAxis.getGeometry().getCoordinates();
        final Date time = timeAxes[0].getTime(coordinates[0]);
        TestUtil.assertCorrectUTCDate(2011, 1, 2, 23, 37, 1, time);
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
        final String prefix = "CAL_LID_L2_VFM-Standard-V4-10.";
        final String expected = prefix + YYYY + "-" + MM + "-"+DD+"T"+hh+"-"+mm+"-"+ss+"Z[DN].hdf";
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher;
        // valid day                                                                       ⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-04ZD.hdf");
        assertEquals(true ,matcher.matches());
        // valid night                                                                     ⇓
        matcher = pattern.matcher("CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-04ZN.hdf");
        assertEquals(true ,matcher.matches());

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

    private File getCalipsoFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"calipso-vfm", "CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-04ZD.hdf"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private Expectation[] getVariables_Expectations() {
        return new Expectation[]{
                    new Expectation("Profile_ID", DataType.INT, Arrays.asList(
                                new Attribute("units", "NoUnits"), new Attribute("format", "Int_32"), new Attribute("valid_range", "1...3153600000"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.INT, false))
                    )),
                    new Expectation("Latitude", DataType.FLOAT, Arrays.asList(
                                new Attribute("units", "degrees"), new Attribute("format", "Float_32"), new Attribute("valid_range", "-90.0...90.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false))
                    )),
                    new Expectation("Longitude", DataType.FLOAT, Arrays.asList(
                                new Attribute("units", "degrees"), new Attribute("format", "Float_32"), new Attribute("valid_range", "-180.0...180.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false))
                    )),
                    new Expectation("Profile_Time", DataType.DOUBLE, Arrays.asList(
                                new Attribute("units", "seconds"), new Attribute("format", "Float_64"), new Attribute("valid_range", "4.204E8...1.072E9"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false))
                    )),
                    new Expectation("Profile_UTC_Time", DataType.DOUBLE, Arrays.asList(
                                new Attribute("units", "NoUnits"), new Attribute("format", "Float_64"), new Attribute("valid_range", "60426.0...261231.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false))
                    )),
                    new Expectation("Day_Night_Flag", DataType.SHORT, Arrays.asList(
                                new Attribute("_Unsigned", "true"), new Attribute("units", "NoUnits"), new Attribute("format", "UInt_16"), new Attribute("valid_range", "0...1"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.SHORT, true))
                    )),
                    new Expectation("Land_Water_Mask", DataType.BYTE, Arrays.asList(
                                new Attribute("units", "NoUnits"), new Attribute("format", "Int_8"), new Attribute("valid_range", "0...7"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, (byte) -9)
                    )),
                    new Expectation("Spacecraft_Position_x", DataType.DOUBLE, Arrays.asList(
                                new Attribute("units", "kilometers"), new Attribute("format", "Float_64"), new Attribute("valid_range", "-8000.0...8000.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false))
                    )),
                    new Expectation("Spacecraft_Position_y", DataType.DOUBLE, Arrays.asList(
                                new Attribute("units", "kilometers"), new Attribute("format", "Float_64"), new Attribute("valid_range", "-8000.0...8000.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false))
                    )),
                    new Expectation("Spacecraft_Position_z", DataType.DOUBLE, Arrays.asList(
                                new Attribute("units", "kilometers"), new Attribute("format", "Float_64"), new Attribute("valid_range", "-8000.0...8000.0"),
                                new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, false))
                    ))
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