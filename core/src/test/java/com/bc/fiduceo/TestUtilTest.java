/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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
package com.bc.fiduceo;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.hamcrest.CoreMatchers;
import org.hamcrest.StringDescription;
import org.junit.*;

public class TestUtilTest {

    private String pattern;
    private TestUtil.StringPatternMatcher matcher;

    @Before
    public void setUp() throws Exception {
        pattern = "\\d{4}-\\d{2}-\\d{2}";
        matcher = new TestUtil.StringPatternMatcher(pattern);
    }

    @Test
    public void testInitalisationOfStringPatternMatcher() {
        final String pattern = "\\d{4}-\\d{2}-\\d{2}";
        new TestUtil.StringPatternMatcher(pattern);
    }

    @Test
    public void testInitalisationOfStringPatternMatcher_emptyPattern() {
        final String pattern = "";
        new TestUtil.StringPatternMatcher(pattern);
    }

    @Test
    public void testInitalisationOfStringPatternMatcher_nullPattern() {
        final String pattern = null;
        try {
            new TestUtil.StringPatternMatcher(pattern);
            fail("Exception expected");
        } catch (Exception expected) {
            assertEquals("Non-null pattern required.", expected.getMessage());
            assertEquals("java.lang.IllegalArgumentException", expected.getClass().getTypeName());
        }
    }

    @Test
    public void testMatchesSafelyOfStringPatternMatcher() {
        assertThat(matcher.matchesSafely("2001-02-03"), is(true));
        assertThat(matcher.matchesSafely("201-02-003"), is(false));
        assertThat(matcher.matchesSafely("2001.02-03"), is(false));
    }

    @Test
    public void testFailureDescriptionOfStringPatternMatcher() {
        final StringDescription description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("a string matching the pattern '\\d{4}-\\d{2}-\\d{2}'", description.toString());
    }

    @Test
    public void testMatchesPattern() {
        assertThat("2001-02-03", TestUtil.matchesPattern(pattern));
        assertThat("2001.02-03", CoreMatchers.not(TestUtil.matchesPattern(pattern)));
    }
}
