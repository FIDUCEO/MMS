package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.*;
import ucar.nc2.Dimension;

import java.awt.*;
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

    @Test
    public void testMergeData_2D_left() {
        final Array left = Array.factory(DataType.INT, new int[]{3, 1});
        Index index = left.getIndex();
        index.set(0, 0);
        left.setInt(index, 1);
        index.set(1, 0);
        left.setInt(index, 2);
        index.set(2, 0);
        left.setInt(index, 3);

        final Array right = Array.factory(DataType.INT, new int[]{3, 2});
        index = right.getIndex();
        index.set(0, 0);
        right.setInt(index, 4);
        index.set(0, 1);
        right.setInt(index, 5);
        index.set(1, 0);
        right.setInt(index, 6);
        index.set(1, 1);
        right.setInt(index, 7);
        index.set(2, 0);
        right.setInt(index, 8);
        index.set(2, 1);
        right.setInt(index, 9);

        final Variable variable = mock(Variable.class);
        when(variable.getRank()).thenReturn(3);
        when(variable.getDataType()).thenReturn(DataType.INT);

        final Array merged = SatelliteFields.mergeData(left, right, 1, new Rectangle(-1, 100, 3, 3), variable);
        final int[] shape = merged.getShape();
        assertEquals(2, shape.length);
        assertEquals(3, shape[0]);
        assertEquals(3, shape[1]);

        index = merged.getIndex();
        index.set(0, 0);
        assertEquals(1, merged.getInt(index));
        index.set(0, 1);
        assertEquals(4, merged.getInt(index));
        index.set(0, 2);
        assertEquals(5, merged.getInt(index));

        verify(variable, times(1)).getRank();
        verify(variable, times(1)).getDataType();
        verifyNoMoreInteractions(variable);
    }

    @Test
    public void testMergeData_2D_right() {
        final Array left = Array.factory(DataType.INT, new int[]{3, 1});
        Index index = left.getIndex();
        index.set(0, 0);
        left.setInt(index, 6);
        index.set(1, 0);
        left.setInt(index, 7);
        index.set(2, 0);
        left.setInt(index, 8);

        final Array right = Array.factory(DataType.INT, new int[]{3, 2});
        index = right.getIndex();
        index.set(0, 0);
        right.setInt(index, 0);
        index.set(0, 1);
        right.setInt(index, 1);
        index.set(1, 0);
        right.setInt(index, 2);
        index.set(1, 1);
        right.setInt(index, 3);
        index.set(2, 0);
        right.setInt(index, 4);
        index.set(2, 1);
        right.setInt(index, 5);

        final Variable variable = mock(Variable.class);
        when(variable.getRank()).thenReturn(3);
        when(variable.getDataType()).thenReturn(DataType.INT);

        final Array merged = SatelliteFields.mergeData(left, right, 1, new Rectangle(1438, 100, 3, 3), variable);
        final int[] shape = merged.getShape();
        assertEquals(2, shape.length);
        assertEquals(3, shape[0]);
        assertEquals(3, shape[1]);

        index = merged.getIndex();
        index.set(0, 0);
        assertEquals(0, merged.getInt(index));
        index.set(0, 1);
        assertEquals(1, merged.getInt(index));
        index.set(0, 2);
        assertEquals(6, merged.getInt(index));

        verify(variable, times(1)).getRank();
        verify(variable, times(1)).getDataType();
        verifyNoMoreInteractions(variable);
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
