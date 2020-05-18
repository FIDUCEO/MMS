package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

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
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() throws IOException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(geometryFactory);

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
        assertEquals(94, variables.size());

        Variable variable = variables.get(0);
        assertEquals("Latitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(2);
        assertEquals("EV_1KM_RefSB_ch08", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(14);
        assertEquals("EV_1KM_RefSB_ch18", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(18);
        assertEquals("EV_1KM_RefSB_Uncert_Indexes_ch09", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(34);
        assertEquals("EV_1KM_Emissive_ch22", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(48);
        assertEquals("EV_1KM_Emissive_Uncert_Indexes_ch20", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(65);
        assertEquals("EV_250_Aggr1km_RefSB_ch02", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(69);
        assertEquals("EV_250_Aggr1km_RefSB_Samples_Used_ch02", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(72);
        assertEquals("EV_500_Aggr1km_RefSB_ch05", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(78);
        assertEquals("EV_500_Aggr1km_RefSB_Uncert_Indexes_ch06", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        // @todo 1 tb/tb continue here 2020-05-15
    }

    @Test
    public void testGetVariables_Aqua() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        reader.open(file);
        final List<Variable> variables = reader.getVariables();
        assertEquals(94, variables.size());

        Variable variable = variables.get(1);
        assertEquals("Longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(3);
        assertEquals("EV_1KM_RefSB_ch09", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(10);
        assertEquals("EV_1KM_RefSB_ch14H", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(19);
        assertEquals("EV_1KM_RefSB_Uncert_Indexes_ch10", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(35);
        assertEquals("EV_1KM_Emissive_ch23", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(49);
        assertEquals("EV_1KM_Emissive_Uncert_Indexes_ch21", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(66);
        assertEquals("EV_250_Aggr1km_RefSB_Uncert_Indexes_ch01", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        variable = variables.get(70);
        assertEquals("EV_500_Aggr1km_RefSB_ch03", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(73);
        assertEquals("EV_500_Aggr1km_RefSB_ch06", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(79);
        assertEquals("EV_500_Aggr1km_RefSB_Uncert_Indexes_ch07", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());

        // @todo 1 tb/tb continue here 2020-05-15
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
