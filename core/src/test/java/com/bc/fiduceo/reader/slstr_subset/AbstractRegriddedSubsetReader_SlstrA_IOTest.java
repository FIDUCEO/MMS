package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.TestData;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public abstract class AbstractRegriddedSubsetReader_SlstrA_IOTest {
    private SlstrRegriddedSubsetReader reader;
    private ReaderContext readerContext;

    @Before
    public void setUp() throws Exception {
        readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));
    }

    private void initReaderNadir() throws IOException {
        final File slstrFile = getSlstrFile();
        reader = new SlstrRegriddedSubsetReader(readerContext, true);
        reader.open(slstrFile);
    }

    private void initReaderOblique() throws IOException {
        final File slstrFile = getSlstrFile();
        reader = new SlstrRegriddedSubsetReader(readerContext, false);
        reader.open(slstrFile);
    }

    protected abstract File getSlstrFile() throws IOException;

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void getVariables_nadir() throws IOException {
        initReaderNadir();
        final List<Variable> variables = reader.getVariables();

        assertEquals(32, variables.size());
        @SuppressWarnings("Convert2MethodRef")
        final List<String> names = variables.stream().map(v -> v.getShortName()).collect(Collectors.toList());
        assertThat(names, Matchers.containsInAnyOrder(
                "bayes_in", "bayes_orphan_in", "cloud_in", "confidence_in", "detector_in",
                "latitude_in", "latitude_tx", "longitude_in", "longitude_tx",
                "pixel_in", "pointing_in", "probability_cloud_dual_in", "probability_cloud_single_in",
                "S1_radiance_in", "S2_radiance_in", "S3_radiance_in", "S4_radiance_in", "S5_radiance_in",
                "S6_radiance_in", "S7_BT_in", "S7_exception_in", "S8_BT_in", "S8_exception_in", "S9_BT_in",
                "S9_exception_in",
                "sat_azimuth_tn", "sat_path_tn", "sat_zenith_tn",
                "scan_in",
                "solar_azimuth_tn", "solar_path_tn", "solar_zenith_tn"
        ));
    }

    @Test
    public void getVariables_oblique() throws IOException {
        initReaderOblique();
        final List<Variable> variables = reader.getVariables();

        assertEquals(32, variables.size());
        @SuppressWarnings("Convert2MethodRef")
        final List<String> names = variables.stream().map(v -> v.getShortName()).collect(Collectors.toList());
        assertThat(names, Matchers.containsInAnyOrder(
                "bayes_io", "bayes_orphan_io", "cloud_io", "confidence_io", "detector_io",
                "latitude_io", "latitude_tx", "longitude_io", "longitude_tx",
                "pixel_io", "pointing_io", "probability_cloud_dual_io", "probability_cloud_single_io",
                "S1_radiance_io", "S2_radiance_io", "S3_radiance_io", "S4_radiance_io", "S5_radiance_io",
                "S6_radiance_io", "S7_BT_io", "S7_exception_io", "S8_BT_io", "S8_exception_io", "S9_BT_io",
                "S9_exception_io",
                "sat_azimuth_to", "sat_path_to", "sat_zenith_to",
                "scan_io",
                "solar_azimuth_to", "solar_path_to", "solar_zenith_to"
        ));
    }

    @Test
    public void read_nadir() throws IOException {
        initReaderNadir();
        final AcquisitionInfo info = reader.read();

        assertNotNull(info);
        assertThat(info.getNodeType(), is(NodeType.ASCENDING));
        assertThat(info.getSensingStart(), is(notNullValue()));
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 12, 2, info.getSensingStart());
        assertThat(info.getSensingStop(), is(notNullValue()));
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 15, 2, info.getSensingStop());

        final Geometry boundingGeometry = info.getBoundingGeometry();
        assertThat(boundingGeometry, is(notNullValue()));
        assertThat(info.getTimeAxes(), is(notNullValue()));

        assertThat(boundingGeometry, is(instanceOf(Polygon.class)));
        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertThat(coordinates.length, is(23));
        final Point cornerUpperLeft = coordinates[0];
        final Point cornerLowerRight = coordinates[11];
        assertThat(cornerUpperLeft.getLon(), is(-3.6059465890532283));
        assertThat(cornerUpperLeft.getLat(), is(-25.83171070667771));
        assertThat(cornerLowerRight.getLon(), is(-20.57531176627247));
        assertThat(cornerLowerRight.getLat(), is(-18.60994342420135));
        assertThat(readerContext.getGeometryFactory().format(boundingGeometry), is(TestData.SLSTR_S3A_SUBSET_GEOMETRY_NADIR));

        final TimeAxis[] timeAxes = info.getTimeAxes();
        assertEquals(1, timeAxes.length);
        final TimeAxis timeAxis = timeAxes[0];

        Date time = timeAxis.getTime(cornerUpperLeft);
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 12, 2, time);

        time = timeAxis.getTime(cornerLowerRight);
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 15, 1, time);
    }

    @Test
    public void read_oblique() throws IOException {
        initReaderOblique();
        final AcquisitionInfo info = reader.read();

        assertNotNull(info);
        assertThat(info.getNodeType(), is(NodeType.ASCENDING));
        assertThat(info.getSensingStart(), is(notNullValue()));
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 12, 2, info.getSensingStart());
        assertThat(info.getSensingStop(), is(notNullValue()));
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 15, 2, info.getSensingStop());

        final Geometry boundingGeometry = info.getBoundingGeometry();
        assertThat(boundingGeometry, is(notNullValue()));
        assertThat(info.getTimeAxes(), is(notNullValue()));

        assertThat(boundingGeometry, is(instanceOf(Polygon.class)));
        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertThat(coordinates.length, is(19));
        final Point cornerUpperLeft = coordinates[0];
        final Point cornerLowerRight = coordinates[9];
        assertThat(cornerUpperLeft.getLon(), is(-8.878673644279557));
        assertThat(cornerUpperLeft.getLat(), is(-27.236764407917907));
        assertThat(cornerLowerRight.getLon(), is(-20.092489043445035));
        assertThat(cornerLowerRight.getLat(), is(-18.516825426872682));
        assertThat(readerContext.getGeometryFactory().format(boundingGeometry), is(TestData.SLSTR_S3A_SUBSET_GEOMETRY_OBLIQUE));

        final TimeAxis[] timeAxes = info.getTimeAxes();
        assertEquals(1, timeAxes.length);
        final TimeAxis timeAxis = timeAxes[0];

        Date time = timeAxis.getTime(cornerUpperLeft);
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 12, 2, time);

        time = timeAxis.getTime(cornerLowerRight);
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 15, 1, time);
    }
}
