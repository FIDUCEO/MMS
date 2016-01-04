package com.bc.fiduceo.reader;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Point;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author muhammad.bc
 */
@RunWith(IOTestRunner.class)
public class HDF_Reader_IO_Test {

    private static final DateFormat DATEFORMAT = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
    private HDF_Reader reader;
    private File testDataDirectory;

    @Before
    public void setUp() throws IOException {
        reader = new HDF_Reader();
        testDataDirectory = TestUtil.getTestDataDirectory();
    }

    @Test
    public void testOpenH5() throws IOException {
        File file = new File(testDataDirectory, "fiduceo_test_product_AMSU_B.h5");
        reader.open(file);
    }

    @Test
    public void testGetElementValues() throws IOException {
        File file = new File(testDataDirectory, "fiduceo_test_product_AMSU_B.h5");
        reader.open(file);
        AcquisitionInfo read = reader.read();
        Assert.assertNotNull(read.getSensingStart());
        Assert.assertNotNull(read.getSensingStop());
        List<Point> coordinates = read.getCoordinates();
        Assert.assertNotNull(coordinates);

    }

    @Test
    public void testTime() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        Date time = new SimpleDateFormat("HHmmssSSSSSS").parse("83730128");
        calendar.setTime(time);
        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.DAY_OF_YEAR, 347);

        String hour = String.valueOf(calendar.get(Calendar.HOUR));
        String min = String.valueOf(calendar.get(Calendar.MINUTE));
        String second = String.valueOf(calendar.get(Calendar.SECOND));
        String mlSecond = String.valueOf(calendar.get(Calendar.MILLISECOND));
        String dy = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String mn = String.valueOf(calendar.get(Calendar.MONTH));
        String yr = String.valueOf(calendar.get(Calendar.YEAR));
        Date date = DATEFORMAT.parse(yr + "-" + mn + "-" + dy + " " + hour + ":" + min + ":" + second + "." + mlSecond);
        Assert.assertNotNull(date);
    }

    @After
    public void testCloseH5() throws IOException {
        File file = new File(testDataDirectory, "fiduceo_test_product_AMSU_B.h5");
        reader.open(file);
        reader.close();
    }
}
