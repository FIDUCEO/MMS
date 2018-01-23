package com.bc.fiduceo.reader;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.util.TempFileUtils;

import java.io.File;
import java.io.IOException;

public class ReaderContext {
    private GeometryFactory geometryFactory;
    private TempFileUtils tempFileUtils;

    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public void setTempFileUtils(TempFileUtils tempFileUtils) {
        this.tempFileUtils = tempFileUtils;
    }

    public File createTempFile(String prefix, String extension) throws IOException {
        return tempFileUtils.create(prefix, extension);
    }

    public void deleteTempFile(File tempFile) {
        tempFileUtils.delete(tempFile);
    }
}
