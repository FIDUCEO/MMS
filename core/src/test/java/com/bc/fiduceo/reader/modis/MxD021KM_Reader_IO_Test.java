package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class MxD021KM_Reader_IO_Test {

    private MxD021KM_Reader reader;

    @Before
    public void setUp() throws IOException {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Archive archive = TestUtil.getArchive();

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(geometryFactory);
        readerContext.setArchive(archive);

        reader = new MxD021KM_Reader(readerContext);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final AcquisitionInfo acquisitionInfo = reader.read();
        assertNotNull(acquisitionInfo);

        final Date sensingStart = acquisitionInfo.getSensingStart();
        TestUtil.assertCorrectUTCDate(2003, 5, 22, 14, 45, 0, 0, sensingStart);

        final Date sensingStop = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2003, 5, 22, 14, 50, 0, 0, sensingStop);

        final NodeType nodeType = acquisitionInfo.getNodeType();
        assertEquals(NodeType.UNDEFINED, nodeType);

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);
        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(31, coordinates.length);
        assertEquals(-119.84687042236328, coordinates[0].getLon(), 1e-8);
        assertEquals(-67.26568603515625, coordinates[0].getLat(), 1e-8);

        assertEquals(-50.65733337402344, coordinates[24].getLon(), 1e-8);
        assertEquals(-77.25603485107422, coordinates[24].getLat(), 1e-8);

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertEquals(1, timeAxes.length);
        Point[] locations = coordinates[0].getCoordinates();
        Date time = timeAxes[0].getTime(locations[0]);
        TestUtil.assertCorrectUTCDate(2003, 5, 22, 14, 45, 1, 454, time);

        locations = coordinates[9].getCoordinates();
        time = timeAxes[0].getTime(locations[0]);
        TestUtil.assertCorrectUTCDate(2003, 5, 22, 14, 49, 59, 249, time);
    }

    @Test
    public void testReadAcquisitionInfo_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final AcquisitionInfo acquisitionInfo = reader.read();
        assertNotNull(acquisitionInfo);

        final Date sensingStart = acquisitionInfo.getSensingStart();
        TestUtil.assertCorrectUTCDate(2011, 6, 17, 22, 10, 0, 0, sensingStart);

        final Date sensingStop = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2011, 6, 17, 22, 15, 0, 0, sensingStop);

        final NodeType nodeType = acquisitionInfo.getNodeType();
        assertEquals(NodeType.UNDEFINED, nodeType);

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);
        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(31, coordinates.length);
        assertEquals(-120.74213409423832, coordinates[0].getLon(), 1e-8);
        assertEquals(68.62024688720705, coordinates[0].getLat(), 1e-8);

        assertEquals(-169.29559326171875, coordinates[24].getLon(), 1e-8);
        assertEquals(61.602462768554695, coordinates[24].getLat(), 1e-8);

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertEquals(1, timeAxes.length);
        Point[] locations = coordinates[0].getCoordinates();
        Date time = timeAxes[0].getTime(locations[0]);
        TestUtil.assertCorrectUTCDate(2011, 6, 17, 22, 10, 0, 0, time);

        locations = coordinates[9].getCoordinates();
        time = timeAxes[0].getTime(locations[0]);
        TestUtil.assertCorrectUTCDate(2011, 6, 17, 22, 14, 59, 489, time);
    }

    @Test
    public void testGetProductSize_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);
        final Dimension productSize = reader.getProductSize();
        assertEquals(1354, productSize.getNx());
        assertEquals(2030, productSize.getNy());
    }

    @Test
    public void testGetProductSize_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);
        final Dimension productSize = reader.getProductSize();
        assertEquals(1354, productSize.getNx());
        assertEquals(2030, productSize.getNy());
    }

    @Test
    public void testGetTimeLocator_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);
        final TimeLocator timeLocator = reader.getTimeLocator();
        assertEquals(1053614674728L, timeLocator.getTimeFor(0, 0));
        assertEquals(1053614674728L, timeLocator.getTimeFor(269, 0));

        assertEquals(1053614704271L, timeLocator.getTimeFor(76, 203));
        assertEquals(1053614733814L, timeLocator.getTimeFor(145, 405));
    }

    @Test
    public void testGetTimeLocator_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);
        final TimeLocator timeLocator = reader.getTimeLocator();
        assertEquals(1308348573820L, timeLocator.getTimeFor(1, 1));
        assertEquals(1308348573820L, timeLocator.getTimeFor(270, 1));

        assertEquals(1308348603362L, timeLocator.getTimeFor(76, 204));
        assertEquals(1308348632905L, timeLocator.getTimeFor(145, 406));
    }

    @Test
    public void testGetVariables_Terra() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        reader.open(file);
        final List<Variable> variables = reader.getVariables();
        assertEquals(113, variables.size());

        Variable variable = variables.get(0);
        assertEquals("Noise_in_Thermal_Detectors_ch20", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(2);
        assertEquals("Noise_in_Thermal_Detectors_ch22", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(14);
        assertEquals("Noise_in_Thermal_Detectors_ch35", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(18);
        assertEquals("EV_1KM_RefSB_ch10", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(34);
        assertEquals("EV_1KM_RefSB_Uncert_Indexes_ch11", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(48);
        assertEquals("EV_1KM_Emissive_ch22", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(65);
        assertEquals("EV_1KM_Emissive_Uncert_Indexes_ch23", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(69);
        assertEquals("EV_1KM_Emissive_Uncert_Indexes_ch28", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(72);
        assertEquals("EV_1KM_Emissive_Uncert_Indexes_ch31", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(78);
        assertEquals("EV_250_Aggr1km_RefSB_ch01", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(83);
        assertEquals("EV_250_Aggr1km_RefSB_Samples_Used_ch02", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(88);
        assertEquals("EV_500_Aggr1km_RefSB_ch07", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(93);
        assertEquals("EV_500_Aggr1km_RefSB_Uncert_Indexes_ch07", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(98);
        assertEquals("EV_500_Aggr1km_RefSB_Samples_Used_ch07", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(103);
        assertEquals("Longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(108);
        assertEquals("SolarZenith", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(112);
        assertEquals("gflags", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());
    }

    @Test
    public void testGetVariables_Aqua() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        reader.open(file);
        final List<Variable> variables = reader.getVariables();
        assertEquals(113, variables.size());

        for(final Variable var : variables) {
            System.out.println(var.getShortName());
        }

        Variable variable = variables.get(1);
        assertEquals("Noise_in_Thermal_Detectors_ch21", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(3);
        assertEquals("Noise_in_Thermal_Detectors_ch23", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(10);
        assertEquals("Noise_in_Thermal_Detectors_ch31", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(19);
        assertEquals("EV_1KM_RefSB_ch11", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(35);
        assertEquals("EV_1KM_RefSB_Uncert_Indexes_ch12", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(49);
        assertEquals("EV_1KM_Emissive_ch23", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(66);
        assertEquals("EV_1KM_Emissive_Uncert_Indexes_ch24", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(70);
        assertEquals("EV_1KM_Emissive_Uncert_Indexes_ch29", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(73);
        assertEquals("EV_1KM_Emissive_Uncert_Indexes_ch32", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(79);
        assertEquals("EV_250_Aggr1km_RefSB_ch02", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(84);
        assertEquals("EV_500_Aggr1km_RefSB_ch03", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(89);
        assertEquals("EV_500_Aggr1km_RefSB_Uncert_Indexes_ch03", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(94);
        assertEquals("EV_500_Aggr1km_RefSB_Samples_Used_ch03", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(99);
        assertEquals("EV_Band26", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(104);
        assertEquals("Height", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(109);
        assertEquals("SolarAzimuth", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(111);
        assertEquals("WaterPresent", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());
    }

    @Test
    public void testReadAcquisitionTime_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);
        final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(35, 109, new Interval(3, 5));
        assertEquals(15, acquisitionTime.getSize());

        // one scan
        NCTestUtils.assertValueAt(1053614689, 0, 0, acquisitionTime);
        NCTestUtils.assertValueAt(1053614689, 1, 0, acquisitionTime);
        NCTestUtils.assertValueAt(1053614689, 1, 1, acquisitionTime);

        // next scan
        NCTestUtils.assertValueAt(1053614690, 1, 3, acquisitionTime);
        NCTestUtils.assertValueAt(1053614690, 2, 3, acquisitionTime);
        NCTestUtils.assertValueAt(1053614690, 1, 4, acquisitionTime);
    }

    @Test
    public void testReadAcquisitionTime_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);
        final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(36, 119, new Interval(3, 5));
        assertEquals(15, acquisitionTime.getSize());

        // one scan
        NCTestUtils.assertValueAt(1308348590, 0, 0, acquisitionTime);
        NCTestUtils.assertValueAt(1308348590, 1, 0, acquisitionTime);
        NCTestUtils.assertValueAt(1308348590, 1, 1, acquisitionTime);

        // next scan
        NCTestUtils.assertValueAt(1308348591, 1, 3, acquisitionTime);
        NCTestUtils.assertValueAt(1308348591, 2, 3, acquisitionTime);
        NCTestUtils.assertValueAt(1308348591, 1, 4, acquisitionTime);
    }

    @Test
    public void testReadAcquisitionTime_Terra_outside_top() throws IOException {
        // fillValue see {@link com.bc.fiduceo.reader.Reader#readAcquisitionTime(int, int, Interval)}
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();

        final File file = getTerraFile();
        reader.open(file);

        final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(37, 1, new Interval(3, 15));
        assertEquals(45, acquisitionTime.getSize());

        // outside
        NCTestUtils.assertValueAt(fillValue, 0, 0, acquisitionTime);
        NCTestUtils.assertValueAt(fillValue, 1, 1, acquisitionTime);
        NCTestUtils.assertValueAt(fillValue, 2, 5, acquisitionTime);

        // first scan
        NCTestUtils.assertValueAt(1053614674, 0, 6, acquisitionTime);
        NCTestUtils.assertValueAt(1053614674, 1, 12, acquisitionTime);
        NCTestUtils.assertValueAt(1053614674, 2, 14, acquisitionTime);
    }

    @Test
    public void testReadAcquisitionTime_Aqua_outside_bottom() throws IOException {
        // fillValue see {@link com.bc.fiduceo.reader.Reader#readAcquisitionTime(int, int, Interval)}
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();

        final File file = getAquaFile();
        reader.open(file);
        final int ny = reader.getProductSize().getNy();

        final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(36, ny - 2, new Interval(5, 5));
        assertEquals(25, acquisitionTime.getSize());

        // last scan
        NCTestUtils.assertValueAt(1308348872, 3, 3, acquisitionTime);
        NCTestUtils.assertValueAt(1308348872, 4, 3, acquisitionTime);

        // outside
        NCTestUtils.assertValueAt(fillValue, 3, 4, acquisitionTime);
        NCTestUtils.assertValueAt(fillValue, 4, 4, acquisitionTime);
    }

    @Test
    public void testReadRaw_250m_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readRaw(46, 89, new Interval(3, 3), "EV_250_Aggr1km_RefSB_ch02");
        NCTestUtils.assertValueAt(7035, 0, 0, array);
        NCTestUtils.assertValueAt(6161, 1, 0, array);

        NCTestUtils.assertValueAt(6967, 1, 1, array);
        NCTestUtils.assertValueAt(7867, 2, 1, array);
    }

    @Test
    public void testReadRaw_250m_Terra_top_edge() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readRaw(47, 0, new Interval(3, 3), "EV_250_Aggr1km_RefSB_Uncert_Indexes_ch01");
        NCTestUtils.assertValueAt(-1, 0, 0, array);
        NCTestUtils.assertValueAt(-1, 1, 0, array);

        NCTestUtils.assertValueAt(-1, 1, 1, array);
        NCTestUtils.assertValueAt(-1, 2, 1, array);
    }

    @Test
    public void testReadRaw_500m_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readRaw(48, 92, new Interval(3, 3), "EV_500_Aggr1km_RefSB_Samples_Used_ch05");
        NCTestUtils.assertValueAt(-1, 0, 0, array);
        NCTestUtils.assertValueAt(-1, 1, 0, array);

        NCTestUtils.assertValueAt(-1, 1, 1, array);
        NCTestUtils.assertValueAt(-1, 2, 1, array);
    }

    @Test
    public void testReadRaw_500m_Aqua_left_edge() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readRaw(1353, 92, new Interval(3, 3), "EV_500_Aggr1km_RefSB_Uncert_Indexes_ch06");
        NCTestUtils.assertValueAt(1, 0, 0, array);
        NCTestUtils.assertValueAt(1, 1, 0, array);
        NCTestUtils.assertValueAt(-1, 2, 0, array);

        NCTestUtils.assertValueAt(15, 0, 1, array);
        NCTestUtils.assertValueAt(15, 1, 1, array);
        NCTestUtils.assertValueAt(-1, 2, 1, array);
    }

    @Test
    public void testReadRaw_1km_refl_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readRaw(49, 93, new Interval(3, 3), "EV_1KM_RefSB_ch13L");
        NCTestUtils.assertValueAt(65535, 0, 0, array);
        NCTestUtils.assertValueAt(65535, 1, 0, array);

        NCTestUtils.assertValueAt(65535, 1, 1, array);
        NCTestUtils.assertValueAt(65535, 2, 1, array);
    }

    @Test
    public void testReadRaw_1km_refl_Aqua_bottom() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readRaw(50, 2029, new Interval(3, 3), "EV_1KM_RefSB_Uncert_Indexes_ch26");
        NCTestUtils.assertValueAt(5, 0, 0, array);
        NCTestUtils.assertValueAt(5, 1, 0, array);

        NCTestUtils.assertValueAt(4, 1, 1, array);
        NCTestUtils.assertValueAt(4, 2, 1, array);

        NCTestUtils.assertValueAt(-1, 0, 2, array);
        NCTestUtils.assertValueAt(-1, 1, 2, array);
    }

    @Test
    public void testReadRaw_1km_emissive_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readRaw(51, 94, new Interval(3, 3), "EV_1KM_Emissive_ch22");
        NCTestUtils.assertValueAt(3682, 0, 0, array);
        NCTestUtils.assertValueAt(3738, 1, 0, array);

        NCTestUtils.assertValueAt(3693, 1, 1, array);
        NCTestUtils.assertValueAt(3542, 2, 1, array);
    }

    @Test
    public void testReadRaw_1km_emissive_Aqua_left() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readRaw(0, 95, new Interval(3, 3), "EV_1KM_Emissive_Uncert_Indexes_ch23");
        NCTestUtils.assertValueAt(-1, 0, 0, array);
        NCTestUtils.assertValueAt(1, 1, 0, array);
        NCTestUtils.assertValueAt(1, 2, 0, array);

        NCTestUtils.assertValueAt(-1, 0, 1, array);
        NCTestUtils.assertValueAt(1, 1, 1, array);
        NCTestUtils.assertValueAt(2, 2, 1, array);
    }

    @Test
    public void testReadRaw_ThermalNoise_Aqua_left() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readRaw(0, 97, new Interval(3, 3), "Noise_in_Thermal_Detectors_ch23");
        NCTestUtils.assertValueAt(-127, 0, 0, array);
        NCTestUtils.assertValueAt(25, 1, 0, array);
        NCTestUtils.assertValueAt(25, 2, 0, array);

        NCTestUtils.assertValueAt(-127, 0, 1, array);
        NCTestUtils.assertValueAt(25, 1, 1, array);
        NCTestUtils.assertValueAt(25, 2, 1, array);
    }

    @Test
    public void testReadRaw_ThermalNoise_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readRaw(52, 95, new Interval(3, 3), "Noise_in_Thermal_Detectors_ch24");
        NCTestUtils.assertValueAt(29, 0, 0, array);
        NCTestUtils.assertValueAt(29, 1, 0, array);

        NCTestUtils.assertValueAt(30, 1, 1, array);

        NCTestUtils.assertValueAt(29, 2, 2, array);
    }

    @Test
    public void testReadRaw_MOD03_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readRaw(52, 95, new Interval(3, 3), "Height");
        NCTestUtils.assertValueAt(0, 0, 0, array);
        NCTestUtils.assertValueAt(0, 1, 0, array);

        NCTestUtils.assertValueAt(0, 1, 1, array);
        NCTestUtils.assertValueAt(0, 2, 1, array);
    }

    @Test
    public void testReadRaw_MYD03_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readRaw(53, 96, new Interval(3, 3), "SensorZenith");
        NCTestUtils.assertValueAt(5945, 0, 0, array);
        NCTestUtils.assertValueAt(5934, 1, 0, array);

        NCTestUtils.assertValueAt(5934, 1, 1, array);
        NCTestUtils.assertValueAt(5923, 2, 1, array);
    }

    @Test
    public void testReadScaled_250m_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readScaled(47, 90, new Interval(3, 3), "EV_250_Aggr1km_RefSB_ch01");
        NCTestUtils.assertValueAt(95.12491860240698, 0, 0, array);
        NCTestUtils.assertValueAt(109.4346143770963, 1, 0, array);

        NCTestUtils.assertValueAt(84.2198552750051, 1, 1, array);
        NCTestUtils.assertValueAt(70.83171414770186, 2, 1, array);
    }

    @Test
    public void testReadScaled_250m_Terra_top_edge() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readScaled(48, 0, new Interval(3, 3), "EV_250_Aggr1km_RefSB_Uncert_Indexes_ch02");
        NCTestUtils.assertValueAt(-7.0, 0, 0, array);
        NCTestUtils.assertValueAt(-7.0, 1, 0, array);

        NCTestUtils.assertValueAt(-7.0, 1, 1, array);
        NCTestUtils.assertValueAt(-7.0, 2, 1, array);
    }

    @Test
    public void testReadScaled_500m_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readScaled(49, 93, new Interval(3, 3), "EV_500_Aggr1km_RefSB_Samples_Used_ch06");
        NCTestUtils.assertValueAt(-1.0, 0, 0, array);
        NCTestUtils.assertValueAt(-1.0, 1, 0, array);

        NCTestUtils.assertValueAt(-1.0, 1, 1, array);
        NCTestUtils.assertValueAt(-1.0, 2, 1, array);
    }

    @Test
    public void testReadScaled_500m_Aqua_left_edge() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readScaled(1353, 93, new Interval(3, 3), "EV_500_Aggr1km_RefSB_Uncert_Indexes_ch07");
        NCTestUtils.assertValueAt(0.0, 0, 0, array);
        NCTestUtils.assertValueAt(5.0, 1, 0, array);
        NCTestUtils.assertValueAt(-5.0, 2, 0, array);

        NCTestUtils.assertValueAt(10.0, 0, 1, array);
        NCTestUtils.assertValueAt(5.0, 1, 1, array);
        NCTestUtils.assertValueAt(-5.0, 2, 1, array);
    }

    @Test
    public void testReadScaled_1km_refl_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readScaled(50, 94, new Interval(3, 3), "EV_1KM_RefSB_ch13H");
        NCTestUtils.assertValueAt(371.64215208758833, 0, 0, array);
        NCTestUtils.assertValueAt(371.64215208758833, 1, 0, array);

        NCTestUtils.assertValueAt(371.64215208758833, 1, 1, array);
        NCTestUtils.assertValueAt(371.64215208758833, 2, 1, array);
    }

    @Test
    public void testReadScaled_1km_refl_Aqua_bottom() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readScaled(51, 2029, new Interval(3, 3), "EV_1KM_RefSB_Uncert_Indexes_ch19");
        NCTestUtils.assertValueAt(0.0, 0, 0, array);
        NCTestUtils.assertValueAt(0.0, 1, 0, array);

        NCTestUtils.assertValueAt(0.0, 1, 1, array);
        NCTestUtils.assertValueAt(0.0, 2, 1, array);

        NCTestUtils.assertValueAt(-7.0, 0, 2, array);
        NCTestUtils.assertValueAt(-7.0, 1, 2, array);
    }

    @Test
    public void testReadScaled_1km_emissive_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readScaled(52, 95, new Interval(3, 3), "EV_1KM_Emissive_ch23");
        NCTestUtils.assertValueAt(2730.871909198817, 0, 0, array);
        NCTestUtils.assertValueAt(2730.861151057761, 1, 0, array);

        NCTestUtils.assertValueAt(2730.8628122413065, 1, 1, array);
        NCTestUtils.assertValueAt(2730.8533197639044, 2, 1, array);
    }

    @Test
    public void testReadScaled_1km_emissive_Aqua_left() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readScaled(0, 96, new Interval(3, 3), "EV_1KM_Emissive_Uncert_Indexes_ch24");
        NCTestUtils.assertValueAt(-4.0, 0, 0, array);
        NCTestUtils.assertValueAt(16.0, 1, 0, array);
        NCTestUtils.assertValueAt(16.0, 2, 0, array);

        NCTestUtils.assertValueAt(-4.0, 0, 1, array);
        NCTestUtils.assertValueAt(16.0, 1, 1, array);
        NCTestUtils.assertValueAt(16.0, 2, 1, array);
    }

    @Test
    public void testReadScaled_MOD03_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readScaled(54, 97, new Interval(3, 3), "SensorAzimuth");
        NCTestUtils.assertValueAt(144.84, 0, 0, array);
        NCTestUtils.assertValueAt(144.79, 1, 0, array);

        NCTestUtils.assertValueAt(144.72, 1, 1, array);
        NCTestUtils.assertValueAt(144.67, 2, 1, array);
    }

    @Test
    public void testReadScaled_MYD03_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readScaled(55, 97, new Interval(3, 3), "Range");
        NCTestUtils.assertValueAt(1237150.0, 0, 0, array);
        NCTestUtils.assertValueAt(1234225.0, 1, 0, array);

        NCTestUtils.assertValueAt(1234225.0, 1, 1, array);
        NCTestUtils.assertValueAt(1231325.0, 2, 1, array);
    }

    @Test
    public void testReadScaled_ThermalNoise_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readScaled(55, 98, new Interval(3, 3), "Noise_in_Thermal_Detectors_ch25");
        NCTestUtils.assertValueAt(27.0, 0, 0, array);
        NCTestUtils.assertValueAt(29.0, 1, 1, array);
        NCTestUtils.assertValueAt(28.0, 2, 2, array);
    }

    @Test
    public void testGetPixelLocator_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final PixelLocator pixelLocator = reader.getPixelLocator();
        assertNotNull(pixelLocator);

        final Point2D geoLocation = pixelLocator.getGeoLocation(856, 1603, null);
        assertEquals(-168.64809403912324, geoLocation.getX(), 1e-8);
        assertEquals(-83.37166595458984, geoLocation.getY(), 1e-8);

        final Point2D[] pixelLocation = pixelLocator.getPixelLocation(-168.64809403912324, -83.37166595458984);
        assertEquals(1, pixelLocation.length);
        assertEquals(856.0091562622307, pixelLocation[0].getX(), 1e-8);
        assertEquals(1602.9897901257505, pixelLocation[0].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocator_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final PixelLocator pixelLocator = reader.getPixelLocator();
        assertNotNull(pixelLocator);

        final Point2D geoLocation = pixelLocator.getGeoLocation(857.5, 1604.5, null);
        assertEquals(176.8111726476808, geoLocation.getX(), 1e-8);
        assertEquals(77.82994079589844, geoLocation.getY(), 1e-8);

        final Point2D[] pixelLocation = pixelLocator.getPixelLocation(176.8111726476808, 77.82994079589844);
        assertEquals(1, pixelLocation.length);
        assertEquals(857.494889374718, pixelLocation[0].getX(), 1e-8);
        assertEquals(1604.5010092939542, pixelLocation[0].getY(), 1e-8);
    }

    private File getTerraFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mod021km-te", "v61", "2003", "05", "22", "MOD021KM.A2003142.1445.061.2017194130122.hdf"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getAquaFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"myd021km-aq", "v61", "2011", "06", "17", "MYD021KM.A2011168.2210.061.2018032001033.hdf"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
