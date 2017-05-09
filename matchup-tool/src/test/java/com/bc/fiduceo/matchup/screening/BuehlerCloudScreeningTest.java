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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuehlerCloudScreeningTest {

    private BuehlerCloudScreening screening;
    private BuehlerCloudScreening.Configuration configuration;

    @Before
    public void setUp(){
        screening = new BuehlerCloudScreening();
        configuration = new BuehlerCloudScreening.Configuration();
    }

    @Test
    public void testApply_emptyInputSet() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();
        final Reader primaryReader = mock(Reader.class);
        final Reader secondaryReader = mock(Reader.class);

        assertEquals(0, matchupSet.getNumObservations());

        screening.apply(matchupSet, primaryReader, new Reader[]{secondaryReader}, null);

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_onlyPrimary_btempDelta() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(43, 74, 55, 454));   // <- this one gets removed
        sampleSets.add(createSampleSet(44, 75, 56, 455));
        sampleSets.add(createSampleSet(45, 76, 57, 456));

        final Array highBtempArray = mock(ucar.ma2.Array.class);
        when(highBtempArray.getDouble(0)).thenReturn(258.64);

        final Array lowBtempArray = mock(ucar.ma2.Array.class);
        when(lowBtempArray.getDouble(0)).thenReturn(257.22);

        final Array VZAArray = mock(ucar.ma2.Array.class);
        when(VZAArray.getDouble(0)).thenReturn(9.65);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(43), eq(74), anyObject(), eq("btemps_ch18"))).thenReturn(lowBtempArray);   // condition for removal: ch20 > ch18
        when(primaryReader.readScaled(eq(43), eq(74), anyObject(), eq("btemps_ch20"))).thenReturn(highBtempArray);
        when(primaryReader.readScaled(eq(43), eq(74), anyObject(), eq("Satellite_zenith_angle"))).thenReturn(VZAArray);
        when(primaryReader.readScaled(eq(44), eq(75), anyObject(), eq("btemps_ch18"))).thenReturn(highBtempArray);
        when(primaryReader.readScaled(eq(44), eq(75), anyObject(), eq("btemps_ch20"))).thenReturn(lowBtempArray);
        when(primaryReader.readScaled(eq(44), eq(75), anyObject(), eq("Satellite_zenith_angle"))).thenReturn(VZAArray);
        when(primaryReader.readScaled(eq(45), eq(76), anyObject(), eq("btemps_ch18"))).thenReturn(lowBtempArray);
        when(primaryReader.readScaled(eq(45), eq(76), anyObject(), eq("btemps_ch20"))).thenReturn(lowBtempArray);
        when(primaryReader.readScaled(eq(45), eq(76), anyObject(), eq("Satellite_zenith_angle"))).thenReturn(VZAArray);

        final Reader secondaryReader = mock(Reader.class);

        configuration.primaryNarrowChannelName = "btemps_ch18";
        configuration.primaryWideChannelName = "btemps_ch20";
        configuration.primaryVZAVariableName = "Satellite_zenith_angle";

        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, new Reader[]{secondaryReader}, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());

        assertEquals(44, sampleSets.get(0).getPrimary().x);
        assertEquals(45, sampleSets.get(1).getPrimary().x);
    }

    @Test
    public void testApply_onlyPrimary_threshold() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(43, 74, 55, 454));
        sampleSets.add(createSampleSet(44, 75, 56, 455));   // <- this one gets removed
        sampleSets.add(createSampleSet(45, 76, 57, 456));

        final Array highBtempArray = mock(ucar.ma2.Array.class);
        when(highBtempArray.getDouble(0)).thenReturn(258.64);

        final Array lowBtempArray = mock(ucar.ma2.Array.class);
        when(lowBtempArray.getDouble(0)).thenReturn(239.22);

        final Array highVZAArray = mock(ucar.ma2.Array.class);
        when(highVZAArray.getDouble(0)).thenReturn(10.65);

        final Array lowVZAArray = mock(ucar.ma2.Array.class);
        when(lowVZAArray.getDouble(0)).thenReturn(0.63);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(43), eq(74), anyObject(), eq("btemps_ch18"))).thenReturn(highBtempArray);
        when(primaryReader.readScaled(eq(43), eq(74), anyObject(), eq("btemps_ch20"))).thenReturn(lowBtempArray);
        when(primaryReader.readScaled(eq(43), eq(74), anyObject(), eq("Satellite_zenith_angle"))).thenReturn(lowVZAArray);
        when(primaryReader.readScaled(eq(44), eq(75), anyObject(), eq("btemps_ch18"))).thenReturn(lowBtempArray);
        when(primaryReader.readScaled(eq(44), eq(75), anyObject(), eq("btemps_ch20"))).thenReturn(lowBtempArray);
        when(primaryReader.readScaled(eq(44), eq(75), anyObject(), eq("Satellite_zenith_angle"))).thenReturn(highVZAArray);
        when(primaryReader.readScaled(eq(45), eq(76), anyObject(), eq("btemps_ch18"))).thenReturn(highBtempArray);
        when(primaryReader.readScaled(eq(45), eq(76), anyObject(), eq("btemps_ch20"))).thenReturn(lowBtempArray);
        when(primaryReader.readScaled(eq(45), eq(76), anyObject(), eq("Satellite_zenith_angle"))).thenReturn(lowVZAArray);

        final Reader secondaryReader = mock(Reader.class);

        configuration.primaryNarrowChannelName = "btemps_ch18";
        configuration.primaryWideChannelName = "btemps_ch20";
        configuration.primaryVZAVariableName = "Satellite_zenith_angle";

        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, new Reader[]{secondaryReader}, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());

        assertEquals(43, sampleSets.get(0).getPrimary().x);
        assertEquals(45, sampleSets.get(1).getPrimary().x);
    }

    @Test
    public void testApply_onlySecondary_btempDelta() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(53, 64, 45, 454));
        sampleSets.add(createSampleSet(54, 65, 46, 455));
        sampleSets.add(createSampleSet(55, 66, 47, 456)); // <- this one gets removed

        final Array highBtempArray = mock(ucar.ma2.Array.class);
        when(highBtempArray.getDouble(0)).thenReturn(262.64);

        final Array lowBtempArray = mock(ucar.ma2.Array.class);
        when(lowBtempArray.getDouble(0)).thenReturn(258.22);

        final Array VZAArray = mock(ucar.ma2.Array.class);
        when(VZAArray.getDouble(0)).thenReturn(10.65);

        final Reader primaryReader = mock(Reader.class);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(45), eq(454), anyObject(), eq("btemps_ch3"))).thenReturn(highBtempArray);
        when(secondaryReader.readScaled(eq(45), eq(454), anyObject(), eq("btemps_ch4"))).thenReturn(highBtempArray);
        when(secondaryReader.readScaled(eq(45), eq(454), anyObject(), eq("Satellite_zenith_angle"))).thenReturn(VZAArray);
        when(secondaryReader.readScaled(eq(46), eq(455), anyObject(), eq("btemps_ch3"))).thenReturn(highBtempArray);
        when(secondaryReader.readScaled(eq(46), eq(455), anyObject(), eq("btemps_ch4"))).thenReturn(lowBtempArray);
        when(secondaryReader.readScaled(eq(46), eq(455), anyObject(), eq("Satellite_zenith_angle"))).thenReturn(VZAArray);
        when(secondaryReader.readScaled(eq(47), eq(456), anyObject(), eq("btemps_ch3"))).thenReturn(lowBtempArray); // condition for removal: ch20 > ch18
        when(secondaryReader.readScaled(eq(47), eq(456), anyObject(), eq("btemps_ch4"))).thenReturn(highBtempArray);
        when(secondaryReader.readScaled(eq(47), eq(456), anyObject(), eq("Satellite_zenith_angle"))).thenReturn(VZAArray);

        configuration.secondaryNarrowChannelName = "btemps_ch3";
        configuration.secondaryWideChannelName = "btemps_ch4";
        configuration.secondaryVZAVariableName = "Satellite_zenith_angle";

        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, new Reader[]{secondaryReader}, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());

        assertEquals(45, sampleSets.get(0).getSecondary().x);
        assertEquals(46, sampleSets.get(1).getSecondary().x);
    }

    @Test
    public void testApply_emptyConfig() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(43, 74, 55, 454));   // <- this one gets removed
        sampleSets.add(createSampleSet(44, 75, 56, 455));
        sampleSets.add(createSampleSet(45, 76, 57, 456));

        final Reader primaryReader = mock(Reader.class);
        final Reader secondaryReader = mock(Reader.class);

        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, new Reader[]{secondaryReader}, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(3, sampleSets.size());
    }

        @Test
    public void testCalculateThreshold() {
        double threshold = screening.calculateThreshold(0.63);
        assertEquals(240.07048176677782, threshold, 1e-8);

        threshold = screening.calculateThreshold(1.89);
        assertEquals(240.11893416680937, threshold, 1e-8);

        threshold = screening.calculateThreshold(8.2);
        assertEquals(240.0080236705834, threshold, 1e-8);

        threshold = screening.calculateThreshold(10.72);
        assertEquals(239.8979225974507, threshold, 1e-8);

        threshold = screening.calculateThreshold(35.2);
        assertEquals(238.0978930658674, threshold, 1e-8);

        threshold = screening.calculateThreshold(50.37);
        assertEquals(235.70333750520854, threshold, 1e-8);
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final SampleSet sampleSet = new SampleSet();
        final Sample primary = new Sample(primaryX, primaryY, 2.0987, 3.876, 2014783);
        sampleSet.setPrimary(primary);

        final Sample secondary = new Sample(secondaryX, secondaryY, 3.0987, 5.876, 3014783);
        sampleSet.setSecondary(secondary);

        return sampleSet;
    }
}
