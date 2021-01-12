package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ConfigurationTest {

    @Test
    public void testSetGetNWPAuxDir() {
        final String nwpAuxDir = "/here/are/the/files";

        final Configuration config = new Configuration();
        config.setNWPAuxDir(nwpAuxDir);
        assertEquals(nwpAuxDir, config.getNWPAuxDir());
    }

    @Test
    public void testSetGetEra5Collection() {
        final String collection = "the-most-recent";

        final Configuration config = new Configuration();
        config.setEra5Collection(collection);
        assertEquals(collection, config.getEra5Collection());
    }

    @Test
    public void testSetGetSatelliteFields() {
        final Configuration config = new Configuration();
        final SatelliteFieldsConfiguration satFields = new SatelliteFieldsConfiguration();

        config.setSatelliteFields(satFields);
        assertSame(satFields, config.getSatelliteFields());
    }

    @Test
    public void testSetGetMatchupFields() {
        final Configuration config = new Configuration();
        final MatchupFieldsConfiguration matchupFields = new MatchupFieldsConfiguration();

        config.setMatchupFields(matchupFields);
        assertSame(matchupFields, config.getMatchupFields());
    }
}
