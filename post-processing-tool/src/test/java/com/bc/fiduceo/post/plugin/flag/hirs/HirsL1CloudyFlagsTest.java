package com.bc.fiduceo.post.plugin.flag.hirs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.post.PostProcessing;
import org.junit.*;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class HirsL1CloudyFlagsTest {

    private HirsL1CloudyFlags postProcessing;
    private String btVarName_11_1_µm;
    private String btVarName_6_5_µm;
    private String flagVarName;

    @Before
    public void setUp() throws Exception {
        btVarName_11_1_µm = "hirs-n18_bt_ch08";
        btVarName_6_5_µm = "hirs-n18_bt_ch12";
        flagVarName = "hirs-n18_flags_cloudy";
        postProcessing = new HirsL1CloudyFlags(btVarName_11_1_µm, btVarName_6_5_µm, flagVarName);
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
    }

    @Test
    public void testThatPrepareAddsTheFlagVariable() throws Exception {
        final NetcdfFile file = mock(NetcdfFile.class);
        final NetcdfFileWriter fileWriter = mock(NetcdfFileWriter.class);
        final Variable variable = mock(Variable.class);

        when(file.findVariable(null, btVarName_11_1_µm)).thenReturn(variable);
        when(variable.getDimensionsString()).thenReturn("a b c");

        postProcessing.prepare(file, fileWriter);

        verify(file, times(1)).findVariable(null, btVarName_11_1_µm);
        verify(variable, times(1)).getDimensionsString();
        verify(fileWriter, times(1)).addVariable(null, flagVarName, DataType.BYTE, "a b c");
        verifyNoMoreInteractions(file, variable, fileWriter);
    }
}
