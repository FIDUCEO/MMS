package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;
import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class SciCciInsituReader_IO_Test {

    private SicCciInsituReader reader;

    @Before
    public void setUp() {
        reader = new SicCciInsituReader();
    }

    @Test
    public void testReadAcquisitionInfo_DMISIC0() throws IOException {
        final File testFile = getDMISIC0();

        try {
            reader.open(testFile);

            final AcquisitionInfo acquisitionInfo = reader.read();
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 1, 0, 0, acquisitionInfo.getSensingStart());
            TestUtil.assertCorrectUTCDate(2016, 12, 31, 16, 0, 0, acquisitionInfo.getSensingStop());

            assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());
            assertNull(acquisitionInfo.getBoundingGeometry());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_ANTXXXI() throws IOException {
        final File testFile = getANTXXXI();

        try {
            reader.open(testFile);

            final AcquisitionInfo acquisitionInfo = reader.read();
            TestUtil.assertCorrectUTCDate(2015, 12, 13, 8, 0, 0, acquisitionInfo.getSensingStart());
            TestUtil.assertCorrectUTCDate(2016, 2, 8, 17, 0, 0, acquisitionInfo.getSensingStop());

            assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());
            assertNull(acquisitionInfo.getBoundingGeometry());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_DTUSIC1() throws IOException, InvalidRangeException {
        final File testFile = getDTUSIC1();

        try {
            reader.open(testFile);

            final List<Variable> variables = reader.getVariables();
            assertEquals(6, variables.size());

            Variable variable = variables.get(0);
            assertEquals("longitude", variable.getShortName());
            NCTestUtils.assertAttribute(variable, CF_FILL_VALUE_NAME, "9.969209968386869E36");

            variable = variables.get(5);
            assertEquals("areachange", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_DMISIC0() throws IOException, InvalidRangeException {
        final File testFile = getDMISIC0();

        try {
            reader.open(testFile);

            final List<Variable> variables = reader.getVariables();
            assertEquals(5, variables.size());

            Variable variable = variables.get(0);
            assertEquals("longitude", variable.getShortName());
            NCTestUtils.assertAttribute(variable, CF_UNITS_NAME, "degree_east");

            variable = variables.get(4);
            assertEquals("SIC", variable.getShortName());
            NCTestUtils.assertAttribute(variable, CF_STANDARD_NAME, "sea_ice_area_fraction");
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_ANTXXXI() throws IOException, InvalidRangeException {
        final File testFile = getANTXXXI();

        try {
            reader.open(testFile);

            final List<Variable> variables = reader.getVariables();
            assertEquals(33, variables.size());

            Variable variable = variables.get(0);
            assertEquals("longitude", variable.getShortName());
            NCTestUtils.assertAttribute(variable, CF_UNITS_NAME, "degree_east");

            variable = variables.get(13);
            assertEquals("Ice-type-secondary", variable.getShortName());
            NCTestUtils.assertAttribute(variable, CF_LONG_NAME, "Ice-type-secondary ASPeCt code");

            variable = variables.get(25);
            assertEquals("Snow-depth-tertiary", variable.getShortName());
            NCTestUtils.assertAttribute(variable, CF_UNITS_NAME, "m");

            variable = variables.get(32);
            assertEquals("Weather", variable.getShortName());
            NCTestUtils.assertAttribute(variable, CF_FILL_VALUE_NAME, "-127");
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_DMISIC0() throws IOException {
        final File testFile = getDMISIC0();

        try {
            reader.open(testFile);

            final Dimension productSize = reader.getProductSize();

            assertNotNull(productSize);
            assertEquals("product_size", productSize.getName());
            assertEquals(1, productSize.getNx());
            assertEquals(6514, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_DTUSIC1() throws IOException {
        final File testFile = getDTUSIC1();

        try {
            reader.open(testFile);

            reader.getPixelLocator();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator_DMISIC0() throws IOException {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = geometryFactory.createPolygon(Arrays.asList(
                geometryFactory.createPoint(5, 9),
                geometryFactory.createPoint(6, 0),
                geometryFactory.createPoint(6, 10)
        ));

        final File testFile = getDMISIC0();

        try {
            reader.open(testFile);
            reader.getSubScenePixelLocator(polygon);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_DTUSIC1() throws IOException {
        final File testFile = getDTUSIC1();

        try {
            reader.open(testFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1485042015000L, timeLocator.getTimeFor(0, 0));
            assertEquals(1499290209000L, timeLocator.getTimeFor(10, 245));
            assertEquals(1509821155000L, timeLocator.getTimeFor(20, 711));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_ANTXXI() throws IOException {
        final File testFile = getANTXXXI();

        try {
            reader.open(testFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1449997200000L, timeLocator.getTimeFor(0, 1));
            assertEquals(1452020400000L, timeLocator.getTimeFor(11, 246));
            assertEquals(1454950800000L, timeLocator.getTimeFor(21, 551));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1x1_DMISIC0() throws InvalidRangeException, IOException {
        final File testFile = getDMISIC0();

        try {
            reader.open(testFile);
            // reference data
            Array array = reader.readRaw(6, 3, new Interval(1, 1), "latitude");

            assertNotNull(array);
            assertArrayEquals(new int[]{1, 1}, array.getShape());
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(63.5f, array.getFloat(0), 1e-8);

            // ERA5
            array = reader.readRaw(5, 27, new Interval(1, 1), "ERA5_msl");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(1032.9f, array.getFloat(0), 1e-8);

            // AMSR2
            array = reader.readRaw(5, 28, new Interval(1, 1), "AMSR2_6.9GHzH");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(88.62f, array.getFloat(0), 1e-8);

            // ASCAT
            array = reader.readRaw(5, 29, new Interval(1, 1), "ASCAT_sigma_40");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(-15.76655f, array.getFloat(0), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1x1_DTUSIC1() throws InvalidRangeException, IOException {
        final File testFile = getDTUSIC1();

        try {
            reader.open(testFile);

            // reference data
            Array array = reader.readRaw(7, 4, new Interval(1, 1), "reference-id");
            final char[] valueAsArray = (char[]) array.get1DJavaArray(char.class);
            assertEquals("COMPRESSIONCELLS_DTU", new String(valueAsArray).trim());

            // ERA5
            array = reader.readRaw(7, 5, new Interval(1, 1), "ERA5_u10");
            assertEquals(-3.8f, array.getFloat(0), 1e-8);

            // AMSR2
            array = reader.readRaw(7, 6, new Interval(1, 1), "AMSR2_6.9GHzV");
            assertEquals(257.9100036621094f, array.getFloat(0), 1e-8);

            // ASCAT
            array = reader.readRaw(5, 7, new Interval(1, 1), "ASCAT_sigma_40_mask");
            assertEquals(-15.17677f, array.getFloat(0), 1e-8);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1x1_DTUSIC1_SMOS_SMAP() throws InvalidRangeException, IOException {
        final File testFile = getDTUSIC1_SMOS_SMAP();

        try {
            reader.open(testFile);

            // reference data
            Array array = reader.readRaw(7, 5, new Interval(1, 1), "SIC");
            final char[] valueAsArray = (char[]) array.get1DJavaArray(char.class);
            assertEquals(1.f, array.getFloat(0), 1e-8);

            // ERA5
            array = reader.readRaw(7, 6, new Interval(1, 1), "ERA_v10");
            assertEquals(1.18f, array.getFloat(0), 1e-8);

            // AMSR2
            array = reader.readRaw(7, 7, new Interval(1, 1), "AMSR2_7.3GHzH");
            assertEquals(187.99f, array.getFloat(0), 1e-8);

            // ASCAT
            array = reader.readRaw(5, 8, new Interval(1, 1), "ASCAT_nb_samples");
            assertEquals(21, array.getShort(0));

            // SMOS
            array = reader.readRaw(5, 9, new Interval(1, 1), "SMOS_Tbv");
            assertEquals(227.93663f, array.getFloat(0), 1e-8);

            // SMAP
            array = reader.readRaw(5, 10, new Interval(1, 1), "SMAP_Tbh");
            assertEquals(204.8144989013672f, array.getFloat(0), 1e-8);

            // QSCAT
            array = reader.readRaw(5, 11, new Interval(1, 1), "QSCAT_latitude");
            assertEquals(82.5f, array.getFloat(0), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_3x3_ANTXXXI() throws InvalidRangeException, IOException {
        final File testFile = getANTXXXI();

        try {
            reader.open(testFile);

            // reference data
            Array array = reader.readRaw(7, 5, new Interval(3, 3), "SIC-total");
            assertEquals(DataType.BYTE, array.getDataType());
            NCTestUtils.assertValueAt(-127, 0, 0, array);
            NCTestUtils.assertValueAt(10, 1, 1, array);
            NCTestUtils.assertValueAt(-127, 2, 2, array);

            // ERA5
            array = reader.readRaw(7, 6, new Interval(3, 3), "ERA_v10");
            NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 1, array);
            NCTestUtils.assertValueAt(4.17f, 1, 1, array);
            NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 1, array);

            // AMSR2
            array = reader.readRaw(7, 7, new Interval(3, 3), "AMSR2_7.3GHzH");
            NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 1, array);
            NCTestUtils.assertValueAt(129.34f, 1, 1, array);
            NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 1, array);

            // ASCAT
            array = reader.readRaw(7, 8, new Interval(3, 3), "ASCAT_nb_samples");
            NCTestUtils.assertValueAt(-32767, 0, 1, array);
            NCTestUtils.assertValueAt(-32767, 1, 1, array);
            NCTestUtils.assertValueAt(-32767, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_1x1_DMISIC0() throws InvalidRangeException, IOException {
        final File testFile = getDMISIC0();

        try {
            reader.open(testFile);
            // reference data
            Array array = reader.readScaled(6, 5, new Interval(1, 1), "SIC");

            assertNotNull(array);
            assertArrayEquals(new int[]{1, 1}, array.getShape());
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(0.f, array.getFloat(0), 1e-8);

            // ERA5
            array = reader.readScaled(5, 28, new Interval(1, 1), "ERA5_ws");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(16.88f, array.getFloat(0), 1e-8);

            // AMSR2
            array = reader.readScaled(5, 29, new Interval(1, 1), "AMSR2_7.3GHzV");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(162.24f, array.getFloat(0), 1e-8);

            // ASCAT
            array = reader.readScaled(5, 30, new Interval(1, 1), "ASCAT_warning");
            assertEquals(DataType.SHORT, array.getDataType());
            assertEquals(0, array.getShort(0), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_1x1_DTUSIC1_QSCAT() throws InvalidRangeException, IOException {
        final File testFile = getDTUSIC1_QSCAT();

        try {
            reader.open(testFile);

            // reference data
            Array array = reader.readScaled(6, 6, new Interval(1, 1), "areachange");
            assertEquals(0.993f, array.getFloat(0), 1e-8);

            // ERA5
            array = reader.readScaled(5, 7, new Interval(1, 1), "ERA_t2m");
            assertEquals(253.53f, array.getFloat(0), 1e-8);

            // AMSR2
            array = reader.readScaled(5, 8, new Interval(1, 1), "AMSR2_23.8GHzH");
            assertEquals(229.82f, array.getFloat(0), 1e-8);

            // ASCAT
            array = reader.readScaled(5, 9, new Interval(1, 1), "ASCAT_std");
            assertEquals(0.0755f, array.getFloat(0), 1e-8);

            // QSCAT
            array = reader.readScaled(5, 10, new Interval(1, 1), "QSCAT_sigma0_inner");
            assertEquals(9.969209968386869E36f, array.getFloat(0), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_1x1_DTUSIC1() throws IOException, InvalidRangeException {
        final File testFile = getDTUSIC1();

        try {
            reader.open(testFile);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(12, 34, new Interval(1, 1));
            NCTestUtils.assertValueAt(1490836147, 0, 0, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_3x1_DMISIC0() throws IOException, InvalidRangeException {
        final File testFile = getDMISIC0();

        try {
            reader.open(testFile);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(13, 35, new Interval(3, 1));
            NCTestUtils.assertValueAt(-2147483647, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1451793600, 1, 0, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 2, 0, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    private static File getDMISIC0() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", "DMISIC0-sic-cci", "v3", "ASCAT-vs-AMSR2-vs-ERA5-vs-DMISIC0-2016-N.text"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getDTUSIC1() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", "DTUSIC1-sic-cci", "v3", "ASCAT-vs-AMSR2-vs-ERA5-vs-DTUSIC1-2017-S.text"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getDTUSIC1_QSCAT() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", "DTUSIC1-sic-cci", "v3", "QSCAT-vs-ASCAT-vs-AMSR2-vs-ERA-vs-DTUSIC1-2016-S.text"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getDTUSIC1_SMOS_SMAP() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", "DTUSIC1-sic-cci", "v3", "QSCAT-vs-SMAP-vs-SMOS-vs-ASCAT-vs-AMSR2-vs-ERA-vs-DTUSIC1-2016-N.text"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getANTXXXI() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", "ANTXXXI-sic-cci", "v3", "ASCAT-vs-AMSR2-vs-ERA-vs-ANTXXXI_2_FROSN_SeaIceObservations_reformatted.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
