package com.bc.fiduceo.reader.avhrr_frac;

import org.esa.snap.core.datamodel.TiePointGrid;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AVHRR_FRAC_PixelLocatorTest {

    @Test
    public void testExtractSections_oneSection() throws IOException {
        final float[] latitudes = {39.599f, 39.598f,
                39.590f, 39.588f,
                39.580f, 39.578f,
                39.570f, 39.568f,
                39.560f, 39.559f};

        final TiePointGrid lat_tp = new TiePointGrid("lats", 2, 5, 0.5, 0.5, 1, 1, latitudes);

        final List<AVHRR_FRAC_PixelLocator.Section> sections = AVHRR_FRAC_PixelLocator.extractSections(lat_tp);
        assertEquals(1, sections.size());

        final AVHRR_FRAC_PixelLocator.Section section = sections.get(0);
        assertEquals(0, section.startLine);
        assertEquals(4, section.stopLine);
    }

    @Test
    public void testExtractSections_twoSections_polarFirst() throws IOException {
        final float[] latitudes = {87.599f, 88.099f, 89.199f,
                87.499f, 87.999f, 89.099f,
                87.399f, 87.899f, 88.999f,
                87.299f, 87.799f, 88.899f,
                87.199f, 87.699f, 88.799f,
                87.099f, 87.599f, 88.699f,
                86.999f, 87.499f, 88.599f};

        final TiePointGrid lat_tp = new TiePointGrid("lats", 3, 7, 0.5, 0.5, 1, 1, latitudes);

        final List<AVHRR_FRAC_PixelLocator.Section> sections = AVHRR_FRAC_PixelLocator.extractSections(lat_tp);
        assertEquals(2, sections.size());

        AVHRR_FRAC_PixelLocator.Section section = sections.get(0);
        assertEquals(0, section.startLine);
        assertEquals(2, section.stopLine);

        section = sections.get(1);
        assertEquals(2, section.startLine);
        assertEquals(6, section.stopLine);
    }

    @Test
    public void testExtractSections_twoSections_polarLast() throws IOException {
        final float[] latitudes = {-88.619f, -88.752f,
                -88.693f, -88.833f,
                -88.776f, -88.923f,
                -88.853f, -89.009f,
                -88.926f, -89.092f,
                -88.933f, -89.100f,
                -88.941f, -89.109f,
                -88.948f, -89.117f,
                -88.955f, -89.125f};

        final TiePointGrid lat_tp = new TiePointGrid("lats", 2, 9, 0.5, 0.5, 1, 1, latitudes);

        final List<AVHRR_FRAC_PixelLocator.Section> sections = AVHRR_FRAC_PixelLocator.extractSections(lat_tp);
        assertEquals(2, sections.size());

        AVHRR_FRAC_PixelLocator.Section section = sections.get(0);
        assertEquals(0, section.startLine);
        assertEquals(3, section.stopLine);

        section = sections.get(1);
        assertEquals(3, section.startLine);
        assertEquals(8, section.stopLine);
    }

    @Test
    public void testIsPolarLine() {
        final float[] non_polar = {39.599f, 39.598f, 39.597f, 39.596f};
        final float[] polar = {87.23f, 88.24f, 89.25f, 88.26f};

        assertFalse(AVHRR_FRAC_PixelLocator.isPolarLine(non_polar));
        assertTrue(AVHRR_FRAC_PixelLocator.isPolarLine(polar));
    }
}
