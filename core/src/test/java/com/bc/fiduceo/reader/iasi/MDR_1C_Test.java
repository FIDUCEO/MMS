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

package com.bc.fiduceo.reader.iasi;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MDR_1C_Test {

    @Test
    public void testConstruction_getRaw_record() {
        final MDR_1C mdr_1C = new MDR_1C();

        final byte[] raw_record = mdr_1C.getRaw_record();
        assertNotNull(raw_record);
        assertEquals(2728908, raw_record.length);
    }

    @Test
    public void testGetEFOVIndex() {
        assertEquals(3, MDR_1C.getEFOVIndex(2, 0));
        assertEquals(3, MDR_1C.getEFOVIndex(16, 0));

        assertEquals(0, MDR_1C.getEFOVIndex(3, 0));
        assertEquals(0, MDR_1C.getEFOVIndex(17, 0));

        assertEquals(2, MDR_1C.getEFOVIndex(2, 1));
        assertEquals(2, MDR_1C.getEFOVIndex(44, 1));

        assertEquals(1, MDR_1C.getEFOVIndex(3, 1));
        assertEquals(1, MDR_1C.getEFOVIndex(45, 1));
    }

    @Test
    public void testGetReadProxies() {
        final HashMap<String, ReadProxy> proxies = MDR_1C.getReadProxies();
        assertNotNull(proxies);
        assertEquals(38, proxies.size());

        ReadProxy readProxy = proxies.get("GEPSDatIasi");
        assertEquals(long.class, readProxy.getDataType());
        assertTrue(Double.isNaN(readProxy.getScaleFactor()));

        readProxy = proxies.get("GGeoSondLoc_Lat");
        assertEquals(int.class, readProxy.getDataType());
        assertEquals(1e-6, readProxy.getScaleFactor(), 1e-8);

        readProxy = proxies.get("GCcsRadAnalNbClass");
        assertEquals(int.class, readProxy.getDataType());
        assertTrue(Double.isNaN(readProxy.getScaleFactor()));

        readProxy = proxies.get("IDefNslast1b");
        assertEquals(int.class, readProxy.getDataType());
        assertTrue(Double.isNaN(readProxy.getScaleFactor()));

        readProxy = proxies.get("GEUMAvhrr1BCldFrac");
        assertEquals(byte.class, readProxy.getDataType());
        assertTrue(Double.isNaN(readProxy.getScaleFactor()));

        readProxy = proxies.get("GQisQualIndex");
        assertEquals(float.class, readProxy.getDataType());
        assertTrue(Double.isNaN(readProxy.getScaleFactor()));
    }
}
