package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
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
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ProductReaderTestRunner.class)
public class AIRS_L1B_Reader_IO_Test {

    private static final String POLYGON = "POLYGON ((-166.1730278143979 12.279422414280331, -162.39683002343054 12.90958056597399, -159.94095951942776 13.315677944081953, -157.6556993669512 13.67661657960202, -154.74718921639902 14.083472777091306, -151.5331749119938 14.457717633993736, -152.06650535950993 17.345604743760823, -152.588313707934 20.234188963104703, -153.09977952563165 23.1231074508905, -153.59932438347053 26.01299578883174, -154.09564030862734 28.902654665660663, -154.5821016348156 31.79208533265723, -155.06233296781772 34.68070844814089, -155.22100159384635 35.643407945336584, -156.4154886637448 35.564455266536505, -160.98026268214568 35.12461153309757, -163.90151635747785 34.72262175342894, -166.58712305434253 34.28162850478243, -169.9464526366916 33.66017283151052, -173.5921218067924 32.90472542332997, -172.5375133144322 30.099306276396508, -171.54686144641073 27.283026236683604, -170.62209588981182 24.455439692836173, -169.7457472101792 21.62002239734624, -168.9188957089004 18.77628013209512, -168.13382558718357 15.92572437459079, -167.38615193712403 13.06898909272133, -166.1730278143979 12.279422414280331))";

    private SatelliteObservation observation;
    private AIRS_L1B_Reader airsL1bReader;

    @Before
    public void setUp() throws IOException {
        final File dataDirectory = TestUtil.getTestDataDirectory();
        final File airsL1bFile = new File(dataDirectory, "AIRS.2015.08.03.001.L1B.AMSU_Rad.v5.0.14.0.R15214205337.hdf");

        airsL1bReader = new AIRS_L1B_Reader();
        airsL1bReader.open(airsL1bFile);
        observation = airsL1bReader.read();
    }

    @After
    public void endTest() throws IOException {
        airsL1bReader.close();
    }

    @Test
    public void testPolygon() throws com.vividsolutions.jts.io.ParseException {
        assertNotNull(observation);
        Geometry geometryTest = observation.getGeoBounds();
        assertNotNull(geometryTest);

        WKTReader wktReader = new WKTReader(new GeometryFactory());
        Polygon read = (Polygon) wktReader.read(POLYGON);
        assertTrue(geometryTest.contains(read));
    }

    @Test
    public void testReadSatelliteObservation() throws IOException, ParseException {
        assertNotNull(observation);
        final Date startTime = observation.getStartTime();
        final Date stopTime = observation.getStopTime();
        assertNotNull(startTime);
        assertNotNull(stopTime);
        DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        final Date expectedStart = dateFormat.parse("2015-08-03 00:05:22.000000Z");
        final Date expectedStop = dateFormat.parse("2015-08-03 00:11:21.999999Z");
        assertEquals(expectedStart.getTime(), startTime.getTime());
        assertEquals(expectedStop.getTime(), stopTime.getTime());
    }

    @Test
    public void testNoteType() {
        assertNotNull(observation);
        assertEquals(NodeType.fromId(0), observation.getNodeType());
    }
}
