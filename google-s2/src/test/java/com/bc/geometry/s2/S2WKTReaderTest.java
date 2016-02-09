package com.bc.geometry.s2;

import com.google.common.geometry.S2Polygon;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author muhammad.bc
 */
public class S2WKTReaderTest {
    @Test
    public void testWKReader() throws IOException {
        S2WKTReader reader = new S2WKTReader();
        List<S2Polygon> read = (List<S2Polygon>) reader.read("MULTIPOLYGON(((30 20, 100 10)),((100 10, 300 10)),((30 20,100 10)))");
        Assert.assertNotNull(read);

        assertTrue(read.size() == 3);
        assertEquals("Polygon: (1) loops:\n" +
                             "loop <\n" +
                             "(20.0, 29.999999999999993)\n" +
                             "(10.0, 100.0)\n" +
                             ">\n",read.get(0).toString());
    }
}
