package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.esa.s3tbx.dataio.s3.Manifest;
import org.esa.s3tbx.dataio.s3.XfduManifest;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@SuppressWarnings("resource")
public class SlstrRegriddedSubsetReaderTest {

    @Test
    public void testExtractName() {
        assertEquals("name.nc", SlstrRegriddedSubsetReader.extractName("name.nc"));
        assertEquals("name.nc", SlstrRegriddedSubsetReader.extractName("egal\\welcher\\pfad\\name.nc"));
        assertEquals("name.nc", SlstrRegriddedSubsetReader.extractName("egal/welcher/pfad/name.nc"));
    }

    @Test
    public void testGetRegEx() {
        final SlstrRegriddedSubsetReader reader = new SlstrRegriddedSubsetReader(null); // this test does not require a context class tb 2022-08-19

        final String expected = "S3[AB]_SL_1_RBT____(\\d{8}T\\d{6}_){3}\\d{4}(_\\d{3}){2}_.*_\\d{3}(.SEN3|.zip)";
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher("S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.SEN3");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("S3A_SL_1_RBT____20161112T120208_20161112T120508_20181003T074857_0179_011_023______LR1_R_NT_003.zip");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("S3A_SL_1_RBT____20220809T000124_20220809T000424_20220810T075621_0179_088_258_5400_PS1_O_NT_004.SEN3");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("S3B_SL_1_RBT____20191117T231801_20191117T232101_20191119T035119_0180_032_172_5400_LN2_O_NT_003.SEN3");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.zip");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("FIDUCEO_FCDR_L1C_AVHRR_N19ALL_20110705055721_20110705073927_EASY_v0.2Bet_fv2.0.0.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        final Reader reader = new SlstrRegriddedSubsetReader(new ReaderContext());

        assertEquals("longitude_in", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final Reader reader = new SlstrRegriddedSubsetReader(new ReaderContext());

        assertEquals("latitude_in", reader.getLatitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        final SlstrRegriddedSubsetReader reader = new SlstrRegriddedSubsetReader(null);

        int[] ymd = reader.extractYearMonthDayFromFilename("S3B_SL_1_RBT____20191117T231801_20191117T232101_20191119T035119_0180_032_172_5400_LN2_O_NT_003.SEN3");
        assertEquals(3, ymd.length);
        assertEquals(2019, ymd[0]);
        assertEquals(11, ymd[1]);
        assertEquals(17, ymd[2]);

        ymd = reader.extractYearMonthDayFromFilename("S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.zip");
        assertEquals(3, ymd.length);
        assertEquals(2020, ymd[0]);
        assertEquals(5, ymd[1]);
        assertEquals(22, ymd[2]);
    }

    @Test
    public void testGetRasterInfo() throws ParserConfigurationException, IOException, SAXException {
        final String manifestXML = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>" +
                "<xfdu:XFDU xmlns:xfdu=\"urn:ccsds:schema:xfdu:1\" xmlns:sentinel-safe=\"http://www.esa.int/safe/sentinel/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:sentinel3=\"http://www.esa.int/safe/sentinel/sentinel-3/1.0\" xmlns:slstr=\"http://www.esa.int/safe/sentinel/sentinel-3/slstr/1.0\" version=\"esa/safe/sentinel/sentinel-3/slstr/level-1/1.0\">" +
                "    <metadataSection>" +
                "        <metadataObject ID=\"slstrProductInformation\" classification=\"DESCRIPTION\" category=\"DMD\">" +
                "            <metadataWrap mimeType=\"text/xml\" vocabularyName=\"Sentinel-SAFE\" textInfo=\"Slstr Product Information\">" +
                "                <xmlData>" +
                "                    <slstr:slstrProductInformation>" +
                "                        <slstr:resolution grid=\"1 km\">" +
                "                            <slstr:spatialResolution>1000</slstr:spatialResolution>" +
                "                        </slstr:resolution>" +
                "                        <slstr:resolution grid=\"Tie Points\">" +
                "                            <slstr:spatialResolution>16000</slstr:spatialResolution>" +
                "                        </slstr:resolution>" +
                "                        <slstr:nadirImageSize grid=\"1 km\">" +
                "                            <sentinel3:trackOffset>998</sentinel3:trackOffset>" +
                "                            <sentinel3:rows>1200</sentinel3:rows>" +
                "                            <sentinel3:columns>1500</sentinel3:columns>" +
                "                        </slstr:nadirImageSize>" +
                "                        <slstr:nadirImageSize grid=\"Tie Points\">" +
                "                            <sentinel3:trackOffset>64</sentinel3:trackOffset>" +
                "                        </slstr:nadirImageSize>" +
                "                    </slstr:slstrProductInformation>" +
                "                </xmlData>" +
                "            </metadataWrap>" +
                "        </metadataObject>" +
                "    </metadataSection>" +
                "</xfdu:XFDU>";
        final InputSource is = new InputSource(new StringReader(manifestXML));
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        final Manifest manifest = XfduManifest.createManifest(document);

        final RasterInfo rasterInfo = SlstrRegriddedSubsetReader.getRasterInfo(manifest);
        assertNotNull(rasterInfo);

        assertEquals(1500, rasterInfo.rasterWidth);
        assertEquals(1200, rasterInfo.rasterHeight);
        assertEquals(1000, rasterInfo.rasterResolution);
        assertEquals(16000, rasterInfo.tiePointResolution);
        assertEquals(998, rasterInfo.rasterTrackOffset);
        assertEquals(64, rasterInfo.tiePointTrackOffset);
    }
}
