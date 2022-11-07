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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
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
    @Ignore
    public void testReadRaw_1x1_DMISIC0() throws InvalidRangeException, IOException {
        final File testFile = getDMISIC0();

        try {
            reader.open(testFile);
            final Array array = reader.readRaw(6, 3, new Interval(1, 1), "latitude");

            assertNotNull(array);
            assertArrayEquals(new int[]{1, 1}, array.getShape());
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(71.32252502441406f, array.getFloat(0), 1e-8);
        } finally {
            reader.close();
        }
    }

    private static File getDMISIC0() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", "DMI_SIC0", "v3", "ASCAT-vs-AMSR2-vs-ERA5-vs-DMISIC0-2016-N.text"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getDTUSIC1() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", "DTU_SIC1", "v3", "ASCAT-vs-AMSR2-vs-ERA5-vs-DTUSIC1-2017-S.text"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
