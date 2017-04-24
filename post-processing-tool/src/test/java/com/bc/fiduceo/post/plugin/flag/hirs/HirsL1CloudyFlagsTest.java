package com.bc.fiduceo.post.plugin.flag.hirs;

import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.INTERCHANNEL_TEST_CLOUDY;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.SPACE_CONTRAST_TEST_CLOUDY;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags.SPACE_CONTRAST_TEST_WARNING;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.post.PostProcessing;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.util.Arrays;

public class HirsL1CloudyFlagsTest {

    private HirsL1CloudyFlags postProcessing;
    private String btVarName_11_1_µm;
    private String btVarName_6_5_µm;
    private String flagVarName;
    private String latVarName;
    private String lonVarName;
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

        distanceToLandMap = mock(DistanceToLandMap.class);
        netcdfFile = mock(NetcdfFile.class);
        netcdfFileFromWriter = mock(NetcdfFile.class);
        netcdfFileWriter = mock(NetcdfFileWriter.class);

        postProcessing = new HirsL1CloudyFlags(btVarName_11_1_µm, btVarName_6_5_µm, flagVarName, latVarName, lonVarName, distanceToLandMap);
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
        assertThat(postProcessing.btVarName_11_1_µm, is(equalTo("hirs-n18_bt_ch08")));
        assertThat(postProcessing.btVarName_6_5_µm, is(equalTo("hirs-n18_bt_ch12")));
        assertThat(postProcessing.flagVarName, is(equalTo("hirs-n18_flags_cloudy")));
        assertThat(postProcessing.latVarName, is(equalTo("hirs-n18_lat")));
        assertThat(postProcessing.lonVarName, is(equalTo("hirs-n18_lon")));
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
    public void testIsLandOrIceCovered() throws Exception {
        final DistanceToLandMap map = mock(DistanceToLandMap.class);

        when(map.getDistance(anyDouble(), anyDouble())).thenReturn(0.299999999999999);
        assertThat(HirsL1CloudyFlags.isLandOrIceCovered(map, 1, 2), is(equalTo(true)));

        when(map.getDistance(anyDouble(), anyDouble())).thenReturn(0.3);
        assertThat(HirsL1CloudyFlags.isLandOrIceCovered(map, 1, 2), is(equalTo(false)));
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
}
