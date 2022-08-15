package com.bc.fiduceo.reader.slstr.utility;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;

public class ManifestUtil {

    public static int extractTrackOffset(MetadataElement element) {
        final MetadataAttribute trackOffset = element.getAttribute("trackOffset");
        final String trackOffsetString = trackOffset.getData().getElemString();
        return Integer.parseInt(trackOffsetString);
    }
}
