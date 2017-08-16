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

import com.bc.fiduceo.util.TimeUtils;

import java.util.Calendar;
import java.util.HashMap;

class PathContext {

    private final HashMap<String, String> map;

    PathContext(String sensor, String version, int year, int month, int day) {
        map = new HashMap<>();
        map.put("SENSOR", sensor);
        map.put("VERSION", version);
        map.put("YEAR", Integer.toString(year));
        map.put("MONTH", String.format("%02d", month));
        map.put("DAY", String.format("%02d", day));

        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.set(Calendar.YEAR, year);
        utcCalendar.set(Calendar.MONTH, month - 1);
        utcCalendar.set(Calendar.DAY_OF_MONTH, day);
        map.put("DAY_OF_YEAR", String.format("%02d", utcCalendar.get(Calendar.DAY_OF_YEAR)));
    }

    String getSegment(String pathElement) {
        final String segment = map.get(pathElement);
        if (segment == null) {
            return pathElement;
        }
        return segment;
    }
}
