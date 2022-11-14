package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_MillisSince1970;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SicCciInsituReader implements Reader {

    private static final String REG_EX = "ASCAT-vs-AMSR2-vs-ERA5-vs-\\p{Upper}{6}\\d{1}-\\d{4}-[N|S].text";

    private FileReader fileReader;
    private TimeLocator timeLocator;
    private ArrayList<String> linelist;
    private SectionCache sectionCache;
    private ReferenceSectionParser referenceSectionParser;

    @Override
    public void open(File file) throws IOException {
        fileReader = new FileReader(file);

        readLines();

        final String fileName = file.getName();
        referenceSectionParser = createReferenceParser(fileName);
        sectionCache = new SectionCache(linelist, createSectionParsers(fileName));
    }

    @Override
    public void close() throws IOException {
        if (sectionCache != null) {
            sectionCache.close();
            sectionCache = null;
        }
        if (linelist != null) {
            linelist.clear();
            linelist = null;
        }
        if (fileReader != null) {
            fileReader.close();
            fileReader = null;
        }
        timeLocator = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        parseSensingTimes(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            createTimeLocator();
        }
        return timeLocator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        return new int[3];
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        try {
            final Array insituArray = sectionCache.get(variableName, centerY);
            final DataType dataType = insituArray.getDataType();
            if (dataType == DataType.CHAR) {
                // string data is not patched or padded, just returned tb 2022-11-09
                return insituArray;
            }

            final int windowHeight = interval.getY();
            final int windowWidth = interval.getX();
            final Array windowArray = NetCDFUtils.create(dataType,
                    new int[]{windowHeight, windowWidth},
                    NetCDFUtils.getDefaultFillValue(dataType, false));

            final int windowCenterX = windowWidth / 2;
            final int windowCenterY = windowHeight / 2;
            windowArray.setObject(windowWidth * windowCenterY + windowCenterX, insituArray.getObject(0));

            return windowArray;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        // raw data is already in geophysical representation tb 2022-11-11
        return readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final Array timeArray = readRaw(x, y, interval, "time");

        return (ArrayInt.D2) timeArray;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        return referenceSectionParser.getVariables();
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Dimension productSize = new Dimension();

        productSize.setName("product_size");
        productSize.setNx(1);
        productSize.setNy(linelist.size());

        return productSize;
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude";
    }

    private AbstractSectionParser[] createSectionParsers(String fileName) {
        final ArrayList<AbstractSectionParser> parsers = new ArrayList<>();
        parsers.add(referenceSectionParser);
        if (fileName.contains("ERA5")) {
            parsers.add(new ERASectionParser("ERA5"));
        } else if (fileName.contains("ERA")) {
            parsers.add(new ERASectionParser("ERA"));
        }
        if (fileName.contains("AMSR2")) {
            parsers.add(new AMSR2SectionParser());
        }
        if (fileName.contains("ASCAT")) {
            parsers.add(new ASCATSectionParser());
        }
        if (fileName.contains("SMOS")) {
            parsers.add(new SMOSSectionParser());
        }
        if (fileName.contains("SMAP")) {
            parsers.add(new SMAPSectionParser());
        }
        if (fileName.contains("QSCAT")) {
            parsers.add(new QSCATSectionParser());
        }
        return parsers.toArray(new AbstractSectionParser[0]);
    }

    static ReferenceSectionParser createReferenceParser(String fileName) throws IOException {
        if (fileName.contains("DMISIC0")) {
            return new DMISIC0SectionParser();
        } else if (fileName.contains("DTUSIC1")) {
            return new DTUSIC1SectionParser();
        } else if (fileName.contains("ANTXXXI")) {
            return new ANTXXXISectionParser();
        }

        throw new IOException("Invalid format, no known reference section found");
    }

    private void readLines() throws IOException {
        linelist = new ArrayList<>();
        final BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#")) {
                // skip comment lines tb 2022-11-03
                continue;
            }
            linelist.add(line);
        }
    }

    private void parseSensingTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        Date minDate = new Date(Long.MAX_VALUE);
        Date maxDate = new Date(0);
        try {
            for (String line : linelist) {
                final Date refTime = referenceSectionParser.parseTime(line);
                if (minDate.after(refTime)) {
                    minDate = refTime;
                }
                if (maxDate.before(refTime)) {
                    maxDate = refTime;
                }
            }

            acquisitionInfo.setSensingStart(minDate);
            acquisitionInfo.setSensingStop(maxDate);

        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }

    private void createTimeLocator() throws IOException {
        long[] timeArray = new long[linelist.size()];
        try {
            int i = 0;
            for (String line : linelist) {
                final Date refTime = referenceSectionParser.parseTime(line);
                timeArray[i] = refTime.getTime();
                ++i;
            }

            timeLocator = new TimeLocator_MillisSince1970(timeArray);
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }
}
