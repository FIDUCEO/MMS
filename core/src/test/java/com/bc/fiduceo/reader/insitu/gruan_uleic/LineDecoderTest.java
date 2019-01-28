package com.bc.fiduceo.reader.insitu.gruan_uleic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LineDecoderTest {

    @Test
    public void testDecodeLon() {
        final LineDecoder.Lon decoder = new LineDecoder.Lon();

        assertEquals(-156.6161, (float) decoder.get("1234330303.0, -156.6161, 71.32294, BAR/2009/BAR-RS-01_2_RS92-GDP_002_20090211T060000_1-000-001.nc"), 1e-4);
    }

    @Test
    public void testDecodeLat() {
        final LineDecoder.Lat decoder = new LineDecoder.Lat();

        assertEquals(71.32302, (float) decoder.get("1240031598.0, -156.61618, 71.32302, BAR/2009/BAR-RS-01_2_RS92-GDP_002_20090418T060000_1-000-001.nc"), 1e-4);
    }

    @Test
    public void testDecodeTime() {
        final LineDecoder.Time decoder = new LineDecoder.Time();

        assertEquals(1250573213, (int) decoder.get("1250573213.0, -156.616, 71.32304, BAR/2009/BAR-RS-01_2_RS92-GDP_002_20090818T060000_1-000-001.nc"));
    }

    @Test
    public void testDecodeSource() {
        final LineDecoder.Source decoder = new LineDecoder.Source();

        assertEquals("BAR/2009/BAR-RS-01_2_RS92-GDP_002_20091214T060000_1-000-001.nc", decoder.get("1260768146.0, -156.61618, 71.322914, BAR/2009/BAR-RS-01_2_RS92-GDP_002_20091214T060000_1-000-001.nc"));
    }
}
