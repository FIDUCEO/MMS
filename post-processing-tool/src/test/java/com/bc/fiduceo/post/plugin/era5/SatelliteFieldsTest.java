package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.*;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SatelliteFieldsTest {

    @Test
    public void testGetVariables() {
        final SatelliteFields satelliteFields = new SatelliteFields();
        final SatelliteFieldsConfiguration config = new SatelliteFieldsConfiguration();
        config.set_an_skt_name("Skate");

        final Map<String, TemplateVariable> variables = satelliteFields.getVariables(config);
        assertEquals(13, variables.size());

        TemplateVariable template = variables.get("an_sfc_u10");
        assertNull(template.getStandardName());
        assertEquals("m s**-1", template.getUnits());
        assertEquals("10 metre U wind component", template.getLongName());
        assertEquals("nwp_u10", template.getName());
        assertFalse(template.is3d());

        template = variables.get("an_sfc_skt");
        assertNull(template.getStandardName());
        assertEquals("K", template.getUnits());
        assertEquals("Skin temperature", template.getLongName());
        assertEquals("Skate", template.getName());
        assertFalse(template.is3d());
    }

    @Test
    public void testToEra5TimeStamp() {
        assertEquals(1212400800, SatelliteFields.toEra5TimeStamp(1212399488));
        assertEquals(1212145200, SatelliteFields.toEra5TimeStamp(1212145250));
    }

    @Test
    public void testConvertToEra5TimeStamp() {
        final Array acquisitionTime = Array.factory(DataType.INT, new int[]{6}, new int[]{1480542129, 1480545559, 1480541820, 1480543482, 1480542437, 1480542946});

        final Array converted = SatelliteFields.convertToEra5TimeStamp(acquisitionTime);
        assertEquals(6, converted.getSize());
        assertEquals(1480543200, converted.getInt(0));
        assertEquals(1480546800, converted.getInt(1));
        assertEquals(1480543200, converted.getInt(2));
        assertEquals(1480543200, converted.getInt(3));
        assertEquals(1480543200, converted.getInt(4));
        assertEquals(1480543200, converted.getInt(5));
    }

    @Test
    public void testGetNwpShape() {
        final SatelliteFieldsConfiguration config = new SatelliteFieldsConfiguration();
        config.set_x_dim(3);
        config.set_y_dim(5);

        final int[] matchupShape = {11, 7, 7};

        final int[] nwpShape = SatelliteFields.getNwpShape(config, matchupShape);
        assertEquals(3, nwpShape.length);
        assertEquals(11, nwpShape[0]);
        assertEquals(5, nwpShape[1]);
        assertEquals(3, nwpShape[2]);
    }

    @Test
    public void testGetNwpShape_clip() {
        final SatelliteFieldsConfiguration config = new SatelliteFieldsConfiguration();
        config.set_x_dim(7);
        config.set_y_dim(7);

        final int[] matchupShape = {12, 3, 5};

        final int[] nwpShape = SatelliteFields.getNwpShape(config, matchupShape);
        assertEquals(3, nwpShape.length);
        assertEquals(12, nwpShape[0]);
        assertEquals(3, nwpShape[1]);
        assertEquals(5, nwpShape[2]);
    }

    @Test
    public void testGetNwpOffset() {
        final int[] matchupShape = {118, 7, 7};
        final int[] nwpShape = {118, 5, 5};

        final int[] nwpOffset = SatelliteFields.getNwpOffset(matchupShape, nwpShape);
        assertEquals(3, nwpOffset.length);
        assertEquals(0, nwpOffset[0]);
        assertEquals(1, nwpOffset[1]);
        assertEquals(1, nwpOffset[2]);
    }

    @Test
    public void testAddAttributes() {
        final TemplateVariable templateVariable = new TemplateVariable("theName", "metres", "a_long_name", "a_standard_name", true);
        final Variable variable = mock(Variable.class);

        SatelliteFields.addAttributes(templateVariable, variable);

        verify(variable, times(1)).addAttribute(new Attribute("units", "metres"));
        verify(variable, times(1)).addAttribute(new Attribute("long_name", "a_long_name"));
        verify(variable, times(1)).addAttribute(new Attribute("standard_name", "a_standard_name"));
        verify(variable, times(1)).addAttribute(new Attribute("_FillValue", 9.96921E36f));
        verifyNoMoreInteractions(variable);
    }

    @Test
    public void testAddAttributes_missingStandarName() {
        final TemplateVariable templateVariable = new TemplateVariable("Carola", "gramm", "Heffalump", null, true);
        final Variable variable = mock(Variable.class);

        SatelliteFields.addAttributes(templateVariable, variable);

        verify(variable, times(1)).addAttribute(new Attribute("units", "gramm"));
        verify(variable, times(1)).addAttribute(new Attribute("long_name", "Heffalump"));
        verify(variable, times(1)).addAttribute(new Attribute("_FillValue", 9.96921E36f));
        verifyNoMoreInteractions(variable);
    }

    @Test
    public void testSetGetDimensions_2D() {
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(new Dimension(FiduceoConstants.MATCHUP_COUNT, 10));

        final SatelliteFields satelliteFields = new SatelliteFields();
        final SatelliteFieldsConfiguration config = createConfig();

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addDimension(config.get_x_dim_name(), config.get_x_dim())).thenReturn(new Dimension(config.get_x_dim_name(), config.get_x_dim()));
        when(writer.addDimension(config.get_y_dim_name(), config.get_y_dim())).thenReturn(new Dimension(config.get_y_dim_name(), config.get_y_dim()));

        satelliteFields.setDimensions(config, writer, ncFile);

        final List<Dimension> dimensions = satelliteFields.getDimensions(new TemplateVariable("what", "ever", "we", "write", false));
        assertEquals(3, dimensions.size());

        Dimension dimension = dimensions.get(0);
        assertEquals(FiduceoConstants.MATCHUP_COUNT, dimension.getShortName());
        assertEquals(10, dimension.getLength());

        dimension = dimensions.get(1);
        assertEquals("y_dim", dimension.getShortName());
        assertEquals(14, dimension.getLength());

        dimension = dimensions.get(2);
        assertEquals("x_dim", dimension.getShortName());
        assertEquals(12, dimension.getLength());

        verify(writer, times(3)).addDimension(anyString(), anyInt());
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testSetGetDimensions_3D() {
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(new Dimension(FiduceoConstants.MATCHUP_COUNT, 11));

        final SatelliteFields satelliteFields = new SatelliteFields();
        final SatelliteFieldsConfiguration config = createConfig();
        config.set_z_dim(17);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addDimension(config.get_x_dim_name(), config.get_x_dim())).thenReturn(new Dimension(config.get_x_dim_name(), config.get_x_dim()));
        when(writer.addDimension(config.get_y_dim_name(), config.get_y_dim())).thenReturn(new Dimension(config.get_y_dim_name(), config.get_y_dim()));
        when(writer.addDimension(config.get_z_dim_name(), config.get_z_dim())).thenReturn(new Dimension(config.get_z_dim_name(), config.get_z_dim()));

        satelliteFields.setDimensions(config, writer, ncFile);

        final List<Dimension> dimensions = satelliteFields.getDimensions(new TemplateVariable("what", "ever", "we", "write", true));
        assertEquals(4, dimensions.size());

        Dimension dimension = dimensions.get(0);
        assertEquals(FiduceoConstants.MATCHUP_COUNT, dimension.getShortName());
        assertEquals(11, dimension.getLength());

        dimension = dimensions.get(1);
        assertEquals("z_dim", dimension.getShortName());
        assertEquals(17, dimension.getLength());

        dimension = dimensions.get(2);
        assertEquals("y_dim", dimension.getShortName());
        assertEquals(14, dimension.getLength());

        dimension = dimensions.get(3);
        assertEquals("x_dim", dimension.getShortName());
        assertEquals(12, dimension.getLength());

        verify(writer, times(3)).addDimension(anyString(), anyInt());
        verifyNoMoreInteractions(writer);
    }

    private SatelliteFieldsConfiguration createConfig() {
        final SatelliteFieldsConfiguration config = new SatelliteFieldsConfiguration();
        config.set_x_dim(12);
        config.set_x_dim_name("x_dim");
        config.set_y_dim(14);
        config.set_y_dim_name("y_dim");
        config.set_z_dim_name("z_dim");
        config.set_nwp_time_variable_name("nwp_time");
        config.set_time_variable_name("sat_time");
        config.set_longitude_variable_name("sat_lon");
        config.set_latitude_variable_name("sat_lat");
        return config;
    }
}
