package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class SlstrNTReaderPlugin extends SlstrReaderPlugin {

    private static final String[] SENSOR_KEYS = {"slstr-s3a-nt", "slstr-s3b-nt"};

    public SlstrNTReaderPlugin() {
        super(SENSOR_KEYS, ProductType.NT);
    }
}
