package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AMSU_MHS_L1B_Reader;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.geometry.s2.S2WKTReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author muhammad.bc
 */
public class S2MultiPolygonTest {
    private S2WKTReader s2WKTReader;
    private NetcdfFile netcdfFile;
    private AMSU_MHS_L1B_Reader reader;


    @Before
    public void setUp() throws IOException {
        s2WKTReader = new S2WKTReader();

        File testDataDirectory = TestUtil.getTestDataDirectory();
        File file = new File(testDataDirectory, "NSS.AMBX.NK.D15348.S0057.E0250.B9144748.GC.h5");
        netcdfFile = NetcdfFile.open(file.getPath());
        reader = new AMSU_MHS_L1B_Reader();
        reader.open(file);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
        netcdfFile.close();
    }


    @Test
    public void testMultiPolygonFromFile() throws IOException {
        AcquisitionInfo acquisitionInfo = reader.read();

        List<Polygon> polygons = acquisitionInfo.getPolygons();
        String multiPolygon = BoundingPolygonCreator.plotMultiPolygon(polygons);

        assertEquals("MULTIPOLYGON(((-97.86539752771206 21.40989945914043,-87.36939779286331 23.37569940948015,-78.28879802225856 24.473999381734757,-79.61389798878372 32.309399183795904,-80.8503979575471 40.14389898587979,-81.99969792851334 47.97329878809251,-83.04969790198811 55.79489859050227,-83.95919787901221 63.60739839314192,-84.60049786281161 71.41009819602914,-84.45279786654282 79.20129799920687,-77.60479803953785 86.95229780340014,83.14399789960589 85.1658978485284,86.30149781984073 77.40039804470145,86.1600978234128 69.61679824133171,85.43649784169247 61.827198438113555,84.48979786560812 54.03249863502424,83.4170978927068 46.23289883205871,82.25039792218013 38.429999029176535,80.99789795382094 30.625799226327217,79.65619798771513 22.824399423407158,78.21409802414564 15.03079962029005,76.65309806357982 7.251599816809178,74.94649810669216 -0.5048999872451532,73.0563981544401 -8.22829979213566,70.92909820818022 -15.906599598165483,68.48709826987033 -23.523499405746406,66.04839833147707 -30.00809924193164,77.71499803675397 -32.292799184215255,87.94689777827443 -33.357899157308566,89.21159774632542 -25.573299353964103,90.57729771182494 -17.788399550627222,92.05809767441679 -10.009099747148865,93.67609763354267 -2.2428999433395806,95.46419758837146 5.50149986102042,97.4688975377285 13.212299666229226,99.75799747990095 20.874799472658196,102.43129741236771 28.469099280810042,105.64229733125103 35.96709909139463,109.63469723039454 43.32499890551844,114.81299709957966 50.47019872501551,121.87539692116844 57.270398553228006,132.0412966643562 63.46869839664578,147.2297962806624 68.54659826836723,168.993895730855 71.56179819219687,-165.71089581379056 71.47999819426332,-144.24439635607996 68.33329827375564,-129.34169673255383 63.17339840410568,-119.36029698470523 56.921898562031856,-112.40929716030223 50.08059873485763,-107.29989728937655 42.89839891629527,-103.35219738910382 35.504099103090994,-100.17149746945506 27.969099293441104)),((87.94689777827443 -33.357899157308566,75.88619808295334 -32.01549919122045,66.04839833147707 -30.00809924193164,62.666498416911054 -37.44419905407994,58.438498523719325 -44.72189887022978,52.91999866312836 -51.75979869243748,45.34529885448137 -58.40899852446455,34.40199913093238 -64.3780983736724,18.16889954101498 -69.08939825465495,-4.3379998904129025 -71.5612981922095,-29.039099266410634 -70.90559820877388,-49.14029875861161 -67.38879829761572,-63.00409840838257 -62.06389843213401,-72.39099817124952 -55.75509859150771,-79.017698003845 -48.9033987645962,-83.94729787931283 -41.730098945809004,-87.79349778214964 -34.351599132205585,-90.91679770324845 -26.83359932212625,-93.5378976370339 -19.216099514560483,-95.79699757996424 -11.526099708826223,-97.7869975296926 -3.7828999044359093,-99.57119748461993 3.998899898979289,-101.19389744362707 11.807199701725041,-102.68649740592083 19.632299504046387,-104.07089737094795 27.466099306147953,-105.36129733834969 35.30209910819394,-106.4015973120695 42.03889893800807,-119.51969698067846 40.63899897337251,-130.0135967155802 38.3488990312253,-126.57459680245667 30.861099220383036,-123.7421968740091 23.261499412365083,-121.33829693473675 15.583399606330207,-119.24789698754466 7.849399801707477,-117.39399703437812 0.07659999806492124,-115.72389707656839 -7.7218998049283964,-114.2009971150401 -15.53519960754784,-112.79989715043486 -23.35469941001065,-111.5040971831695 -31.173399212493678,-110.30529721345374 -38.9859990151308,-109.20509724124715 -46.78889881801297,-108.22099726610759 -54.579698621200805,-107.40489728672401 -62.3572984247221,-106.90579729933233 -70.12109822859202,-107.27469729001315 -77.86889803286613,-112.95589714649395 -85.57109783829219,92.51309766292252 -86.506497814662,84.24859787170135 -78.82869800861954,83.66439788645948 -71.0931982040347,84.10069787543762 -63.3457983997505,84.88709785557148 -55.589098595701216,85.85159783120616 -47.8233987918793,86.93549780382455 -40.04919898827211)))",
                     multiPolygon);


        Polygon polygon = polygons.get(0);
        assertEquals("Polygon: (1) loops:\n" +
                             "loop <\n" +
                             "(21.40989945914043, -97.86539752771206)\n" +
                             "(23.37569940948015, -87.36939779286331)\n" +
                             "(24.473999381734757, -78.28879802225856)\n" +
                             "(32.309399183795904, -79.61389798878372)\n" +
                             "(40.14389898587979, -80.8503979575471)\n" +
                             "(47.97329878809251, -81.99969792851334)\n" +
                             "(55.79489859050227, -83.04969790198811)\n" +
                             "(63.60739839314192, -83.95919787901221)\n" +
                             "(71.41009819602914, -84.60049786281161)\n" +
                             "(79.20129799920687, -84.45279786654282)\n" +
                             "(86.95229780340014, -77.60479803953785)\n" +
                             "(85.1658978485284, 83.14399789960589)\n" +
                             "(77.40039804470145, 86.30149781984073)\n" +
                             "(69.61679824133171, 86.1600978234128)\n" +
                             "(61.827198438113555, 85.43649784169247)\n" +
                             "(54.03249863502424, 84.48979786560812)\n" +
                             "(46.23289883205871, 83.4170978927068)\n" +
                             "(38.429999029176535, 82.25039792218013)\n" +
                             "(30.625799226327217, 80.99789795382094)\n" +
                             "(22.824399423407158, 79.65619798771513)\n" +
                             "(15.03079962029005, 78.21409802414564)\n" +
                             "(7.251599816809178, 76.65309806357982)\n" +
                             "(-0.5048999872451532, 74.94649810669216)\n" +
                             "(-8.22829979213566, 73.0563981544401)\n" +
                             "(-15.906599598165483, 70.92909820818022)\n" +
                             "(-23.523499405746406, 68.48709826987033)\n" +
                             "(-30.00809924193164, 66.04839833147707)\n" +
                             "(-32.292799184215255, 77.71499803675397)\n" +
                             "(-33.357899157308566, 87.94689777827443)\n" +
                             "(-25.573299353964103, 89.21159774632542)\n" +
                             "(-17.788399550627222, 90.57729771182494)\n" +
                             "(-10.009099747148865, 92.05809767441679)\n" +
                             "(-2.2428999433395806, 93.67609763354267)\n" +
                             "(5.50149986102042, 95.46419758837146)\n" +
                             "(13.212299666229226, 97.4688975377285)\n" +
                             "(20.874799472658196, 99.75799747990095)\n" +
                             "(28.469099280810042, 102.43129741236771)\n" +
                             "(35.96709909139463, 105.64229733125103)\n" +
                             "(43.32499890551844, 109.63469723039454)\n" +
                             "(50.47019872501551, 114.81299709957966)\n" +
                             "(57.270398553228006, 121.87539692116844)\n" +
                             "(63.46869839664578, 132.0412966643562)\n" +
                             "(68.54659826836723, 147.2297962806624)\n" +
                             "(71.56179819219687, 168.993895730855)\n" +
                             "(71.47999819426332, -165.71089581379056)\n" +
                             "(68.33329827375564, -144.24439635607996)\n" +
                             "(63.17339840410568, -129.34169673255383)\n" +
                             "(56.921898562031856, -119.36029698470523)\n" +
                             "(50.08059873485763, -112.40929716030223)\n" +
                             "(42.89839891629527, -107.29989728937655)\n" +
                             "(35.504099103090994, -103.35219738910382)\n" +
                             "(27.969099293441104, -100.17149746945506)\n" +
                             ">\n", polygon.toString());

        Point[] coordinates1 = polygon.getCoordinates();
        assertTrue(coordinates1.length == 52);

        assertEquals(21.40989945914043, coordinates1[0].getLat(), 1e-8);
        assertEquals(-97.86539752771206, coordinates1[0].getLon(), 1e-8);

        assertEquals(27.969099293441104, coordinates1[51].getLat(), 1e-8);
        assertEquals(-100.17149746945506, coordinates1[51].getLon(), 1e-8);

    }

