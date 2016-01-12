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
import org.junit.Assert;
import org.junit.Test;

/**
 * @author muhammad.bc
 */
public class ServicesUtilsTest {
    @Test
    public void getReaderTest() {
        Driver driver;
        Reader reader;
        String searchTerm;
        ServicesUtils servicesUtils = new ServicesUtils<>();
        Assert.assertNotNull(servicesUtils);

        reader = (Reader) servicesUtils.getReader(Reader.class, "AIRS");
        String readerName = reader.getReaderName();
        Assert.assertTrue(readerName.equals("AIRS"));


        reader = (Reader) servicesUtils.getReader(Reader.class, "AIRS");
        String readerAirs = reader.getReaderName();
        Assert.assertTrue(readerAirs.equals("AIRS"));

        reader = (Reader) servicesUtils.getReader(Reader.class, "EUMETASAT");
        String readerEum = reader.getReaderName();
        Assert.assertTrue(readerEum.equals("EUMETASAT"));


        driver = (Driver) servicesUtils.getReader(Driver.class, "jdbc:h2:mem:fiduceo");
        searchTerm = driver.getUrlPattern().toLowerCase();
        Assert.assertTrue(searchTerm.equals("jdbc:h2"));


        driver = (Driver) servicesUtils.getReader(Driver.class, "jdbc:mysql://localhost:3306/test");
        searchTerm = driver.getUrlPattern().toLowerCase();
        Assert.assertTrue(searchTerm.equals("jdbc:mysql"));


        driver = (Driver) servicesUtils.getReader(Driver.class, "jdbc:postgresql://localhost:5432/test");
        searchTerm = driver.getUrlPattern().toLowerCase();
        Assert.assertTrue(searchTerm.equals("jdbc:postgresql"));
    }

}
