package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MatchupFieldsTest {

    private MatchupFieldsConfiguration config;
    private MatchupFields matchupFields;

    @Before
    public void setUp() {
        config = new MatchupFieldsConfiguration();
        matchupFields = new MatchupFields();
    }

    @Test
    public void testGetVariables() {
        config.set_fc_mslhf_name("messelfh");

        final Map<String, TemplateVariable> variables = matchupFields.getVariables(config);
        assertEquals(10, variables.size());

        TemplateVariable template = variables.get("an_sfc_siconc");
        assertEquals("sea_ice_area_fraction", template.getStandardName());
        assertEquals("(0 - 1)", template.getUnits());
        assertEquals("Sea ice area fraction", template.getLongName());
        assertEquals("nwp_mu_siconc", template.getName());
        assertFalse(template.is3d());

        template = variables.get("fc_sfc_mslhf");
        assertNull(template.getStandardName());
        assertEquals("W m**-2", template.getUnits());
        assertEquals("Mean surface latent heat flux", template.getLongName());
        assertEquals("messelfh", template.getName());
        assertFalse(template.is3d());
    }

    @Test
    public void testSetGetDimensions() {
        config.setTime_dim_name("tihime");
        config.setTime_steps_future(12);
        config.setTime_steps_past(29);
        final int timeDimLenght = 12 + 29 + 1;

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addDimension(config.getTime_dim_name(), timeDimLenght)).thenReturn(new Dimension(config.getTime_dim_name(), timeDimLenght));

        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(new Dimension(FiduceoConstants.MATCHUP_COUNT, 11));

        final List<Dimension> dimensions = matchupFields.getDimensions(config, writer, ncFile);
        assertEquals(2, dimensions.size());
        Dimension dimension = dimensions.get(0);
        assertEquals(FiduceoConstants.MATCHUP_COUNT, dimension.getShortName());
        assertEquals(11, dimension.getLength());
        dimension = dimensions.get(1);
        assertEquals("tihime", dimension.getShortName());
        assertEquals(42, dimension.getLength());

        verify(writer, times(1)).addDimension("tihime", timeDimLenght);
        verify(ncFile, times(1)).findDimension(FiduceoConstants.MATCHUP_COUNT);
        verifyNoMoreInteractions(writer, ncFile);
    }
}
