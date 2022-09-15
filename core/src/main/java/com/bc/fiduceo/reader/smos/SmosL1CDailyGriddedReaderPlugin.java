package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class SmosL1CDailyGriddedReaderPlugin implements ReaderPlugin {

    private static final String[] SENSOR_KEYS = new String[]{"miras-smos-CDF3TD", "miras-smos-CDF3TA"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new SmosL1CDailyGriddedReader(readerContext);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SENSOR_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}
