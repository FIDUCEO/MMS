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
package com.bc.fiduceo.db;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author muhammad.bc
 */
public class DriverUtilsTest {

    @Test
    public void testGetDriver() {
        final DriverUtils driverUtils = new DriverUtils();

        Driver driver = driverUtils.getDriver("jdbc:h2:mem:fiduceo");
        assertTrue(driver.getUrlPattern().toLowerCase().equals("jdbc:h2"));


        driver = driverUtils.getDriver("jdbc:mysql://localhost:3306/test");
        assertTrue(driver.getUrlPattern().toLowerCase().equals("jdbc:mysql"));


        driver = driverUtils.getDriver("jdbc:postgresql://localhost:5432/test");
        assertTrue(driver.getUrlPattern().toLowerCase().equals("jdbc:postgresql"));
    }

    @Test
    public void testGetDriver_invalidDriverName() {
        final DriverUtils driverUtils = new DriverUtils();

        try {
            driverUtils.getDriver("windows_10_is_shitty");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
