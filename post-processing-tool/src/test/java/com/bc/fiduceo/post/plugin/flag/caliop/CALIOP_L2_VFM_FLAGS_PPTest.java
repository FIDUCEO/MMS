package com.bc.fiduceo.post.plugin.flag.caliop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.reader.ReaderCache;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Sabine on 21.07.2017.
 */
@RunWith(IOTestRunner.class)
public class CALIOP_L2_VFM_FLAGS_PPTest {

    private CALIOP_L2_VFM_FLAGS_PP pp;

    @Before
    public void setUp() throws Exception {
        pp = new CALIOP_L2_VFM_FLAGS_PP();
        // In regular usage the PostProcessingContext will be set by post processing framework.
        pp.setContext(createPostProcessingContext());
        // a call setContext(...) generates an framework call to initReaderCache() method
    }

    @After
    public void tearDown() throws Exception {
        pp.dispose();
    }

    @Test
    public void prepare() throws Exception {
        final Path testDirPath = TestUtil.getTestDataDirectory().toPath();
        final Path relMmd15sst = Paths.get("post-processing", "mmd15sst", "mmd15_sst_drifter-sst_amsre-aq_caliop_vfm-cal_2008-149_2008-155.nc");
        final Path absMmd15sst = testDirPath.resolve(relMmd15sst);
        // open the file with NetCDFUtils is needed because the standard open mechanism changes the file size
        final NetcdfFile reader = NetCDFUtils.openReadOnly(absMmd15sst.toAbsolutePath().toString());

        final String numFlagsDimName = "center-fcf-flags";
        final String dimStr = reader.findVariable(NetcdfFile.makeValidCDLName("caliop_vfm.Latitude")).getDimensionsString() + " " + numFlagsDimName;

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final Variable variable = mock(Variable.class);
        when(writer.addVariable(null, "caliop_vfm.Center_Feature_Classification_Flags", DataType.SHORT, dimStr)).thenReturn(variable);

        //execution
        try {
            pp.prepare(reader, writer);
        } finally {
            reader.close();
        }

        //verification
        verify(writer, times(1)).addDimension(null, numFlagsDimName, 545);
        verify(writer, times(1)).addVariable(null, "caliop_vfm.Center_Feature_Classification_Flags", DataType.SHORT, dimStr);
        verify(variable, times(1)).addAttribute(argThat(is(equalTo(new Attribute("_Unsigned", "true")))));
        verify(variable, times(1)).addAttribute(argThat(is(equalTo(new Attribute("units", "NoUnits")))));
        verify(variable, times(1)).addAttribute(argThat(is(equalTo(new Attribute("format", "UInt_16")))));
        verify(variable, times(1)).addAttribute(argThat(is(equalTo(new Attribute("valid_range", "1...49146")))));
        verifyNoMoreInteractions(variable);
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void compute() throws Exception {
    }

    @Test
    public void testReaderCacheIsInitialized() throws Exception {
        assertNotNull(pp.readerCache);
    }

    @Test
    public void testDisposeIsCallingReaderCacheClose() throws Exception {
        //preparation
        final ReaderCache readerCache = mock(ReaderCache.class);
        pp.readerCache = readerCache;

        //execution
        pp.dispose();

        //verification
        verify(readerCache, times(1)).close();
        verifyNoMoreInteractions(readerCache);
    }

    private PostProcessingContext createPostProcessingContext() throws IOException {
        PostProcessingContext pp_context = new PostProcessingContext();
        pp_context.setSystemConfig(createSystemConfig());
        return pp_context;
    }

    private SystemConfig createSystemConfig() throws IOException {
        final String archivePath = TestUtil.getTestDataDirectory().getAbsolutePath();
        return SystemConfig.load(new ByteArrayInputStream(
                    ("<system-config>" +
                     "    <geometry-library name=\"S2\"/>" +
                     "    <reader-cache-size>24</reader-cache-size>" +
                     "    <archive>" +
                     "        <root-path>" + archivePath + "</root-path>" +
                     "        <rule sensors=\"drifter-sst, ship-sst\">insitu/SENSOR/VERSION</rule>" +
                     "    </archive>" +
                     "</system-config>").getBytes()
        ));
    }

}