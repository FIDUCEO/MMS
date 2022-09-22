package com.bc.fiduceo.reader.slstr.utility;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;

public class ManifestUtil {

    public static int getObliqueGridOffset(MetadataElement metadataRoot) {
        final MetadataElement manifestElement = metadataRoot.getElement("Manifest");
        final MetadataElement metadataElement = manifestElement.getElement("metadataSection");
        final MetadataElement productInformationElement = metadataElement.getElement("slstrProductInformation");

        int nadirTrackOffset = -1;
        int obliqueTrackOffset = -1;
        final MetadataElement[] elements = productInformationElement.getElements();
        for (final MetadataElement element : elements) {
            if (element.getName().equalsIgnoreCase("nadirImageSize")) {
                final MetadataAttribute grid = element.getAttribute("grid");
                if (grid.getData().getElemString().equalsIgnoreCase("1 km")) {
                    nadirTrackOffset = ManifestUtil.extractTrackOffset(element);
                }
            }
            if (element.getName().equalsIgnoreCase("obliqueImageSize")) {
                final MetadataAttribute grid = element.getAttribute("grid");
                if (grid.getData().getElemString().equalsIgnoreCase("1 km")) {
                    obliqueTrackOffset = ManifestUtil.extractTrackOffset(element);
                }
            }
        }

        if (nadirTrackOffset < 0 | obliqueTrackOffset < 0) {
            throw new RuntimeException("Unable to extract raster offsets from metadata.");
        }

        return nadirTrackOffset - obliqueTrackOffset;
    }

    static int extractTrackOffset(MetadataElement element) {
        final MetadataAttribute trackOffset = element.getAttribute("trackOffset");
        final String trackOffsetString = trackOffset.getData().getElemString();
        return Integer.parseInt(trackOffsetString);
    }
}
