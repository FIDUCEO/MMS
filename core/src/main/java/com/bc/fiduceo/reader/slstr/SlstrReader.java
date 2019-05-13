package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.snap.SNAP_Reader;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;

public class SlstrReader extends SNAP_Reader {

    private static final String REGEX = "S3([AB])_SL_1_RBT_.*(.SEN3)?";
    private static final Interval INTERVAL = new Interval(100, 100);
    private static final int NUM_SPLITS = 1;

    SlstrReader(ReaderContext readerContext) {
        super(readerContext);
    }

    @Override
    public void open(File file) throws IOException {
        open(file, "Sen3");
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = read(INTERVAL, NUM_SPLITS);

        setOrbitNodeInfo(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REGEX;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() {
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement time_stamp_a = metadataRoot.getElement("time_stamp_a");
        final MetadataAttribute values = time_stamp_a.getAttribute("value");
        final ProductData valuesData = values.getData();
        final long[] timeStamps = (long[]) valuesData.getElems();
        final long[] subs_times = subSampleTimes(timeStamps);

        return new TimeLocator_MicrosSince2000(subs_times);
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude_tx";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude_tx";
    }

        // package access for testing only tb 2019-05-13
    static long[] subSampleTimes(long[] timeStamps) {
        final long[] subs_times = new long[timeStamps.length/2];

        int writeIndex = 0;
        for (int i = 0; i < timeStamps.length; i++) {
            if (i % 2 == 0) {
                subs_times[writeIndex] = timeStamps[i];
                ++writeIndex;
            }
        }
        return subs_times;
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
}
