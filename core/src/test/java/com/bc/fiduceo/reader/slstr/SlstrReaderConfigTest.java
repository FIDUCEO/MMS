package com.bc.fiduceo.reader.slstr;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SlstrReaderConfigTest {

    @Test
    public void testConstruction() {
        final SlstrReaderConfig config = new SlstrReaderConfig();

        assertFalse(config.usePixelGeoCoding());
    }

    @Test
    public void testLoadAndGet_usePixelGeoCoding() {
        final String useCaseXml = "<slstr-reader-config>" +
                "    <use-pixel-geocoding>true</use-pixel-geocoding>" +
                "</slstr-reader-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final SlstrReaderConfig config = SlstrReaderConfig.load(inputStream);

        assertTrue(config.usePixelGeoCoding());
    }
}
