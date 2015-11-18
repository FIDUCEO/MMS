package com.bc.fiduceo.reader;

import java.io.File;
import java.io.IOException;

public interface Reader {

    void open(File file) throws IOException;

    void close() throws IOException;

    AcquisitionInfo read() throws IOException;
}
