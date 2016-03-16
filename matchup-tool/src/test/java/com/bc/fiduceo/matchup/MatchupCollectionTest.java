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

package com.bc.fiduceo.matchup;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MatchupCollectionTest {

    private MatchupCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = new MatchupCollection();
    }

    @Test
    public void testGet_emptyCollection() {
        final List<MatchupSet> sets = collection.getSets();
        assertNotNull(sets);
        assertEquals(0, sets.size());
    }

    @Test
    public void testAddAndGet() {
        MatchupSet set = new MatchupSet();
        collection.add(set);

        List<MatchupSet> sets = collection.getSets();
        assertEquals(1, sets.size());

        set = new MatchupSet();
        collection.add(set);

        sets = collection.getSets();
        assertEquals(2, sets.size());
    }

    @Test
    public void testGetNumMatchups_emptyList() {
        assertEquals(0, collection.getNumMatchups());
    }

    @Test
    public void testGetNumMatchups_oneSet() {
        final MatchupSet set = mock(MatchupSet.class);
        when(set.getNumObservations()).thenReturn(27);

        collection.add(set);

        assertEquals(27, collection.getNumMatchups());
    }

    @Test
    public void testGetNumMatchups_twoSets() {
        final MatchupSet set_1 = mock(MatchupSet.class);
        when(set_1.getNumObservations()).thenReturn(57);
        collection.add(set_1);

        final MatchupSet set_2 = mock(MatchupSet.class);
        when(set_2.getNumObservations()).thenReturn(109);
        collection.add(set_2);

        assertEquals(57 + 109, collection.getNumMatchups());
    }

    @Test
    public void testGetFirst_emptyList() {
        final MatchupSet set = collection.getFirst();
        assertNull(set);
    }

    @Test
    public void testGetFirst() {
        final MatchupSet first = new MatchupSet();
        collection.add(first);

        final MatchupSet second = new MatchupSet();
        collection.add(second);

        final MatchupSet set = collection.getFirst();
        assertSame(first, set);
    }
}
