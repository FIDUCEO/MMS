package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.snap.SNAP_Reader;
import com.bc.fiduceo.reader.snap.VariableProxy;
import com.bc.fiduceo.reader.time.TimeLocator_MicrosSince2000;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.engine_utilities.util.ZipUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.reader.slstr.VariableType.NADIR_1km;
import static com.bc.fiduceo.reader.slstr.VariableType.NADIR_500m;
import static ucar.ma2.DataType.INT;

public class SlstrReader extends SNAP_Reader {

    private static final String REGEX_ALL = "S3([AB])_SL_1_RBT_.*(.SEN3|zip)";
    private static final String REGEX_NR = "S3([AB])_SL_1_RBT_.*_NR_.*(.SEN3|zip)";
    private static final String REGEX_NT = "S3([AB])_SL_1_RBT_.*_NT_.*(.SEN3|zip)";
    private static final Interval INTERVAL = new Interval(100, 100);
    private static final int NUM_SPLITS = 1;

    private final VariableNames variableNames;
    private final String regEx;
    final private ReaderContext readerContext;
    private long[] subs_times;
    private TransformFactory transformFactory;
    private File productDir;

    SlstrReader(ReaderContext readerContext, ProductType productType) {
        super(readerContext);
        this.readerContext = readerContext;
        productDir = null;

        variableNames = new VariableNames();

        if (productType == ProductType.ALL) {
            this.regEx = REGEX_ALL;
        } else if (productType == ProductType.NR) {
            this.regEx = REGEX_NR;
        } else if (productType == ProductType.NT) {
            this.regEx = REGEX_NT;
        } else {
            throw new IllegalArgumentException("Unsupported product type");
        }
    }

    // package access for testing only tb 2019-05-13
    static long[] subSampleTimes(long[] timeStamps) {
        final long[] subs_times = new long[(int) Math.ceil(timeStamps.length / 2.0)];

        int writeIndex = 0;
        for (int i = 0; i < timeStamps.length; i++) {
            if (i % 2 == 0) {
                subs_times[writeIndex] = timeStamps[i];
                ++writeIndex;
            }
        }
        return subs_times;
    }

