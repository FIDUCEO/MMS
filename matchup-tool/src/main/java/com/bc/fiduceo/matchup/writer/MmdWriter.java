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

package com.bc.fiduceo.matchup.writer;


import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.util.TimeUtils;

import java.util.Date;
import java.util.List;

public class MmdWriter {

    // package access for testing only tb 2016-03-10
    static String createMMDFileName(UseCaseConfig useCaseConfig, Date startDate, Date endDate) {
        final StringBuilder nameBuilder = new StringBuilder();

        nameBuilder.append(useCaseConfig.getName());
        nameBuilder.append("_");

        // @todo 1 tb/tb make this more elegant :-) 2016-03-10
        final List<Sensor> sensors = useCaseConfig.getSensors();
        final Sensor[] sensorArray = new Sensor[sensors.size()];
        final Sensor firt = sensors.get(0);
        if (firt.isPrimary()) {
            sensorArray[0]= firt;
            sensorArray[1] = sensors.get(1);
        }else{
            sensorArray[1]= firt;
            sensorArray[0] = sensors.get(1);
        }

        nameBuilder.append(sensorArray[0].getName());
        nameBuilder.append("_");
        nameBuilder.append(sensorArray[1].getName());
        nameBuilder.append("_");

        nameBuilder.append(TimeUtils.formatToDOY(startDate));
        nameBuilder.append("_");

        nameBuilder.append(TimeUtils.formatToDOY(endDate));
        nameBuilder.append(".nc");

        return nameBuilder.toString();
    }
}
