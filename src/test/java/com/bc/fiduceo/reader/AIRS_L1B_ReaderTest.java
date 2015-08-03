package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AIRS_L1B_ReaderTest {

    @Test
    public void testReadSatelliteObservation() throws IOException {
        final URL resourceUrl = AIRS_L1B_ReaderTest.class.getResource("AIRS.2015.08.03.001.L1B.AMSU_Rad.v5.0.14.0.R15214205337.hdf");
        assertNotNull(resourceUrl);

        final File airsL1bFile = new File(resourceUrl.getFile());

        final Reader airsL1bReader = new AIRS_L1B_Reader();

        try {
            airsL1bReader.open(airsL1bFile);
            final SatelliteObservation observation = airsL1bReader.read();
            assertNotNull(observation);
            final Date startTime = observation.getStartTime();
            assertNotNull(startTime);
            final Date expectedStart = createDate(2015, 8, 3, 0, 5, 22, 0);

            // @todo 1 TB/MB make this work :-) 2015-08-03
            //assertEquals(expectedStart.getTime(), startTime.getTime());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            airsL1bReader.close();
        }
    }

    private Date createDate(int year, int month, int day, int hour, int minute, int seconds, int millis) {
        final Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, seconds);
        cal.set(Calendar.MILLISECOND, millis);

        return cal.getTime();
    }
}
