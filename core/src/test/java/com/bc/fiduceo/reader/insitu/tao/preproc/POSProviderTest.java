package com.bc.fiduceo.reader.insitu.tao.preproc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class POSProviderTest {

    @Test
    public void testInterpolate() {
        final POSRecord before = new POSRecord();
        before.date = 1451952099;
        before.lon = 28.5f;
        before.lat = -10.f;

        final POSRecord after = new POSRecord();
        after.date = 1451952299;
        after.lon = 29.5f;
        after.lat = -10.4f;

        POSRecord interpolated = POSProvider.interpolate(before, after, 1451952199);
        assertEquals(1451952199, interpolated.date);
        assertEquals(29.f, interpolated.lon, 1e-8);
        assertEquals(-10.2f, interpolated.lat, 1e-8);

        interpolated = POSProvider.interpolate(before, after, 1451952110);
        assertEquals(1451952110, interpolated.date);
        assertEquals(28.555f, interpolated.lon, 1e-8);
        assertEquals(-10.022f, interpolated.lat, 1e-8);

        interpolated = POSProvider.interpolate(before, after, 1451952286);
        assertEquals(1451952286, interpolated.date);
        assertEquals(29.435f, interpolated.lon, 1e-8);
        assertEquals(-10.374f, interpolated.lat, 1e-8);
    }
}
