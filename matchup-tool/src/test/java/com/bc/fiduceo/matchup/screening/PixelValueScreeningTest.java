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
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PixelValueScreeningTest {

    private PixelValueScreening screening;
    private String secondarySensorName;
    private HashMap<String, Reader> secondaryReaderMap;


    @Before
    @Test
    public void setUp() {
        screening = new PixelValueScreening();
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
    public void testApply_onlyPrimaryExpression() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(33, 274, 45, 654));
        sampleSets.add(createSampleSet(34, 275, 46, 655));
        sampleSets.add(createSampleSet(35, 276, 47, 656));  // <- this one gets removed

        final Array regularScanArray = mock(ucar.ma2.Array.class);
        when(regularScanArray.getInt(0)).thenReturn(0);

        final Array calibrationScanArray = mock(ucar.ma2.Array.class);
        when(calibrationScanArray.getInt(0)).thenReturn(3);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(33), eq(274), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(primaryReader.readScaled(eq(34), eq(275), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(primaryReader.readScaled(eq(35), eq(276), any(), eq("scanline_type"))).thenReturn(calibrationScanArray);

        final List<Variable> variables = createVariablesList();
        when(primaryReader.getVariables()).thenReturn(variables);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(45), eq(654), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(secondaryReader.readScaled(eq(46), eq(655), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(secondaryReader.readScaled(eq(47), eq(656), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(secondaryReader.getVariables()).thenReturn(variables);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);


        final PixelValueScreening.Configuration configuration = new PixelValueScreening.Configuration();
        configuration.primaryExpression = "scanline_type == 0";
        //configuration.secondaryExpression = "scanline_type == 0";

        screening.configure(configuration);

        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());

        assertEquals(33, sampleSets.get(0).getPrimary().getX());
        assertEquals(34, sampleSets.get(1).getPrimary().getX());
    }

    @Test
    public void testApply_onlySecondaryExpression() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(34, 275, 46, 655));  // <- this one gets removed
        sampleSets.add(createSampleSet(35, 276, 47, 656));
        sampleSets.add(createSampleSet(36, 277, 48, 657));

        final Array regularScanArray = mock(ucar.ma2.Array.class);
        when(regularScanArray.getInt(0)).thenReturn(0);

        final Array calibrationScanArray = mock(ucar.ma2.Array.class);
        when(calibrationScanArray.getInt(0)).thenReturn(3);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(34), eq(275), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(primaryReader.readScaled(eq(35), eq(276), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(primaryReader.readScaled(eq(36), eq(277), any(), eq("scanline_type"))).thenReturn(regularScanArray);

        final List<Variable> variables = createVariablesList();
        when(primaryReader.getVariables()).thenReturn(variables);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(46), eq(655), any(), eq("scanline_type"))).thenReturn(calibrationScanArray);
        when(secondaryReader.readScaled(eq(47), eq(656), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(secondaryReader.readScaled(eq(48), eq(657), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(secondaryReader.getVariables()).thenReturn(variables);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        final PixelValueScreening.Configuration configuration = new PixelValueScreening.Configuration();
//        configuration.primaryExpression = "scanline_type == 0";
        configuration.secondaryExpression = "scanline_type == 0";

        screening.configure(configuration);

        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(2, sampleSets.size());

        assertEquals(35, sampleSets.get(0).getPrimary().getX());
        assertEquals(36, sampleSets.get(1).getPrimary().getX());
    }

    @Test
    public void testApply_bothExpression() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(35, 276, 47, 656));  // <- this one gets removed
        sampleSets.add(createSampleSet(36, 277, 48, 657));
        sampleSets.add(createSampleSet(37, 278, 49, 658));  // <- this one gets removed

        final Array regularScanArray = mock(ucar.ma2.Array.class);
        when(regularScanArray.getInt(0)).thenReturn(0);

        final Array calibrationScanArray = mock(ucar.ma2.Array.class);
        when(calibrationScanArray.getInt(0)).thenReturn(3);

        final Reader primaryReader = mock(Reader.class);
        when(primaryReader.readScaled(eq(35), eq(276), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(primaryReader.readScaled(eq(36), eq(277), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(primaryReader.readScaled(eq(37), eq(278), any(), eq("scanline_type"))).thenReturn(calibrationScanArray);

        final List<Variable> variables = createVariablesList();
        when(primaryReader.getVariables()).thenReturn(variables);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(47), eq(656), any(), eq("scanline_type"))).thenReturn(calibrationScanArray);
        when(secondaryReader.readScaled(eq(48), eq(657), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(secondaryReader.readScaled(eq(49), eq(658), any(), eq("scanline_type"))).thenReturn(regularScanArray);
        when(secondaryReader.getVariables()).thenReturn(variables);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        final PixelValueScreening.Configuration configuration = new PixelValueScreening.Configuration();
        configuration.primaryExpression = "scanline_type == 0";
        configuration.secondaryExpression = "scanline_type == 0";

        screening.configure(configuration);

        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());

        assertEquals(36, sampleSets.get(0).getPrimary().getX());
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final SampleSet sampleSet = new SampleSet();
        final Sample primary = new Sample(primaryX, primaryY, 4.0987, 5.876, 6014783);
        sampleSet.setPrimary(primary);

        final Sample secondary = new Sample(secondaryX, secondaryY, 7.0987, 8.876, 9014783);
        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), secondary);

        return sampleSet;
    }

    private List<Variable> createVariablesList() {
        final List<Variable> variables = new ArrayList<>();
        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn("scanline_type");
        when(variable.getShortName()).thenReturn("scanline_type");
        when(variable.getDataType()).thenReturn(DataType.INT);
        variables.add(variable);
        return variables;
    }

}
