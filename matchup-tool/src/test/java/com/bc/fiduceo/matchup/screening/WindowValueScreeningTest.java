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
 */

package com.bc.fiduceo.matchup.screening;

import static com.bc.fiduceo.matchup.screening.WindowValueScreening.Evaluate.EntireWindow;
import static com.bc.fiduceo.matchup.screening.WindowValueScreening.Evaluate.IgnoreNoData;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.SimpleNc4ReaderForTestCases;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RunWith(IOTestRunner.class)
public class WindowValueScreeningTest {

    private Reader reader;

    @Before
    public void setUp() throws Exception {
        final Path testDir = TestUtil.createTestDirectory().toPath();
        final Path nc4testfile = testDir.resolve("nc4testfile").toAbsolutePath();
        writeTestFile(nc4testfile);
        reader = new SimpleNc4ReaderForTestCases();
        reader.open(nc4testfile.toFile());
    }

    @After
    public void tearDown() throws Exception {
        if (reader != null) {
            reader.close();
        }
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testApply_emptyInputSet() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();
        final Reader primaryReader = mock(Reader.class);
        final Reader secondaryReader = mock(Reader.class);

        assertEquals(0, matchupSet.getNumObservations());

        final WindowValueScreening screening = new WindowValueScreening(new WindowValueScreening.Configuration());
        screening.apply(matchupSet, primaryReader, secondaryReader, null);

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_onlyPrimaryExpression() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(0, 0, 3, 3));
        sampleSets.add(createSampleSet(5, 5, 3, 3));  // <- this one gets removed

        final Screening.ScreeningContext screeningContext = mock(Screening.ScreeningContext.class);
        when(screeningContext.getPrimaryDimension()).thenReturn(new Dimension("name", 3, 3));

        final WindowValueScreening.Configuration configuration = new WindowValueScreening.Configuration();
        configuration.primaryExpression = "varI <= 27";
        configuration.primaryPercentage = 44d;
        configuration.primaryEvaluate = EntireWindow;

        final WindowValueScreening screening = new WindowValueScreening(configuration);

