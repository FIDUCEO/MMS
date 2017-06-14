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

public class MDR_1C_v4_Test {

    @Test
    public void testConstruction_getRaw_record() {
        final MDR_1C_v4 mdr_1C = new MDR_1C_v4();

        final byte[] raw_record = mdr_1C.getRaw_record();
        assertNotNull(raw_record);
        assertEquals(2727768, raw_record.length);
    }

    @Test
    public void testGetMdrSize() {
        final MDR_1C_v4 mdr_1C = new MDR_1C_v4();

        assertEquals(2727768, mdr_1C.getMdrSize());
    }

    @Test
    public void testGetReadProxies() {
        final HashMap<String, ReadProxy> proxies = MDR_1C_v4.getReadProxies();
        // @todo 1 tb/tb continue here 2017-06-14
//        assertNotNull(proxies);
//        assertEquals(38, proxies.size());
    }
}
