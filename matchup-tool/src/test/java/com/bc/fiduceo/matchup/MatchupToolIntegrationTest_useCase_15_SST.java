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
public class MatchupToolIntegrationTest_useCase_15_SST extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_AATSR_MOD021KM() throws IOException, SQLException, ParseException, InvalidRangeException {
        insert_AATSR();
        insert_MOD021KM();

        final MatchupToolTestUseCaseConfigBuilder useCaseConfigBuilder = createUseCaseConfigBuilder();
        final UseCaseConfig useCaseConfig = useCaseConfigBuilder.withTimeDeltaSeconds(2400, null)
                .withMaxPixelDistanceKm(0.2f, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-15-sst.xml");

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2012-046", "-end", "2012-046"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2012-046", "2012-046");
        assertTrue(mmdFile.isFile());
        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(20241, matchupCount);

            NCTestUtils.assert3DVariable("mod021km-te_Noise_in_Thermal_Detectors_ch20", 0, 0, 1453, 28, mmd);
            NCTestUtils.assert3DVariable("mod021km-te_EV_1KM_RefSB_ch09", 1, 0, 65535, 28, mmd);
        }
    }

    private void insert_AATSR() throws IOException, SQLException {
        final String sensorKey = "aatsr-en";
        final String version = "v3";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2012", "02", "15", "ATS_TOA_1PUUPA20120215_010547_000065273111_00361_52099_6045.N1"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
        storage.insert(satelliteObservation);
    }

    private void insert_MOD021KM() throws IOException, SQLException {
        final String sensorKey = "mod021km-te";
        final String version = "v61";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2012", "02", "15", "MOD021KM.A2012046.0055.061.2017330053712.hdf"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("aatsr-en");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("mod021km-te"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("aatsr-en", 3, 3));
        dimensions.add(new Dimension("mod021km-te", 5, 5));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd15_SST")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-15-sst").getPath())
                .withDimensions(dimensions);
    }
}
