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

import static org.junit.Assert.*;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_15_SST extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_AATSR_MOD021KM() throws IOException, SQLException, ParseException, InvalidRangeException {
        final File mmdWriterConfig = new File(configDir, "mmd-writer-config.xml");
        if (!mmdWriterConfig.delete()) {
            fail("unable to delete test file");
        }
        TestUtil.writeMmdWriterConfig(configDir, MODIS_SCALED_VARIABLES);

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
            NCTestUtils.assert3DVariable("mod021km-te_EV_1KM_RefSB_ch09", 1, 0, 1454, 913.4769513877109, mmd);
            NCTestUtils.assert3DVariable("mod021km-te_EV_1KM_RefSB_ch13L", 2, 0, 1455, 389.833833881421, mmd);
            NCTestUtils.assert3DVariable("mod021km-te_EV_1KM_RefSB_Uncert_Indexes_ch10", 0, 1, 1456, 1785.0, mmd);
            NCTestUtils.assert3DVariable("mod021km-te_EV_1KM_Emissive_ch27", 1, 1, 1457, 2731.9157639251207, mmd);

            // @todo 2 tb/tb add more tests 2020-06-07
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

    private static final String MODIS_SCALED_VARIABLES ="<variables-configuration> \n" +
            "    <sensors names=\"mod021km-te, myd021km-aq\" >\n" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch08\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch09\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch10\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch11\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch12\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch13L\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch13H\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch14L\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch14H\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch15\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch16\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch17\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch18\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch19\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_ch26\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch08\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch09\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch10\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch11\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch12\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch13L\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch13H\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch14L\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch14H\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch15\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch16\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch17\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch18\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch19\" />" +
            "        <writeScaled source-name=\"EV_1KM_RefSB_Uncert_Indexes_ch26\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch20\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch21\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch22\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch23\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch24\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch25\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch27\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch28\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch29\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch30\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch31\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch32\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch33\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch34\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch35\" />" +
            "        <writeScaled source-name=\"EV_1KM_Emissive_ch36\" />" +
            "    </sensors>\n" +
            "</variables-configuration>";
}
