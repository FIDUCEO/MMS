package com.bc.fiduceo.ingest;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;

public class Archive {

    private Path rootPath;
    private FileSystem fileSystem;

    public Archive(Path rootPath) {
        this.rootPath=  rootPath;
        fileSystem = FileSystems.getDefault();
    }

    Archive(Path rootPath, FileSystem fileSystemForTestPurposes) {
        this.rootPath=  rootPath;
        fileSystem = fileSystemForTestPurposes;
    }

    public Path[] get(Date startDate, Date endDate, String processingVersion, String sensorType) {

        return new Path[0];
    }
}
