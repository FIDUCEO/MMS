package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class HIRS_FCDR_ReaderPlugin implements ReaderPlugin {

    private static final String[] SENSOR_KEYS = {"hirs-n06-fcdr", "hirs-n07-fcdr", "hirs-n08-fcdr", "hirs-n09-fcdr", "hirs-n10-fcdr", "hirs-n11-fcdr", "hirs-n12-fcdr", "hirs-n14-fcdr", "hirs-n15-fcdr", "hirs-n16-fcdr", "hirs-n17-fcdr", "hirs-n18-fcdr", "hirs-n19-fcdr", "hirs-ma-fcdr", "hirs-mb-fcdr"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new HIRS_FCDR_Reader(readerContext);
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
