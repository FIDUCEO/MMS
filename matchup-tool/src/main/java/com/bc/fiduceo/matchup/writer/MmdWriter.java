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
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MmdWriter {

    private NetcdfFileWriter netcdfFileWriter;

    public void create(File mmdFile) throws IOException {
        netcdfFileWriter = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, mmdFile.getPath());

        addGlobalAttribute("title", "FIDUCEO multi-sensor match-up dataset (MMD)");
        addGlobalAttribute("institution", "Brockmann Consult GmbH");
        addGlobalAttribute("contact", "Tom Block (tom.block@brockmann-consult.de)");
        addGlobalAttribute("license", "This dataset is released for use under CC-BY licence and was developed in the EC FIDUCEO project \"Fidelity and Uncertainty in Climate Data Records from Earth Observations\". Grant Agreement: 638822.");
        addGlobalAttribute("creation_date", TimeUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));

        netcdfFileWriter.create();
    }


    public void close() throws IOException {
        if (netcdfFileWriter != null) {
            netcdfFileWriter.close();
            netcdfFileWriter = null;
        }
    }

    public static String createMMDFileName(UseCaseConfig useCaseConfig, Date startDate, Date endDate) {
        final StringBuilder nameBuilder = new StringBuilder();

        nameBuilder.append(useCaseConfig.getName());
        nameBuilder.append("_");

        nameBuilder.append(useCaseConfig.getPrimarySensor().getName());
        nameBuilder.append("_");

        final List<Sensor> additionalSensors = useCaseConfig.getAdditionalSensors();
        if (additionalSensors.size() > 0) {
            for (final Sensor additionalSensor : additionalSensors) {
                nameBuilder.append(additionalSensor.getName());
                nameBuilder.append("_");
            }
        } else {
            nameBuilder.append("_");
        }

        nameBuilder.append(TimeUtils.formatToDOY(startDate));
        nameBuilder.append("_");

        nameBuilder.append(TimeUtils.formatToDOY(endDate));
        nameBuilder.append(".nc");

        return nameBuilder.toString();
    }

    private void addGlobalAttribute(String name, String val) {
        netcdfFileWriter.addGroupAttribute(null, new Attribute(name, val));
    }
}
