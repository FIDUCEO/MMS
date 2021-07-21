package com.bc.fiduceo.reader.insitu.sirds_sst;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class SirdsInsituReaderPlugin implements ReaderPlugin {

    private final String[] SUPPORTED_KEYS = {"animal-sirds", "argo-sirds", "argo_surf-sirds", "bottle-sirds", "ctd-sirds", "drifter-sirds", "drifter_cmems-sirds", "gtmba-sirds", "mbt-sirds", "mooring-sirds", "ship-sirds", "xbt-sirds"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new SirdsInsituReader();
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SUPPORTED_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.INSITU;
    }
}
