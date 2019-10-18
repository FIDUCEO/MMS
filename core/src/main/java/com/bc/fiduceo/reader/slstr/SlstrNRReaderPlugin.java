package com.bc.fiduceo.reader.slstr;

public class SlstrNRReaderPlugin extends SlstrReaderPlugin {

    private static final String[] SENSOR_KEYS = {"slstr-s3a-nr", "slstr-s3b-nr"};

    public SlstrNRReaderPlugin() {
        super(SENSOR_KEYS, ProductType.NR);
    }
}
