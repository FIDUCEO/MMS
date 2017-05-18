package com.bc.fiduceo.matchup.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.*;

import java.nio.file.Paths;
import java.util.List;

/**
 * Created by Sabine on 17.05.2017.
 */
public class SampleReceiverPermutatorTest {

    private final int u = 1; // u means unimportant
    private final Sample s0 =new Sample(u, u, u, u, 0);;
    private final Sample s1 =new Sample(u, u, u, u, 1);;
    private final Sample s2 =new Sample(u, u, u, u, 2);;
    private final Sample s3 =new Sample(u, u, u, u, 3);;
    private final Sample s4 =new Sample(u, u, u, u, 4);;
    private final Sample s5 =new Sample(u, u, u, u, 5);;
    private final Sample s6 =new Sample(u, u, u, u, 6);;
    private final Sample s7 =new Sample(u, u, u, u, 7);;
    private final Sample s8 =new Sample(u, u, u, u, 8);;
    private final Sample s9 =new Sample(u, u, u, u, 9);;

    private Sensor primSen;
    private Sensor secSen1;
    private Sensor secSen2;

    private SatelliteObservation primaryObs1;
    private SatelliteObservation primaryObs2;
    private SatelliteObservation primaryObs3;
    private SatelliteObservation secSen1Obs1;
    private SatelliteObservation secSen1Obs2;
    private SatelliteObservation secSen2Obs1;
    private SatelliteObservation secSen2Obs2;

    private String senNameSec1;
    private String senNameSec2;

    @Before
    public void setUp() throws Exception {
        initObservationsAndSamples();
    }

    @Test
    public void testIfTheUserDontAddSecondarySamplesThereWillBeNoMatchups() throws Exception {
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s0);
        receiver.setPrimarySample(s1);

        // If the user dont add secondary Samples there will be no matchups
        // receiver.addSecondarySample(secSen1Obs1, s1);

