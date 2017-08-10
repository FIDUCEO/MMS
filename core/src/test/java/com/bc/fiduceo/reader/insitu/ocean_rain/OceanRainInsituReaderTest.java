/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.insitu.ocean_rain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OceanRainInsituReaderTest {

    @Test
    public void testGetRegEx() {
        final OceanRainInsituReader reader = new OceanRainInsituReader();

        assertEquals("OceanRAIN_allships_2010-2017_SST.ascii", reader.getRegEx());
    }

    @Test
    public void testGetLongitudeVariableName() {
        final OceanRainInsituReader reader = new OceanRainInsituReader();

        assertEquals("lon", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final OceanRainInsituReader reader = new OceanRainInsituReader();

        assertEquals("lat", reader.getLatitudeVariableName());
    }

    @Test
    public void testDecode() {
        byte[] bytes = "16092016 2126 8294.893056   1474061100   8.9958 -138.0500  28.5\n".getBytes();

        Line line = OceanRainInsituReader.decode(bytes);
        assertNotNull(line);
        assertEquals(-138.05f, line.getLon(), 1e-8);
        assertEquals(8.9958f, line.getLat(), 1e-8);
        assertEquals(1474061100, line.getTime());
        assertEquals(28.5f, line.getSst(), 1e-8);

        bytes = "13062010 2012 6007.841667   1276459920  64.5660    0.3195   9.5\n".getBytes();
        line = OceanRainInsituReader.decode(bytes);
        assertNotNull(line);
        assertEquals(0.3195f, line.getLon(), 1e-8);
        assertEquals(64.5660f, line.getLat(), 1e-8);
        assertEquals(1276459920, line.getTime());
        assertEquals(9.5f, line.getSst(), 1e-8);

        bytes = "17062010 0247 6011.115972   1276742820  73.8891  -19.5511  -1.8\n".getBytes();
        line = OceanRainInsituReader.decode(bytes);
        assertNotNull(line);
        assertEquals(-19.5511f, line.getLon(), 1e-8);
        assertEquals(73.8891f, line.getLat(), 1e-8);
        assertEquals(1276742820, line.getTime());
        assertEquals(-1.8f, line.getSst(), 1e-8);

    }
}
