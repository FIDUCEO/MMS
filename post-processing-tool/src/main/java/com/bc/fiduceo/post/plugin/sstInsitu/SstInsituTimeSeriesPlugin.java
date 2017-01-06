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
 */

package com.bc.fiduceo.post.plugin.sstInsitu;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Element;

public class SstInsituTimeSeriesPlugin implements PostProcessingPlugin{

    public static final String TAG_NAME_SST_INSITU_TIME_SERIES = "sst-insitu-time-series";
    public static final String TAG_NAME_VERSION = "version";
    public static final String TAG_NAME_TIME_RANGE_SECONDS = "time-range-in-seconds";
    public static final String TAG_NAME_TIME_SERIES_SIZE = "time-series-size";
    public static final String TAG_NAME_SECONDARY_SENSOR_MATCHUP_TIME_VARIABLE = "secondary-sensor-matchup-time-variable";

    @Override
    public PostProcessing createPostProcessing(Element element) {
        if (!getPostProcessingName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getPostProcessingName() + "' expected.");
        }
        final String processingVersion = JDomUtils.getMandatoryChildMandatoryTextTrim(element, TAG_NAME_VERSION);

        final String timeRange = JDomUtils.getMandatoryChildMandatoryTextTrim(element, TAG_NAME_TIME_RANGE_SECONDS);
        final int timeRangeSeconds = Integer.parseInt(timeRange);

        final String seriesSize = JDomUtils.getMandatoryChildMandatoryTextTrim(element, TAG_NAME_TIME_SERIES_SIZE);
        final int timeSeriesSize = Integer.parseInt(seriesSize);

        final String matchupTimeVarName = JDomUtils.getMandatoryChildMandatoryTextTrim(element, TAG_NAME_SECONDARY_SENSOR_MATCHUP_TIME_VARIABLE);

        return new SstInsituTimeSeries(processingVersion, timeRangeSeconds, timeSeriesSize, matchupTimeVarName);
    }

    @Override
    public String getPostProcessingName() {
        return TAG_NAME_SST_INSITU_TIME_SERIES;
    }
}
