package com.bc.fiduceo.matchup;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.util.NetCDFUtils;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_border_overlap_bug extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchupWithBorderOverlap() throws IOException, SQLException, ParseException, InvalidRangeException {
        insert_AATSR();
        insert_ARGO_SST();

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(63072000, null) // 2 years - just for testing
                .withMaxPixelDistanceKm(1.f, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-test_SST.xml");

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2012-046", "-end", "2013-256"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2012-046", "2013-256");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(1, matchupCount);

            NCTestUtils.assert3DVariable("aatsr-en_lon_corr_fward", 0, 0, 0, 9.969209968386869E36, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_lon_corr_fward", 9, 1, 0, 9.969209968386869E36, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_lon_corr_fward", 10, 2, 0, -0.21979323029518127, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_lon_corr_fward", 11, 3, 0, -0.1797853708267212, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_lon_corr_fward", 12, 4, 0, -0.1397777795791626, mmd);
        }
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("argo-sst");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("aatsr-en"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("argo-sst", 1, 1));
        dimensions.add(new Dimension("aatsr-en", 27, 5));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd03_test")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-test_sst").getPath())
                .withDimensions(dimensions);
    }

    private void insert_AATSR() throws IOException, SQLException {
        final String sensorKey = "aatsr-en";
        final String version = "v3";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2012", "02", "15", "ATS_TOA_1PUUPA20120215_010547_000065273111_00361_52099_6045.N1"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
        storage.insert(satelliteObservation);
    }

    private void insert_ARGO_SST() throws IOException, SQLException {
        final String processingVersion = "v04.0";
        final String sensorKey = "argo-sst";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "argo-sst", processingVersion, "insitu_5_WMOID_5904372_20130819_20161225.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation argoInsitu = readSatelliteObservation(sensorKey, absolutePath, processingVersion);
        storage.insert(argoInsitu);
    }
}
