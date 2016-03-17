package com.bc.fiduceo.matchup.screening;

import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DistanceScreeningTest {

    private MatchupCollection matchupCollection;

    @Test
    public void testExecute_emptyMatchupCollection() {
        final MatchupCollection matchupCollection = new MatchupCollection();
        final DistanceScreening distanceScreening = new DistanceScreening(2);
        MatchupCollection matchupCollectionExecute = distanceScreening.execute(matchupCollection);
        assertNotNull(matchupCollectionExecute);
        assertEquals(0, matchupCollectionExecute.getNumMatchups());
    }

    @Test
    public void testExecute_Distance3KM() throws Exception {

        DistanceScreening distanceScreening = new DistanceScreening(3);
        MatchupCollection matchupCollectionExecute = distanceScreening.execute(matchupCollection);

        assertNotNull(matchupCollectionExecute);
        List<MatchupSet> sets = matchupCollectionExecute.getSets();
        assertEquals(4, matchupCollectionExecute.getNumMatchups());

        MatchupSet matchupSetExecute = sets.get(0);
        List<SampleSet> sampleSetsExecute = matchupSetExecute.getSampleSets();

        assertEquals(-97.24099731445312, getPrimaryLon(sampleSetsExecute, 0), 1e-8);
        assertEquals(19.50200080871582, getPrimaryLat(sampleSetsExecute, 0), 1e-8);
        assertEquals(-97.22698974609375, getSecondaryLon(sampleSetsExecute, 0), 1e-8);
        assertEquals(19.506000518798828, getSecondaryLat(sampleSetsExecute, 0), 1e-8);

        assertEquals(-97.2449951171875, getPrimaryLon(sampleSetsExecute, 1), 1e-8);
        assertEquals(19.5310001373291, getPrimaryLat(sampleSetsExecute, 1), 1e-8);
        assertEquals(-97.25799560546875, getSecondaryLon(sampleSetsExecute, 1), 1e-8);
        assertEquals(19.54199981689453, getSecondaryLat(sampleSetsExecute, 1), 1e-8);


        assertEquals(-97.44100952148438, getPrimaryLon(sampleSetsExecute, 2), 1e-8);
        assertEquals(19.514999389648438, getPrimaryLat(sampleSetsExecute, 2), 1e-8);
        assertEquals(-97.42498779296875, getSecondaryLon(sampleSetsExecute, 2), 1e-8);
        assertEquals(19.507999420166016, getSecondaryLat(sampleSetsExecute, 2), 1e-8);

        assertEquals(-97.44100952148438, getPrimaryLon(sampleSetsExecute, 3), 1e-8);
        assertEquals(19.514999389648438, getPrimaryLat(sampleSetsExecute, 3), 1e-8);
        assertEquals(-97.42498779296875, getSecondaryLon(sampleSetsExecute, 3), 1e-8);
        assertEquals(19.507999420166016, getSecondaryLat(sampleSetsExecute, 3), 1e-8);
    }

    @Test
    public void testDistance1KM() throws Exception {

        DistanceScreening distanceScreening = new DistanceScreening(1);
        MatchupCollection matchupCollectionExecute = distanceScreening.execute(matchupCollection);
        assertNotNull(matchupCollectionExecute);
        assertEquals(0, matchupCollectionExecute.getNumMatchups());
    }

    @Before
    public void setUp() {
        List<SampleSet> sampleSets = new ArrayList<>();
        sampleSets.add(createSampleSet(-97.24099731445312, 19.50200080871582, -97.22698974609375, 19.506000518798828));
        sampleSets.add(createSampleSet(-97.2449951171875, 19.5310001373291, -97.25799560546875, 19.54199981689453));
        sampleSets.add(createSampleSet(-97.44100952148438, 19.514999389648438, -97.42498779296875, 19.507999420166016));
        sampleSets.add(createSampleSet(-97.44100952148438, 19.514999389648438, -97.42498779296875, 19.507999420166016));

        sampleSets.add(createSampleSet(-108.82499694824219, 31.836999893188477, -108.89300537109375, 31.840999603271484));
        sampleSets.add(createSampleSet(-108.93800354003906, 31.825000762939453, -108.89599609375, 31.812000274658203));
        sampleSets.add(createSampleSet(-108.9949951171875, 31.81800079345703, -109.08999633789062, 31.820999145507812));
        sampleSets.add(createSampleSet(-109.05099487304688, 31.812000274658203, -109.08999633789062, 31.820999145507812));


        MatchupSet matchupSet = new MatchupSet();
        matchupSet.setSampleSets(sampleSets);

        matchupCollection = new MatchupCollection();
        matchupCollection.add(matchupSet);
    }

    private double getPrimaryLon(List<SampleSet> sampleSetsExecute, int index) {
        return sampleSetsExecute.get(index).getPrimary().lon;
    }

    private double getPrimaryLat(List<SampleSet> sampleSetsExecute, int index) {
        return sampleSetsExecute.get(index).getPrimary().lat;
    }

    private double getSecondaryLon(List<SampleSet> sampleSetsExecute, int index) {
        return sampleSetsExecute.get(index).getSecondary().lon;
    }

    private double getSecondaryLat(List<SampleSet> sampleSetsExecute, int index) {
        return sampleSetsExecute.get(index).getSecondary().lat;
    }

    private SampleSet createSampleSet(double primaryLon, double primaryLat, double secondaryLon, double secondaryLat) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(0, 0, primaryLon, primaryLat, 0));
        sampleSet.setSecondary(new Sample(0, 0, secondaryLon, secondaryLat, 0));
        return sampleSet;
    }
}