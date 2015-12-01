package com.bc.fiduceo.geometry.s2;


import com.google.common.geometry.S2LatLng;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class S2PointTest {

    @Test
    public void testGetInner() {
        final S2LatLng s2LatLng = S2LatLng.fromDegrees(11, 87);

        final S2Point s2Point = new S2Point(s2LatLng);

        final Object inner = s2Point.getInner();
        assertSame(s2LatLng, inner);
    }
}
