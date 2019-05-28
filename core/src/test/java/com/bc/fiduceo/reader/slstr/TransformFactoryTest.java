package com.bc.fiduceo.reader.slstr;

import org.junit.Test;

import static com.bc.fiduceo.reader.slstr.VariableType.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TransformFactoryTest {

    @Test
    public void testGet() {
        final TransformFactory factory = new TransformFactory(100, 200, 18);

        Transform transform = factory.get(NADIR_1km);
        assertTrue(transform instanceof Nadir1kmTransform);

        transform = factory.get(NADIR_500m);
        assertTrue(transform instanceof Nadir500mTransform);

        transform = factory.get(OBLIQUE_1km);
        assertTrue(transform instanceof Oblique1kmTransform);

        transform = factory.get(OBLIQUE_500m);
        assertTrue(transform instanceof Oblique500mTransform);
    }
}
