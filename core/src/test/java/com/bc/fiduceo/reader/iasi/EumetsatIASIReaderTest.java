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

package com.bc.fiduceo.reader.iasi;

import com.bc.fiduceo.TestUtil;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EumetsatIASIReaderTest {

    private EumetsatIASIReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new EumetsatIASIReader(null);
    }

    @Test
    public void testGetGlobalAttributeAsDate() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        final Attribute attribute = mock(Attribute.class);
        when(attribute.getStringValue()).thenReturn("1999-08-21T17:11:52Z");

        when(netcdfFile.findGlobalAttribute("time_converage_start")).thenReturn(attribute);

        final Date start = EumetsatIASIReader.getGlobalAttributeAsDate("time_converage_start", netcdfFile);
        assertNotNull(start);

        TestUtil.assertCorrectUTCDate(1999, 8, 21, 17, 11, 52, start);
    }

    @Test
    public void testGetGlobalAttributeAsDate_parsingError() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        final Attribute attribute = mock(Attribute.class);
        when(attribute.getStringValue()).thenReturn("unparseable date");

        when(netcdfFile.findGlobalAttribute("the_failing_attribute")).thenReturn(attribute);

        try {
            EumetsatIASIReader.getGlobalAttributeAsDate("the_failing_attribute", netcdfFile);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetGlobalAttributeAsDate_attributeNotPresent() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        when(netcdfFile.findGlobalAttribute("missing_attribute")).thenReturn(null);

        try {
            EumetsatIASIReader.getGlobalAttributeAsDate("missing_attribute", netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }
}
