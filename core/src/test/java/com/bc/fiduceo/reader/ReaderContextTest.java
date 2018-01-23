package com.bc.fiduceo.reader;

import com.bc.fiduceo.geometry.GeometryFactory;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class ReaderContextTest {

    @Test
    public void testSetGetGeometryFactory() {
        final ReaderContext readerContext = new ReaderContext();

        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);

        readerContext.setGeometryFactory(geometryFactory);
        assertSame(geometryFactory, readerContext.getGeometryFactory());
    }
}
