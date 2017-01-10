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


import com.bc.fiduceo.core.TimeRange;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

class NwpPostProcessing extends PostProcessing {

    private static final int SEVENTY_TWO_HOURS_IN_SECONDS = 72 * 60 * 60;
    private static final int FOURTY_EIGHT_HOURS_IN_SECONDS = 48 * 60 * 60;

    private final Configuration configuration;

    NwpPostProcessing(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable timeVariable = NetCDFUtils.getVariable(reader, configuration.getTimeVariableName());
        final Array timeArray = timeVariable.read();

        final Number fillValue = NetCDFUtils.getFillValue(timeVariable);
        final TimeRange timeRange = extractTimeRange(timeArray, fillValue);
        final List<String> directoryNamesList = toDirectoryNamesList(timeRange);

        final int matchupCount = NetCDFUtils.getDimensionLength("matchup_count", reader);
        final GeoFile geoFile = new GeoFile(matchupCount);
        geoFile.create(configuration.isDeleteOnExit());

    }

    // package access for testing only tb 2017-01-06
    static TimeRange extractTimeRange(Array timesArray, Number fillValue) {
        int startTime = Integer.MAX_VALUE;
        int endTime = Integer.MIN_VALUE;
        final int fill = fillValue.intValue();

        for (int i = 0; i < timesArray.getSize(); i++) {
            final int currentTime = timesArray.getInt(i);
            if (currentTime == fill) {
                continue;
            }

            if (currentTime > endTime) {
                endTime = currentTime;
            }
            if (currentTime < startTime) {
                startTime = currentTime;
            }
        }

        final Date startDate = TimeUtils.create(startTime);
        final Date endDate = TimeUtils.create(endTime);
        return new TimeRange(startDate, endDate);
    }

    // package access for testing only tb 2017-01-06
    static List<String> toDirectoryNamesList(TimeRange timeRange) {
        final Date startDate = timeRange.getStartDate();
        final Date extractStartDate = TimeUtils.addSeconds(-SEVENTY_TWO_HOURS_IN_SECONDS, startDate);
        final Date beginningOfDay = TimeUtils.getBeginningOfDay(extractStartDate);

        final Date stopDate = timeRange.getStopDate();
        final Date extractStopDate = TimeUtils.addSeconds(FOURTY_EIGHT_HOURS_IN_SECONDS, stopDate);

        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(beginningOfDay);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        final List<String> directoryNameList = new ArrayList<>();
        while (!utcCalendar.getTime().after(extractStopDate)) {
            directoryNameList.add(simpleDateFormat.format(utcCalendar.getTime()));
            utcCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return directoryNameList;
    }
}
