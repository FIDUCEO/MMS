package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

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
}
