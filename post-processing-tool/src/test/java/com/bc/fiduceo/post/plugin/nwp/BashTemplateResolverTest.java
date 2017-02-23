/*
 * Copyright (C) 2017 Brockmann Consult GmbH
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

package com.bc.fiduceo.post.plugin.nwp;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class BashTemplateResolverTest {

    private static final String CDO_MATCHUP_AN_TEMPLATE =
            "#! /bin/sh\n" +
                    "${CDO} ${CDO_OPTS} -f nc2 mergetime ${GGAS_TIMESTEPS} ${GGAS_TIME_SERIES} && " +
                    "${CDO} ${CDO_OPTS} -f nc2 setreftime,${REFTIME} -remapbil,${GEO} -selname,CI,SSTK,U10,V10 ${GGAS_TIME_SERIES} ${AN_TIME_SERIES}\n";

    @Test
    public void testResolveAnalysisTemplate() {
        final Properties properties = new Properties();
        properties.put("CDO", "/fiduceo/bin/cdo_dir");
        properties.put("CDO_OPTS", "-L -M");
        properties.put("GGAS_TIMESTEPS", "/home/tom/ggas12345.nc");
        properties.put("GGAS_TIME_SERIES", "/home/tom/ggas6789.nc");
        properties.put("REFTIME", "1970-01-01,00:00:00,seconds");
        properties.put("GEO", "/home/tom/geo123.nc");
        properties.put("AN_TIME_SERIES", "/home/tom/an_target.nc");

        final BashTemplateResolver templateResolver = new BashTemplateResolver(properties);
        final String resolved = templateResolver.resolve(CDO_MATCHUP_AN_TEMPLATE);
        assertEquals("#! /bin/sh\n" +
                "/fiduceo/bin/cdo_dir -L -M -f nc2 mergetime /home/tom/ggas12345.nc /home/tom/ggas6789.nc && /fiduceo/bin/cdo_dir -L -M -f nc2 setreftime,1970-01-01,00:00:00,seconds -remapbil,/home/tom/geo123.nc -selname,CI,SSTK,U10,V10 /home/tom/ggas6789.nc /home/tom/an_target.nc\n",
                resolved);
    }

    @Test
    public void testIsResolved_resolved() {
        final String resolved = "#! /bin/sh\n/fiduceo/bin/cdo_dir -L -M -f nc2 mergetime /home/tom/ggas12345.nc /home/tom/ggas6789.nc && /fiduceo/bin/cdo_dir -L -M -f nc2 setreftime,1970-01-01,00:00:00,seconds -remapbil,/home/tom/geo123.nc -selname,CI,SSTK,U10,V10 /home/tom/ggas6789.nc /home/tom/an_target.nc\n";

        final BashTemplateResolver resolver = new BashTemplateResolver(new Properties());
        assertTrue(resolver.isResolved(resolved));
    }

    @Test
    public void testIsResolved_unresolved() {
        final BashTemplateResolver resolver = new BashTemplateResolver(new Properties());
        assertFalse(resolver.isResolved(CDO_MATCHUP_AN_TEMPLATE));
    }

    @Test
    public void testResolve_static_resolved() {
        final Properties properties = new Properties();
        properties.put("CDO", "/fiduceo/bin/cdo_dir");

        final String resolved = BashTemplateResolver.resolve("huhu, ${CDO}", properties);
        assertEquals("huhu, /fiduceo/bin/cdo_dir", resolved);
    }

    @Test
    public void testResolve_static_unresolved() {
        final Properties properties = new Properties();

        try {
            BashTemplateResolver.resolve("huhu, ${CDO}", properties);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
