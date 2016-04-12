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

package com.bc.fiduceo.reader.amsu_mhs;


import com.bc.fiduceo.TestUtil;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AMSUB_MHS_ReaderTest {

    @Test
    public void testGetLongitudes_missingGroup() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        try {
            AMSUB_MHS_L1C_Reader.getLongitudes(netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testGetLongitudes_missingVariable() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Group geolocationGroup = mock(Group.class);
        when(netcdfFile.findGroup("Geolocation")).thenReturn(geolocationGroup);

        try {
            AMSUB_MHS_L1C_Reader.getLongitudes(netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testGetLongitudes_missingScaleFactorAttribute() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Group geolocationGroup = mock(Group.class);
        final Variable longitude = mock(Variable.class);
        when(geolocationGroup.findVariable("Longitude")).thenReturn(longitude);
        when(netcdfFile.findGroup("Geolocation")).thenReturn(geolocationGroup);

        try {
            AMSUB_MHS_L1C_Reader.getLongitudes(netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testGetLongitudes_retrieveScaledData() throws IOException {
        final Array unscaledLongitudes = Array.factory(DataType.DOUBLE, new int[]{2, 2}, new double[]{3, 4, 5, 6});
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Group geolocationGroup = mock(Group.class);
        final Variable longitude = mock(Variable.class);
        final Attribute scaleAttribute = mock(Attribute.class);
        when(scaleAttribute.getNumericValue()).thenReturn(0.5);
        when(longitude.read()).thenReturn(unscaledLongitudes);
        when(longitude.findAttribute("Scale")).thenReturn(scaleAttribute);
        when(geolocationGroup.findVariable("Longitude")).thenReturn(longitude);
        when(netcdfFile.findGroup("Geolocation")).thenReturn(geolocationGroup);

        final Array longitudes = AMSUB_MHS_L1C_Reader.getLongitudes(netcdfFile);
        assertNotNull(longitudes);
        assertEquals(1.5, longitudes.getDouble(0), 1e-8);
        assertEquals(2.0, longitudes.getDouble(1), 1e-8);
        assertEquals(2.5, longitudes.getDouble(2), 1e-8);
        assertEquals(3.0, longitudes.getDouble(3), 1e-8);
    }

    @Test
    public void testGetDate() {
        Date date = AMSUB_MHS_L1C_Reader.getDate(2002, 125, 0);
        TestUtil.assertCorrectUTCDate(2002, 5, 5, 0, 0, 0, 0, date);

        date = AMSUB_MHS_L1C_Reader.getDate(2002, 126, 0);
        TestUtil.assertCorrectUTCDate(2002, 5, 6, 0, 0, 0, 0, date);

        date = AMSUB_MHS_L1C_Reader.getDate(2008, 217, 1000);
        TestUtil.assertCorrectUTCDate(2008, 8, 4, 0, 0, 1, 0, date);

        date = AMSUB_MHS_L1C_Reader.getDate(2008, 217, 22567);
        TestUtil.assertCorrectUTCDate(2008, 8, 4, 0, 0, 22, 567, date);

        date = AMSUB_MHS_L1C_Reader.getDate(2008, 217, 82022567);
        TestUtil.assertCorrectUTCDate(2008, 8, 4, 22, 47, 2, 567, date);
    }
}