    @Test
    public void testS2MultiPolygon() {
        S2MultiPolygon s2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON(((30 20, 100 10)),((100 10, 300 10)),((30 20,100 10)))");
        Point[] coordinates = s2MultiPolygon.getCoordinates();
        assertTrue(coordinates.length > 2);
        assertNotNull(s2MultiPolygon);
        assertEquals(coordinates[0].toString(), "POINT(29.999999999999993 20.0)");
        assertEquals(coordinates[1].toString(), "POINT(100.0 10.0)");
    }

    @Test
    public void testIntersectMultiPolygon() {
        S2Polygon s2Polygon = createS2Polygon("POLYGON ((10 10, 80 10, 80 80, 10 80))");
        S2MultiPolygon s2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        Geometry intersection = s2MultiPolygon.intersection(s2Polygon);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        Point[] coordinates = intersection.getCoordinates();
        assertEquals(15, coordinates.length);
        assertEquals(19.999999999999993, coordinates[0].getLon(), 1e-8);
        assertEquals(11.039051540001541, coordinates[0].getLat(), 1e-8);

        assertEquals(49.99999999999999, coordinates[2].getLon(), 1e-8);
        assertEquals(0.0, coordinates[2].getLat(), 1e-8);
        assertEquals(49.99999999999999, coordinates[14].getLon(), 1e-8);
        assertEquals(81.75015492348156, coordinates[14].getLat(), 1e-8);
    }


