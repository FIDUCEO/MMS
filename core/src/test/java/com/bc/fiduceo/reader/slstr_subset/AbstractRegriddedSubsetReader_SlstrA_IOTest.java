package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestData;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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

@RunWith(IOTestRunner.class)
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
        assertThat(cornerUpperLeft.getLon(), is(-3.605947));
        assertThat(cornerUpperLeft.getLat(), is(-25.831709));
        assertThat(cornerLowerRight.getLon(), is(-20.575312));
        assertThat(cornerLowerRight.getLat(), is(-18.609944000000002));
        assertThat(readerContext.getGeometryFactory().format(boundingGeometry), is(TestData.SLSTR_S3A_SUBSET_GEOMETRY_NADIR));

        final TimeAxis[] timeAxes = info.getTimeAxes();
        assertEquals(1, timeAxes.length);
        final TimeAxis timeAxis = timeAxes[0];

        Date time = timeAxis.getTime(cornerUpperLeft);
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 12, 2, time);

        time = timeAxis.getTime(cornerLowerRight);
        TestUtil.assertCorrectUTCDate(2020, 5, 22, 23, 15, 2, time);
    }

    @Test
    @Ignore
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
