package com.bc.fiduceo.post.plugin.gruan_uleic;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    public void testPrepare() throws IOException, InvalidRangeException {
        final AddGruanSource.Configuration configuration = new AddGruanSource.Configuration();
        configuration.targetVariableName = "the_source_file_name";

        final ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension(FiduceoConstants.MATCHUP_COUNT, 142));
        dimensions.add(new Dimension("file_name", 128));

        when(reader.getDimensions()).thenReturn(dimensions);

        final Variable targetVariable = mock(Variable.class);
        when(writer.addVariable(any(), eq("the_source_file_name"), eq(DataType.CHAR), (List<Dimension>) any())).thenReturn(targetVariable);

        final AddGruanSource plugin = new AddGruanSource(configuration);

        plugin.prepare(reader, writer);

        verify(reader, times(1)).getDimensions();
        verify(writer, times(1)).addVariable(null, "the_source_file_name", DataType.CHAR, dimensions);
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testExtractTargetDimensions() {
        final ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("ignore", 12));
        dimensions.add(new Dimension(FiduceoConstants.MATCHUP_COUNT, 142));
        dimensions.add(new Dimension("file_name", 128));
        dimensions.add(new Dimension("not_required", 13));

        final ArrayList<Dimension> extracted = AddGruanSource.extractTargetDimensions(dimensions);

        assertEquals(2, extracted.size());
        assertEquals(FiduceoConstants.MATCHUP_COUNT, extracted.get(0).getShortName());
        assertEquals("file_name", extracted.get(1).getShortName());
    }

    @Test
    public void testExtractTargetDimensions_missingMatchupCount() {
        final ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("ignore", 12));
        dimensions.add(new Dimension("file_name", 128));
        dimensions.add(new Dimension("not_required", 13));

        try {
            AddGruanSource.extractTargetDimensions(dimensions);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testExtractTargetDimensions_missingFileName() {
        final ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("ignore", 12));
        dimensions.add(new Dimension(FiduceoConstants.MATCHUP_COUNT, 142));
        dimensions.add(new Dimension("not_required", 13));

        try {
            AddGruanSource.extractTargetDimensions(dimensions);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }


    static Element createFullConfigElement() throws JDOMException, IOException {
        final String configXML = "<add-gruan-source>" +
                "    <target-variable name=\"the_quelle\" />" +
                "    <y-variable name=\"üppsilon\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-gruan-source>";

        return TestUtil.createDomElement(configXML);
    }
}
