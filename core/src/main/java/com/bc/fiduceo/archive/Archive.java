/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.archive;

import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.util.TimeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class Archive {

    private final Logger log;
    private final HashMap<String, String[]> pathMaps;
    private final String[] defaultPath;

    private Path rootPath;

    public Archive(ArchiveConfig config) {
        rootPath = config.getRootPath();
        log = FiduceoLogger.getLogger();
        pathMaps = new HashMap<>();

        defaultPath = createDefaultPathElements();

        final Map<String, String[]> rules = config.getRules();
        pathMaps.putAll(rules);
    }

    public Path[] get(Date startDate, Date endDate, String processingVersion, String sensorType) throws IOException {
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
                    final Path next = iterator.next();
                    if (pathArrayList.contains(next)) {
                        // @todo 3 tb/tb this is a crude skip-what-you-already-have logic. We really need to determine
                        // the time loop increment by checking the pathSegments defined - which is not that simple tb 2016-11-02
                        continue;
                    }
                    pathArrayList.add(next);
                }
            } else {
                log.warning("The directory doest not exist: " + productsDir.toString());
            }
            instance.add(Calendar.DAY_OF_MONTH, 1);
        }
        return pathArrayList.toArray(new Path[0]);
    }

    Path createValidProductPath(String processingVersion, String sensorType, int year, int month, int day) {
        final String[] pathElements = getPathElements(sensorType);
        final PathContext pathContext = new PathContext(sensorType, processingVersion, year, month, day);

        Path path = this.rootPath;
        for (String pathElement : pathElements) {
            final String segment = pathContext.getSegment(pathElement);
            path = path.resolve(segment);
        }

        return path;
    }

    // package access for testing only tb 2016-11-01
    String[] getPathElements(String sensorType) {
        String[] pathElements = pathMaps.get(sensorType);
        if (pathElements == null) {
            pathElements = defaultPath;
        }

        return pathElements;
    }

    // package access for testing only tb 2016-11-01
    static String[] createDefaultPathElements() {
        final String[] pathElements = new String[5];

        pathElements[0] = "SENSOR";
        pathElements[1] = "VERSION";
        pathElements[2] = "YEAR";
        pathElements[3] = "MONTH";
        pathElements[4] = "DAY";

        return pathElements;
    }

    // for testing only tb 2016-11-01
    Path getRootPath() {
        return rootPath;
    }
}
