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

package com.bc.fiduceo.post.plugin;

import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFileWriter;

import static org.mockito.Mockito.*;

public class AmsreSolarAnglesTest {

    @Test
    public void testPrepare() {

        final AddAmsreSolarAngles addAmsreSolarAngles = new AddAmsreSolarAngles();
        final AddAmsreSolarAngles.Configuration configuration = new AddAmsreSolarAngles.Configuration();
        configuration.szaVariable = "sun_zenith_angle";
        configuration.saaVariable = "sun_azimuth_angle";
        addAmsreSolarAngles.configure(configuration);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);

        addAmsreSolarAngles.prepareImpl(null, writer);  // we do not need the reader now tb 2016-12-14

        // @todo 1 tb/tb continue here 2016-12-14
        //verify(writer).addVariable(null, "sun_zenith_angle", DataType.FLOAT, "TODO");


    }
}