        final MatchupCollection matchups = receiver.getPermutations();
        assertThat(matchups, is(notNullValue()));
        assertThat(matchups.getNumMatchups(), is(0));
    }

    @Test
    public void test1SecondarySensors_1SecondarySample_1Matchup() throws Exception {
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s0);
        receiver.addSecondarySample(secSen1Obs1, s1);

        final MatchupCollection matchupCollection = receiver.getPermutations();
        assertThat(matchupCollection, is(notNullValue()));
        final List<MatchupSet> matchupSets = matchupCollection.getSets();
        assertThat(matchupSets.size(), is(1));

        final MatchupSet matchupSet = matchupSets.get(0);
        assertThat(matchupSet, is(notNullValue()));
        assertThat(matchupSet.getPrimaryObservationPath(), is(Paths.get("pp1")));
        assertThat(matchupSet.getPrimaryProcessingVersion(), is("pp1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec1), is(Paths.get("s1p1")));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec1), is("s1p1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec2), is(nullValue()));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec2), is(nullValue()));

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertThat(sampleSets, is(notNullValue()));
        assertThat(sampleSets.size(), is(1));

        final SampleSet sampleSet = sampleSets.get(0);
        assertThat(sampleSet, is(notNullValue()));
        assertThat(sampleSet.getPrimary(), is(sameInstance(s0)));
        assertThat(sampleSet.getSecondary(senNameSec1), is(sameInstance(s1)));
        assertThat(sampleSet.getSecondary(senNameSec2), is(nullValue()));
    }

    @Test
    public void test1SecondarySensors_3SecondarySamples_3Matchups_primaryTimeOrdered() throws Exception {
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s3);
        receiver.addSecondarySample(secSen1Obs1, s0);
        receiver.setPrimarySample(s2);
        receiver.addSecondarySample(secSen1Obs1, s0);
        receiver.setPrimarySample(s1);
        receiver.addSecondarySample(secSen1Obs1, s0);

        final MatchupCollection matchupCollection = receiver.getPermutations();
        assertThat(matchupCollection, is(notNullValue()));
        final List<MatchupSet> matchupSets = matchupCollection.getSets();
        assertThat(matchupSets.size(), is(1));

        final MatchupSet matchupSet = matchupSets.get(0);
        assertThat(matchupSet, is(notNullValue()));
        assertThat(matchupSet.getPrimaryObservationPath(), is(Paths.get("pp1")));
        assertThat(matchupSet.getPrimaryProcessingVersion(), is("pp1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec1), is(Paths.get("s1p1")));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec1), is("s1p1v"));

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertThat(sampleSets, is(notNullValue()));
        assertThat(sampleSets.size(), is(3));

        final Sample[] expected = new Sample[]{s1, s2, s3};
        for (int i = 0; i < sampleSets.size(); i++) {
            SampleSet sampleSet = sampleSets.get(i);
            final String reason = "[" + i + "]";
            assertThat(reason, sampleSet.getPrimary(), is(sameInstance(expected[i])));
            assertThat(reason, sampleSet.getSecondary(senNameSec1), is(sameInstance(s0)));
        }
    }

    @Test
    public void test2SecondarySensors_PerSensor1SecondarySample_1Matchup() throws Exception {
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1, senNameSec2);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s0);
        receiver.addSecondarySample(secSen1Obs1, s1);
        receiver.addSecondarySample(secSen2Obs1, s2);

        final MatchupCollection matchupCollection = receiver.getPermutations();
        assertThat(matchupCollection, is(notNullValue()));
        assertThat(matchupCollection.getNumMatchups(), is(1));

        final List<MatchupSet> matchupSets = matchupCollection.getSets();
        assertThat(matchupSets.size(), is(1));

        final MatchupSet matchupSet = matchupSets.get(0);
        assertThat(matchupSet, is(notNullValue()));
        assertThat(matchupSet.getPrimaryObservationPath(), is(Paths.get("pp1")));
        assertThat(matchupSet.getPrimaryProcessingVersion(), is("pp1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec1), is(Paths.get("s1p1")));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec1), is("s1p1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec2), is(Paths.get("s2p1")));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec2), is("s2p1v"));

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertThat(sampleSets, is(notNullValue()));
        assertThat(sampleSets.size(), is(1));

        final SampleSet sampleSet = sampleSets.get(0);
        assertThat(sampleSet, is(notNullValue()));
        assertThat(sampleSet.getPrimary(), is(sameInstance(s0)));
        assertThat(sampleSet.getSecondary(senNameSec1), is(sameInstance(s1)));
        assertThat(sampleSet.getSecondary(senNameSec2), is(sameInstance(s2)));
    }

    @Test
    public void test2SecondarySensors_firstSecondary2Samples_secondSecondary1Sample_2Matchup() throws Exception {
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1, senNameSec2);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s0);
        receiver.addSecondarySample(secSen1Obs1, s1);
        receiver.addSecondarySample(secSen1Obs1, s2);
        receiver.addSecondarySample(secSen2Obs1, s3);

        final MatchupCollection matchupCollection = receiver.getPermutations();
        assertThat(matchupCollection, is(notNullValue()));
        assertThat(matchupCollection.getNumMatchups(), is(2));

        final List<MatchupSet> matchupSets = matchupCollection.getSets();
        assertThat(matchupSets.size(), is(1));

        final MatchupSet matchupSet = matchupSets.get(0);
        assertThat(matchupSet, is(notNullValue()));
        assertThat(matchupSet.getPrimaryObservationPath(), is(Paths.get("pp1")));
        assertThat(matchupSet.getPrimaryProcessingVersion(), is("pp1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec1), is(Paths.get("s1p1")));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec1), is("s1p1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec2), is(Paths.get("s2p1")));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec2), is("s2p1v"));

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertThat(sampleSets, is(notNullValue()));
        assertThat(sampleSets.size(), is(2));
        Sample[][] expected = new Sample[][]{
                    new Sample[]{s1, s3},
                    new Sample[]{s2, s3},
                    };

        for (int i = 0; i < sampleSets.size(); i++) {
            SampleSet sampleSet = sampleSets.get(i);
            final String reason = "[" + i + "]";
            assertThat(reason, sampleSet, is(notNullValue()));
            assertThat(reason, sampleSet.getPrimary(), is(sameInstance(s0)));
            assertThat(reason, sampleSet.getSecondary(senNameSec1), is(sameInstance(expected[i][0])));
            assertThat(reason, sampleSet.getSecondary(senNameSec2), is(sameInstance(expected[i][1])));
        }
    }

    @Test
    public void noMatchupsIfOneOfTheSecondariesIsEmpty() throws Exception {
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1, senNameSec2);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s1);
        receiver.addSecondarySample(secSen1Obs1, s3);
        receiver.addSecondarySample(secSen2Obs1, null);
        receiver.setPrimarySample(s2);
        receiver.addSecondarySample(secSen1Obs1, null);
        receiver.addSecondarySample(secSen2Obs1, s3);

        final MatchupCollection matchupCollection = receiver.getPermutations();
        assertThat(matchupCollection, is(notNullValue()));
        assertThat(matchupCollection.getNumMatchups(), is(0));
    }

    @Test
    public void test2SecondarySensors_firstSecondary1Samples_secondSecondary2Sample_2Matchup() throws Exception {
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1, senNameSec2);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s0);
        receiver.addSecondarySample(secSen1Obs1, s1);
        receiver.addSecondarySample(secSen2Obs1, s3);
        receiver.addSecondarySample(secSen2Obs1, s4);

        final MatchupCollection matchupCollection = receiver.getPermutations();
        assertThat(matchupCollection, is(notNullValue()));
        assertThat(matchupCollection.getNumMatchups(), is(2));

        final List<MatchupSet> matchupSets = matchupCollection.getSets();
        assertThat(matchupSets.size(), is(1));

        final MatchupSet matchupSet = matchupSets.get(0);
        assertThat(matchupSet, is(notNullValue()));
        assertThat(matchupSet.getPrimaryObservationPath(), is(Paths.get("pp1")));
        assertThat(matchupSet.getPrimaryProcessingVersion(), is("pp1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec1), is(Paths.get("s1p1")));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec1), is("s1p1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec2), is(Paths.get("s2p1")));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec2), is("s2p1v"));

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertThat(sampleSets, is(notNullValue()));
        assertThat(sampleSets.size(), is(2));
        Sample[][] expected = new Sample[][]{
                    new Sample[]{s1, s3},
                    new Sample[]{s1, s4},
                    };

        for (int i = 0; i < sampleSets.size(); i++) {
            SampleSet sampleSet = sampleSets.get(i);
            final String reason = "At pos i = " + i ;
            assertThat(reason, sampleSet, is(notNullValue()));
            assertThat(reason, sampleSet.getPrimary(), is(sameInstance(s0)));
            assertThat(reason, sampleSet.getSecondary(senNameSec1), is(sameInstance(expected[i][0])));
            assertThat(reason, sampleSet.getSecondary(senNameSec2), is(sameInstance(expected[i][1])));
        }
    }

    @Test
    public void _2SecondarySensors_firstSecondary2Samples_secondSecondary2Sample_4Matchup() throws Exception {
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1, senNameSec2);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s0);
        receiver.addSecondarySample(secSen1Obs1, s1);
        receiver.addSecondarySample(secSen1Obs1, s2);
        receiver.addSecondarySample(secSen2Obs1, s3);
        receiver.addSecondarySample(secSen2Obs1, s4);

        final MatchupCollection matchupCollection = receiver.getPermutations();
        assertThat(matchupCollection, is(notNullValue()));
        assertThat(matchupCollection.getNumMatchups(), is(4));

        final List<MatchupSet> matchupSets = matchupCollection.getSets();
        assertThat(matchupSets.size(), is(1));

        final MatchupSet matchupSet = matchupSets.get(0);
        assertThat(matchupSet, is(notNullValue()));
        assertThat(matchupSet.getPrimaryObservationPath(), is(Paths.get("pp1")));
        assertThat(matchupSet.getPrimaryProcessingVersion(), is("pp1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec1), is(Paths.get("s1p1")));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec1), is("s1p1v"));
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec2), is(Paths.get("s2p1")));
        assertThat(matchupSet.getSecondaryProcessingVersion(senNameSec2), is("s2p1v"));

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertThat(sampleSets, is(notNullValue()));
        assertThat(sampleSets.size(), is(4));
        Sample[][] expected = new Sample[][]{
                    new Sample[]{s1, s3},
                    new Sample[]{s1, s4},
                    new Sample[]{s2, s3},
                    new Sample[]{s2, s4},
                    };

        for (int i = 0; i < sampleSets.size(); i++) {
            SampleSet sampleSet = sampleSets.get(i);
            final String reason = "At pos i = " + i ;
            assertThat(reason, sampleSet, is(notNullValue()));
            assertThat(reason, sampleSet.getPrimary(), is(sameInstance(s0)));
            assertThat(reason, sampleSet.getSecondary(senNameSec1), is(sameInstance(expected[i][0])));
            assertThat(reason, sampleSet.getSecondary(senNameSec2), is(sameInstance(expected[i][1])));
        }
    }

    @Test
    public void test2SecondarySensors_firstSecondary2Samples_secondSecondary0Sample_0Matchup() throws Exception {
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1, senNameSec2);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s0);
        receiver.addSecondarySample(secSen1Obs1, s1);
        receiver.addSecondarySample(secSen1Obs1, s2);
        // No secondary sample
        // receiver.addSecondarySample(secSen2Obs1, s3);

        final MatchupCollection matchupCollection = receiver.getPermutations();
        assertThat(matchupCollection, is(notNullValue()));
        assertThat(matchupCollection.getNumMatchups(), is(0));
    }

    @Test
    public void testThatMatchupsetIsCreatedAfterPrimatrySensorChange_ToKeppTheMapsAsSmallAsPossible() throws Exception {
        //execution
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s0);
        receiver.addSecondarySample(secSen1Obs1, s1);
        receiver.setCurrentPrimary(primaryObs2);

        //verification
        assertThat(receiver.getMatchupCollection(), is(notNullValue()));
        assertThat(receiver.getMatchupCollection().getSets().size(), is(1));

        //execution
        receiver.setPrimarySample(s1);
        receiver.addSecondarySample(secSen1Obs1, s2);
        receiver.setCurrentPrimary(primaryObs3);

        //verification
        assertThat(receiver.getMatchupCollection(), is(notNullValue()));
        assertThat(receiver.getMatchupCollection().getSets().size(), is(2));

        //execution
        receiver.setPrimarySample(s2);
        receiver.addSecondarySample(secSen1Obs2, s3);
        final MatchupCollection matchupCollection = receiver.getPermutations();

        //verification
        assertThat(matchupCollection, is(notNullValue()));
        final List<MatchupSet> matchupSets = matchupCollection.getSets();
        assertThat(matchupSets.size(), is(3));

        MatchupSet matchupSet;
        List<SampleSet> sampleSets;

        matchupSet = matchupSets.get(0);
        sampleSets = matchupSet.getSampleSets();
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec1), is(Paths.get("s1p1")));
        assertThat(sampleSets.size(), is(1));
        assertThat(sampleSets.get(0), is(notNullValue()));
        assertThat(sampleSets.get(0).getPrimary(), is(sameInstance(s0)));
        assertThat(sampleSets.get(0).getSecondary(senNameSec1), is(sameInstance(s1)));
        assertThat(sampleSets.get(0).getSecondary(senNameSec2), is(nullValue()));


        matchupSet = matchupSets.get(1);
        sampleSets = matchupSet.getSampleSets();
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec1), is(Paths.get("s1p1")));
        assertThat(sampleSets.size(), is(1));
        assertThat(sampleSets.get(0), is(notNullValue()));
        assertThat(sampleSets.get(0).getPrimary(), is(sameInstance(s1)));
        assertThat(sampleSets.get(0).getSecondary(senNameSec1), is(sameInstance(s2)));
        assertThat(sampleSets.get(0).getSecondary(senNameSec2), is(nullValue()));


        matchupSet = matchupSets.get(2);
        sampleSets = matchupSet.getSampleSets();
        assertThat(matchupSet.getSecondaryObservationPath(senNameSec1), is(Paths.get("s1p2")));
        assertThat(sampleSets.size(), is(1));
        assertThat(sampleSets.get(0), is(notNullValue()));
        assertThat(sampleSets.get(0).getPrimary(), is(sameInstance(s2)));
        assertThat(sampleSets.get(0).getSecondary(senNameSec1), is(sameInstance(s3)));
        assertThat(sampleSets.get(0).getSecondary(senNameSec2), is(nullValue()));
    }

    @Test
    public void ensureValidSecondarySensorType() throws Exception {
        //preparation
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator("validSecName");
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s0);
        try {
            receiver.addSecondarySample(secSen1Obs1, s2);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), is(equalTo("Illegal secondary sensor type.")));
        } catch (Exception e) {
            fail("RuntimeException expected");
        }
    }

    @Test
    public void ifAnEqualPrimarySampleIsSet_theFirstInstanceWillBeUsed() throws Exception {
        final SampleReceiverPermutator receiver = new SampleReceiverPermutator(senNameSec1);
        receiver.setCurrentPrimary(primaryObs1);
        receiver.setPrimarySample(s0);
        receiver.addSecondarySample(secSen1Obs1, s1);
        receiver.addSecondarySample(secSen1Obs1, s3);
        receiver.setPrimarySample(clone(s0));
        receiver.addSecondarySample(secSen1Obs1, s2);
        receiver.addSecondarySample(secSen1Obs1, s4);

        final MatchupCollection collection = receiver.getPermutations();
        assertThat(collection.getNumMatchups(), is(4));
        final List<SampleSet> sampleSets = collection.getSets().get(0).getSampleSets();
        for (SampleSet sampleSet : sampleSets) {
            assertThat(sampleSet.getPrimary(), is(sameInstance(s0)));
        }
    }

    private Sample clone(Sample s) {
        return new Sample(s.x, s.y, s.lon, s.lat, s.time);
    }

    private void initObservationsAndSamples() {
        senNameSec1 = "secondary1";
        senNameSec2 = "secondary2";

        primSen = new Sensor("primary");
        secSen1 = new Sensor(senNameSec1);
        secSen2 = new Sensor(senNameSec2);

        primaryObs1 = new SatelliteObservation();
        primaryObs1.setSensor(primSen);
        primaryObs1.setDataFilePath("pp1");
        primaryObs1.setVersion("pp1v");

        primaryObs2 = new SatelliteObservation();
        primaryObs2.setSensor(primSen);
        primaryObs2.setDataFilePath("pp2");
        primaryObs2.setVersion("pp2v");

        primaryObs3 = new SatelliteObservation();
        primaryObs3.setSensor(primSen);
        primaryObs3.setDataFilePath("pp3");
        primaryObs3.setVersion("pp3v");

        secSen1Obs1 = new SatelliteObservation();
        secSen1Obs1.setSensor(secSen1);
        secSen1Obs1.setDataFilePath("s1p1");
        secSen1Obs1.setVersion("s1p1v");

        secSen1Obs2 = new SatelliteObservation();
        secSen1Obs2.setSensor(secSen1);
        secSen1Obs2.setDataFilePath("s1p2");
        secSen1Obs2.setVersion("s1p2v");

        secSen2Obs1 = new SatelliteObservation();
        secSen2Obs1.setSensor(secSen2);
        secSen2Obs1.setDataFilePath("s2p1");
        secSen2Obs1.setVersion("s2p1v");

        secSen2Obs2 = new SatelliteObservation();
        secSen2Obs2.setSensor(secSen2);
        secSen2Obs2.setDataFilePath("s2p2");
        secSen2Obs2.setVersion("s2p2v");
    }
}