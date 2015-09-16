package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.NodeType;
import com.vividsolutions.jts.geom.Coordinate;
import org.esa.snap.framework.datamodel.ProductData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ProductReaderTestRunner.class)
public class AIRS_L1B_Reader_IO_Test {

    private AIRS_L1B_Reader airsL1bReader;
    private File dataDirectory;
    private DateFormat dateFormat;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();
        airsL1bReader = new AIRS_L1B_Reader();
        dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
    }

    @After
    public void endTest() throws IOException {
        airsL1bReader.close();
    }

    @Test
    public void testRead_closeToPole() throws com.vividsolutions.jts.io.ParseException, IOException, ParseException {
        final File airsL1bFile = new File(dataDirectory, "AIRS.2015.09.02.006.L1B.AIRS_Rad.v5.0.23.0.G15246014542.hdf");
        airsL1bReader.open(airsL1bFile);

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final List<Coordinate> coordinates = acquisitionInfo.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(41, coordinates.size());
        assertCoordinate(-164.84726526737956, 78.65846904183893, coordinates.get(0));
        assertCoordinate(14.132166700261118, 82.20332415748388, coordinates.get(10));
        assertCoordinate(71.66176722582615, 70.98479982855267, coordinates.get(20));

        assertEquals(0, acquisitionInfo.getTimeAxisStartIndex());
        assertEquals(12, acquisitionInfo.getTimeAxisEndIndex());

        assertCorrectDate("2015-09-02 00:35:22.000000Z", acquisitionInfo.getSensingStart());
        assertCorrectDate("2015-09-02 00:41:21.999999Z", acquisitionInfo.getSensingStop());

        assertEquals(NodeType.DESCENDING, acquisitionInfo.getNodeType());
    }

    @Test
    public void testRead_descendingNode() throws com.vividsolutions.jts.io.ParseException, IOException, ParseException {
        final File airsL1bFile = new File(dataDirectory, "AIRS.2015.09.02.023.L1B.AIRS_Rad.v5.0.23.0.G15246021652.hdf");
        airsL1bReader.open(airsL1bFile);

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final List<Coordinate> coordinates = acquisitionInfo.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(41, coordinates.size());
        assertCoordinate(-6.4170300611108315, 88.23613967607469, coordinates.get(1));
        assertCoordinate(-13.02427652238762, 69.09764314472332, coordinates.get(11));
        assertCoordinate(27.651190877552047, 64.80095475900657, coordinates.get(21));

        assertEquals(0, acquisitionInfo.getTimeAxisStartIndex());
        assertEquals(12, acquisitionInfo.getTimeAxisEndIndex());

        assertCorrectDate("2015-09-02 02:17:22.000000Z", acquisitionInfo.getSensingStart());
        assertCorrectDate("2015-09-02 02:23:21.999999Z", acquisitionInfo.getSensingStop());

        assertEquals(NodeType.DESCENDING, acquisitionInfo.getNodeType());
    }

    @Test
    public void testRead_ascendingNode() throws com.vividsolutions.jts.io.ParseException, IOException, ParseException {
        final File airsL1bFile = new File(dataDirectory, "AIRS.2015.09.02.135.L1B.AIRS_Rad.v5.0.23.0.G15246114803.hdf");
        airsL1bReader.open(airsL1bFile);

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final List<Coordinate> coordinates = acquisitionInfo.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(41, coordinates.size());
        assertCoordinate(-3.5416849171828058, 15.6620445809086, coordinates.get(2));
        assertCoordinate(5.98667051416274, 24.885289473168978, coordinates.get(12));
        assertCoordinate(-2.9671231963127838, 38.0742812192399, coordinates.get(22));

        assertEquals(8, acquisitionInfo.getTimeAxisStartIndex());
        assertEquals(20, acquisitionInfo.getTimeAxisEndIndex());

        assertCorrectDate("2015-09-02 13:29:22.000000Z", acquisitionInfo.getSensingStart());
        assertCorrectDate("2015-09-02 13:35:21.999999Z", acquisitionInfo.getSensingStop());

        assertEquals(NodeType.ASCENDING, acquisitionInfo.getNodeType());
    }

    private void assertCorrectDate(String expected, Date date) throws ParseException {
        assertNotNull(date);

        final Date expectedDate = dateFormat.parse(expected);
        assertEquals(expectedDate.getTime(), date.getTime());
    }

    private void assertCoordinate(double expectedX, double expectedY, Coordinate coordinate) {
        assertEquals(expectedX, coordinate.x, 1e-8);
        assertEquals(expectedY, coordinate.y, 1e-8);
    }
}
