package com.bc.fiduceo.reader;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.archive.ArchiveConfig;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.insitu.sst_cci.SSTInsituReaderPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.nc2.Variable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

@RunWith(IOTestRunner.class)
public class ReaderCache_IO_Test {

    private ReaderCache readerCache;
    private ReaderFactory readerFactory;

    @Before
    public void setUp() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        // we don't need temp file support, archive or configDir here tb 2018-01-23
        readerFactory = ReaderFactory.create(geometryFactory, null, null, null);
        readerCache = new ReaderCache(2, readerFactory, null);
    }

    @Test
    public void testGetReaderFor_secondCall_andGetCall() throws Exception {
        //preparation
        final Path relativeInsituFile = Paths.get("insitu", "drifter-sst", "v03.3", "insitu_0_WMOID_51939_20031105_20131121.nc");
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final Path insituFile = testDataDirectory.toPath().resolve(relativeInsituFile);
        final String sensorType = new SSTInsituReaderPlugin().getSupportedSensorKeys()[0];

        assertNull(readerCache.get(insituFile));

        //execution
        final Reader readerFor = readerCache.getReaderFor(sensorType, insituFile, null);
        final Reader secondCallReader = readerCache.getReaderFor(sensorType, insituFile, null);
        final Reader reader = readerCache.get(insituFile);

        //verification
        assertEquals("com.bc.fiduceo.reader.insitu.sst_cci.SSTInsituReader",readerFor.getClass().getTypeName());
        assertSame(readerFor, reader);
        assertSame(readerFor, secondCallReader);
    }

    @Test
    public void getInsituFileOpened_InPostProcessingContext_ServingAnArchiveToReaderCache() throws Exception {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final String root = testDataDirectory.getAbsolutePath();
        final String systemConfigXml = "<system-config>" +
                "    <archive>" +
                "        <root-path>" +
                "            " + root +
                "        </root-path>" +
                "        <rule sensors = \"animal-sst\">" +
                "            insitu/SENSOR/VERSION" +
                "        </rule>" +
                "    </archive>" +
                "</system-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(systemConfigXml.getBytes());

        final String processingVersion = "v03.3";
        final SystemConfig systemConfig = SystemConfig.load(inputStream);
        final ArchiveConfig archiveConfig = systemConfig.getArchiveConfig();
        final Archive archive = new Archive(archiveConfig);
        final ReaderCache readerCache = new ReaderCache(30, readerFactory, archive);

        // action
        final Reader insituFileOpened = readerCache
                .getReaderFor("animal-sst", Paths.get("insitu_12_WMOID_11835_20040110_20040127.nc"), processingVersion);

        //validation
        assertNotNull(insituFileOpened);
        final List<Variable> variables = insituFileOpened.getVariables();
        assertNotNull(variables);
        final String[] expectedNames = {
                "insitu.time",
                "insitu.lat",
                "insitu.lon",
                "insitu.sea_surface_temperature",
                "insitu.sst_uncertainty",
                "insitu.sst_depth",
                "insitu.sst_qc_flag",
                "insitu.sst_track_flag",
                "insitu.mohc_id",
                "insitu.id"
        };
        assertEquals(expectedNames.length, variables.size());
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            assertEquals(i + ": " + expectedNames[i], i + ": " + variable.getShortName());
        }
    }

    @Test
    public void testGetReaderFor_CallTwice_InPostProcessingContext_ServingAnArchiveToReaderCache() throws Exception {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final String root = testDataDirectory.getAbsolutePath();
        final String systemConfigXml = "<system-config>" +
                "    <archive>" +
                "        <root-path>" +
                "            " + root +
                "        </root-path>" +
                "        <rule sensors = \"animal-sst\">" +
                "            insitu/SENSOR/VERSION" +
                "        </rule>" +
                "    </archive>" +
                "</system-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(systemConfigXml.getBytes());

        final String processingVersion = "v03.3";
        final SystemConfig systemConfig = SystemConfig.load(inputStream);
        final ArchiveConfig archiveConfig = systemConfig.getArchiveConfig();
        final Archive archive = new Archive(archiveConfig);
        final ReaderCache readerCache = new ReaderCache(30, readerFactory, archive);

        // action
        final Reader insituFileOpened = readerCache
                .getReaderFor("animal-sst", Paths.get("insitu_12_WMOID_11835_20040110_20040127.nc"), processingVersion);
        final Reader secondInsituFileOpened = readerCache
                .getReaderFor("animal-sst", Paths.get("insitu_12_WMOID_11835_20040110_20040127.nc"), processingVersion);

        //validation
        assertNotNull(insituFileOpened);
        assertSame(insituFileOpened, secondInsituFileOpened);
    }
}
