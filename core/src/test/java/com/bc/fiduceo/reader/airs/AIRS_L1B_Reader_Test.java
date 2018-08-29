/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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
package com.bc.fiduceo.reader.airs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.ReaderContext;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.mockito.Mockito;

import java.io.IOException;

public class AIRS_L1B_Reader_Test {

    private AIRS_L1B_Reader airsL1bReader;

    @Before
    public void setUp() throws IOException {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));
        airsL1bReader = new AIRS_L1B_Reader(readerContext);
    }

    @After
    public void tearDown() throws Exception {
        airsL1bReader.close();
    }

    @Test
    public void testGetRegex() {
        assertEquals("AIRS\\.\\d{4}\\.\\d{2}\\.\\d{2}\\.\\d{3}\\.L1B\\.AIRS_Rad\\..*\\.hdf", airsL1bReader.getRegEx());
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        try {
            airsL1bReader.getSubScenePixelLocator(Mockito.mock(Polygon.class));
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("not implemented", expected.getMessage());
        }
    }

    @Test
    public void testGetLonLatVariableName() {
        assertEquals("Latitude", airsL1bReader.getLatitudeVariableName());
        assertEquals("Longitude", airsL1bReader.getLongitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        //preparation
        final String validFileName = "AIRS.2010.01.07.001.L1B.AIRS_Rad.v5.0.0.0.G10007112420.hdf";

        //execution
        final int[] ymd = airsL1bReader.extractYearMonthDayFromFilename(validFileName);

        //verification
        assertArrayEquals(new int[]{2010, 1, 7}, ymd);
    }

    @Test
    public void testExtractYearMonthDayFromFilename_Filename_invalid() {
        //preparation
        final String invalidFileName = "AIRS.2010.01.07.001.L1B.AIRS_XXX.v5.0.0.0.G10007112420.hdf";

        //execution
        try {
            airsL1bReader.extractYearMonthDayFromFilename(invalidFileName);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            //verification
            final String message = expected.getMessage();
            final String regEx = airsL1bReader.getRegEx();
            assertThat(message, is("A file name matching the expression \"" + regEx + "\" expected. " +
                                   "But was \"AIRS.2010.01.07.001.L1B.AIRS_XXX.v5.0.0.0.G10007112420.hdf\""));
        }
    }

    @Test
    public void testExtractYearMonthDayFromFilename_Filename_null() {
        //preparation
        final String fileName = null;

        //execution
        try {
            airsL1bReader.extractYearMonthDayFromFilename(fileName);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String message = expected.getMessage();
            assertThat(message, is("The file name \"null\" is not valid."));
        }
    }
}
