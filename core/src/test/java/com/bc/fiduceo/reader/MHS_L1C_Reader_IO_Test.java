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

package com.bc.fiduceo.reader;


import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.esa.snap.core.datamodel.GeoCoding;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class MHS_L1C_Reader_IO_Test {

    @Test
    public void testGetGeoCoding() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File file = new File(testDataDirectory, "L8912163.NSS.AMBX.NK.D08001.S0000.E0155.B5008586.GC.gz.l1c.h5");
        assertTrue(file.isFile());

        final AMSU_MHS_L1B_Reader reader = new AMSU_MHS_L1B_Reader();
        reader.open(file);

        GeoCoding geoCoding = reader.getGeoCoding();
        assertNotNull(geoCoding);

        try {

        } finally {
            reader.close();
        }
    }
}
