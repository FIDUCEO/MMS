
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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("OctalInteger")
@RunWith(IOTestRunner.class)
public class EumetsatIASIReader_IO_Test {

    //--- Polygon with the interval of 6 at the
    private EumetsatIASIReader eumetSatIASIReader;
    private AcquisitionInfo acquisitionInfo;

    @Before
    public void setUp() throws IOException {
        final File dataDirectory = TestUtil.getTestDataDirectory();

        File file = new File(dataDirectory, "W_XX-EUMETSAT-Darmstadt,HYPERSPECT+SOUNDING,MetOpA+IASI_C_EUMP_20130528172543_34281_eps_o_l1.nc");
        eumetSatIASIReader = new EumetsatIASIReader();
        eumetSatIASIReader.open(file);

        acquisitionInfo = eumetSatIASIReader.read();
        assertNotNull(acquisitionInfo);
    }

    @After
    public void tearDown() throws IOException {
        eumetSatIASIReader.close();
    }


    @Test
    public void testGeoCoordinate() throws com.vividsolutions.jts.io.ParseException {
       acquisitionInfo.getCoordinates();


    }

    @Test
    public void testDateTime() {
        Date date = acquisitionInfo.getSensingStart();
        TestUtil.assertCorrectUTCDate(2013, 5, 28, 17, 25, 43, date);

        date = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2013, 5, 28, 17, 44, 54, date);
    }
}
