package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.reader.Reader;

import java.io.IOException;
import java.io.InputStream;

abstract class NdbcReader implements Reader {

    StationDatabase parseStationDatabase(String resourceName) throws IOException {
        final InputStream is = getClass().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IllegalStateException("The internal resource file could not be read.");
        }

        final StationDatabase sdb = new StationDatabase();
        sdb.load(is);

        is.close();

        return sdb;
    }
}
