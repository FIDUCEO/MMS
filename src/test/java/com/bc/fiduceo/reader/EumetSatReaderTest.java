package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import org.esa.snap.framework.datamodel.ProductData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author muhammad.bc on 8/17/2015.
 */
public class EumetSatReaderTest {

    private static String POLYGON = "POLYGON ((55.87028884887695 -35.308406829833984, 56.83158493041992 -35.44773864746094, 59.47855758666992 -34.846309661865234, 60.09541702270508 -34.95961380004883, 61.90144348144531 -34.456600189208984, 62.370750427246094 -34.55516052246094, 63.78819274902344 -34.10406494140625, 64.18729400634766 -34.194393157958984, 65.41410827636719 -33.763938903808594, 65.78119659423828 -33.847591400146484, 66.93694305419922 -33.41936492919922, 67.30072784423828 -33.499610900878906, 68.47260284423828 -33.04697036743164, 68.85814666748047 -33.12163162231445, 70.14606475830078 -32.61613082885742, 70.58624267578125 -32.68478012084961, 72.13375091552734 -32.073265075683594, 72.68500518798828 -32.12751770019531, 74.77808380126953 -31.29849624633789, 75.56712341308594 -31.312149047851562, 76.86502838134766 -30.924989700317383, 75.78663635253906 -28.206226348876953, 74.775634765625 -25.47521209716797, 73.82406616210938 -22.73427963256836, 72.92638397216797 -19.983680725097656, 72.07574462890625 -17.225391387939453, 71.26689910888672 -14.459179878234863, 70.49500274658203 -11.689537048339844, 69.760498046875 -8.9143705368042, 69.05183410644531 -6.133365154266357, 68.37297821044922 -3.3495399951934814, 67.72008514404297 -0.5597879886627197, 67.09012603759766 2.2251479625701904, 66.48159790039062 5.0208048820495605, 65.89221954345703 7.813554763793945, 65.32039642333984 10.605917930603027, 64.76741790771484 13.40539264678955, 64.22606658935547 16.200977325439453, 63.70378875732422 18.997163772583008, 63.19192886352539 21.792984008789062, 62.69246292114258 24.588891983032227, 62.2016487121582 27.384212493896484, 61.72381591796875 30.178367614746094, 61.25872039794922 32.97134780883789, 60.8322868347168 35.55801010131836, 59.96375274658203 35.18706130981445, 57.25285720825195 35.03995132446289, 56.72904968261719 34.739097595214844, 54.85454177856445 34.62086486816406, 54.47588348388672 34.35828399658203, 52.98577117919922 34.25475311279297, 52.67864990234375 34.011497497558594, 51.375606536865234 33.90918731689453, 51.101776123046875 33.675472259521484, 49.86803436279297 33.56570053100586, 49.603233337402344 33.33042907714844, 48.347591400146484 33.20153045654297, 48.068660736083984 32.95449447631836, 46.69029235839844 32.78884506225586, 46.372135162353516 32.51240921020508, 44.723445892333984 32.27332305908203, 44.31479263305664 31.94959259033203, 42.10429382324219 31.552936553955078, 41.50112533569336 31.1207275390625, 40.24205780029297 30.729732513427734, 41.301063537597656 27.99849510192871, 42.296573638916016 25.253313064575195, 43.231632232666016 22.500795364379883, 44.11351013183594 19.741939544677734, 44.950252532958984 16.974531173706055, 45.7451171875 14.201818466186523, 46.50136947631836 11.422638893127441, 47.229793548583984 8.638200759887695, 47.92136001586914 5.849494934082031, 48.583412170410156 3.0620439052581787, 49.22612762451172 0.2703750133514404, 49.846099853515625 -2.523092031478882, 50.4420051574707 -5.318364143371582, 51.01765441894531 -8.113835334777832, 51.57957077026367 -10.910110473632812, 52.11975860595703 -13.70463752746582, 52.64958953857422 -16.499055862426758, 53.16362762451172 -19.29591178894043, 53.66362762451172 -22.087858200073242, 54.150047302246094 -24.880748748779297, 54.62488555908203 -27.67125129699707, 55.08891677856445 -30.45973777770996, 55.542598724365234 -33.24894714355469, 55.87028884887695 -35.308406829833984))";
    SatelliteObservation satelliteObservation;
    private final ThreadLocal<EumetSatReader> eumetSatReader = new ThreadLocal<>();
    private boolean testDataPresent = true;
    private String testFilenName;

    @Before
    public void setUp() throws IOException, ParseException, com.vividsolutions.jts.io.ParseException {
        testFilenName = "W_XX-EUMETSAT-Darmstadt,HYPERSPECT+SOUNDING,MetOpA+IASI_C_EUMP_20130528172543_34281_eps_o_l1.nc";
        final URL urlEumetSat = EumetSatReaderTest.class.getResource(testFilenName);
        if (urlEumetSat == null) {
            testDataPresent = false;
            return;
        }
        File file = new File(urlEumetSat.getPath());
        eumetSatReader.set(new EumetSatReader(6, 6));
        assertNotNull(eumetSatReader.get());
        eumetSatReader.get().open(file);
        satelliteObservation = eumetSatReader.get().read();

    }

    @Test
    public void testGeoCoordinate() throws com.vividsolutions.jts.io.ParseException {
        if (!testDataPresent) {
            System.err.println("Test file: '" + testFilenName + "' not found. Skipping test");
            return;
        }
        assertNotNull(satelliteObservation);
        Geometry geometryTest = satelliteObservation.getGeoBounds();
        assertNotNull(geometryTest);

        WKTReader wktReader = new WKTReader(new GeometryFactory());
        Polygon read = (Polygon) wktReader.read(POLYGON);
        assertTrue(geometryTest.contains(read));
    }

    @Test
    public void testDateTime() {
        if (!testDataPresent) {
            System.err.println("Test file: '" + testFilenName + "' not found. Skipping test");
            return;
        }
        assertNotNull(satelliteObservation);
        Calendar calendar = ProductData.UTC.createCalendar();

        //--- Start date.
        Date date = satelliteObservation.getStartTime();
        calendar.setTime(date);
        Assert.assertEquals(2013, calendar.get(Calendar.YEAR));
        Assert.assertEquals(28, calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(05 - 1, calendar.get(Calendar.MONTH));
        Assert.assertEquals(17, calendar.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(25, calendar.get(Calendar.MINUTE));
        Assert.assertEquals(43, calendar.get(Calendar.SECOND));

        //--- End date.
        date = satelliteObservation.getStopTime();
        calendar.setTime(date);
        Assert.assertEquals(2013, calendar.get(Calendar.YEAR));
        Assert.assertEquals(28, calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(05 - 1, calendar.get(Calendar.MONTH));
        Assert.assertEquals(17, calendar.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(44, calendar.get(Calendar.MINUTE));
        Assert.assertEquals(54, calendar.get(Calendar.SECOND));
    }

}
