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
 */

package com.bc.fiduceo.post.util;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.util.SobolSequenceGenerator;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.esa.snap.core.util.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class DistanceToLandMapTest {

    @Test
    public void getDistance() throws Exception {
        final Path path = TestUtil.getTestDataDirectory().toPath()
                .resolve("distance_to_land_map")
                .resolve("Globolakes-static_distance_to_land_Map-300m-P5Y-2005-ESACCI_WB-fv1.0_RES120.nc");
        final DistanceToLandMap map = new DistanceToLandMap(path);

        try {
            // Hamburg
            assertEquals(0.0, map.getDistance(10.0, 53.55), 1e-7);
            // Nordsee vor Elbmündung
            assertEquals(19.100, map.getDistance(7.882718, 54.000474), 1e-4);
            // Nordsee zwischen GB und Norwegen
            assertEquals(219.200, map.getDistance(1.865498, 57.873503), 1e-4);
            // Atlantik
            assertEquals(1236.700, map.getDistance(-42.737256, 33.194949), 1e-4);

            final SobolSequenceGenerator sobolSequenceGenerator = new SobolSequenceGenerator(2);
            final StopWatch stopWatch = new StopWatch();
            for (int i = 0; i < 1000; i++) {
                final double[] doubles = sobolSequenceGenerator.nextVector();
                final double lon = 360 * doubles[0] - 180;
                final double lat = 180 * doubles[1] - 90;
                map.getDistance(lon, lat);
            }
            stopWatch.stop();
            final long timeDiff = stopWatch.getTimeDiff();
//        System.out.println("timeDiff = " + timeDiff);
            assertThat(timeDiff, is(lessThan(10L)));
        } finally {
            map.close();
        }
    }

    @Test
    public void fileDoesNotExist() {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final Path path = fileSystem.getPath("not", "existing", "path");
        try {
            new DistanceToLandMap(path);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Missing file: '/work/not/existing/path'", expected.getMessage());
        }
    }
}