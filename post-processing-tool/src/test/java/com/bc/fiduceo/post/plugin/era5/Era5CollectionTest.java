package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Era5CollectionTest {

    @Test
    public void testFromString() {
        assertEquals(Era5Collection.ERA_5, Era5Collection.fromString("ERA5"));
        assertEquals(Era5Collection.ERA_5, Era5Collection.fromString("ERA_5"));

        assertEquals(Era5Collection.ERA_5T, Era5Collection.fromString("era5t"));
        assertEquals(Era5Collection.ERA_5T, Era5Collection.fromString("era_5t"));

        assertEquals(Era5Collection.ERA_51, Era5Collection.fromString("ERA51"));
        assertEquals(Era5Collection.ERA_51, Era5Collection.fromString("era_51"));
    }

    @Test
    public void testFromString_invalid() {
        try {
            Era5Collection.fromString("Nasenmann");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
