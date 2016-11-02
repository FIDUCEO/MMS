/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

package com.bc.fiduceo.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SystemConfigTest {


    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testLoadAndGet_geometryLibrary() throws IOException {
        final String useCaseXml = "<system-config>" +
                "    <geometry-library name = \"lib_name\" />" +
                "</system-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final SystemConfig systemConfig = SystemConfig.load(inputStream);

        assertEquals("lib_name", systemConfig.getGeometryLibraryType());
    }

    // @todo 1 tb/tb root element missing 2016-11-02

//    @Test
//    public void testLoadFileNotPresent() throws IOException {
//        final SystemConfig systemConfig = new SystemConfig();
//        try {
//            systemConfig.loadFrom(testDirectory);
//            fail("RuntimeException expected");
//        } catch (RuntimeException expected) {
//        }
//    }

    @Test
    public void testDefaultValues() {
        final SystemConfig systemConfig = new SystemConfig();

        assertEquals("S2", systemConfig.getGeometryLibraryType());
    }
}
