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

package com.bc.fiduceo.matchup.screening;

import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ScreeningFactoryTest {

    private ScreeningFactory screeningFactory;

    @Before
    public void setUp() {
        screeningFactory = ScreeningFactory.get();
        assertNotNull(screeningFactory);
    }

    @Test
    public void testGet_isSingleton() {
        final ScreeningFactory screeningFactory_2 = ScreeningFactory.get();
        assertNotNull(screeningFactory_2);

        assertSame(screeningFactory, screeningFactory_2);
    }

    @Test
    public void testGetScreening_angular() throws JDOMException, IOException {
        final String XML = "<angular/>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Screening screening = screeningFactory.getScreening(rootElement);
        assertNotNull(screening);
        assertTrue(screening instanceof AngularScreening);
    }

    @Test
    public void testGetScreening_unkownTag() throws JDOMException, IOException {
        final String XML = "<I_WILL_FAIL/>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            screeningFactory.getScreening(rootElement);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
