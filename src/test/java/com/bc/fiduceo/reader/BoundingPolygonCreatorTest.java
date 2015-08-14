package com.bc.fiduceo.reader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.ArrayDouble;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @muhammad.bc on 8/14/2015.
 */
public class BoundingPolygonCreatorTest {

    BoundingPolygonCreator boundingPolygonCreator;
    File productFile;


    @Before
    public void setUp() {
        boundingPolygonCreator = new BoundingPolygonCreator(8, 8);
        assertNotNull(boundingPolygonCreator);
        final URL resourceUrl = AIRS_L1B_ReaderTest.class.getResource("AIRS.2015.08.03.001.L1B.AMSU_Rad.v5.0.14.0.R15214205337.hdf");
        assertNotNull(resourceUrl);
        productFile = new File(resourceUrl.getFile());
    }

    @Test
    public void testCreateCoordinate() throws IOException, ParseException {
        NetcdfFile netcdfFile = NetcdfFile.open(productFile.getPath());
        List<ArrayDouble.D2> coordinate = boundingPolygonCreator.createCoordinate(netcdfFile);
        assertNotNull(coordinate);
        assertEquals(coordinate.size(), 2);
        assertEquals(coordinate.get(0).getSize(), 1350);

        Geometry geometry = boundingPolygonCreator.createPolygon(coordinate.get(0), coordinate.get(1));
        assertNotNull(geometry);
        WKTReader wkbReader = new WKTReader(new GeometryFactory());
        assertTrue(geometry.equals(wkbReader.read(POLYGON)));
    }


    private static final String POLYGON = "POLYGON ((-166.1730278143979 12.279422414280331, -161.50715746033566 13.057308306143742, -158.43923494622842 13.556043659558377, -154.74718921639902 14.083472777091306, -151.5331749119938 14.457717633993736, -152.24160661916514 18.30840676225526, -152.93041231406852 22.16010663394288, -153.59932438347053 26.01299578883174, -154.25857874349714 29.8657940266665, -154.90287384906094 33.71784589442994, -156.4154886637448 35.564455266536505, -162.03767213761958 34.98975986081048, -165.67042484593026 34.43861715006958, -169.9464526366916 33.66017283151052, -173.5921218067924 32.90472542332997, -172.1983000207946 29.162140412110194, -170.92247683141954 25.39940941633738, -169.7457472101792 21.62002239734624, -168.65273530924344 17.826844811300536, -167.63148361076378 14.021764653386432, -166.1730278143979 12.279422414280331))";
}
