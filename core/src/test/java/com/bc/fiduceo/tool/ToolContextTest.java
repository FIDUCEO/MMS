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

package com.bc.fiduceo.tool;


import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.core.UseCaseConfigBuilder;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.ReaderFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class ToolContextTest {

    private ToolContext context;

    @Before
    public void setUp() {
        context = new ToolContext();
    }

    @Test
    public void testSetGetStartDate() {
        final Date date = new Date();
        context.setStartDate(date);
        assertEquals(date.getTime(), context.getStartDate().getTime());
    }

    @Test
    public void testSetGetEndDate() {
        final Date date = new Date();
        context.setEndDate(date);
        assertEquals(date.getTime(), context.getEndDate().getTime());
    }

    @Test
    public void testSetGetStorage() {
        final Storage storage = mock(Storage.class);

        context.setStorage(storage);
        assertSame(storage, context.getStorage());
    }

    @Test
    public void testSetGetUseCaseConfig() {
        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder.build("mmd-12").createConfig();

        context.setUseCaseConfig(useCaseConfig);
        assertSame(useCaseConfig, context.getUseCaseConfig());
    }

    @Test
    public void testSetGetGeometryFactory() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);

        context.setGeometryFactory(geometryFactory);
        assertSame(geometryFactory, context.getGeometryFactory());
    }

    @Test
    public void testSetGetSystemConfig() {
        final SystemConfig systemConfig = new SystemConfig();

        context.setSystemConfig(systemConfig);
        assertSame(systemConfig, context.getSystemConfig());
    }

    @Test
    public void testSetGetReaderFactory() {
        final ReaderFactory readerFactory = mock(ReaderFactory.class);

        context.setReaderFactory(readerFactory);
        assertSame(readerFactory, context.getReaderFactory());
    }
}
