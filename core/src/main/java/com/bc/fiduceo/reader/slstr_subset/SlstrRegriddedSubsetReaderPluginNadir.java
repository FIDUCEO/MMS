package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class SlstrRegriddedSubsetReaderPluginNadir implements ReaderPlugin {
    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new SlstrRegriddedSubsetReader(readerContext, true);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return new String[] {"slstr.a","slstr.b"};
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}
