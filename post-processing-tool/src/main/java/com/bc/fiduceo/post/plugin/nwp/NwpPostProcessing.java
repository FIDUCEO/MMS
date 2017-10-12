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

package com.bc.fiduceo.post.plugin.nwp;


import com.bc.fiduceo.post.Constants;
import com.bc.fiduceo.post.PostProcessing;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;

class NwpPostProcessing extends PostProcessing {

    private final Configuration configuration;
    private final TemplateVariables templateVariables;
    private final TempFileManager tempFileManager;

    NwpPostProcessing(Configuration configuration) {
        this.configuration = configuration;
        templateVariables = new TemplateVariables(configuration);
        tempFileManager = new TempFileManager();

        final String tempDir = configuration.getTempDir();
        if (StringUtils.isNotNullAndNotEmpty(tempDir)) {
            tempFileManager.setTempDir(tempDir);
        }
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Dimension matchupCountDimension = reader.findDimension(Constants.DIMENSION_NAME_MATCHUP_COUNT);
        if (matchupCountDimension == null) {
            throw new RuntimeException("Expected dimension not present in file: " + Constants.DIMENSION_NAME_MATCHUP_COUNT);
        }

        final Context context = createContext(reader, writer);
        if (configuration.isTimeSeriesExtraction()) {
            final Strategy timeSeries = StrategyFactory.getTimeSeries();
            timeSeries.prepare(context);
        }

        if (configuration.isSensorExtraction()) {
            final Strategy sensorExtracts = StrategyFactory.getSensorExtracts();
            sensorExtracts.prepare(context);
        }
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Context context = createContext(reader, writer);

        try {
            if (configuration.isTimeSeriesExtraction()) {
                final Strategy timeSeries = StrategyFactory.getTimeSeries();
                timeSeries.compute(context);
            }

            if (configuration.isSensorExtraction()) {
                final Strategy sensorExtracts = StrategyFactory.getSensorExtracts();
                sensorExtracts.compute(context);
            }

        } finally {
            if (configuration.isDeleteOnExit()) {
                tempFileManager.cleanup();
            }
        }
    }


    private Context createContext(NetcdfFile reader, NetcdfFileWriter writer) {
        final Context context = new Context();

        context.setReader(reader);
        context.setWriter(writer);
        context.setConfiguration(configuration);
        context.setTempFileManager(tempFileManager);
        context.setTemplateVariables(templateVariables);

        return context;
    }


}
