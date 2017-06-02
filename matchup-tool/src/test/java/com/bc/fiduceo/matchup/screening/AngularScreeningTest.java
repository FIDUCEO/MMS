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

public class AngularScreeningTest {

    private AngularScreening screening;
    private AngularScreening.Configuration configuration;
    private String secondarySensorName;
    private HashMap<String, Reader> secondaryReaderMap;


    @Before
    public void setUp() {
        screening = new AngularScreening();
        configuration = new AngularScreening.Configuration();
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

        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_onlyPrimaryVZA() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(23, 54, 223, 254));
        sampleSets.add(createSampleSet(24, 55, 224, 255));  // <- this one gets removed
        sampleSets.add(createSampleSet(25, 56, 225, 256));

        final Array highAngleArray = mock(Array.class);
        when(highAngleArray.getDouble(0)).thenReturn(44.8);

        final Array lowAngleArray = mock(Array.class);
        when(lowAngleArray.getDouble(0)).thenReturn(8.2);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(23), eq(54), anyObject(), eq("VZA"))).thenReturn(lowAngleArray);
        when(primaryReader.readScaled(eq(24), eq(55), anyObject(), eq("VZA"))).thenReturn(highAngleArray);
        when(primaryReader.readScaled(eq(25), eq(56), anyObject(), eq("VZA"))).thenReturn(lowAngleArray);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(223), eq(254), anyObject(), eq("VZA"))).thenReturn(lowAngleArray);
        when(secondaryReader.readScaled(eq(224), eq(255), anyObject(), eq("VZA"))).thenReturn(lowAngleArray);
        when(secondaryReader.readScaled(eq(225), eq(256), anyObject(), eq("VZA"))).thenReturn(lowAngleArray);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        configuration.usePrimary = true;
        configuration.primaryVariableName = "VZA";
        configuration.maxPrimaryVZA = 10.0;
        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());
        assertEquals(23, sampleSets.get(0).getPrimary().x);
        assertEquals(25, sampleSets.get(1).getPrimary().x);
    }

    @Test
    public void testApply_onlySecondaryVZA() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(33, 64, 233, 264));
        sampleSets.add(createSampleSet(34, 65, 234, 265));
        sampleSets.add(createSampleSet(35, 66, 235, 266));  // <- this one gets removed

        final Array highAngleArray = mock(Array.class);
        when(highAngleArray.getDouble(0)).thenReturn(34.8);

        final Array lowAngleArray = mock(Array.class);
        when(lowAngleArray.getDouble(0)).thenReturn(9.2);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(33), eq(64), anyObject(), eq("satellite_zenith"))).thenReturn(lowAngleArray);
        when(primaryReader.readScaled(eq(34), eq(65), anyObject(), eq("satellite_zenith"))).thenReturn(lowAngleArray);
        when(primaryReader.readScaled(eq(55), eq(66), anyObject(), eq("satellite_zenith"))).thenReturn(lowAngleArray);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(233), eq(264), anyObject(), eq("satellite_zenith"))).thenReturn(lowAngleArray);
        when(secondaryReader.readScaled(eq(234), eq(265), anyObject(), eq("satellite_zenith"))).thenReturn(lowAngleArray);
        when(secondaryReader.readScaled(eq(235), eq(266), anyObject(), eq("satellite_zenith"))).thenReturn(highAngleArray);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        configuration.useSecondary = true;
        configuration.secondaryVariableName = "satellite_zenith";
        configuration.maxSecondaryVZA = 10.0;
        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());
        assertEquals(33, sampleSets.get(0).getPrimary().x);
        assertEquals(34, sampleSets.get(1).getPrimary().x);
    }

    @Test
    public void testApply_bothVZA_onDifferentPixels() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(43, 54, 243, 254));  // <- this one gets removed
        sampleSets.add(createSampleSet(44, 55, 244, 255));
        sampleSets.add(createSampleSet(45, 56, 245, 256));  // <- this one gets removed

        final Array highAngleArray = mock(Array.class);
        when(highAngleArray.getDouble(0)).thenReturn(34.8);

        final Array lowAngleArray = mock(Array.class);
        when(lowAngleArray.getDouble(0)).thenReturn(8.2);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(43), eq(54), anyObject(), eq("the_angle"))).thenReturn(highAngleArray);
        when(primaryReader.readScaled(eq(44), eq(55), anyObject(), eq("the_angle"))).thenReturn(lowAngleArray);
        when(primaryReader.readScaled(eq(45), eq(56), anyObject(), eq("the_angle"))).thenReturn(lowAngleArray);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(243), eq(254), anyObject(), eq("the_other_angle"))).thenReturn(lowAngleArray);
        when(secondaryReader.readScaled(eq(244), eq(255), anyObject(), eq("the_other_angle"))).thenReturn(lowAngleArray);
        when(secondaryReader.readScaled(eq(245), eq(256), anyObject(), eq("the_other_angle"))).thenReturn(highAngleArray);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        configuration.usePrimary = true;
        configuration.primaryVariableName = "the_angle";
        configuration.maxPrimaryVZA = 10.0;
        configuration.useSecondary = true;
        configuration.secondaryVariableName = "the_other_angle";
        configuration.maxSecondaryVZA = 9.0;
        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());
        assertEquals(44, sampleSets.get(0).getPrimary().x);
    }

    @Test
    public void testApply_bothVZA_onSamePixel() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(53, 64, 253, 264));
        sampleSets.add(createSampleSet(54, 65, 254, 265));   // <- this one gets removed
        sampleSets.add(createSampleSet(55, 66, 255, 266));

        final Array highAngleArray = mock(Array.class);
        when(highAngleArray.getDouble(0)).thenReturn(24.8);

        final Array lowAngleArray = mock(Array.class);
        when(lowAngleArray.getDouble(0)).thenReturn(8.2);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(53), eq(64), anyObject(), eq("the_angle"))).thenReturn(lowAngleArray);
        when(primaryReader.readScaled(eq(54), eq(65), anyObject(), eq("the_angle"))).thenReturn(highAngleArray);
        when(primaryReader.readScaled(eq(55), eq(66), anyObject(), eq("the_angle"))).thenReturn(lowAngleArray);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(253), eq(264), anyObject(), eq("the_other_angle"))).thenReturn(lowAngleArray);
        when(secondaryReader.readScaled(eq(254), eq(265), anyObject(), eq("the_other_angle"))).thenReturn(highAngleArray);
        when(secondaryReader.readScaled(eq(255), eq(266), anyObject(), eq("the_other_angle"))).thenReturn(lowAngleArray);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        configuration.usePrimary = true;
        configuration.primaryVariableName = "the_angle";
        configuration.maxPrimaryVZA = 10.0;
        configuration.useSecondary = true;
        configuration.secondaryVariableName = "the_other_angle";
        configuration.maxSecondaryVZA = 10.0;
        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());
        assertEquals(53, sampleSets.get(0).getPrimary().x);
        assertEquals(55, sampleSets.get(1).getPrimary().x);
    }

    @Test
    public void testApply_vzaDelta() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(63, 74, 263, 274));
        sampleSets.add(createSampleSet(64, 75, 264, 275));
        sampleSets.add(createSampleSet(65, 76, 265, 276));   // <- this one gets removed

        final Array tooHighAngleArray = mock(Array.class);
        when(tooHighAngleArray.getDouble(0)).thenReturn(26.8);

        final Array highAngleArray = mock(Array.class);
        when(highAngleArray.getDouble(0)).thenReturn(6.8);

        final Array lowAngleArray = mock(Array.class);
        when(lowAngleArray.getDouble(0)).thenReturn(8.2);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(63), eq(74), anyObject(), eq("the_angle"))).thenReturn(lowAngleArray);
        when(primaryReader.readScaled(eq(64), eq(75), anyObject(), eq("the_angle"))).thenReturn(highAngleArray);
        when(primaryReader.readScaled(eq(65), eq(76), anyObject(), eq("the_angle"))).thenReturn(tooHighAngleArray);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(263), eq(274), anyObject(), eq("the_other_angle"))).thenReturn(highAngleArray);
        when(secondaryReader.readScaled(eq(264), eq(275), anyObject(), eq("the_other_angle"))).thenReturn(lowAngleArray);
        when(secondaryReader.readScaled(eq(265), eq(276), anyObject(), eq("the_other_angle"))).thenReturn(lowAngleArray);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        configuration.primaryVariableName = "the_angle";
        configuration.secondaryVariableName = "the_other_angle";
        configuration.useDelta = true;
        configuration.maxAngleDelta = 5.5;
        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());
        assertEquals(63, sampleSets.get(0).getPrimary().x);
        assertEquals(64, sampleSets.get(1).getPrimary().x);
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final SampleSet sampleSet = new SampleSet();
        final Sample primary = new Sample(primaryX, primaryY, 1.0987, 3.876, 1994783);
        sampleSet.setPrimary(primary);
        final Sample secondary = new Sample(secondaryX, secondaryY, 2.0987, 4.876, 2994783);
        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), secondary);

        return sampleSet;
    }
}
