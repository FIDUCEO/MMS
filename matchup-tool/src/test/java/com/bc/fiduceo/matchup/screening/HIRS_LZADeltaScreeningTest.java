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


import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HIRS_LZADeltaScreeningTest {

    private HIRS_LZADeltaScreening screening;
    private String secondarySensorName;
    private HashMap<String, Reader> secondaryReaderMap;


    @Before
    public void SetUp() {
        screening = new HIRS_LZADeltaScreening();
        secondarySensorName = SampleSet.getOnlyOneSecondaryKey();
        secondaryReaderMap = new HashMap<>();
    }

    @Test
    public void testApply_emptyInputSet() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();
        final Reader primaryReader = mock(Reader.class);
        final Reader secondaryReader = mock(Reader.class);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        assertEquals(0, matchupSet.getNumObservations());

        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(23, 174, 35, 554));
        sampleSets.add(createSampleSet(24, 175, 36, 555));  // <- this one gets removed
        sampleSets.add(createSampleSet(25, 176, 37, 556));

        final Array highLZAArray = mock(ucar.ma2.Array.class);
        when(highLZAArray.getDouble(0)).thenReturn(27.72);

        final Array midLZAArray = mock(ucar.ma2.Array.class);
        when(midLZAArray.getDouble(0)).thenReturn(7.13);

        final Array lowLZAArray = mock(ucar.ma2.Array.class);
        when(lowLZAArray.getDouble(0)).thenReturn(5.09);

        final Array leftPosArray = mock(ucar.ma2.Array.class);
        when(leftPosArray.getInt(0)).thenReturn(27);

        final Array righPosArray = mock(ucar.ma2.Array.class);
        when(righPosArray.getInt(0)).thenReturn(28);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(23), eq(174), anyObject(), eq("lza"))).thenReturn(midLZAArray);
        when(primaryReader.readScaled(eq(23), eq(174), anyObject(), eq("scanpos"))).thenReturn(leftPosArray);
        when(primaryReader.readScaled(eq(24), eq(175), anyObject(), eq("lza"))).thenReturn(highLZAArray);
        when(primaryReader.readScaled(eq(24), eq(175), anyObject(), eq("scanpos"))).thenReturn(righPosArray);
        when(primaryReader.readScaled(eq(25), eq(176), anyObject(), eq("lza"))).thenReturn(lowLZAArray);
        when(primaryReader.readScaled(eq(25), eq(176), anyObject(), eq("scanpos"))).thenReturn(righPosArray);

        final Reader secondReader = mock(Reader.class);
        when(secondReader.readScaled(eq(35), eq(554), anyObject(), eq("lza"))).thenReturn(lowLZAArray);
        when(secondReader.readScaled(eq(35), eq(554), anyObject(), eq("scanpos"))).thenReturn(leftPosArray);
        when(secondReader.readScaled(eq(36), eq(555), anyObject(), eq("lza"))).thenReturn(lowLZAArray);
        when(secondReader.readScaled(eq(36), eq(555), anyObject(), eq("scanpos"))).thenReturn(leftPosArray);
        when(secondReader.readScaled(eq(37), eq(556), anyObject(), eq("lza"))).thenReturn(midLZAArray);
        when(secondReader.readScaled(eq(37), eq(556), anyObject(), eq("scanpos"))).thenReturn(righPosArray);
        secondaryReaderMap.put(secondarySensorName, secondReader);


        final HIRS_LZADeltaScreening.Configuration configuration = new HIRS_LZADeltaScreening.Configuration();
        configuration.maxLzaDelta = 10.0;
        screening.configure(configuration);

        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());

        assertEquals(23, sampleSets.get(0).getPrimary().x);
        assertEquals(25, sampleSets.get(1).getPrimary().x);
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final SampleSet sampleSet = new SampleSet();
        final Sample primary = new Sample(primaryX, primaryY, 3.0987, 4.876, 5014783);
        sampleSet.setPrimary(primary);

        final Sample secondary = new Sample(secondaryX, secondaryY, 6.0987, 7.876, 8014783);
        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), secondary);

        return sampleSet;
    }
}
