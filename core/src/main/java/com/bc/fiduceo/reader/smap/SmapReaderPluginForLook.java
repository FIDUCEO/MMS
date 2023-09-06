package com.bc.fiduceo.reader.smap;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

/**
 * For look Reader Plugin for SMAP Salinity data products.
 * For more detailed information about the meaning of "for look" please see page 56 in the document:
 * https://data.remss.com/smap/SSS/V05.0/documents/SMAP_NASA_RSS_Salinity_Release_V5.0.pdf
 */
public class SmapReaderPluginForLook implements ReaderPlugin {

    private static final String[] SUPPORTED_KEYS = {"smap-sss-for"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        // Since index positions are zero based, 0 means "look 1". And "look 1" means "for look".
        final int forLook = 0;
        return new SmapReader(readerContext, forLook);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SUPPORTED_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}
