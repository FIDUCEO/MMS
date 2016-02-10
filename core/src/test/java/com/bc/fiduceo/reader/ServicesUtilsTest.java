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

import com.bc.fiduceo.core.ServicesUtils;
import com.bc.fiduceo.db.Driver;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * @author muhammad.bc
 */
public class ServicesUtilsTest {
    @Test
    public void getReaderTest() {
        Driver driver;
        Reader reader;
        ServicesUtils servicesUtils = new ServicesUtils<>();

        reader = (Reader) servicesUtils.getServices(Reader.class, "AIRS");
        assertTrue("AIRS".equals("AIRS"));


        reader = (Reader) servicesUtils.getServices(Reader.class, "AMSU");
        assertTrue("AMSU".contains("AMSU"));

        reader = (Reader) servicesUtils.getServices(Reader.class, "MHS");
        assertTrue("MHS".contains("MHS"));

        reader = (Reader) servicesUtils.getServices(Reader.class, "EUMETSAT");
        assertTrue("EUMETSAT".equals("EUMETSAT"));


        driver = (Driver) servicesUtils.getServices(Driver.class, "jdbc:h2:mem:fiduceo");
        assertTrue(driver.getUrlPattern().toLowerCase().equals("jdbc:h2"));


        driver = (Driver) servicesUtils.getServices(Driver.class, "jdbc:mysql://localhost:3306/test");
        assertTrue(driver.getUrlPattern().toLowerCase().equals("jdbc:mysql"));


        driver = (Driver) servicesUtils.getServices(Driver.class, "jdbc:postgresql://localhost:5432/test");
        assertTrue(driver.getUrlPattern().toLowerCase().equals("jdbc:postgresql"));
    }

}
