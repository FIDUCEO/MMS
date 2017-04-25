package com.bc.fiduceo.post.plugin.flag.hirs;

import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.INTERCHANNEL_TEST_CLOUDY;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.SPACE_CONTRAST_TEST_CLOUDY;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.SPACE_CONTRAST_TEST_WARNING;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingContext;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class HirsL1CloudyFlagsTest {

    private HirsL1CloudyFlags postProcessing;
    private String btVarName_11_1_µm;
    private String btVarName_6_5_µm;
    private String flagVarName;
    private String latVarName;
    private String lonVarName;
    private String sourceFileVarName;
    private DistanceToLandMap distanceToLandMap;
    private NetcdfFile netcdfFile;
    private NetcdfFile netcdfFileFromWriter;
    private NetcdfFileWriter netcdfFileWriter;

    @Before
    public void setUp() throws Exception {
        btVarName_11_1_µm = "hirs-n18_bt_ch08";
        btVarName_6_5_µm = "hirs-n18_bt_ch12";
        flagVarName = "hirs-n18_flags_cloudy";
        latVarName = "hirs-n18_lat";
        lonVarName = "hirs-n18_lon";
        sourceFileVarName = "hirs-n18_file_name";

        distanceToLandMap = mock(DistanceToLandMap.class);
        netcdfFile = mock(NetcdfFile.class);
        netcdfFileFromWriter = mock(NetcdfFile.class);
        netcdfFileWriter = mock(NetcdfFileWriter.class);

        postProcessing = new HirsL1CloudyFlags(btVarName_11_1_µm, btVarName_6_5_µm, flagVarName, latVarName, lonVarName, sourceFileVarName, distanceToLandMap);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testThatClassImplementsPostProcessing() {
        assertThat(postProcessing, is(instanceOf(PostProcessing.class)));
    }

    @Test
    public void testThatFinalFieldsAreSetInTheConstructor() throws Exception {
        assertThat(postProcessing.btVarName_11_1_µm, is(equalTo(btVarName_11_1_µm)));
        assertThat(postProcessing.btVarName_6_5_µm, is(equalTo(btVarName_6_5_µm)));
        assertThat(postProcessing.flagVarName, is(equalTo(flagVarName)));
        assertThat(postProcessing.latVarName, is(equalTo(latVarName)));
        assertThat(postProcessing.lonVarName, is(equalTo(lonVarName)));
        assertThat(postProcessing.sourceFileVarName, is(equalTo(sourceFileVarName)));
        assertThat(postProcessing.distanceToLandMap, is(sameInstance(distanceToLandMap)));
    }

    @Test
    public void testThatPrepareAddsTheFlagVariable() throws Exception {
        final NetcdfFile file = mock(NetcdfFile.class);
        final NetcdfFileWriter fileWriter = mock(NetcdfFileWriter.class);
        final Variable variable = mock(Variable.class);

        when(file.findVariable(null, btVarName_11_1_µm)).thenReturn(variable);
        when(variable.getDimensionsString()).thenReturn("a b c");
        when(fileWriter.addVariable(null, flagVarName, DataType.BYTE, "a b c")).thenReturn(variable);

        postProcessing.prepare(file, fileWriter);

        verify(file, times(1)).findVariable(null, btVarName_11_1_µm);
        verify(variable, times(1)).getDimensionsString();
        verify(fileWriter, times(1)).addVariable(null, flagVarName, DataType.BYTE, "a b c");

        Array masks = new ArrayByte(new int[]{4});
        masks.setByte(0, SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE);
        masks.setByte(1, SPACE_CONTRAST_TEST_WARNING);
        masks.setByte(2, SPACE_CONTRAST_TEST_CLOUDY);
        masks.setByte(3, INTERCHANNEL_TEST_CLOUDY);
        final String Separator = "\t";

        verify(variable, times(1)).addAttribute(new Attribute("flag_meanings", Arrays.asList("sc_all", "sc_warning", "sc_cloudy", "ic_cloudy")));
        verify(variable, times(1)).addAttribute(new Attribute("flag_masks", masks));
        verify(variable, times(1)).addAttribute(new Attribute("flag_coding_name", "hirs_cloudy_flags"));
        verify(variable, times(1)).addAttribute(new Attribute("flag_descriptions", "space contrast test, all pixels are usable"
                                                                                   + Separator +
                                                                                   "space contrast test, warning, less than 99 percent are usable"
                                                                                   + Separator +
                                                                                   "space contrast test, cloudy"
                                                                                   + Separator +
                                                                                   "interchannel test, cloudy"));
        verifyNoMoreInteractions(file, variable, fileWriter);
    }

    @Test
    public void testThatCloudyDetectionWorksOverLand() throws Exception {
        when(distanceToLandMap.getDistance(anyDouble(), anyDouble())).thenReturn(0.0);
        when(netcdfFileWriter.getNetcdfFile()).thenReturn(netcdfFileFromWriter);

//        postProcessing.compute(netcdfFile, netcdfFileWriter);
    }

    @Test
    public void testIsLand() throws Exception {
        final DistanceToLandMap map = mock(DistanceToLandMap.class);

        when(map.getDistance(anyDouble(), anyDouble())).thenReturn(0.299999999999999);
        assertThat(HirsL1CloudyFlags.isLand(map, 1, 2), is(equalTo(true)));

        when(map.getDistance(anyDouble(), anyDouble())).thenReturn(0.3);
        assertThat(HirsL1CloudyFlags.isLand(map, 1, 2), is(equalTo(false)));
    }

    @Test
    public void testGetMaximumAndFlags() throws Exception {
        HirsL1CloudyFlags.MaximumAndFlags mf;
        final int maxNumInvalidPixels = 1;
        final int F = 2; // _FillValue

        mf = HirsL1CloudyFlags.getMaximumAndFlags(Array.factory(new float[]{4, 5, 8, 6, 1, 6, 7, 5, 3}), F, maxNumInvalidPixels);
        assertThat(mf.maximum, is(equalTo(8.0)));
        assertThat(mf.flags, is(equalTo((byte) 1))); // 1 means all pixel are usable

        mf = HirsL1CloudyFlags.getMaximumAndFlags(Array.factory(new float[]{4, 5, F, 6, 1, 6, 7, 5, 3}), F, maxNumInvalidPixels);
        assertThat(mf.maximum, is(equalTo(7.0)));
        assertThat(mf.flags, is(equalTo((byte) 0))); // 1 means all pixel are usable

        mf = HirsL1CloudyFlags.getMaximumAndFlags(Array.factory(new float[]{4, 5, F, 6, 1, 6, F, 5, 3}), F, maxNumInvalidPixels);
        assertThat(mf.maximum, is(equalTo(6.0)));
        assertThat(mf.flags, is(equalTo((byte) 2))); // 2 means warning, because there are more invalids than maxNumInvalidPixels
    }

    @Test
    public void testExtractYearMonthDayFromFilename() throws Exception {
        final PostProcessingContext processingContext = new PostProcessingContext();
        processingContext.setSystemConfig(
                    SystemConfig.load(
                                new ByteArrayInputStream(
                                            ("<system-config>" +
                                             "    <geometry-library name = \"S2\" />" +
                                             "    <archive>" +
                                             "        <root-path>anyPath</root-path>" +
                                             "        <rule sensors = \"hirs-n18\">anyRule</rule>" +
                                             "    </archive>" +
                                             "</system-config>").getBytes()
                                )
                    )
        );

        final HirsL1CloudyFlags.CloudRC readerCache = new HirsL1CloudyFlags.CloudRC(processingContext);
        String hirsFileName;
        int[] ymd;

        hirsFileName = "189800453.NSS.HIRX.NN.D11233.S0808.E1003.B3221112.GC.nc";
        ymd = readerCache.extractYearMonthDayFromFilename(hirsFileName);
        assertArrayEquals(new int[]{2011, 8, 21}, ymd);

        hirsFileName = "191062833.NSS.HIRX.NN.D88123.S1356.E1551.B3227172.WI.nc";
        ymd = readerCache.extractYearMonthDayFromFilename(hirsFileName);
        assertArrayEquals(new int[]{1988, 5, 2}, ymd);
    }
}
