package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.snap.framework.datamodel.ProductData;
import org.jdom2.Content;
import org.jdom2.Element;
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

    @Test
    public void testReadSatelliteObservation() throws IOException, ParseException {
        final URL resourceUrl = AIRS_L1B_ReaderTest.class.getResource("AIRS.2015.08.03.001.L1B.AMSU_Rad.v5.0.14.0.R15214205337.hdf");
        assertNotNull(resourceUrl);
        final File airsL1bFile = new File(resourceUrl.getFile());
        final AIRS_L1B_Reader airsL1bReader = new AIRS_L1B_Reader();

        try {
            airsL1bReader.open(airsL1bFile);
            final SatelliteObservation observation = airsL1bReader.read();
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

            assertNotNull(observation.getGeometry());
        } finally {
            airsL1bReader.close();
        }
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
    public void testGetEosMetadata_groupNotPresent() throws IOException {
        final Group mockGroup = mock(Group.class);
        when(mockGroup.findVariable("whatever")).thenReturn(null);

        final String metadata = AIRS_L1B_Reader.getEosMetadata("whatever", mockGroup);
        assertNull(metadata);
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
