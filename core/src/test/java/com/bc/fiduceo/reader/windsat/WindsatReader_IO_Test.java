package com.bc.fiduceo.reader.windsat;


import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class WindsatReader_IO_Test {

    private WindsatReader reader;

    @Before
    public void setUp() throws IOException {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new WindsatReader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = getWindsatFile();

        try {
            reader.open(file);

            final AcquisitionInfo info = reader.read();

            final Geometry boundingGeometry = info.getBoundingGeometry();
            assertTrue(boundingGeometry instanceof Polygon);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(5, coordinates.length);
            assertEquals(-179.9375, coordinates[0].getLon(), 1e-8);
            assertEquals(-89.9375, coordinates[0].getLat(), 1e-8);
            assertEquals(179.9375, coordinates[1].getLon(), 1e-8);
            assertEquals(-89.9375, coordinates[1].getLat(), 1e-8);

            final Date sensingStart = info.getSensingStart();
            TestUtil.assertCorrectUTCDate(2018, 4, 29, 17, 42, 38, sensingStart);
            final Date sensingStop = info.getSensingStop();
            TestUtil.assertCorrectUTCDate(2018, 4, 29, 19, 30, 45, sensingStop);

            final TimeAxis[] timeAxes = info.getTimeAxes();
            assertEquals(1, timeAxes.length);
            final TimeAxis timeAxis = timeAxes[0];
            assertTrue(timeAxis instanceof L3TimeAxis);
            final Geometry geometry = timeAxis.getGeometry();
            coordinates = geometry.getCoordinates();
            assertEquals(4, coordinates.length);
            assertEquals(-179.9375, coordinates[0].getLon(), 1e-8);
            assertEquals(0.0, coordinates[0].getLat(), 1e-8);
            assertEquals(0.0, coordinates[3].getLon(), 1e-8);
            assertEquals(-89.9375, coordinates[3].getLat(), 1e-8);

            assertEquals(NodeType.UNDEFINED, info.getNodeType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws IOException {
        final File file = getWindsatFile();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(3120, productSize.getNx());
            assertEquals(1440, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelocator() throws IOException {
        final File file = getWindsatFile();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);
        } finally {
            reader.close();
        }
    }

    private File getWindsatFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"windsat-coriolis", "v1.0", "2018", "04", "29", "RSS_WindSat_TB_L1C_r79285_20180429T174238_2018119_V08.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
