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

package com.bc.fiduceo.ingest;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class IngestionToolIntegrationTest {

    @Test
    public void testIngest_notInputParameter() throws ParseException {
        // @todo 3 tb/tb find a way to steal system.err to implement assertions 2015-12-09
        final String[] args = new String[0];
        IngestionToolMain.main(args);
    }

    @Test
    public void testIngest_help() throws ParseException {
        // @todo 3 tb/tb find a way to steal system.err to implement assertions 2015-12-09
        String[] args = new String[] {"-h"};
        IngestionToolMain.main(args);

        args = new String[] {"--help"};
        IngestionToolMain.main(args);
    }
}
