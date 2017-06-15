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

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class IASI_ReaderTest {

    private IASI_Reader reader;

    @Before
    public void setUp() throws Exception {
        reader = new IASI_Reader(null); // we do not need a geometry factory in this test tb 2017-04-24
    }

    @Test
    public void testGetRegEx() {
        final String expected = "IASI_xxx_1C_M0[1-3]_\\d{14}Z_\\d{14}Z_\\w_\\w_\\d{14}Z.nat";

        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("IASI_xxx_1C_M02_20110914105054Z_20110914122958Z_N_O_20110914114312Z.nat");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("IASI_xxx_1C_M01_20140720180857Z_20140720194753Z_N_O_20140720190004Z.nat");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_201011230036_D.hdf");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.E2");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetVariables() throws IOException {
        final List<Variable> variables = reader.getVariables();
        assertNotNull(variables);
        assertEquals(38, variables.size());

        Variable variable = variables.get(0);
        assertEquals("DEGRADED_INST_MDR", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());
        List<Attribute> attributes = variable.getAttributes();
        assertEquals("Quality of MDR has been degraded from nominal due to an instrument degradation", attributes.get(0).getStringValue());
        assertEquals(NetCDFUtils.getDefaultFillValue(byte.class), attributes.get(1).getNumericValue());

        variable = variables.get(6);
        assertEquals("GEPSDatIasi", variable.getShortName());
        assertEquals(DataType.LONG, variable.getDataType());
        attributes = variable.getAttributes();
        assertEquals("Date of IASI measure (corrected UTC)", attributes.get(0).getStringValue());
        assertEquals("ms", attributes.get(1).getStringValue());
        assertEquals("Corrected UTC in in milliseconds since 1970-01-01 00:00:00", attributes.get(2).getStringValue());
        assertEquals(NetCDFUtils.getDefaultFillValue(long.class), attributes.get(3).getNumericValue());

        variable = variables.get(14);
        assertEquals("GQisQualIndexSpect", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
        attributes = variable.getAttributes();
        assertEquals("Spectral quality index for sounder product", attributes.get(0).getStringValue());
        assertEquals(NetCDFUtils.getDefaultFillValue(float.class), attributes.get(1).getNumericValue());

        variable = variables.get(17);
        assertEquals("GGeoSondLoc_Lon", variable.getShortName());
        assertEquals(DataType.INT, variable.getDataType());
        attributes = variable.getAttributes();
        assertEquals("Location of pixel centre in geodetic coordinates for each sounder pixel (lon)", attributes.get(0).getStringValue());
        assertEquals("longitude", attributes.get(1).getStringValue());
        assertEquals("degrees_east", attributes.get(2).getStringValue());
        assertEquals(NetCDFUtils.getDefaultFillValue(int.class), attributes.get(3).getNumericValue());
        assertEquals(1e-6, attributes.get(4).getNumericValue().doubleValue(), 1e-8);

        variable = variables.get(23);
        assertEquals("EARTH_SATELLITE_DISTANCE", variable.getShortName());
        assertEquals(DataType.INT, variable.getDataType());
        attributes = variable.getAttributes();
        assertEquals("Distance of satellite from Earth centre", attributes.get(0).getStringValue());
        assertEquals("m", attributes.get(1).getStringValue());
        assertEquals(NetCDFUtils.getDefaultFillValue(int.class), attributes.get(2).getNumericValue());

        variable = variables.get(30);
        assertEquals("GCcsImageClassifiedNbCol", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());
        attributes = variable.getAttributes();
        assertEquals("Radiance Analysis: Number of useful columns", attributes.get(0).getStringValue());
        assertEquals(NetCDFUtils.getDefaultFillValue(short.class), attributes.get(1).getNumericValue());

        variable = variables.get(37);
        assertEquals("GEUMAvhrr1BQual", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());
        attributes = variable.getAttributes();
        assertEquals("Quality indicator. If the quality is good, it gives the coverage of snow/ice.", attributes.get(0).getStringValue());
        assertEquals(NetCDFUtils.getDefaultFillValue(byte.class), attributes.get(1).getNumericValue());
    }

    @Test
    public void testCheckRecordSubClass() {
        final GenericRecordHeader genericRecordHeader = new GenericRecordHeader();
        genericRecordHeader.recordSubclassVersion = 4;

        IASI_Reader.checkRecordSubClass(genericRecordHeader);

        genericRecordHeader.recordSubclassVersion = 5;

        IASI_Reader.checkRecordSubClass(genericRecordHeader);
    }

    @Test
    public void testCheckRecordSubClass_invalid() {
        final GenericRecordHeader genericRecordHeader = new GenericRecordHeader();
        genericRecordHeader.recordSubclassVersion = 3;

        try {
            IASI_Reader.checkRecordSubClass(genericRecordHeader);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

        genericRecordHeader.recordSubclassVersion = 6;

        try {
            IASI_Reader.checkRecordSubClass(genericRecordHeader);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
