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

public class AtsrAngularScreeningTest {

    private AtsrAngularScreening.Configuration configuration;
    private AtsrAngularScreening screening;
    private String secondarySensorName;
    private HashMap<String, Reader> secondaryReaderMap;


    @Before
    public void setUp() {
        configuration = new AtsrAngularScreening.Configuration();
        screening = new AtsrAngularScreening();
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
    public void testApply_nadirView_leftOfNadir_ATSR() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();
        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(367, 54, 219, 254));
        sampleSets.add(createSampleSet(368, 55, 220, 255));  // <- this one gets removed

        final Array nadirViewElevationHigh = mock(Array.class);
        when(nadirViewElevationHigh.getDouble(0)).thenReturn(78.4);

        final Array nadirViewElevationLow = mock(Array.class);
        when(nadirViewElevationLow.getDouble(0)).thenReturn(54.4);

        final Array fwardViewElevation = mock(Array.class);
        when(fwardViewElevation.getDouble(0)).thenReturn(34.76);

        final Array satZenithAngle = mock(Array.class);
        when(satZenithAngle.getDouble(0)).thenReturn(18.76);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(367), eq(54), anyObject(), eq("view_elev_nadir"))).thenReturn(nadirViewElevationHigh);
        when(primaryReader.readScaled(eq(368), eq(55), anyObject(), eq("view_elev_nadir"))).thenReturn(nadirViewElevationLow);
        when(primaryReader.readScaled(eq(367), eq(54), anyObject(), eq("view_elev_fward"))).thenReturn(fwardViewElevation);
        when(primaryReader.readScaled(eq(368), eq(55), anyObject(), eq("view_elev_fward"))).thenReturn(fwardViewElevation);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(219), eq(254), anyObject(), eq("satellite_zenith_angle"))).thenReturn(satZenithAngle);
        when(secondaryReader.readScaled(eq(220), eq(255), anyObject(), eq("satellite_zenith_angle"))).thenReturn(satZenithAngle);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        configuration.angleDeltaNadir = 10.0;
        configuration.angleDeltaFward = 10.0;
        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());
        assertEquals(367, sampleSets.get(0).getPrimary().x);
    }

    @Test
    public void testApply_nadirView_ATSRLeft_AVHRRRight() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();
        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(467, 54, 119, 254)); // <- this one gets removed
        sampleSets.add(createSampleSet(468, 55, 120, 255));

        final Array nadirViewElevationHigh = mock(Array.class);
        when(nadirViewElevationHigh.getDouble(0)).thenReturn(82.4);

        final Array nadirViewElevationLow = mock(Array.class);
        when(nadirViewElevationLow.getDouble(0)).thenReturn(53.4);

        final Array fwardViewElevation = mock(Array.class);
        when(fwardViewElevation.getDouble(0)).thenReturn(33.76);

        final Array satZenithAngle = mock(Array.class);
        when(satZenithAngle.getDouble(0)).thenReturn(0.76);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(467), eq(54), anyObject(), eq("view_elev_nadir"))).thenReturn(nadirViewElevationLow);
        when(primaryReader.readScaled(eq(468), eq(55), anyObject(), eq("view_elev_nadir"))).thenReturn(nadirViewElevationHigh);
        when(primaryReader.readScaled(eq(467), eq(54), anyObject(), eq("view_elev_fward"))).thenReturn(fwardViewElevation);
        when(primaryReader.readScaled(eq(468), eq(55), anyObject(), eq("view_elev_fward"))).thenReturn(fwardViewElevation);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(119), eq(254), anyObject(), eq("satellite_zenith_angle"))).thenReturn(satZenithAngle);
        when(secondaryReader.readScaled(eq(120), eq(255), anyObject(), eq("satellite_zenith_angle"))).thenReturn(satZenithAngle);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        configuration.angleDeltaNadir = 10.0;
        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());
        assertEquals(468, sampleSets.get(0).getPrimary().x);
    }

    @Test
    public void testApply_fwardView_rightOfNadir_ATSR() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();
        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(67, 54, 19, 254));   // <- this one gets removed
        sampleSets.add(createSampleSet(68, 55, 20, 255));

        final Array nadirViewElevationHigh = mock(Array.class);
        when(nadirViewElevationHigh.getDouble(0)).thenReturn(78.4);

        final Array fwardViewElevationLow = mock(Array.class);
        when(fwardViewElevationLow.getDouble(0)).thenReturn(22.4);

        final Array fwardViewElevation = mock(Array.class);
        when(fwardViewElevation.getDouble(0)).thenReturn(34.76);

        final Array satZenithAngle = mock(Array.class);
        when(satZenithAngle.getDouble(0)).thenReturn(56.76);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(67), eq(54), anyObject(), eq("view_elev_nadir"))).thenReturn(nadirViewElevationHigh);
        when(primaryReader.readScaled(eq(68), eq(55), anyObject(), eq("view_elev_nadir"))).thenReturn(nadirViewElevationHigh);
        when(primaryReader.readScaled(eq(67), eq(54), anyObject(), eq("view_elev_fward"))).thenReturn(fwardViewElevationLow);
        when(primaryReader.readScaled(eq(68), eq(55), anyObject(), eq("view_elev_fward"))).thenReturn(fwardViewElevation);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(19), eq(254), anyObject(), eq("satellite_zenith_angle"))).thenReturn(satZenithAngle);
        when(secondaryReader.readScaled(eq(20), eq(255), anyObject(), eq("satellite_zenith_angle"))).thenReturn(satZenithAngle);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        configuration.angleDeltaNadir = 10.0;
        configuration.angleDeltaFward = 10.0;
        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());
        assertEquals(68, sampleSets.get(0).getPrimary().x);
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final SampleSet sampleSet = new SampleSet();
        final Sample primary = new Sample(primaryX, primaryY, 2.0987, 3.876, 4994783);
        sampleSet.setPrimary(primary);
        final Sample secondary = new Sample(secondaryX, secondaryY, 5.0987, 6.876, 7994783);
        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), secondary);

        return sampleSet;
    }
}
