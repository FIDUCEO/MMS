package com.bc.fiduceo.reader.insitu.gruan_uleic;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_STANDARD_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_UNITS_NAME;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class GruanUleicInsituReader_IO_Test {

    private GruanUleicInsituReader reader;

    @Before
    public void setUp() throws IOException {
        reader = new GruanUleicInsituReader();

        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "gruan-uleic", "v1.0", "nya_matchup_points.txt"}, false);
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File testFile = new File(testDataDirectory, relativePath);
        reader.open(testFile);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final AcquisitionInfo info = reader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2009, 1, 1, 5, 54, 22, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2018, 3, 27, 11, 9, 35, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = reader.getVariables();

        assertNotNull(variables);
        assertEquals(3, variables.size());

        Variable variable = variables.get(0);
        assertEquals("lon", variable.getShortName());
        assertEquals("longitude", NetCDFUtils.getAttributeString(variable, CF_STANDARD_NAME, "default"));
        assertEquals("degree_east", NetCDFUtils.getAttributeString(variable, CF_UNITS_NAME, "default"));
        assertEquals(9.969209968386869E36f, NetCDFUtils.getAttributeFloat(variable, CF_FILL_VALUE_NAME, -0.8f), 1e-8);

        variable = variables.get(1);
        assertEquals("lat", variable.getShortName());
        assertEquals("latitude", NetCDFUtils.getAttributeString(variable, CF_STANDARD_NAME, "default"));
        assertEquals("degree_north", NetCDFUtils.getAttributeString(variable, CF_UNITS_NAME, "default"));
        assertEquals(9.969209968386869E36f, NetCDFUtils.getAttributeFloat(variable, CF_FILL_VALUE_NAME, -0.8f), 1e-8);

        variable = variables.get(2);
        assertEquals("time", variable.getShortName());
        assertEquals("time", NetCDFUtils.getAttributeString(variable, CF_STANDARD_NAME, "default"));
        assertEquals("Seconds since 1970-01-01", NetCDFUtils.getAttributeString(variable, CF_UNITS_NAME, "default"));
        assertEquals(-2147483647, NetCDFUtils.getAttributeInt(variable, CF_FILL_VALUE_NAME, 100));

    }

    @Test
    public void testReadRaw_1x1_lat() {
        final Array array = reader.readRaw(6, 3, new Interval(1, 1), "lat");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(71.32252502441406f, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_1x1_lon() {
        final Array array = reader.readRaw(7, 2, new Interval(1, 1), "lon");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-156.61607360839844f, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_3x3_time() {
        final Array array = reader.readRaw(8, 3, new Interval(3, 3), "time");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.INT, array.getDataType());

        NCTestUtils.assertValueAt(-2147483647, 0, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 1, array);
        NCTestUtils.assertValueAt(1231393795, 1, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 2, array);
    }

    @Test
    public void testReadScaled_3x3_lon() {
        final Array array = reader.readScaled(8, 4, new Interval(3, 3), "lon");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());

        NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 0, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 1, 0, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 0, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 1, array);
        NCTestUtils.assertValueAt(-156.61599731445312f, 1, 1, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 1, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 2, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 1, 2, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 2, array);
    }

    @Test
    public void testReadAcquisitionTime_3x3() {
        final ArrayInt.D2 array = reader.readAcquisitionTime(9, 5, new Interval(3, 3));

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.INT, array.getDataType());

        NCTestUtils.assertValueAt(-2147483647, 0, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 1, array);
        NCTestUtils.assertValueAt(1232216778, 1, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 2, array);
    }

    @Test
    public void testReadAcquisitionTime_1x1() {
        final ArrayInt.D2 array = reader.readAcquisitionTime(10, 6, new Interval(1, 1));

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.INT, array.getDataType());

        assertEquals(1232308814, array.getInt(0));
    }

    @Test
    public void testGetProductSize() {
        final Dimension productSize = reader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(10571, productSize.getNy());
    }

    @Test
    public void testGetTimeLocator() {
        final TimeLocator timeLocator = reader.getTimeLocator();

        assertNotNull(timeLocator);
        assertEquals(1232340640000L, timeLocator.getTimeFor(2, 7));
        assertEquals(1232948693000L, timeLocator.getTimeFor(2, 17));
        assertEquals(1233553095000L, timeLocator.getTimeFor(2, 27));
    }

    @Test
    public void testGetPixelLocator() {
        try {
            reader.getPixelLocator();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetSubScenePixelLocator() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = geometryFactory.createPolygon(Arrays.asList(
                geometryFactory.createPoint(6, 8),
                geometryFactory.createPoint(7, 8),
                geometryFactory.createPoint(7, 9)
        ));

        try {
            reader.getSubScenePixelLocator(polygon);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        try {
            reader.extractYearMonthDayFromFilename("nya_matchup_points.txt");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testReadSourcePath() {
        String sourcePath = reader.readSourcePath(1);
        assertEquals("BAR/2009/BAR-RS-01_2_RS92-GDP_002_20090101T180000_1-000-001.nc", sourcePath);

        sourcePath = reader.readSourcePath(109);
        assertEquals("BAR/2009/BAR-RS-01_2_RS92-GDP_002_20090505T060000_1-000-001.nc", sourcePath);

        sourcePath = reader.readSourcePath(9754);
        assertEquals("NYA/2015/NYA-RS-01_2_RS92-GDP_002_20150101T120000_1-000-001.nc", sourcePath);
    }
}