    @Test
    public void testGetCoordinates() {
        S2MultiPolygon s2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        final Point[] coordinates = s2MultiPolygon.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(8, coordinates.length);
        assertEquals(20.0, coordinates[0].getLon(), 1e-8);
        assertEquals(-0.0, coordinates[0].getLat(), 1e-8);

        assertEquals(20.0, coordinates[3].getLon(), 1e-8);
        assertEquals(49.99999999999999, coordinates[3].getLat(), 1e-8);

        assertEquals(20.0, coordinates[7].getLon(), 1e-8);
        assertEquals(90.0, coordinates[7].getLat(), 1e-8);
    }


    @Test
    public void testPlotMultiPolygon() {
        List<Polygon> s2PolygonList = new ArrayList<>();
        s2PolygonList.add(createS2Polygon("POLYGON ((10 10, 80 10, 80 80, 10 80))"));
        s2PolygonList.add(createS2Polygon("POLYGON((-8 -10,-8 12,9 12,9 -10,-8 -10))"));
        String multiPolygon = BoundingPolygonCreator.plotMultiPolygon(s2PolygonList);
        assertEquals("MULTIPOLYGON(((9.999999999999998 10.0,80.0 10.0,80.0 80.0,10.0 80.0)),((9.0 -10.0,9.000000000000002 12.000000000000002,-7.999999999999998 12.000000000000002,-7.999999999999998 -10.0)))", multiPolygon);

    }


    private S2MultiPolygon createS2MultiPolygon(String wellKnownText) {
        List<com.google.common.geometry.S2Polygon> read = (List<com.google.common.geometry.S2Polygon>) s2WKTReader.read(wellKnownText);
        return new S2MultiPolygon(read);
    }

    private S2Polygon createS2Polygon(String wellKnownText) {
        com.google.common.geometry.S2Polygon polygon_1 = (com.google.common.geometry.S2Polygon) s2WKTReader.read(wellKnownText);
        return new S2Polygon(polygon_1);
    }
}
