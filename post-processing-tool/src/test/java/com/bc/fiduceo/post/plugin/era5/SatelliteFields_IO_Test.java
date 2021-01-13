package com.bc.fiduceo.post.plugin.era5;


import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(IOTestRunner.class)
public class SatelliteFields_IO_Test {

    @Test
    public void testReadSubset_2D() throws IOException, InvalidRangeException {
        final File anMlFile = TestUtil.getTestDataFileAsserted("era-5/v1/an_ml/2008/05/30/ecmwf-era5_oper_an_ml_200805301100.lnsp.nc");

        try (NetcdfFile netcdfFile = NetcdfFile.open(anMlFile.getAbsolutePath())) {
            final Variable lnsp = netcdfFile.findVariable("lnsp");
            assertNotNull(lnsp);

            final Rectangle era5Positions = new Rectangle(1200, 400, 3, 3);

            final Array lnspArray = SatelliteFields.readSubset(1, era5Positions, lnsp);
            assertNotNull(lnspArray);

            final int[] shape = lnspArray.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            assertEquals(11.503242492675781f, lnspArray.getFloat(0), 1e-8);
            assertEquals(11.503653526306152f, lnspArray.getFloat(1), 1e-8);
            assertEquals(11.505866050720215f, lnspArray.getFloat(2), 1e-8);
        }
    }

    @Test
    public void testReadSubset_3D() throws IOException, InvalidRangeException {
        final File o3File = TestUtil.getTestDataFileAsserted("era-5/v1/an_ml/2008/05/30/ecmwf-era5_oper_an_ml_200805301100.o3.nc");

        try (NetcdfFile netcdfFile = NetcdfFile.open(o3File.getAbsolutePath())) {
            final Variable o3 = netcdfFile.findVariable("o3");
            assertNotNull(o3);

            final Rectangle era5Positions = new Rectangle(1201, 401, 3, 3);

            final Array lnspArray = SatelliteFields.readSubset(137, era5Positions, o3);
            assertNotNull(lnspArray);

            final int[] shape = lnspArray.getShape();
            assertEquals(3, shape.length);
            assertEquals(137, shape[0]);
            assertEquals(3, shape[1]);
            assertEquals(3, shape[2]);

            assertEquals(1.9680332741245365E-7f, lnspArray.getFloat(1), 1e-8);
            assertEquals(1.9680332741245365E-7f, lnspArray.getFloat(2), 1e-8);
            assertEquals(1.9680332741245365E-7f, lnspArray.getFloat(3), 1e-8);
        }
    }

    @Test
    @Ignore
    public void testReadSubset_2D_antiMeridianOverlap() throws IOException, InvalidRangeException {
        final File anMlFile = TestUtil.getTestDataFileAsserted("era-5/v1/an_ml/2008/06/02/ecmwf-era5_oper_an_ml_200806021000.lnsp.nc");

        try (NetcdfFile netcdfFile = NetcdfFile.open(anMlFile.getAbsolutePath())) {
            final Variable lnsp = netcdfFile.findVariable("lnsp");
            assertNotNull(lnsp);

            final Rectangle era5Positions = new Rectangle(1439, 402, 3, 3);

            final Array lnspArray = SatelliteFields.readSubset(1, era5Positions, lnsp);
            assertNotNull(lnspArray);

            final int[] shape = lnspArray.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            assertEquals(11.503242492675781f, lnspArray.getFloat(0), 1e-8);
            assertEquals(11.503653526306152f, lnspArray.getFloat(1), 1e-8);
            assertEquals(11.505866050720215f, lnspArray.getFloat(2), 1e-8);
        }
    }
}
