package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
import org.esa.beam.dataio.netcdf.ProfileReadContext;
import org.esa.beam.dataio.netcdf.metadata.profiles.hdfeos.HdfEosMetadataPart;
import org.esa.beam.dataio.netcdf.metadata.profiles.hdfeos.HdfEosUtils;
import org.esa.beam.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.beam.dataio.netcdf.util.RasterDigest;
import org.esa.beam.framework.datamodel.Product;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AIRS_L1B_Reader implements Reader {

    private NetcdfFile netcdfFile;

    public void open(File file) throws IOException {
        netcdfFile = NetcdfFileOpener.open(file.getPath());
        if (netcdfFile == null) {
            throw new IOException("Failed to open file " + file.getPath());
        }
    }

    public void close() throws IOException {
        netcdfFile.close();
    }

    public SatelliteObservation read() throws IOException {
        final Product product = new Product("dummy", "dummy", 2, 2);
        final HdfEosMetadataPart hdfEosMetadataPart = new HdfEosMetadataPart();
        final EosReadContext eosReadContext = new EosReadContext(netcdfFile);
        hdfEosMetadataPart.decode(eosReadContext, product);

        final SatelliteObservation satelliteObservation = new SatelliteObservation();
        satelliteObservation.setStartTime(new Date());
        return satelliteObservation;

    }


    private class EosReadContext implements ProfileReadContext {
        private static final String CORE_METADATA = "CoreMetadata";
        private NetcdfFile netcdfFile;
        private final Map<String, Object> propertyMap;


        public EosReadContext(NetcdfFile netcdfFile) throws IOException {
            this.netcdfFile = netcdfFile;
            propertyMap = new HashMap<String, Object>();

            final Group eosGroup = netcdfFile.getRootGroup();
            setProperty(CORE_METADATA, HdfEosUtils.getEosElement(CORE_METADATA, eosGroup));
        }

        public NetcdfFile getNetcdfFile() {
            return netcdfFile;
        }

        public void setRasterDigest(RasterDigest rasterDigest) {

        }

        public RasterDigest getRasterDigest() {
            return null;
        }

        public void setProperty(String name, Object value) {
            propertyMap.put(name, value);
        }

        public Object getProperty(String name) {
            return propertyMap.get(name);
        }
    }
}
