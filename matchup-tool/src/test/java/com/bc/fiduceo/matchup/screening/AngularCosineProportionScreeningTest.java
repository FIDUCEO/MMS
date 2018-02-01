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
import com.bc.fiduceo.core.Sample;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AngularCosineProportionScreeningTest {

    private AngularCosineProportionScreening screening;
    private AngularCosineProportionScreening.Configuration configuration;
    private String secondarySensorName;
    private HashMap<String, Reader> secondaryReaderMap;

    @Before
    public void setUp() {
        screening = new AngularCosineProportionScreening();
        configuration = new AngularCosineProportionScreening.Configuration();
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
        sampleSets.add(createSampleSet(33, 64, 45, 354));
        sampleSets.add(createSampleSet(34, 65, 46, 355));  // <- this one gets removed
        sampleSets.add(createSampleSet(35, 66, 47, 356));

        final Array highAngleArray = mock(Array.class);
        when(highAngleArray.getDouble(0)).thenReturn(9.46);

        final Array lowAngleArray = mock(Array.class);
        when(lowAngleArray.getDouble(0)).thenReturn(10.73);

        final Array outAngleArray = mock(Array.class);
        when(outAngleArray.getDouble(0)).thenReturn(48.93);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(33), eq(64), any(), eq("Satellite_zenith_angle"))).thenReturn(lowAngleArray);
        when(primaryReader.readScaled(eq(34), eq(65), any(), eq("Satellite_zenith_angle"))).thenReturn(outAngleArray);
        when(primaryReader.readScaled(eq(35), eq(66), any(), eq("Satellite_zenith_angle"))).thenReturn(highAngleArray);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(45), eq(354), any(), eq("the_other_angle"))).thenReturn(highAngleArray);
        when(secondaryReader.readScaled(eq(46), eq(355), any(), eq("the_other_angle"))).thenReturn(lowAngleArray);
        when(secondaryReader.readScaled(eq(47), eq(356), any(), eq("the_other_angle"))).thenReturn(highAngleArray);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        configuration.primaryVariableName = "Satellite_zenith_angle";
        configuration.secondaryVariableName = "the_other_angle";
        configuration.threshold = 0.01;

        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());

        assertEquals(33, sampleSets.get(0).getPrimary().getX());
        assertEquals(35, sampleSets.get(1).getPrimary().getX());
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final SampleSet sampleSet = new SampleSet();
        final Sample primary = new Sample(primaryX, primaryY, 2.0987, 3.876, 2014783);
        sampleSet.setPrimary(primary);

        final Sample secondary = new Sample(secondaryX, secondaryY, 3.0987, 5.876, 3014783);
        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), secondary);

        return sampleSet;
    }
}
