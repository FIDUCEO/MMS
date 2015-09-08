package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import org.esa.snap.framework.datamodel.ProductData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ProductReaderTestRunner.class)
public class AIRS_L1B_Reader_IO_Test {

    private AIRS_L1B_Reader airsL1bReader;
    private File dataDirectory;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();

        airsL1bReader = new AIRS_L1B_Reader();
    }

    @After
    public void endTest() throws IOException {
        airsL1bReader.close();
    }

    @Test
    public void testRead_closeToPole() throws com.vividsolutions.jts.io.ParseException, IOException, ParseException {
        final File airsL1bFile = new File(dataDirectory, "AIRS.2015.09.02.006.L1B.AIRS_Rad.v5.0.23.0.G15246014542.hdf");
        airsL1bReader.open(airsL1bFile);

        final SatelliteObservation observation = airsL1bReader.read();
        assertNotNull(observation);

        final Geometry geoBounds = observation.getGeoBounds();
        assertNotNull(geoBounds);
        System.out.println("geoBounds = " + geoBounds);

//
//        List<Coordinate> coordinates = new ArrayList<>();
//        final Coordinate[] boundaryCoordinates = geoBounds.getCoordinates();
//        for (int i = observation.getTimeAxisStartIndex(); i <= observation.getTimeAxisEndIndex(); i++) {
//            coordinates.add(boundaryCoordinates[i]);
//        }
//
//        final GeometryFactory geometryFactory = new GeometryFactory();
//        final LineString lineString = geometryFactory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
//        System.out.println("lineString = " + lineString);

//        final WKTReader wktReader = new WKTReader();
//        final Polygon expected = (Polygon) wktReader.read(POLYGON);
//        assertTrue(geoBounds.contains(expected));

//        final Date startTime = observation.getStartTime();
//        final Date stopTime = observation.getStopTime();
//        assertNotNull(startTime);
//        assertNotNull(stopTime);
//
//        final DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
//        final Date expectedStart = dateFormat.parse("2015-08-03 00:05:22.000000Z");
//        final Date expectedStop = dateFormat.parse("2015-08-03 00:11:21.999999Z");
//        assertEquals(expectedStart.getTime(), startTime.getTime());
//        assertEquals(expectedStop.getTime(), stopTime.getTime());

        assertEquals(NodeType.DESCENDING, observation.getNodeType());
    }

    @Test
    public void testRead_descendingNode() throws com.vividsolutions.jts.io.ParseException, IOException, ParseException {
        final File airsL1bFile = new File(dataDirectory, "AIRS.2015.09.02.023.L1B.AIRS_Rad.v5.0.23.0.G15246021652.hdf");
        airsL1bReader.open(airsL1bFile);

        final SatelliteObservation observation = airsL1bReader.read();
        assertNotNull(observation);

        final Geometry geoBounds = observation.getGeoBounds();
        assertNotNull(geoBounds);

//        System.out.println("geoBounds = " + geoBounds);
//
//        List<Coordinate> coordinates = new ArrayList<>();
//        final Coordinate[] boundaryCoordinates = geoBounds.getCoordinates();
//        for (int i = observation.getTimeAxisStartIndex(); i <= observation.getTimeAxisEndIndex(); i++) {
//            coordinates.add(boundaryCoordinates[i]);
//        }
//
//        final GeometryFactory geometryFactory = new GeometryFactory();
//        final LineString lineString = geometryFactory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
//        System.out.println("lineString = " + lineString);

//        final WKTReader wktReader = new WKTReader();
//        final Polygon expected = (Polygon) wktReader.read(POLYGON);
//        assertTrue(geoBounds.contains(expected));

//        final Date startTime = observation.getStartTime();
//        final Date stopTime = observation.getStopTime();
//        assertNotNull(startTime);
//        assertNotNull(stopTime);
//
//        final DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
//        final Date expectedStart = dateFormat.parse("2015-08-03 00:05:22.000000Z");
//        final Date expectedStop = dateFormat.parse("2015-08-03 00:11:21.999999Z");
//        assertEquals(expectedStart.getTime(), startTime.getTime());
//        assertEquals(expectedStop.getTime(), stopTime.getTime());

        assertEquals(NodeType.DESCENDING, observation.getNodeType());
    }

    @Test
    public void testRead_ascendingNode() throws com.vividsolutions.jts.io.ParseException, IOException, ParseException {
        final File airsL1bFile = new File(dataDirectory, "AIRS.2015.09.02.135.L1B.AIRS_Rad.v5.0.23.0.G15246114803.hdf");
        airsL1bReader.open(airsL1bFile);

        final SatelliteObservation observation = airsL1bReader.read();
        assertNotNull(observation);

        final Geometry geoBounds = observation.getGeoBounds();
        assertNotNull(geoBounds);

//        final WKTReader wktReader = new WKTReader();
//        final Polygon expected = (Polygon) wktReader.read(POLYGON);
//        assertTrue(geoBounds.contains(expected));

//        final Date startTime = observation.getStartTime();
//        final Date stopTime = observation.getStopTime();
//        assertNotNull(startTime);
//        assertNotNull(stopTime);
//
//        final DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
//        final Date expectedStart = dateFormat.parse("2015-08-03 00:05:22.000000Z");
//        final Date expectedStop = dateFormat.parse("2015-08-03 00:11:21.999999Z");
//        assertEquals(expectedStart.getTime(), startTime.getTime());
//        assertEquals(expectedStop.getTime(), stopTime.getTime());

        assertEquals(NodeType.ASCENDING, observation.getNodeType());
    }
}
