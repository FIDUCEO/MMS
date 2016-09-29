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

import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_NAME;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_PRIMARY;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_ROOT;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_SENSOR;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_SENSORS;
import static com.bc.fiduceo.matchup.writer.MmdWriterFactory.NetcdfType.N3;
import static com.bc.fiduceo.matchup.writer.MmdWriterFactory.NetcdfType.N4;
import static org.junit.Assert.*;

import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.util.Date;

public class MmdWriterFactoryTest {

    @Test
    public void testCreateMMDFileName() {
        final String config = "<" + TAG_NAME_ROOT + " name=\"mmd-12\">" +
                              "  <" + TAG_NAME_SENSORS + ">" +
                              "    <" + TAG_NAME_SENSOR + ">" +
                              "      <" + TAG_NAME_NAME + ">avhrr-n07</" + TAG_NAME_NAME + ">" +
                              "    </" + TAG_NAME_SENSOR + ">" +
                              "    <" + TAG_NAME_SENSOR + ">" +
                              "      <" + TAG_NAME_NAME + ">avhrr-n08</" + TAG_NAME_NAME + ">" +
                              "      <" + TAG_NAME_PRIMARY + ">true</" + TAG_NAME_PRIMARY + ">" +
                              "    </" + TAG_NAME_SENSOR + ">" +
                              "  </" + TAG_NAME_SENSORS + ">" +
                              "</" + TAG_NAME_ROOT + ">";


        final UseCaseConfig useCaseConfig = UseCaseConfig.load(new ByteArrayInputStream(config.getBytes()));

        final Date startDate = TimeUtils.parseDOYBeginOfDay("2011-245");
        final Date endDate = TimeUtils.parseDOYEndOfDay("2011-251");

        final String fileName = MmdWriterFactory.createMMDFileName(useCaseConfig, startDate, endDate);
        assertEquals("mmd-12_avhrr-n08_avhrr-n07_2011-245_2011-251.nc", fileName);
    }

    @Test
    public void testCreateFileWriter_fromEnum() {
        MmdWriter writer = MmdWriterFactory.createFileWriter(N3, 128, new MmdWriterConfig());
        assertNotNull(writer);
        assertTrue(writer instanceof MmdWriterNC3);

        writer = MmdWriterFactory.createFileWriter(N4, 128, new MmdWriterConfig());
        assertNotNull(writer);
        assertTrue(writer instanceof MmdWriterNC4);
    }

    @Test
    public void testCreateFileWriter_fromString() {
        MmdWriter writer = MmdWriterFactory.createFileWriter("N3", 128, new MmdWriterConfig());
        assertNotNull(writer);
        assertTrue(writer instanceof MmdWriterNC3);

        writer = MmdWriterFactory.createFileWriter("N4", 128, new MmdWriterConfig());
        assertNotNull(writer);
        assertTrue(writer instanceof MmdWriterNC4);
    }

    @Test
    public void testCreateFileWriter_fromString_invalidString() {
        try {
            MmdWriterFactory.createFileWriter("Hanswurst", 128, new MmdWriterConfig());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
