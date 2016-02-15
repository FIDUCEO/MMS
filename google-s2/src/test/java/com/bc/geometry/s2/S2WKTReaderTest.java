package com.bc.geometry.s2;

import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author muhammad.bc
 */
@SuppressWarnings("unchecked")
public class S2WKTReaderTest {
    @Test
    public void testWKReader_S2MultiPolygon() throws IOException {
        S2WKTReader reader = new S2WKTReader();
        List<S2Polygon> read = (List<S2Polygon>) reader.read("MULTIPOLYGON(((30 20, 100 10)),((100 10, 300 10)),((30 20,100 10)))");
        Assert.assertNotNull(read);

        assertTrue(read.size() == 3);
        assertEquals("Polygon: (1) loops:\n" +
                             "loop <\n" +
                             "(20.0, 29.999999999999993)\n" +
                             "(10.0, 100.0)\n" +
                             ">\n", read.get(0).toString());
    }

    @Test
    public void testWKReader_S2MultiLineString() throws IOException {
        S2WKTReader reader = new S2WKTReader();
        List<S2Polyline> read = (List<S2Polyline>) reader.read("MULTILINESTRING((10 18, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
        assertNotNull(read);
        assertEquals(read.size(), 2);
        assertEquals(read.get(0).numVertices(), 3);
        assertEquals(read.get(1).numVertices(), 4);

        assertEquals(read.get(0).vertex(2).getX(), 0.7544065067354889, 1e-8);
        assertEquals(read.get(0).vertex(2).getY(), 0.133022221559489, 1e-8);

        assertEquals(read.get(1).vertex(0).getX(), 0.5868240888334652, 1e-8);
        assertEquals(read.get(1).vertex(0).getY(), 0.49240387650610395, 1e-8);
    }
}
