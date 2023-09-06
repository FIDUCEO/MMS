package com.bc.fiduceo.reader.smap;


import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.L3TimeAxis;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
@RunWith(IOTestRunner.class)
public class SmapReader_IO_Test {

    private Reader reader;

    @Before
    public void setUp() throws IOException {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new SmapReaderPluginAftLook().createReader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = getSmapSssFile();

        try {
            reader.open(file);

            final AcquisitionInfo info = reader.read();

            final Geometry boundingGeometry = info.getBoundingGeometry();
            assertTrue(boundingGeometry instanceof Polygon);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(5, coordinates.length);
            assertEquals(-179.98402404785156, coordinates[0].getLon(), 1e-8);
            assertEquals(-86.61946868896484, coordinates[0].getLat(), 1e-8);
            assertEquals(179.99179077148438, coordinates[1].getLon(), 1e-8);
            assertEquals(-86.61946868896484, coordinates[1].getLat(), 1e-8);
            assertEquals(179.99179077148438, coordinates[2].getLon(), 1e-8);
            assertEquals(86.40682220458984, coordinates[2].getLat(), 1e-8);
            assertEquals(-179.98402404785156, coordinates[3].getLon(), 1e-8);
            assertEquals(86.40682220458984, coordinates[3].getLat(), 1e-8);
            assertEquals(-179.98402404785156, coordinates[4].getLon(), 1e-8);
            assertEquals(-86.61946868896484, coordinates[4].getLat(), 1e-8);

            final Date sensingStart = info.getSensingStart();
            TestUtil.assertCorrectUTCDate(2018, 2, 4, 20, 23, 11, sensingStart);
            final Date sensingStop = info.getSensingStop();
            TestUtil.assertCorrectUTCDate(2018, 2, 4, 22, 4, 56, sensingStop);

            final TimeAxis[] timeAxes = info.getTimeAxes();
            assertEquals(1, timeAxes.length);
            final TimeAxis timeAxis = timeAxes[0];
            assertTrue(timeAxis instanceof L3TimeAxis);
            final Geometry geometry = timeAxis.getGeometry();
            coordinates = geometry.getCoordinates();
            assertEquals(4, coordinates.length);
            assertEquals(-179.98402404785156, coordinates[0].getLon(), 1e-8);
            assertEquals(0.0, coordinates[0].getLat(), 1e-8);
            assertEquals(179.99179077148438, coordinates[1].getLon(), 1e-8);
            assertEquals(0.0, coordinates[1].getLat(), 1e-8);
            assertEquals(0.0, coordinates[2].getLon(), 1e-8);
            assertEquals(86.40682220458984, coordinates[2].getLat(), 1e-8);
            assertEquals(0.0, coordinates[3].getLon(), 1e-8);
            assertEquals(-86.61946868896484, coordinates[3].getLat(), 1e-8);

            assertEquals(NodeType.UNDEFINED, info.getNodeType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws IOException {
        final File file = getSmapSssFile();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(1560, productSize.getNx());
            assertEquals(720, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File file = getSmapSssFile();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 25.5, null);
            assertEquals(53.93850708, geoLocation.getX(), 1e-8);
            assertEquals(-83.62055206, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(3119.5, 0.5, null); // x coordinate is outside the product
            assertNull(geoLocation);

            geoLocation = pixelLocator.getGeoLocation(20.5, 40.5, null);
            assertEquals(48.90038681, geoLocation.getX(), 1e-8);
            assertEquals(-79.87638855, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(780.5, 700.5, null);
            assertEquals(218.93360901, geoLocation.getX(), 1e-8);
            assertEquals(85.11258698, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocations = pixelLocator.getPixelLocation(53.93850708, -83.62055206);
            assertEquals(2, pixelLocations.length);
            assertEquals(0.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(25.5, pixelLocations[0].getY(), 1e-8);
            assertEquals(1440.5, pixelLocations[1].getX(), 1e-8);
            assertEquals(25.5, pixelLocations[1].getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(48.90038681, -79.87638855);
            assertEquals(2, pixelLocations.length);
            assertEquals(20.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(40.5, pixelLocations[0].getY(), 1e-8);
            assertEquals(1460.5, pixelLocations[1].getX(), 1e-8);
            assertEquals(40.5, pixelLocations[1].getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(218.93360901, 85.11258698);
            assertEquals(1, pixelLocations.length);
            assertEquals(780.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(700.5, pixelLocations[0].getY(), 1e-8);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File file = getSmapSssFile();

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
    public void testGetTimeLocator() throws IOException {
        final File file = getSmapSssFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            // check fill value areas
            assertEquals(-1, timeLocator.getTimeFor(200, 310));
            assertEquals(-1, timeLocator.getTimeFor(600, 310));
            assertEquals(-1, timeLocator.getTimeFor(1300, 310));

            // check data areas
            assertEquals(1517776253000L, timeLocator.getTimeFor(300, 70));
            assertEquals(1517779003000L, timeLocator.getTimeFor(1000, 690));
            assertEquals(1517781791000L, timeLocator.getTimeFor(1450, 50));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws IOException, InvalidRangeException {
        final File file = getSmapSssFile();

        try {
            reader.open(file);

            // read a section that covers a swath border tb 2022-11-28
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(369, 336, new Interval(5, 3));
            assertEquals(15, acquisitionTime.getSize());

            NCTestUtils.assertValueAt(1517777339, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1517777343, 1, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1517777346, 2, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1517777349, 3, 0, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 4, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1517777343, 0, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1517777346, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1517777350, 2, 1, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 3, 1, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 4, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1517777346, 0, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1517777350, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1517777353, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1517777356, 3, 2, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 4, 2, acquisitionTime);


        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws IOException, InvalidRangeException {
        final File file = getSmapSssFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(124, variables.size());

            Variable variable = variables.get(0);
            assertEquals("time_aft", variable.getShortName());
            assertEquals(DataType.DOUBLE, variable.getDataType());
            Attribute attribute = variable.attributes().findAttribute("units");
            assertEquals("seconds since 2000-1-1 0:0:0 0", attribute.getStringValue());

            variable = variables.get(4);
            assertEquals("fland_aft", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
            attribute = variable.attributes().findAttribute("add_offset");
            assertEquals(0.f, attribute.getNumericValue().floatValue(), 1e-8);

            variable = variables.get(15);
            assertEquals("temp_ant_aft_V", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
            attribute = variable.attributes().findAttribute("units");
            assertEquals("Kelvin", attribute.getStringValue());

            variable = variables.get(26);
            assertEquals("sun_beta_aft", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
            attribute = variable.attributes().findAttribute("coverage_content_type");
            assertEquals("physicalMeasurement", attribute.getStringValue());

            variable = variables.get(37);
            assertEquals("dtemp_ant_aft_H", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
            attribute = variable.attributes().findAttribute("long_name");
            assertEquals("Empirical correction to physical temperature of reflector. Pol basis V,H", attribute.getStringValue());

            variable = variables.get(48);
            assertEquals("ta_gal_ref_aft_Q", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
            attribute = variable.attributes().findAttribute("scale_factor");
            assertEquals(1.f, attribute.getNumericValue().floatValue(), 1e-8);

            variable = variables.get(59);
            assertEquals("tb_toi_aft_H", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
            attribute = variable.attributes().findAttribute("standard_name");
            assertEquals("brightness_temperature", attribute.getStringValue());

            variable = variables.get(88);
            assertEquals("iqc_flag_aft", variable.getShortName());
            assertEquals(DataType.INT, variable.getDataType());
            attribute = variable.attributes().findAttribute("flag_meaning");
            assertEquals("bit  0 set: no radiometer observation in cell. SSS not retrieved. " +
                         "bit  1 set: problenm with OI. SSS not retrieved. " +
                         "bit  2 set: strong land contamination. SSS not retrieved. " +
                         "bit  3 set: strong sea ice contamination. SSS not retrieved. " +
                         "bit  4 set: MLE in SSS retrieval algo has not converged. SSS not retrieved. " +
                         "bit  5 set: sunglint. SSS retrieved. very strong degradation. " +
                         "bit  6 set: moonglint. SSS retrieved. moderate - strong degradation. " +
                         "bit  7 set: high reflected galaxy. SSS retrieved. moderate - strong degradation. " +
                         "bit  8 set: moderate land contamination. SSS retrieved. strong degradation. " +
                         "bit  9 set: moderate sea ice contamination. SSS retrieved. strong degradation. " +
                         "bit 10 set: high residual of MLE in SSS retrieval algo. SSS retrieved. strong degradation. " +
                         "bit 11 set: low SST. SSS retrieved. moderate - strong degradation. " +
                         "bit 12 set: high wind. SSS retrieved. moderate degradation. " +
                         "bit 13 set: light land contamination. SSS retrieved. light degradation. not used in ocean target cal. " +
                         "bit 14 set: light sea ice contamination. SSS retrieved. light - moderate degradation. not used in ocean target cal. " +
                         "bit 15 set: rain flag. SSS retrieved. possibly light degradation. not used in ocean target cal. " +
                         "bit 16 set: climatological sea-ice flag set. no AMSR2 data available for sea-ice detection or correction. no SSS retrieved."
                    , attribute.getStringValue());
            attribute = variable.attributes().findAttribute("coverage_content_type");
            assertEquals("qualityInformation", attribute.getStringValue());

            variable = variables.get(variables.size() - 1);
            assertNotNull(variable);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw() throws IOException, InvalidRangeException {
        final File file = getSmapSssFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(342, 112, new Interval(3, 3), "sss_smap_40km_unc_comp_sst_aft");
            assertNotNull(array);
            assertArrayEquals(new int[]{3, 3}, array.getShape());
            NCTestUtils.assertValueAt(0.2572820484638214, 0, 0, array);
            NCTestUtils.assertValueAt(-9999.0, 1, 0, array);
            NCTestUtils.assertValueAt(-9999.0, 2, 0, array);
            NCTestUtils.assertValueAt(0.28291627764701843, 0, 1, array);
            NCTestUtils.assertValueAt(6.475381851196289, 1, 1, array);
            NCTestUtils.assertValueAt(-9999.0, 2, 1, array);
            NCTestUtils.assertValueAt(-9999.0, 0, 2, array);
            NCTestUtils.assertValueAt(-9999.0, 1, 2, array);
            NCTestUtils.assertValueAt(0.6615973711013794, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_data_partially_outside() throws IOException, InvalidRangeException {
        final File file = getSmapSssFile();

        try {
            reader.open(file);

            // upper left corner
            Array partialOutsideRead = reader.readRaw(0, 6, new Interval(3, 15), "ta_ant_aft_S3");
            assertNotNull(partialOutsideRead);
            assertArrayEquals(new int[]{15, 3}, partialOutsideRead.getShape());
            IndexIterator indexIterator = partialOutsideRead.getIndexIterator();
            while (indexIterator.hasNext()) {
                Object next = indexIterator.next();
                final int[] currentCounter = indexIterator.getCurrentCounter();
                if (Arrays.equals(new int[]{14, 1}, currentCounter)) {
                    assertEquals(0.921287477016449, (Float) next, 1e-8);
                } else if (Arrays.equals(new int[]{14, 2}, currentCounter)) {
                    assertEquals(0.6892231106758118, (Float) next, 1e-8);
                } else {
                    assertEquals("Pos: " + Arrays.toString(currentCounter), -9999.0f, (Float) next, 1e-8);
                }
            }

            // lower border
            partialOutsideRead = reader.readRaw(780, 713, new Interval(3, 17), "ta_ant_aft_S3");
            assertNotNull(partialOutsideRead);
            assertArrayEquals(new int[]{17, 3}, partialOutsideRead.getShape());
            indexIterator = partialOutsideRead.getIndexIterator();
            while (indexIterator.hasNext()) {
                Object next = indexIterator.next();
                final int[] currentCounter = indexIterator.getCurrentCounter();
                if (Arrays.equals(new int[]{0, 0}, currentCounter)) {
                    assertEquals(0.21593234, (Float) next, 1e-8);
                } else if (Arrays.equals(new int[]{0, 2}, currentCounter)) {
                    assertEquals(0.10643639, (Float) next, 1e-8);
                } else {
                    assertEquals("Pos: " + Arrays.toString(currentCounter), -9999.0f, (Float) next, 1e-8);
                }
            }

            // right border
            partialOutsideRead = reader.readRaw(1549, 37, new Interval(25, 3), "ta_ant_aft_S3");
            assertNotNull(partialOutsideRead);
            assertArrayEquals(new int[]{3, 25}, partialOutsideRead.getShape());
            indexIterator = partialOutsideRead.getIndexIterator();
            while (indexIterator.hasNext()) {
                Object next = indexIterator.next();
                final int[] currentCounter = indexIterator.getCurrentCounter();
                if (Arrays.equals(new int[]{0, 0}, currentCounter)) {
                    assertEquals(0.43374702, (Float) next, 1e-8);
                } else if (Arrays.equals(new int[]{1, 0}, currentCounter)) {
                    // Attention. This value cannot be displayed with the Panoply tool. Panoply only displays
                    // values within the defined value range. The variable "ta_ant" has a defined value range
                    // from "valid_min = 0.0f" to "valid_max = 330.0f".
                    // The HDFViewer displays this value.
                    assertEquals(-0.25885728, (Float) next, 1e-8);
                } else if (Arrays.equals(new int[]{2, 0}, currentCounter)) {
                    assertEquals(0.61770272, (Float) next, 1e-8);
                } else {
                    assertEquals("Pos: " + Arrays.toString(currentCounter), -9999.0f, (Float) next, 1e-8);
                }
            }
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled() throws IOException, InvalidRangeException {
        final File file = getSmapSssFile();

        try {
            reader.open(file);

            // Since SMAP variables apparently have neither an add_offset nor a scale_factor != 1.0, the results are the same as readRaw.
            final Array array = reader.readScaled(342, 112, new Interval(3, 3), "sss_smap_40km_unc_comp_sst_aft");
            assertNotNull(array);
            assertArrayEquals(new int[]{3, 3}, array.getShape());
            NCTestUtils.assertValueAt(0.2572820484638214, 0, 0, array);
            NCTestUtils.assertValueAt(-9999.0, 1, 0, array);
            NCTestUtils.assertValueAt(-9999.0, 2, 0, array);
            NCTestUtils.assertValueAt(0.28291627764701843, 0, 1, array);
            NCTestUtils.assertValueAt(6.475381851196289, 1, 1, array);
            NCTestUtils.assertValueAt(-9999.0, 2, 1, array);
            NCTestUtils.assertValueAt(-9999.0, 0, 2, array);
            NCTestUtils.assertValueAt(-9999.0, 1, 2, array);
            NCTestUtils.assertValueAt(0.6615973711013794, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    private File getSmapSssFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"smap-sss", "v05.0", "2018", "02", "04", "RSS_SMAP_SSS_L2C_r16092_20180204T202311_2018035_FNL_V05.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
