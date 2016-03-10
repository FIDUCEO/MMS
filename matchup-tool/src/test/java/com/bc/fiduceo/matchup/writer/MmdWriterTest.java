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


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MmdWriterTest {

    @Test
    public void testCreateMMDFileName() {
        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        useCaseConfig.setName("mmd-12");

        final List<Sensor> sensors = new ArrayList<>();
        sensors.add(new Sensor("avhrr-n07"));
        final Sensor primary = new Sensor("avhrr-n08");
        primary.setPrimary(true);
        sensors.add(primary);
        useCaseConfig.setSensors(sensors);

        final Date startDate = TimeUtils.parseDOYBeginOfDay("2011-245");
        final Date endDate = TimeUtils.parseDOYEndOfDay("2011-251");

        final String fileName = MmdWriter.createMMDFileName(useCaseConfig, startDate, endDate);
        assertEquals("mmd-12_avhrr-n08_avhrr-n07_2011-245_2011-251.nc", fileName);
    }
}
