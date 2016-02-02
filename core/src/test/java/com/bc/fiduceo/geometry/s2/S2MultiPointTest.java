package com.bc.fiduceo.geometry.s2;

import com.vividsolutions.jts.io.WKTReader;
import com.google.common.geometry.S2Point;
import org.junit.Before;

import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * @author muhammad.bc
 */
public class S2MultiPointTest {
    S2Point innerPoint;
    private List<S2Polygon> innerPolygon;

    @Before
    public void setUp() {
        innerPoint = mock(S2Point.class);
//        new S2MultiPoint(innerPolygon);
//        wktReader = new WKTReader();
    }
}
