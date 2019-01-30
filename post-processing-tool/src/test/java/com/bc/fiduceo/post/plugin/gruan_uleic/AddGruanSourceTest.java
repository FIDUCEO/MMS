package com.bc.fiduceo.post.plugin.gruan_uleic;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AddGruanSourceTest {

    private NetcdfFile reader;
    private NetcdfFileWriter writer;

    @Before
    public void setUp() {
        reader = mock(NetcdfFile.class);
        writer = mock(NetcdfFileWriter.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPrepare() {
        final AddGruanSource.Configuration configuration = new AddGruanSource.Configuration();
        configuration.targetVariableName = "the_source_file_name";

        final Dimension matchupDimension = new Dimension(FiduceoConstants.MATCHUP_COUNT, 142);
        final Dimension fileNameDimension = new Dimension("file_name", 128);

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(matchupDimension);
        dimensions.add(fileNameDimension);

        when(reader.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(matchupDimension);
        when(reader.findDimension("file_name")).thenReturn(fileNameDimension);

        final Variable targetVariable = mock(Variable.class);
        when(writer.addVariable(any(), eq("the_source_file_name"), eq(DataType.CHAR), (List<Dimension>) any())).thenReturn(targetVariable);

        final AddGruanSource plugin = new AddGruanSource(configuration);

        plugin.prepare(reader, writer);

        verify(reader, times(2)).findDimension(any());
        verify(writer, times(1)).addVariable(null, "the_source_file_name", DataType.CHAR, dimensions);
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testParseConfig() throws JDOMException, IOException {
        final Element configElement = createFullConfigElement();

        final AddGruanSource.Configuration config = AddGruanSource.parseConfiguration(configElement);
        assertNotNull(config);

        assertEquals("the_quelle", config.targetVariableName);
        assertEquals("yppsilon", config.yCoordinateName);
        assertEquals("fileName", config.filenameVariableName);
        assertEquals("proc-ver", config.processingVersionVariableName);
    }

    @Test
    public void testExtractTargetDimensions() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Dimension fileNameDimension = new Dimension("file_name", 128);
        final Dimension matchupCountDimensions = new Dimension(FiduceoConstants.MATCHUP_COUNT, 6);

        when(netcdfFile.findDimension("file_name")).thenReturn(fileNameDimension);
        when(netcdfFile.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(matchupCountDimensions);

        final ArrayList<Dimension> dimensions = AddGruanSource.extractTargetDimensions(netcdfFile);
        assertEquals(2, dimensions.size());

        assertEquals(FiduceoConstants.MATCHUP_COUNT, dimensions.get(0).getFullName());
        assertEquals("file_name", dimensions.get(1).getFullName());
    }

    @Test
    public void testExtractTargetDimensions_missingFileName() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Dimension fileNameDimensions = new Dimension("file_name", 6);

        when(netcdfFile.findDimension("file_name")).thenReturn(fileNameDimensions);

        try {
            AddGruanSource.extractTargetDimensions(netcdfFile);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testExtractTargetDimensions_missingMatchupCount() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Dimension matchupCountDimensions = new Dimension(FiduceoConstants.MATCHUP_COUNT, 6);

        when(netcdfFile.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(matchupCountDimensions);

        try {
            AddGruanSource.extractTargetDimensions(netcdfFile);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    static Element createFullConfigElement() throws JDOMException, IOException {
        final String configXML = "<add-gruan-source>" +
                "    <target-variable name=\"the_quelle\" />" +
                "    <y-variable name=\"yppsilon\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-gruan-source>";

        return TestUtil.createDomElement(configXML);
    }
}
