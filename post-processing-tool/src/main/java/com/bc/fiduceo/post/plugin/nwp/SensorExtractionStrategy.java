package com.bc.fiduceo.post.plugin.nwp;

import java.io.File;
import java.io.IOException;
import java.util.List;

class SensorExtractionStrategy extends Strategy {

    @Override
    void prepare(Context context) {
        throw new RuntimeException("not implemented");
    }

    @Override
    void compute(Context context) throws IOException {
        final Configuration configuration = context.getConfiguration();
        final SensorExtractConfiguration sensorExtractConfiguration = configuration.getSensorExtractConfiguration();

        final List<String> nwpDataDirectories = extractNwpDataDirectories(sensorExtractConfiguration.getTimeVariableName(), context.getReader());
    }

    @Override
    File writeGeoFile(Context context) {
        throw new RuntimeException("not implemented");
    }
}
