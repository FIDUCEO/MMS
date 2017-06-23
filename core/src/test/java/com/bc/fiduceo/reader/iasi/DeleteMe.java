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

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class DeleteMe {

    @Test
    public void testReadThatFile() throws IOException {
        final File theFile = new File("/usr/local/data/IASI/IASI_xxx_1C_M02_20110301001152Z_20110301015656Z_N_O_20110301015833Z.nat");

        final IASI_Reader reader = new IASI_Reader(new GeometryFactory(GeometryFactory.Type.S2));

        reader.open(theFile);

        final Dimension productSize = reader.getProductSize();

        for (int y = 0; y < productSize.getNy(); y ++) {
            reader.readSpectrum(0, y);
        }

        reader.close();

    }
}
