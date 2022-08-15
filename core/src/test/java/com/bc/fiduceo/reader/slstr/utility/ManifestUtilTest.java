package com.bc.fiduceo.reader.slstr.utility;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ManifestUtilTest {

    @Test
    public void testExtractTrackOffset() {
        final MetadataElement element = new MetadataElement("whatever");
        element.addAttribute(new MetadataAttribute("trackOffset", new ProductData.ASCII("235"), true));

        final int offset = ManifestUtil.extractTrackOffset(element);
        assertEquals(235, offset);
    }
}
