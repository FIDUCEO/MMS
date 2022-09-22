package com.bc.fiduceo.reader.slstr.utility;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ManifestUtilTest {

    private MetadataElement root;
    private MetadataElement productInformation;

    @Before
    public void setUp() {
        root = new MetadataElement("root");
        final MetadataElement manifest = new MetadataElement("Manifest");
        final MetadataElement metadataSection = new MetadataElement("metadataSection");
        productInformation = new MetadataElement("slstrProductInformation");

        metadataSection.addElement(productInformation);
        manifest.addElement(metadataSection);
        root.addElement(manifest);
    }

    @Test
    public void testExtractTrackOffset() {
        final MetadataElement element = new MetadataElement("whatever");
        element.addAttribute(new MetadataAttribute("trackOffset", new ProductData.ASCII("235"), true));

        final int offset = ManifestUtil.extractTrackOffset(element);
        assertEquals(235, offset);
    }

    @Test
    public void testGetObliqueGridOffset_missingElements() {
        try {
            ManifestUtil.getObliqueGridOffset(root);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetObliqueGridOffset() {
        final MetadataElement nadirImageSize = new MetadataElement("nadirImageSize");
        MetadataAttribute gridAttribute = new MetadataAttribute("grid", new ProductData.ASCII("1 km"), true);
        nadirImageSize.addAttribute(gridAttribute);
        MetadataAttribute nadirOffsetAttribute = new MetadataAttribute("trackOffset", new ProductData.ASCII("550"), true);
        nadirImageSize.addAttribute(nadirOffsetAttribute);

        final MetadataElement obliqueImageSize = new MetadataElement("obliqueImageSize");
        gridAttribute = new MetadataAttribute("grid", new ProductData.ASCII("1 km"), true);
        obliqueImageSize.addAttribute(gridAttribute);
        nadirOffsetAttribute = new MetadataAttribute("trackOffset", new ProductData.ASCII("300"), true);
        obliqueImageSize.addAttribute(nadirOffsetAttribute);

        productInformation.addElement(nadirImageSize);
        productInformation.addElement(obliqueImageSize);

        final int offset = ManifestUtil.getObliqueGridOffset(root);
        assertEquals(250, offset);
    }
}
