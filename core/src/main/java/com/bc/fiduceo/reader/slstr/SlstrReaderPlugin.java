package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class SlstrReaderPlugin implements ReaderPlugin {

    private final String[] sensorKey;
    private final ProductType productType;

    public SlstrReaderPlugin() {
        this(new String[] {"slstr-s3a", "slstr-s3b"}, ProductType.ALL);
    }

    SlstrReaderPlugin(String[] sensorKeys, ProductType productType) {
        this.sensorKey = sensorKeys;
        this.productType = productType;
    }

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new SlstrReader(readerContext, productType);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return sensorKey;
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}
