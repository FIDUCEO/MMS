package com.bc.fiduceo.post.plugin.flag.hirs;

import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.INTERCHANNEL_TEST_CLOUDY;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.SPACE_CONTRAST_TEST_CLOUDY;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.SPACE_CONTRAST_TEST_WARNING;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FLAG_MASKS_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FLAG_MEANINGS_NAME;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.util.DistanceToLandMap;
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
    private String sensorName;
    private String sourceFileVarName;
    private String sourceXVarName;
    private String sourceYVarName;
    private String processingVersionVarName;
    private String sourceBt11_1µmVarName;

    private String flagVarName;
    private String latVarName;
    private String lonVarName;
    private String btVarName_11_1_µm;
    private String btVarName_6_5_µm;
    private DistanceToLandMap distanceToLandMap;

    private NetcdfFile netcdfFile;
    private NetcdfFile netcdfFileFromWriter;
    private NetcdfFileWriter netcdfFileWriter;

    @Before
    public void setUp() throws Exception {
        sensorName = "hirs-n18";
        sourceFileVarName = "hirs-n18_file_name";
        sourceXVarName = "hirs-n18_x";
        sourceYVarName = "hirs-n18_y";
        processingVersionVarName = "hirs-n18_processing_version";
        sourceBt11_1µmVarName = "bt_ch08";

        flagVarName = "hirs-n18_flags_cloudy";
        latVarName = "hirs-n18_lat";
        lonVarName = "hirs-n18_lon";
        btVarName_11_1_µm = "hirs-n18_bt_ch08";
        btVarName_6_5_µm = "hirs-n18_bt_ch12";
        distanceToLandMap = mock(DistanceToLandMap.class);

        netcdfFile = mock(NetcdfFile.class);
        netcdfFileFromWriter = mock(NetcdfFile.class);
        netcdfFileWriter = mock(NetcdfFileWriter.class);

        postProcessing = new HirsL1CloudyFlags(sensorName, sourceFileVarName,
                                               sourceXVarName, sourceYVarName,
                                               processingVersionVarName, sourceBt11_1µmVarName,
                                               flagVarName,
                                               latVarName, lonVarName,
                                               btVarName_11_1_µm, btVarName_6_5_µm,
                                               distanceToLandMap);
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
        assertThat(postProcessing.sensorName, is(equalTo(sensorName)));
        assertThat(postProcessing.sourceFileVarName, is(equalTo(sourceFileVarName)));
        assertThat(postProcessing.sourceXVarName, is(equalTo(sourceXVarName)));
        assertThat(postProcessing.sourceYVarName, is(equalTo(sourceYVarName)));
        assertThat(postProcessing.processingVersionVarName, is(equalTo(processingVersionVarName)));
        assertThat(postProcessing.sourceBt_11_1_µm_VarName, is(equalTo(sourceBt11_1µmVarName)));

        assertThat(postProcessing.flagVarName, is(equalTo(flagVarName)));
        assertThat(postProcessing.latVarName, is(equalTo(latVarName)));
        assertThat(postProcessing.lonVarName, is(equalTo(lonVarName)));
        assertThat(postProcessing.bt_11_1_µm_VarName, is(equalTo(btVarName_11_1_µm)));
        assertThat(postProcessing.bt_6_5_µm_VarName, is(equalTo(btVarName_6_5_µm)));
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

        verify(variable, times(1)).addAttribute(new Attribute(CF_FLAG_MEANINGS_NAME, Arrays.asList("sc_all", "sc_warning", "sc_cloudy", "ic_cloudy")));
        verify(variable, times(1)).addAttribute(new Attribute(CF_FLAG_MASKS_NAME, masks));
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

    @Test
    public void testGetCloudy_SpaceContrastTest() throws Exception {
        final float threshold = 24.0f;
        final float fillValue = -2.0f;
        float value;
        byte expected;

        // A pixel is classified cloudy if the pixel is useable (!= fill value) and value < threshold
        // see chapter 2.2.2.1 - FIDUCEO Multi-sensor Match up System - Implementation Plan

        // value is equal to fill value
        value = fillValue;
        expected = 0x0;
        assertEquals(expected, HirsL1CloudyFlags.getCloudy_SpaceContrastTest(threshold, value, fillValue));

        // value is useable but not less than threshold
        value = threshold;
        expected = 0x0;
        assertEquals(expected, HirsL1CloudyFlags.getCloudy_SpaceContrastTest(threshold, value, fillValue));

        // value is useable and less than threshold
        value = threshold - 0.00001f;
        expected = HirsL1CloudyFlags.SPACE_CONTRAST_TEST_CLOUDY;
        assertEquals(expected, HirsL1CloudyFlags.getCloudy_SpaceContrastTest(threshold, value, fillValue));
    }

    @Test
    public void testGetCloudy_InterChannelTest() throws Exception {
        final float val_11_1 = 10.0f;
        final float val_6_5 = 34.999f;  // 10 - 34.999 = -24.999 ... abs(-24.999) = 24.999 ... true = 24.999 < 25
        final float fill_11_1 = -33.3f;
        final float fill_6_5 = -99.9f;

        byte expected;

        // A pixel is classified cloudy if both values are useable (!= fill value)
        // and Math.abs(value_11_1 - value_6_5) < 25
        // see chapter 2.2.2.2 - FIDUCEO Multi-sensor Match up System - Implementation Plan

        float v1;
        float v2;

        // value 11.1µm and 6.5µm are unuseable (== fill value)
        v1 = fill_11_1;
        v2 = fill_6_5;
        expected = 0x0;
        assertEquals(expected, HirsL1CloudyFlags.getCloudy_InterChannelTest(v1, fill_11_1, v2, fill_6_5));

        // value 11.1µm is unuseable (== fill value)
        v1 = fill_11_1;
        v2 = val_6_5;
        expected = 0x0;
        assertEquals(expected, HirsL1CloudyFlags.getCloudy_InterChannelTest(v1, fill_11_1, v2, fill_6_5));

        // value 6.5µm is unuseable (== fill value)
        v1 = fill_11_1;
        v2 = val_6_5;
        expected = 0x0;
        assertEquals(expected, HirsL1CloudyFlags.getCloudy_InterChannelTest(v1, fill_11_1, v2, fill_6_5));

        // both values are useable (!= fill value) but absolute difference is not less than 25
        v1 = val_11_1;
        v2 = val_6_5 + 1;
        expected = 0x0;
        assertEquals(expected, HirsL1CloudyFlags.getCloudy_InterChannelTest(v1, fill_11_1, v2, fill_6_5));

        // both values are useable (!= fill value)
        v1 = val_11_1;
        v2 = val_6_5;
        expected = HirsL1CloudyFlags.INTERCHANNEL_TEST_CLOUDY;
        assertEquals(expected, HirsL1CloudyFlags.getCloudy_InterChannelTest(v1, fill_11_1, v2, fill_6_5));
    }
}
