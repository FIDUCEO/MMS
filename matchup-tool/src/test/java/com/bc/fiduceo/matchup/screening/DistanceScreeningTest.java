package com.bc.fiduceo.matchup.screening;

import static org.junit.Assert.*;

import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.esa.snap.core.util.math.RsMathUtils;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

public class DistanceScreeningTest {

    public static final double MEAN_EARTH_RADIUS_KM = RsMathUtils.MEAN_EARTH_RADIUS / 1000;
    public static final double MEAN_EARTH_CIRCUMFERENCE_KM = MEAN_EARTH_RADIUS_KM * 2 * Math.PI;
    public static final double KM_DISTANCE_FOR_1_DEGREE = MEAN_EARTH_CIRCUMFERENCE_KM / 360;

    public static final Sample SAMPLE_AT_LON_LAT_00 = new Sample(0, 0, 0, 0, 0);
    public static final Sample SAMPLE_1_DEGREE_AWAY_FROM_LON_LAT_00 = new Sample(0, 0, 1, 0, 0);
    public static final Sample SAMPLE_MORE_THAN_1_DEGREE_AWAY_FROM_LON_LAT_00 = new Sample(0, 0, 1.5, 0, 0);

    @Test
    public void testScreenOutSampleSetsWhereTheDistanceIsGreaterThanMaxDelta_copyrightBySabine() throws Exception {
        final double maxDeltaInKm = KM_DISTANCE_FOR_1_DEGREE + 1;
        final SampleSet validDistanceSampleSet = createValidDistanceSampleSet();
        final SampleSet toBigDistanceSampleSet = createToBigDistanceSampleSet();
        final MatchupCollection collectionToScreen = createCollection(validDistanceSampleSet, toBigDistanceSampleSet);

        final DistanceScreening distanceScreening = new DistanceScreening(maxDeltaInKm);
        final MatchupCollection screenedCollection = distanceScreening.screen(collectionToScreen);

        final List<SampleSet> sampleSets = screenedCollection.getSets().get(0).getSampleSets();
        assertEquals(1, sampleSets.size());
        assertSame(validDistanceSampleSet, sampleSets.get(0));
    }

    public SampleSet createValidDistanceSampleSet() throws Exception {
        final SampleSet validDistanceSampleSet = new SampleSet();
        validDistanceSampleSet.setPrimary(SAMPLE_AT_LON_LAT_00);
        validDistanceSampleSet.setSecondary(SAMPLE_1_DEGREE_AWAY_FROM_LON_LAT_00);
        return validDistanceSampleSet;
    }

    public SampleSet createToBigDistanceSampleSet() throws Exception {
        final SampleSet validDistanceSampleSet = new SampleSet();
        validDistanceSampleSet.setPrimary(SAMPLE_AT_LON_LAT_00);
        validDistanceSampleSet.setSecondary(SAMPLE_MORE_THAN_1_DEGREE_AWAY_FROM_LON_LAT_00);
        return validDistanceSampleSet;
    }

    private MatchupCollection createCollection(SampleSet validDistanceSampleSet, SampleSet toBigDistanceSampleSet) {
        final MatchupCollection collection = new MatchupCollection();
        final MatchupSet matchupSet = new MatchupSet();
        final ArrayList<SampleSet> sampleSets = new ArrayList<>();
        sampleSets.add(validDistanceSampleSet);
        sampleSets.add(toBigDistanceSampleSet);
        matchupSet.setSampleSets(sampleSets);
        collection.add(matchupSet);
        return collection;
    }
}