        screening.apply(matchupSet, reader, null, screeningContext);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());

        assertEquals(0, sampleSets.get(0).getPrimary().x);
    }

    @Test
    public void testApply_onlyPrimaryExpression_onlyValidPixels() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(0, 0, 3, 3));
        sampleSets.add(createSampleSet(5, 5, 3, 3));  // <- this one gets removed

        final Screening.ScreeningContext screeningContext = mock(Screening.ScreeningContext.class);
        when(screeningContext.getPrimaryDimension()).thenReturn(new Dimension("name", 3, 3));

        final WindowValueScreening.Configuration configuration = new WindowValueScreening.Configuration();
        configuration.primaryExpression = "varI <= 27";
        configuration.primaryPercentage = 100d;
        configuration.primaryEvaluate = IgnoreNoData;

        final WindowValueScreening screening = new WindowValueScreening(configuration);

        screening.apply(matchupSet, reader, null, screeningContext);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());

        assertEquals(0, sampleSets.get(0).getPrimary().x);
    }

    @Test
    public void testApply_onlySecondaryExpression() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(3, 3, 2, 2));  // <- this one gets removed
        sampleSets.add(createSampleSet(3, 3, 4, 4));

        final Screening.ScreeningContext screeningContext = mock(Screening.ScreeningContext.class);
        when(screeningContext.getSecondaryDimension()).thenReturn(new Dimension("name", 5, 5));

        final WindowValueScreening.Configuration configuration = new WindowValueScreening.Configuration();
        configuration.secondaryExpression = "varD >= 35";
        configuration.secondaryPercentage = 72d;
        configuration.secondaryEvaluate = EntireWindow;

        final WindowValueScreening screening = new WindowValueScreening(configuration);

        screening.apply(matchupSet, null, reader, screeningContext);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());

        assertEquals(4, sampleSets.get(0).getSecondary().x);
    }

    @Test
    public void testApply_onlySecondaryExpression_onlyValidPixels() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(3, 3, 2, 2));  // <- this one gets removed
        sampleSets.add(createSampleSet(3, 3, 4, 4));

        final Screening.ScreeningContext screeningContext = mock(Screening.ScreeningContext.class);
        when(screeningContext.getSecondaryDimension()).thenReturn(new Dimension("name", 5, 5));

        final WindowValueScreening.Configuration configuration = new WindowValueScreening.Configuration();
        configuration.secondaryExpression = "varD >= 35";
        configuration.secondaryPercentage = 75d;
        configuration.secondaryEvaluate = IgnoreNoData;

        final WindowValueScreening screening = new WindowValueScreening(configuration);

        screening.apply(matchupSet, null, reader, screeningContext);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());

        assertEquals(4, sampleSets.get(0).getSecondary().x);
    }

    @Test
    public void testApply_bothExpression() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(3, 3, 2, 2));  // <- this one gets removed
        sampleSets.add(createSampleSet(3, 3, 1, 1));
        sampleSets.add(createSampleSet(2, 2, 1, 1));  // <- this one gets removed

        final Screening.ScreeningContext screeningContext = mock(Screening.ScreeningContext.class);
        when(screeningContext.getPrimaryDimension()).thenReturn(new Dimension("name", 3, 3));
        when(screeningContext.getSecondaryDimension()).thenReturn(new Dimension("name", 3, 3));

        final WindowValueScreening.Configuration configuration = new WindowValueScreening.Configuration();
        configuration.primaryExpression = "varI >= 27";
        configuration.primaryPercentage = 88d;
        configuration.primaryEvaluate = EntireWindow;
        configuration.secondaryExpression = "varD <= 27.0";
        configuration.secondaryPercentage = 100d;
        configuration.secondaryEvaluate = EntireWindow;

        final WindowValueScreening screening = new WindowValueScreening(configuration);

        screening.apply(matchupSet, reader, reader, screeningContext);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());

        assertEquals(3, sampleSets.get(0).getPrimary().x);
        assertEquals(1, sampleSets.get(0).getSecondary().x);
    }

    @Test
    public void testApply_bothExpression_onlyValidPixels() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(3, 3, 2, 2));  // <- this one gets removed
        sampleSets.add(createSampleSet(3, 3, 1, 1));
        sampleSets.add(createSampleSet(2, 2, 1, 1));  // <- this one gets removed

        final Screening.ScreeningContext screeningContext = mock(Screening.ScreeningContext.class);
        when(screeningContext.getPrimaryDimension()).thenReturn(new Dimension("name", 3, 3));
        when(screeningContext.getSecondaryDimension()).thenReturn(new Dimension("name", 3, 3));

        final WindowValueScreening.Configuration configuration = new WindowValueScreening.Configuration();
        configuration.primaryExpression = "varI >= 27";
        configuration.primaryPercentage = 88d;
        configuration.primaryEvaluate = IgnoreNoData;
        configuration.secondaryExpression = "varD <= 27.0";
        configuration.secondaryPercentage = 100d;
        configuration.secondaryEvaluate = IgnoreNoData;

        final WindowValueScreening screening = new WindowValueScreening(configuration);

        screening.apply(matchupSet, reader, reader, screeningContext);

        sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.size());

        assertEquals(3, sampleSets.get(0).getPrimary().x);
        assertEquals(1, sampleSets.get(0).getSecondary().x);
    }

    void writeTestFile(Path nc4testfile) throws IOException, InvalidRangeException {
        final NetcdfFileWriter writer = NetcdfFileWriter.createNew(
                    NetcdfFileWriter.Version.netcdf4,
                    nc4testfile.toString()
        );
        writer.addDimension(null, "dim", 7);
        final Variable varI = writer.addVariable(null, "varI", DataType.INT, "dim dim");
        final Variable varD = writer.addVariable(null, "varD", DataType.DOUBLE, "dim dim");
        writer.create();
        final double XX = Float.NaN;
        final double[][] values = {
                    {11, 12, 13, 14, 15, 16, 17},
                    {18, 19, 20, 21, 22, 23, 24},
                    {25, 26, 27, 28, 29, 30, 31},
                    {32, 33, 34, 35, 36, 37, 38},
                    {39, 40, 41, 42, XX, 44, 45},
                    {46, 47, 48, 49, 50, 51, 52},
                    {53, 54, 55, 56, 57, 58, 59}
        };
        try {
            final Array data = Array.factory(values);
            writer.write(varI, MAMath.convert(data, DataType.INT));
            writer.write(varD, data);
        } finally {
            writer.close();
        }
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final SampleSet sampleSet = new SampleSet();
        final Sample primary = new Sample(primaryX, primaryY, 4.0987, 5.876, 6014783);
        sampleSet.setPrimary(primary);

        final Sample secondary = new Sample(secondaryX, secondaryY, 7.0987, 8.876, 9014783);
        sampleSet.setSecondary(secondary);

        return sampleSet;
    }
}