    @Override
    public void open(File file) throws IOException {
        File manifestFile = file;
        if (ReaderUtils.isCompressed(file)) {
            final String fileName = FileUtils.getFilenameWithoutExtension(file);
            final long millis = System.currentTimeMillis();
            productDir = readerContext.createDirInTempDir(fileName + millis);
            try {
                ZipUtils.unzip(file.toPath(), productDir.toPath(), true);
                File[] files = productDir.listFiles();
                if (files == null || files.length == 0) {
                    throw new IOException("Corrupt archive, no file listing possible");
                }

                if (files.length == 1) {
                    final File expandedDir = files[0];
                    files = expandedDir.listFiles();
                    if (files == null || files.length == 0) {
                        throw new IOException("Corrupt archive, no file listing possible");
                    }
                }

                for (final File uncompressedFile : files) {
                    if (uncompressedFile == null) {
                        continue;
                    }
                    if (uncompressedFile.getName().contains("manifest.xml")) {
                        manifestFile = uncompressedFile;
                        break;
                    }
                }
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }
        openCached(manifestFile, "Sen3");

        final int obliqueGridOffset = getObliqueGridOffset();
        transformFactory = new TransformFactory(product.getSceneRasterWidth(),
                product.getSceneRasterHeight(),
                obliqueGridOffset);
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (productDir != null) {
            readerContext.deleteTempFile(productDir);
            productDir = null;
        }
        transformFactory = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = read(INTERVAL, NUM_SPLITS);

        setOrbitNodeInfo(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return regEx;
    }

    @Override
    public PixelLocator getPixelLocator() {
        return new SlstrPixelLocator(product.getSceneGeoCoding(), transformFactory.get(NADIR_500m));
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) {
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() {
        ensureTimingVector();

        return new TimeLocator_MicrosSince2000(subs_times);
    }

    @Override
    public List<Variable> getVariables() {
        final List<Variable> result = new ArrayList<>();

        final Band[] bands = product.getBands();
        for (final Band band : bands) {
            if (variableNames.isValidName(band.getName())) {
                final VariableProxy variableProxy = new VariableProxy(band);
                result.add(variableProxy);
            }
        }

        final TiePointGrid[] tiePointGrids = product.getTiePointGrids();
        for (final TiePointGrid tiePointGrid : tiePointGrids) {
            if (variableNames.isValidName(tiePointGrid.getName())) {
                final VariableProxy variableProxy = new VariableProxy(tiePointGrid);
                result.add(variableProxy);
            }
        }

        return result;
    }

    @Override
    public Dimension getProductSize() {
        final Transform transform = transformFactory.get(NADIR_1km);
        return transform.getRasterSize();
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String datePart = fileName.substring(16, 24);
        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(datePart.substring(0, 4));
        ymd[1] = Integer.parseInt(datePart.substring(4, 6));
        ymd[2] = Integer.parseInt(datePart.substring(6, 8));

        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        if (product.containsTiePointGrid(variableName)) {
            // we do not want raw data access on tie-point grids tb 2016-08-11
            return readScaled(centerX, centerY, interval, variableName);
        }

        final VariableType variableType = variableNames.getVariableType(variableName);
        final Transform transform = transformFactory.get(variableType);
        final Dimension rasterSize = transform.getRasterSize();

        final RasterDataNode dataNode = getRasterDataNode(variableName);

        final double noDataValue = getNoDataValue(dataNode);
        final DataType targetDataType = NetCDFUtils.getNetcdfDataType(dataNode.getDataType());

        final Interval mappedInterval = transform.mapInterval(interval);
        final int width = mappedInterval.getX();
        final int height = mappedInterval.getY();
        final int[] shape = getShape(mappedInterval);
        final Array readArray = Array.factory(targetDataType, shape);
        final Array targetArray = NetCDFUtils.create(targetDataType, shape, noDataValue);

        final int mappedX = (int) (transform.mapCoordinate_X(centerX) + 0.5);
        final int mappedY = (int) (transform.mapCoordinate_Y(centerY) + 0.5);

        final int xOffset = mappedX - width / 2 + transform.getOffset();
        final int yOffset = mappedY - height / 2 + transform.getOffset();

        readRawProductData(dataNode, readArray, width, height, xOffset, yOffset);

        final Index index = targetArray.getIndex();
        int readIndex = 0;
        for (int y = 0; y < width; y++) {
            final int currentY = yOffset + y;
            for (int x = 0; x < height; x++) {
                final int currentX = xOffset + x;

                if (currentX >= 0 && currentX < rasterSize.getNx() && currentY >= 0 && currentY < rasterSize.getNy()) {
                    index.set(y, x);
                    targetArray.setObject(index, readArray.getObject(readIndex));
                    ++readIndex;
                }
            }
        }

        if (variableNames.isFlagVariable(variableName)) {
            return transform.processFlags(targetArray, (int) noDataValue);
        } else {
            return transform.process(targetArray, noDataValue);
        }
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        final VariableType variableType = variableNames.getVariableType(variableName);
        final Transform transform = transformFactory.get(variableType);

        final RasterDataNode dataNode = getRasterDataNode(variableName);
        final double noDataValue = SlstrReader.getGeophysicalNoDataValue(dataNode);

        final Interval mappedInterval = transform.mapInterval(interval);
        final DataType sourceDataType = NetCDFUtils.getNetcdfDataType(dataNode.getGeophysicalDataType());
        final int[] shape = getShape(mappedInterval);
        final Array readArray = createReadingArray(sourceDataType, shape);

        final int width = mappedInterval.getX();
        final int height = mappedInterval.getY();

        final int mappedX = (int) (transform.mapCoordinate_X(centerX) + 0.5);
        final int mappedY = (int) (transform.mapCoordinate_Y(centerY) + 0.5);
        final int xOffset = mappedX - width / 2 + transform.getOffset();
        final int yOffset = mappedY - height / 2 + transform.getOffset();

        readProductData(dataNode, readArray, width, height, xOffset, yOffset);

        return transform.process(readArray, noDataValue);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) {
        final int width = interval.getX();
        final int height = interval.getY();
        final int[] timeArray = new int[width * height];

        ensureTimingVector();

        final Transform transform = transformFactory.get(NADIR_1km);
        final Dimension rasterSize = transform.getRasterSize();
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        final int halfHeight = height / 2;
        final int halfWidth = width / 2;
        int writeOffset = 0;

        for (int yRead = y - halfHeight; yRead <= y + halfHeight; yRead++) {
            int lineTimeSeconds = fillValue;
            if (yRead >= 0 && yRead < rasterSize.getNy()) {
                final long lineTimeMillis = TimeUtils.millisSince2000ToUnixEpoch(subs_times[yRead]);
                lineTimeSeconds = (int) Math.round(lineTimeMillis * 0.001);
            }

            for (int xRead = x - halfWidth; xRead <= x + halfWidth; xRead++) {
                if (xRead >= 0 && xRead < rasterSize.getNx()) {
                    timeArray[writeOffset] = lineTimeSeconds;
                } else {
                    timeArray[writeOffset] = fillValue;
                }
                ++writeOffset;
            }
        }

        final int[] shape = getShape(interval);
        return (ArrayInt.D2) Array.factory(INT, shape, timeArray);
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude_tx";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude_tx";
    }

    protected void readProductData(RasterDataNode dataNode, Array targetArray, int width, int height, int xOffset, int yOffset) throws IOException {
        final VariableType variableType = variableNames.getVariableType(dataNode.getName());
        final Transform transform = transformFactory.get(variableType);
        final Dimension rasterSize = transform.getRasterSize();

        readSubsetData(dataNode, targetArray, width, height, xOffset, yOffset, rasterSize.getNx(), rasterSize.getNy());
    }

    @Override
    protected RasterDataNode getRasterDataNode(String variableName) {
        if (!variableNames.isValidName(variableName)) {
            throw new RuntimeException("Requested variable not contained in product: " + variableName);
        }

        return super.getRasterDataNode(variableName);
    }

    private void setOrbitNodeInfo(AcquisitionInfo acquisitionInfo) {
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement manifest = metadataRoot.getElement("Manifest");
        if (manifest != null) {
            final MetadataElement metadataSection = manifest.getElement("metadataSection");
            if (metadataSection != null) {
                final MetadataElement orbitReference = metadataSection.getElement("orbitReference");
                if (orbitReference != null) {
                    final MetadataElement orbitNumber = orbitReference.getElement("orbitNumber");
                    if (orbitNumber != null) {
                        final String groundTrackDirection = orbitNumber.getAttribute("groundTrackDirection").getData().getElemString();
                        if (groundTrackDirection.equalsIgnoreCase("descending")) {
                            acquisitionInfo.setNodeType(NodeType.DESCENDING);
                        } else if (groundTrackDirection.equalsIgnoreCase("ascending")) {
                            acquisitionInfo.setNodeType(NodeType.ASCENDING);
                        }
                    }
                }
            }
        }
    }

    private int getObliqueGridOffset() {
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement manifestElement = metadataRoot.getElement("Manifest");
        final MetadataElement metadataElement = manifestElement.getElement("metadataSection");
        final MetadataElement productInformationElement = metadataElement.getElement("slstrProductInformation");

        int nadirTrackOffset = -1;
        int obliqueTrackOffset = -1;
        final MetadataElement[] elements = productInformationElement.getElements();
        for (final MetadataElement element : elements) {
            if (element.getName().equalsIgnoreCase("nadirImageSize")) {
                final MetadataAttribute grid = element.getAttribute("grid");
                if (grid.getData().getElemString().equalsIgnoreCase("1 km")) {
                    nadirTrackOffset = extractTrackOffset(element);
                }
            }
            if (element.getName().equalsIgnoreCase("obliqueImageSize")) {
                final MetadataAttribute grid = element.getAttribute("grid");
                if (grid.getData().getElemString().equalsIgnoreCase("1 km")) {
                    obliqueTrackOffset = extractTrackOffset(element);
                }
            }
        }

        if (nadirTrackOffset < 0 | obliqueTrackOffset < 0) {
            throw new RuntimeException("Unable to extract raster offsets from metadata.");
        }

        return nadirTrackOffset - obliqueTrackOffset;
    }

    private int extractTrackOffset(MetadataElement element) {
        final MetadataAttribute trackOffset = element.getAttribute("trackOffset");
        final String trackOffsetString = trackOffset.getData().getElemString();
        return Integer.parseInt(trackOffsetString);
    }

    private void ensureTimingVector() {
        if (subs_times == null) {
            final MetadataElement metadataRoot = product.getMetadataRoot();
            final MetadataElement time_stamp_a = metadataRoot.getElement("time_stamp_a");
            final MetadataAttribute values = time_stamp_a.getAttribute("value");
            final ProductData valuesData = values.getData();
            final long[] timeStamps = (long[]) valuesData.getElems();
            subs_times = subSampleTimes(timeStamps);
        }
    }
}
