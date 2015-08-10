
package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.io.WKTReader;
import org.esa.snap.framework.datamodel.ProductData;
import org.jdom2.Content;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AIRS_L1B_ReaderTest {

    private SatelliteObservation observation;
    private AIRS_L1B_Reader airsL1bReader;

    @Before
    public void SetUp() throws IOException, ParseException {
        final URL resourceUrl = AIRS_L1B_ReaderTest.class.getResource("AIRS.2015.08.03.001.L1B.AMSU_Rad.v5.0.14.0.R15214205337.hdf");
        assertNotNull(resourceUrl);
        final File airsL1bFile = new File(resourceUrl.getFile());
        airsL1bReader = new AIRS_L1B_Reader();
        airsL1bReader.open(airsL1bFile);
        observation = airsL1bReader.read();

    }

    @After
    public void endTest() throws IOException {
        airsL1bReader.close();
    }

    @Test
    public void testReadSatelliteObservation() throws IOException, ParseException {
        assertNotNull(observation);
        final Date startTime = observation.getStartTime();
        final Date stopTime = observation.getStopTime();
        assertNotNull(startTime);
        assertNotNull(stopTime);
        DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        final Date expectedStart = dateFormat.parse("2015-08-03 00:05:22.000000Z");
        final Date expectedStop = dateFormat.parse("2015-08-03 00:11:21.999999Z");
        assertEquals(expectedStart.getTime(), startTime.getTime());
        assertEquals(expectedStop.getTime(), stopTime.getTime());
    }


    @Test
    public void testGetEosElement() throws IOException {
        final Element eosElement = AIRS_L1B_Reader.getEosElement(EOS_CORE_META);
        assertNotNull(eosElement);
        List<Content> content = eosElement.getContent();
        assertEquals(1, content.size());
        Content subContent = content.get(0);
        assertNotNull(subContent);
        Element inventoryMetadataElement = eosElement.getChild("INVENTORYMETADATA");
        content = inventoryMetadataElement.getContent();
        assertEquals(10, content.size());
    }

    @Test
    public void testGeoCoordinate() throws IOException, ParseException, com.vividsolutions.jts.io.ParseException {
        Geometry geometry = observation.getGeoBounds();
        assertNotNull(geometry);
        WKTReader wktReader = new WKTReader(new GeometryFactory());
        MultiPoint multiPoint = (MultiPoint) wktReader.read(MULTIPOINT);
        assertTrue(geometry.contains(multiPoint));
    }

    @Test
    public void testGetEosMetadata_groupNotPresent() throws IOException {
        final Group mockGroup = mock(Group.class);
        when(mockGroup.findVariable("whatever")).thenReturn(null);
        final String metadata = AIRS_L1B_Reader.getEosMetadata("whatever", mockGroup);
        assertNull(metadata);
    }


    @Test
    public void testLatIndex() {

    }

    @Test
    public void testElementValue() throws IOException {
        final Element mockElement = mock(Element.class);
        when(mockElement.toString()).thenReturn("2015-08-03");

        final String elementValue = AIRS_L1B_Reader.getElementValue(mockElement, "RANGEENDINGDATE");
        assertNotNull(elementValue);
    }


    @Test
    public void testGetEosMetadata_readMetadata() throws IOException {
        final String expected = "the_text_we_like_to_read";
        final Group mockGroup = mock(Group.class);
        final Variable mockVariable = mock(Variable.class);

        final Array mockArray = mock(Array.class);
        when(mockArray.toString()).thenReturn(expected);

        when(mockVariable.read()).thenReturn(mockArray);
        when(mockGroup.findVariable("with_data")).thenReturn(mockVariable);

        final String metadata = AIRS_L1B_Reader.getEosMetadata("with_data", mockGroup);
        assertNotNull(metadata);
        assertEquals(expected, metadata);
    }


    private static final String MULTIPOINT = "MULTIPOINT ((12.115337647192797 -167.14445722150867), (12.279422414280331 -166.1730278143979), (12.418342761336236 -165.34491307626735), (12.538454687483574 -164.6261943125001), (12.64558042700777 -163.98292693171737), (12.741534952934806 -163.40649453325145), (12.828658973879833 -162.8832597649752), (12.90958056597399 -162.39683002343054), (12.9852789434594 -161.94153074587743), (13.057308306143742 -161.50715746033566), (13.124866278824033 -161.100452164287), (13.190398263241088 -160.70397913632135), (13.253743696550467 -160.31935241365667), (13.315677944081953 -159.94095951942776), (13.377047004594397 -159.56240229123568), (13.436706662069376 -159.19265030647028), (13.496430541126564 -158.81788208658816), (13.556043659558377 -158.43923494622842), (13.616224056308125 -158.05098699634513), (13.67661657960202 -157.6556993669512), (13.738358854067911 -157.24343558040843), (13.801683762942424 -156.811428851981), (13.867331462550466 -156.35223346234997), (13.93617989159244 -155.85657534651386), (14.007293135417145 -155.33086325881328), (14.083472777091306 -154.74718921639902), (14.164771646253456 -154.10089201616628), (14.25326152040048 -153.36642252521602), (14.349660835844801 -152.52785462245453), (14.457717633993736 -151.5331749119938), (12.59218377951515 -167.26481140363063), (14.939025388549755 -151.62291582496854), (13.06898909272133 -167.38615193712403), (15.42030627612875 -151.71233659788544), (13.545424203422888 -167.5083981293018), (15.901471884001388 -151.80134108966604), (14.021764653386432 -167.63148361076378), (16.382884225227368 -151.8900377966074), (14.498055550797586 -167.75559849818796), (16.864310692721695 -151.9784405794427), (14.974099813776323 -167.8807331031198), (17.345604743760823 -152.06650535950993), (15.449947372734886 -168.00678308421558), (17.82692265655212 -152.15416682411234), (15.92572437459079 -168.13382558718357), (18.30840676225526 -152.24160661916514), (16.400924046960583 -168.264394165994), (18.789787013388185 -152.32873853661278), (16.876250121314914 -168.3936228671475), (19.27117620524501 -152.4155312510733), (17.35177319823384 -168.52140407138032), (19.752681975126666 -152.50197705079913), (17.826844811300536 -168.65273530924344), (20.234188963104703 -152.588313707934), (18.301281060201283 -168.78771446219443), (20.715562473707173 -152.67425444759073), (18.77628013209512 -168.9188957089004), (21.196955777896168 -152.7598545379425), (19.250662253077273 -169.05359211370353), (21.67850582735826 -152.84520919425668), (19.724990359677673 -169.18958581682296), (22.16010663394288 -152.93041231406852), (20.19901665160412 -169.32683963797652), (22.64156561993826 -153.0152693738749), (20.672385072297978 -169.46764126185707), (23.1231074508905 -153.09977952563165), (21.146510081134227 -169.6047616128064), (23.604929794480036 -153.1815220849084), (21.62002239734624 -169.7457472101792), (24.086308081637444 -153.26817911650306), (22.093215157709157 -169.88798993254179), (24.56784917034263 -153.3519459323218), (22.565772359201365 -170.0340171063346), (25.049578784011377 -153.43548228389466), (23.03905670119288 -170.17640021126599), (25.5311874830252 -153.51884460201086), (23.51117644716721 -170.32530498567635), (26.01299578883174 -153.59932438347053), (23.98339646647534 -170.4730170468171), (26.494416209104763 -153.68472105975053), (24.455439692836173 -170.62209588981182), (26.97614306634137 -153.76735607022815), (24.927845881964185 -170.77029824715038), (27.457851312892224 -153.84982621150783), (25.39940941633738 -170.92247683141954), (27.939434091447648 -153.93198340482598), (25.870700390276347 -171.07612754693096), (28.420958975821346 -154.01382016691775), (26.341774888628407 -171.23135467695727), (28.902654665660663 -154.09564030862734), (26.81257265575848 -171.38831456641057), (29.38438115487697 -154.17453902519102), (27.283026236683604 -171.54686144641073), (29.8657940266665 -154.25857874349714), (27.753140455189232 -171.70699605174556), (30.34735146245469 -154.33965305028275), (28.222613608440668 -171.87154482886788), (30.829096838393436 -154.42078168018227), (28.69291404139051 -172.0328644804597), (31.310733924008478 -154.49879756467277), (29.162140412110194 -172.1983000207946), (31.79208533265723 -154.5821016348156), (29.630587304190502 -172.36819312443575), (32.273594918105765 -154.6625558579555), (30.099306276396508 -172.5375133144322), (32.755126636754696 -154.74291329890573), (30.56814986145276 -172.7060843388558), (33.23647913841908 -154.82297913729664), (31.036074890960997 -172.8791334218495), (33.71784589442994 -154.90287384906094), (31.503763113972074 -173.0542781976243), (34.201037460553756 -154.94852823116736), (31.971170931680167 -173.23150034415394), (34.68070844814089 -155.06233296781772), (32.438094149560996 -173.41077442358468), (35.16201524802715 -155.1416803055775), (32.90472542332997 -173.5921218067924), (33.145454204522956 -172.4773299907491), (33.34407996180458 -171.52547125004025), (33.512767371973496 -170.6931522740924), (33.66017283151052 -169.9464526366916), (33.78916863468057 -169.2787331015878), (33.90445382671261 -168.6699902459261), (34.00996404468456 -168.10180371620942), (34.106918130550895 -167.5702741344914), (34.19748418781919 -167.06452977370563), (34.28162850478243 -166.58712305434253), (34.36199412926237 -166.1221311128823), (34.43861715006958 -165.67042484593026), (34.512546244933134 -165.22546384215056), (34.58459125170206 -164.78168365499982), (34.65428175554536 -164.34287826298882), (34.72262175342894 -163.90151635747785), (34.78987742007406 -163.45507192828515), (34.85702095876677 -162.99474277369956), (34.92429478448506 -162.51698630366494), (34.98975986081048 -162.03767213761958), (35.05674195120833 -161.52479966631796), (35.12461153309757 -160.98026268214568), (35.19420691619263 -160.39055881046505), (35.26420937800368 -159.76292477111863), (35.336563212017836 -159.06676296976588), (35.410610141952276 -158.29498956978514), (35.487010576550986 -157.41687255374572), (35.564455266536505 -156.4154886637448), (35.643407945336584 -155.22100159384635))";

    private static final String EOS_CORE_META = "\n" +
            "GROUP=INVENTORYMETADATA\n" +
            "  GROUPTYPE=MASTERGROUP\n" +
            "\n" +
            "  GROUP=ECSDATAGRANULE\n" +
            "\n" +
            "    OBJECT=LOCALGRANULEID\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=\"AIRS.2015.08.03.001.L1B.AMSU_Rad.v5.0.14.0.R15214205337.hdf\"\n" +
            "    END_OBJECT=LOCALGRANULEID\n" +
            "\n" +
            "    OBJECT=PRODUCTIONDATETIME\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=\"2015-08-03T00:53:37.000Z\"\n" +
            "    END_OBJECT=PRODUCTIONDATETIME\n" +
            "\n" +
            "    OBJECT=DAYNIGHTFLAG\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=\"Day\"\n" +
            "    END_OBJECT=DAYNIGHTFLAG\n" +
            "\n" +
            "    OBJECT=LOCALVERSIONID\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=\"Unspecified\"\n" +
            "    END_OBJECT=LOCALVERSIONID\n" +
            "\n" +
            "  END_GROUP=ECSDATAGRANULE\n" +
            "\n" +
            "  GROUP=MEASUREDPARAMETER\n" +
            "\n" +
            "    OBJECT=MEASUREDPARAMETERCONTAINER\n" +
            "      CLASS=\"1\"\n" +
            "\n" +
            "      GROUP=QAFLAGS\n" +
            "        CLASS=\"1\"\n" +
            "\n" +
            "        OBJECT=AUTOMATICQUALITYFLAGEXPLANATION\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"1\"\n" +
            "          VALUE=\"Based on percentage of product that is good. Suspect used where true quality is not known.\"\n" +
            "        END_OBJECT=AUTOMATICQUALITYFLAGEXPLANATION\n" +
            "\n" +
            "        OBJECT=AUTOMATICQUALITYFLAG\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"1\"\n" +
            "          VALUE=\"Suspect\"\n" +
            "        END_OBJECT=AUTOMATICQUALITYFLAG\n" +
            "\n" +
            "      END_GROUP=QAFLAGS\n" +
            "\n" +
            "      GROUP=QASTATS\n" +
            "        CLASS=\"1\"\n" +
            "\n" +
            "        OBJECT=QAPERCENTMISSINGDATA\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"1\"\n" +
            "          VALUE=0\n" +
            "        END_OBJECT=QAPERCENTMISSINGDATA\n" +
            "\n" +
            "      END_GROUP=QASTATS\n" +
            "\n" +
            "      OBJECT=PARAMETERNAME\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"MW Brightness Temperatures\"\n" +
            "      END_OBJECT=PARAMETERNAME\n" +
            "\n" +
            "    END_OBJECT=MEASUREDPARAMETERCONTAINER\n" +
            "\n" +
            "  END_GROUP=MEASUREDPARAMETER\n" +
            "\n" +
            "  GROUP=ORBITCALCULATEDSPATIALDOMAIN\n" +
            "\n" +
            "    OBJECT=ORBITCALCULATEDSPATIALDOMAINCONTAINER\n" +
            "      CLASS=\"1\"\n" +
            "\n" +
            "      OBJECT=STARTORBITNUMBER\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=70464\n" +
            "      END_OBJECT=STARTORBITNUMBER\n" +
            "\n" +
            "      OBJECT=EQUATORCROSSINGDATE\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"2015-08-03\"\n" +
            "      END_OBJECT=EQUATORCROSSINGDATE\n" +
            "\n" +
            "      OBJECT=EQUATORCROSSINGTIME\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"00:51:09.984192Z\"\n" +
            "      END_OBJECT=EQUATORCROSSINGTIME\n" +
            "\n" +
            "      OBJECT=EQUATORCROSSINGLONGITUDE\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=11.2007436752319\n" +
            "      END_OBJECT=EQUATORCROSSINGLONGITUDE\n" +
            "\n" +
            "      OBJECT=STOPORBITNUMBER\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=70464\n" +
            "      END_OBJECT=STOPORBITNUMBER\n" +
            "\n" +
            "    END_OBJECT=ORBITCALCULATEDSPATIALDOMAINCONTAINER\n" +
            "\n" +
            "  END_GROUP=ORBITCALCULATEDSPATIALDOMAIN\n" +
            "\n" +
            "  GROUP=COLLECTIONDESCRIPTIONCLASS\n" +
            "\n" +
            "    OBJECT=VERSIONID\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=5\n" +
            "    END_OBJECT=VERSIONID\n" +
            "\n" +
            "    OBJECT=SHORTNAME\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=\"AIRABRAD_NRT\"\n" +
            "    END_OBJECT=SHORTNAME\n" +
            "\n" +
            "  END_GROUP=COLLECTIONDESCRIPTIONCLASS\n" +
            "\n" +
            "  GROUP=INPUTGRANULE\n" +
            "\n" +
            "    OBJECT=INPUTPOINTER\n" +
            "      NUM_VAL=50\n" +
            "      VALUE=(\"LGID:AIRAASCI_NRT:005:AIRS.2015.08.02.240.L1A.AMSU.v5.0.14.0.R15214202902.hdf\", \"LGID:AIRAASCI_NRT:005:AIRS.2015.08.03.001.L1A.AMSU.v5.0.14.0.R15214202902.hdf\", \"L1B.AMSU_AncMain.v3.4.0.anc\", \"L2.tuning.amsu.v9.6.0.anc\", \"L1B.AMSU_SLInterp.v3.0.0.anc\")\n" +
            "    END_OBJECT=INPUTPOINTER\n" +
            "\n" +
            "  END_GROUP=INPUTGRANULE\n" +
            "\n" +
            "  GROUP=SPATIALDOMAINCONTAINER\n" +
            "\n" +
            "    GROUP=HORIZONTALSPATIALDOMAINCONTAINER\n" +
            "\n" +
            "      GROUP=BOUNDINGRECTANGLE\n" +
            "\n" +
            "        OBJECT=EASTBOUNDINGCOORDINATE\n" +
            "          NUM_VAL=1\n" +
            "          VALUE=-151.533172607422\n" +
            "        END_OBJECT=EASTBOUNDINGCOORDINATE\n" +
            "\n" +
            "        OBJECT=WESTBOUNDINGCOORDINATE\n" +
            "          NUM_VAL=1\n" +
            "          VALUE=-173.59211730957\n" +
            "        END_OBJECT=WESTBOUNDINGCOORDINATE\n" +
            "\n" +
            "        OBJECT=SOUTHBOUNDINGCOORDINATE\n" +
            "          NUM_VAL=1\n" +
            "          VALUE=12.1153373718262\n" +
            "        END_OBJECT=SOUTHBOUNDINGCOORDINATE\n" +
            "\n" +
            "        OBJECT=NORTHBOUNDINGCOORDINATE\n" +
            "          NUM_VAL=1\n" +
            "          VALUE=35.6434097290039\n" +
            "        END_OBJECT=NORTHBOUNDINGCOORDINATE\n" +
            "\n" +
            "      END_GROUP=BOUNDINGRECTANGLE\n" +
            "\n" +
            "      GROUP=ZONEIDENTIFIERCLASS\n" +
            "\n" +
            "        OBJECT=ZONEIDENTIFIER\n" +
            "          NUM_VAL=1\n" +
            "          VALUE=\"Other Grid System\"\n" +
            "        END_OBJECT=ZONEIDENTIFIER\n" +
            "\n" +
            "      END_GROUP=ZONEIDENTIFIERCLASS\n" +
            "\n" +
            "    END_GROUP=HORIZONTALSPATIALDOMAINCONTAINER\n" +
            "\n" +
            "  END_GROUP=SPATIALDOMAINCONTAINER\n" +
            "\n" +
            "  GROUP=RANGEDATETIME\n" +
            "\n" +
            "    OBJECT=RANGEENDINGDATE\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=\"2015-08-03\"\n" +
            "    END_OBJECT=RANGEENDINGDATE\n" +
            "\n" +
            "    OBJECT=RANGEENDINGTIME\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=\"00:11:21.999999Z\"\n" +
            "    END_OBJECT=RANGEENDINGTIME\n" +
            "\n" +
            "    OBJECT=RANGEBEGINNINGDATE\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=\"2015-08-03\"\n" +
            "    END_OBJECT=RANGEBEGINNINGDATE\n" +
            "\n" +
            "    OBJECT=RANGEBEGINNINGTIME\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=\"00:05:22.000000Z\"\n" +
            "    END_OBJECT=RANGEBEGINNINGTIME\n" +
            "\n" +
            "  END_GROUP=RANGEDATETIME\n" +
            "\n" +
            "  GROUP=PGEVERSIONCLASS\n" +
            "\n" +
            "    OBJECT=PGEVERSION\n" +
            "      NUM_VAL=1\n" +
            "      VALUE=\"5.0.14.0\"\n" +
            "    END_OBJECT=PGEVERSION\n" +
            "\n" +
            "  END_GROUP=PGEVERSIONCLASS\n" +
            "\n" +
            "  GROUP=ASSOCIATEDPLATFORMINSTRUMENTSENSOR\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"1\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 3\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"2\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"2\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 4\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"2\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"2\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"2\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"3\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"3\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 5\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"3\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"3\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"3\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"4\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"4\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 6\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"4\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"4\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"4\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"5\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"5\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 7\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"5\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"5\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"5\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"6\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"6\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 8\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"6\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"6\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"6\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"7\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"7\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 9\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"7\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"7\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"7\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"8\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"8\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 10\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"8\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"8\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"8\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"9\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"9\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 11\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"9\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"9\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"9\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"10\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"10\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 12\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"10\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"10\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"10\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"11\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"11\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 13\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"11\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"11\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"11\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"12\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"12\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 14\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"12\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"12\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"12\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"13\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"13\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 15\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"13\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"13\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"13\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"14\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"14\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 1\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"14\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"14\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"14\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "    OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "      CLASS=\"15\"\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "        CLASS=\"15\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A Channel 2\"\n" +
            "      END_OBJECT=ASSOCIATEDSENSORSHORTNAME\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "        CLASS=\"15\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Aqua\"\n" +
            "      END_OBJECT=ASSOCIATEDPLATFORMSHORTNAME\n" +
            "\n" +
            "      OBJECT=OPERATIONMODE\n" +
            "        CLASS=\"15\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"Normal\"\n" +
            "      END_OBJECT=OPERATIONMODE\n" +
            "\n" +
            "      OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "        CLASS=\"15\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AMSU-A\"\n" +
            "      END_OBJECT=ASSOCIATEDINSTRUMENTSHORTNAME\n" +
            "\n" +
            "    END_OBJECT=ASSOCIATEDPLATFORMINSTRUMENTSENSORCONTAINER\n" +
            "\n" +
            "  END_GROUP=ASSOCIATEDPLATFORMINSTRUMENTSENSOR\n" +
            "\n" +
            "  GROUP=ADDITIONALATTRIBUTES\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"1\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"1\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NumBadData\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"1\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"1\"\n" +
            "          VALUE=\"2700\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"2\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"2\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NumSpecialData\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"2\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"2\"\n" +
            "          VALUE=\"0\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"3\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"3\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NumProcessData\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"3\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"3\"\n" +
            "          VALUE=\"17550\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"4\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"4\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NumMissingData\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"4\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"4\"\n" +
            "          VALUE=\"0\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"5\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"5\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NumTotalData\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"5\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"5\"\n" +
            "          VALUE=\"20250\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"6\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"6\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NumLandSurface\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"6\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"6\"\n" +
            "          VALUE=\"0\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"7\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"7\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NumOceanSurface\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"7\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"7\"\n" +
            "          VALUE=\"1335\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"8\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"8\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NumFpe\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"8\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"8\"\n" +
            "          VALUE=\"0\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"9\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"9\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"ScanLineCount\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"9\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"9\"\n" +
            "          VALUE=\"45\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"10\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"10\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AIRSGranuleNumber\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"10\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"10\"\n" +
            "          VALUE=\"1\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"11\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"11\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"OrbitPath\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"11\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"11\"\n" +
            "          VALUE=\"169\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"12\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"12\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"SP_STARTING_PATH\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"12\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"12\"\n" +
            "          VALUE=\"169\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"13\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"13\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"SP_STARTING_ROW\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"13\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"13\"\n" +
            "          VALUE=\"196\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"14\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"14\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"SP_ENDING_ROW\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"14\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"14\"\n" +
            "          VALUE=\"210\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"15\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"15\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NumGeoQA\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"15\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"15\"\n" +
            "          VALUE=\"1\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"16\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"16\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NumSunGlint\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"16\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"16\"\n" +
            "          VALUE=\"41\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"17\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"17\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"LonGranuleCen\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"17\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"17\"\n" +
            "          VALUE=\"-162\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"18\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"18\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"LatGranuleCen\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"18\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"18\"\n" +
            "          VALUE=\"24\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"19\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"19\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"LocTimeGranuleCen\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"19\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"19\"\n" +
            "          VALUE=\"795\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"20\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"20\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"NodeType\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"20\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"20\"\n" +
            "          VALUE=\"Ascending\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"21\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"21\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"MoonInViewMWCount\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"21\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"21\"\n" +
            "          VALUE=\"0\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"22\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"22\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"ProductGenerationFacility\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"22\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"22\"\n" +
            "          VALUE=\"R\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"23\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"23\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"AIRSRunTag\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"23\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"23\"\n" +
            "          VALUE=\"15214205337\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "    OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "      CLASS=\"24\"\n" +
            "\n" +
            "      OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "        CLASS=\"24\"\n" +
            "        NUM_VAL=1\n" +
            "        VALUE=\"ProductGenerationHostInformation\"\n" +
            "      END_OBJECT=ADDITIONALATTRIBUTENAME\n" +
            "\n" +
            "      GROUP=INFORMATIONCONTENT\n" +
            "        CLASS=\"24\"\n" +
            "\n" +
            "        OBJECT=PARAMETERVALUE\n" +
            "          NUM_VAL=1\n" +
            "          CLASS=\"24\"\n" +
            "          VALUE=\"airspro7.gesdisc.eosdis.nasa.gov Linux 2.6.32-504.12.2.el6.x86_64 #1_SMP_Wed_Mar_11_22:03:14_UTC_2015 x86_64\"\n" +
            "        END_OBJECT=PARAMETERVALUE\n" +
            "\n" +
            "      END_GROUP=INFORMATIONCONTENT\n" +
            "\n" +
            "    END_OBJECT=ADDITIONALATTRIBUTESCONTAINER\n" +
            "\n" +
            "  END_GROUP=ADDITIONALATTRIBUTES\n" +
            "\n" +
            "END_GROUP=INVENTORYMETADATA\n" +
            "\n" +
            "END\n";
}
