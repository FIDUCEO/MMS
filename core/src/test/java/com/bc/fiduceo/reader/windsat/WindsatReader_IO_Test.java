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
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
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
    public void testGetPixelLocator() throws IOException {
        final File file = getWindsatFile();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(94.1875, geoLocation.getX(), 1e-8);
            assertEquals(-89.9375, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(3119.5, 0.5, null);
            assertEquals(64.3125, geoLocation.getX(), 1e-8);
            assertEquals(-89.9375, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(1528.5, 674.5, null);
            assertEquals(-96.8125, geoLocation.getX(), 1e-8);
            assertEquals(-5.6875, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(2128.5, 1176.5, null);
            assertEquals(-171.8125, geoLocation.getX(), 1e-8);
            assertEquals(57.0625, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocations = pixelLocator.getPixelLocation(94.1875, -89.9375);
            assertEquals(2, pixelLocations.length);
            assertEquals(0.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(0.5, pixelLocations[0].getY(), 1e-8);
            assertEquals(2880.5, pixelLocations[1].getX(), 1e-8);
            assertEquals(0.5, pixelLocations[1].getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(64.3125, -89.9375);
            assertEquals(2, pixelLocations.length);
            assertEquals(239.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(0.5, pixelLocations[0].getY(), 1e-8);
            assertEquals(3119.5, pixelLocations[1].getX(), 1e-8);
            assertEquals(0.5, pixelLocations[1].getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(-96.8125, -5.6875);
            assertEquals(1, pixelLocations.length);
            assertEquals(1528.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(674.5, pixelLocations[0].getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(-171.8125, 57.0625);
            assertEquals(1, pixelLocations.length);
            assertEquals(2128.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(1176.5, pixelLocations[0].getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File file = getWindsatFile();

        try {
            reader.open(file);

            final PixelLocator subScenePixelLocator = reader.getSubScenePixelLocator(null);// geometry is not used here tb 2022-09-29
            final PixelLocator pixelLocator = reader.getPixelLocator();

            assertSame(pixelLocator, subScenePixelLocator);
        } finally {
            reader.close();
        }
    }

    /*
    @Test
    public void testGetTimeLocator() throws IOException {
        final File file = getWindsatFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);
        } finally {
            reader.close();
        }
    }

     */

    @Test
    public void testGetVariables() throws IOException, InvalidRangeException {
        final File file = getWindsatFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(5, variables.size());

            Variable variable = variables.get(1);
            assertEquals("longitude", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
            Attribute attribute = variable.attributes().findAttribute("valid_min");
            assertEquals(0.f, attribute.getNumericValue().floatValue(), 1e-8);

            variable = variables.get(4);
            assertEquals("land_fraction_10", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
            attribute = variable.attributes().findAttribute("add_offset");
            assertEquals(0.f, attribute.getNumericValue().floatValue(), 1e-8);

        } finally {
            reader.close();
        }
    }

    private File getWindsatFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"windsat-coriolis", "v1.0", "2018", "04", "29", "RSS_WindSat_TB_L1C_r79285_20180429T174238_2018119_V08.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
