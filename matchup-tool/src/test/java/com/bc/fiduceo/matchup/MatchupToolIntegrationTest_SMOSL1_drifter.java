package com.bc.fiduceo.matchup;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SMOSL1_drifter extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup() throws IOException, SQLException, ParseException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(7200, null)
                .withMaxPixelDistanceKm(2, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-43.xml");

        insert_SIRDS();
        insert_miras_CDF3TD();

        // @todo 1 tb/tb continue here 2022-10-14
        // final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2017-324", "-end", "2017-324"};
        // MatchupToolMain.main(args);
    }

    private void insert_SIRDS() throws IOException, SQLException {
        final String sensorKey = "drifter-sirds";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sirds", "v1.0", "SSTCCI2_refdata_drifter_201711.nc"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1.0");
        storage.insert(satelliteObservation);
    }

    private void insert_miras_CDF3TD() throws IOException, SQLException {
        final String sensorKey = "miras-smos-CDF3TD";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "re07", "2017", "324", "SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.tgz"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("drifter-sirds");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("miras-smos-CDF3TD"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("drifter-sirds", 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("miras-smos-CDF3TD", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd43")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-43").getPath())
                .withDimensions(dimensions);
    }
}
