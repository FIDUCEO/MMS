package com.bc.fiduceo.location;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.*;

import java.awt.geom.Point2D;

/**
 * Created by Sabine on 18.03.2016.
 */
public class ClippingPixelLocatorTest {

    @Test
    public void testClipPixelAnswer() throws Exception {
        final Point2D.Double yIs3 = new Point2D.Double(200, 3);
        final Point2D.Double yIs7 = new Point2D.Double(200, 7);
        final Point2D.Double yIs2 = new Point2D.Double(200, 2);
        final Point2D.Double yIs8 = new Point2D.Double(200, 8);
        final PixelLocator mock = mock(PixelLocator.class);
        final Point2D[] mockAnswer = {yIs2, yIs3, yIs7, yIs8,};
        when(mock.getPixelLocation(anyDouble(), anyDouble())).thenReturn(mockAnswer);

        final int minY = 3;
        final int maxY = 7;
        final ClippingPixelLocator locator = new ClippingPixelLocator(mock, minY, maxY);
        final Point2D[] locations = locator.getPixelLocation(13, 14);

        assertEquals(2, locations.length);
        assertSame(yIs3, locations[0]);
        assertSame(yIs7, locations[1]);
    }
}