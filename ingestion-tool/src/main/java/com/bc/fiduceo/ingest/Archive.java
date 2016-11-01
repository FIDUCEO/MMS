package com.bc.fiduceo.ingest;

import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.util.TimeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

class Archive {

    private final Logger log;
    private final HashMap<String, PathElement[]> pathMaps;
    private final PathElement[] defaultPath;

    private Path rootPath;

    Archive(Path rootPath) {
        this.rootPath = rootPath;
        log = FiduceoLogger.getLogger();
        pathMaps = new HashMap<>();

        defaultPath = createDefaultPathElements();
    }

    Path[] get(Date startDate, Date endDate, String processingVersion, String sensorType) throws IOException {
        final ArrayList<Path> pathArrayList = new ArrayList<>();
        final Calendar instance = TimeUtils.getUTCCalendar();
        instance.setTime(startDate);

        while (instance.getTime().compareTo(endDate) <= 0) {
            final int year = instance.get(Calendar.YEAR);
            final int month = instance.get(Calendar.MONTH) + 1;
            final int day = instance.get(Calendar.DAY_OF_MONTH);
            final Path productsDir = createValidProductPath(processingVersion, sensorType, year, month, day);

            if (Files.exists(productsDir)) {
                log.info("The product directory :" + productsDir.toString());
                final Iterator<Path> iterator = Files.list(productsDir).iterator();
                while (iterator.hasNext()) {
                    pathArrayList.add(iterator.next());
                }
            } else {
                log.warning("The directory doest not exist: " + productsDir.toString());
            }
            instance.add(Calendar.DAY_OF_MONTH, 1);
        }
        return pathArrayList.toArray(new Path[0]);
    }

    Path createValidProductPath(String processingVersion, String sensorType, int year, int month, int day) {
        final PathElement[] pathElements = getPathElements(sensorType);
        final PathContext pathContext = new PathContext(sensorType, processingVersion, year, month, day);

        Path path = this.rootPath;
        for (PathElement pathElement : pathElements) {
            final String segment = pathContext.getSegment(pathElement);
            path = path.resolve(segment);
        }

        return path;
    }

    // package access for testing only tb 2016-11-01
    PathElement[] getPathElements(String sensorType) {
        PathElement[] pathElements = pathMaps.get(sensorType);
        if (pathElements == null) {
            pathElements = defaultPath;
        }

        return pathElements;
    }

    // package access for testing only tb 2016-11-01
    static PathElement[] createDefaultPathElements() {
        final PathElement[] pathElements = new PathElement[5];

        pathElements[0] = new PathElement("SENSOR", null);
        pathElements[1] = new PathElement("VERSION", null);
        pathElements[2] = new PathElement("YEAR", null);
        pathElements[3] = new PathElement("MONTH", null);
        pathElements[4] = new PathElement("DAY", null);

        return pathElements;
    }
}
