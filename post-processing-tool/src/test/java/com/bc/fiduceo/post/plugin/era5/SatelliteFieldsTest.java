package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import org.junit.Test;
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
