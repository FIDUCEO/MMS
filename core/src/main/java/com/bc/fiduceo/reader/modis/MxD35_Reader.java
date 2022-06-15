/*
 * Copyright (C) 2022 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.hdf.HdfEOSUtil;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_TAI1993Vector;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bc.fiduceo.reader.modis.ModisConstants.LATITUDE_VAR_NAME;
import static com.bc.fiduceo.reader.modis.ModisConstants.LONGITUDE_VAR_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_ADD_OFFSET_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FLAG_MASKS_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FLAG_MEANINGS_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FLAG_VALUES_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_SCALE_FACTOR_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_VALID_RANGE_NAME;

public class MxD35_Reader extends NetCDFReader {

    private static final String REG_EX = "M([OY])D35_L2.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";

    private static final String GEOLOCATION_GROUP = "mod35/Geolocation_Fields";
    private static final String DATA_GROUP = "mod35/Data_Fields";
    private static final int SMALLEST_DIM_IDX = 0;
    private static final int SMALLEST_DIM_SIZE = 1;
    private static final HashMap<String, FlagDefinition> FLAG_DEFINITIONS = new HashMap<>();

    {
        final FlagDefinition cloudMaskFlagDef = new FlagDefinition();
        final FlagDefinition qualAssurFlagDef = new FlagDefinition();

        cloudMaskFlagDef.setDefinitionForLayer(0, new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, new byte[]{
                    b(0b0000_0001),                                                  // mask for bit 0
                    b(0b0000_0110), b(0b0000_0110), b(0b0000_0110), b(0b0000_0110),     // mask for bit field 1 and 2
                    b(0b0000_1000),                                                  // mask for bit 3
                    b(0b0001_0000),                                                  // mask for bit 4
                    b(0b0010_0000),                                                  // mask for bit 5
                    b(0b1100_0000), b(0b1100_0000), b(0b1100_0000), b(0b1100_0000)});  // mask for bit field 6 and 7
            put(CF_FLAG_VALUES_NAME, new byte[]{
                    b(0b0000_0001),                                                  // values for bit 0
                    b(0b0000_0000), b(0b0000_0010), b(0b0000_0100), b(0b0000_0110),     // values for bit field 1 and 2
                    b(0b0000_1000),                                                  // values for bit 3
                    b(0b0001_0000),                                                  // values for bit 4
                    b(0b0010_0000),                                                  // values for bit 5
                    b(0b0000_0000), b(0b0100_0000), b(0b1000_0000), b(0b1100_0000)});  // values for bit field 6 and 7
            put(CF_FLAG_MEANINGS_NAME,
                "cloud_mask_valid " +
                "cloudy uncertain probably_clear confident_clear " +
                "day " +
                "no_sunglint " +
                "no_snow_or_ice " +
                "water coastal desert land"
            );
        }});
        qualAssurFlagDef.setDefinitionForLayer(0, new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, new byte[]{
                    b(0b0000_0001),
                    b(0b0000_1110), b(0b0000_1110), b(0b0000_1110), b(0b0000_1110),
                    b(0b0000_1110), b(0b0000_1110), b(0b0000_1110), b(0b0000_1110)
            });
            put(CF_FLAG_VALUES_NAME, new byte[]{
                    b(0b0000_0001),
                    b(0b0000_0000), b(0b0000_0010), b(0b0000_0100), b(0b0000_0110),
                    b(0b0000_1000), b(0b0000_1010), b(0b0000_1100), b(0b0000_1110)
            });
            put(CF_FLAG_MEANINGS_NAME,
                "cloud_mask_qa " +
                "cloud_mask_conf_qa_lev_0 cloud_mask_conf_qa_lev_1 cloud_mask_conf_qa_lev_2 cloud_mask_conf_qa_lev_3 " +
                "cloud_mask_conf_qa_lev_4 cloud_mask_conf_qa_lev_5 cloud_mask_conf_qa_lev_6 cloud_mask_conf_qa_lev_7"
            );
        }});
        final byte[] acendingFlagMask = {
                b(0b0000_0001), b(0b0000_0010), b(0b0000_0100), b(0b0000_1000),
                b(0b0001_0000), b(0b0010_0000), b(0b0100_0000), b(0b1000_0000)
        };
        final HashMap<String, Object> cf_definition_layer_1 = new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, acendingFlagMask);
            put(CF_FLAG_MEANINGS_NAME,
                "non_cloud_obstruction thin_cirrus_solar snow_cover_ancillary thin_cirrus_infrared " +
                "cloud_adjacency ir_threshold high_cloud_co2 high_cloud_6.7micron");
        }};
        cloudMaskFlagDef.setDefinitionForLayer(1, cf_definition_layer_1);
        qualAssurFlagDef.setDefinitionForLayer(1, cf_definition_layer_1);
        final HashMap<String, Object> cf_definition_layer_2 = new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, acendingFlagMask);
            put(CF_FLAG_MEANINGS_NAME,
                "high_cloud_1.38micron high_cloud_3.9_12 ir_temperature_difference 3.9_11_difference " +
                "visible_reflectance visible_nir_ratio clear_sky_ndvi 7.3_11_difference");
        }};
        cloudMaskFlagDef.setDefinitionForLayer(2, cf_definition_layer_2);
        qualAssurFlagDef.setDefinitionForLayer(2, cf_definition_layer_2);
        final HashMap<String, Object> cf_definition_layer_3 = new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, acendingFlagMask);
            put(CF_FLAG_MEANINGS_NAME,
                "8.6_11_difference clear_sky_spatial_variability clear_sky_other spatial_variability " +
                "suspended_dust 8.6_7.3_difference spatial_variability_11 3.9_11_difference");
        }};
        cloudMaskFlagDef.setDefinitionForLayer(3, cf_definition_layer_3);
        qualAssurFlagDef.setDefinitionForLayer(3, cf_definition_layer_3);
        final HashMap<String, Object> cf_definition_layer_4 = new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, acendingFlagMask);
            put(CF_FLAG_MEANINGS_NAME,
                "250m_element_11 250m_element_12 250m_element_13 250m_element_14 " +
                "250m_element_21 250m_element_22 250m_element_23 250m_element_24");
        }};
        cloudMaskFlagDef.setDefinitionForLayer(4, cf_definition_layer_4);
        qualAssurFlagDef.setDefinitionForLayer(4, cf_definition_layer_4);
        final HashMap<String, Object> cf_definition_layer_5 = new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, acendingFlagMask);
            put(CF_FLAG_MEANINGS_NAME,
                "250m_element_31 250m_element_32 250m_element_33 250m_element_34 " +
                "250m_element_41 250m_element_42 250m_element_43 250m_element_44");
        }};
        cloudMaskFlagDef.setDefinitionForLayer(5, cf_definition_layer_5);
        qualAssurFlagDef.setDefinitionForLayer(5, cf_definition_layer_5);

        qualAssurFlagDef.setDefinitionForLayer(6, new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, new byte[]{
                    b(0b0000_0011), b(0b0000_0011), b(0b0000_0011), b(0b0000_0011),
                    b(0b0000_1100), b(0b0000_1100), b(0b0000_1100), b(0b0000_1100)
            });
            put(CF_FLAG_VALUES_NAME, new byte[]{
                    b(0b0000_0000), b(0b0000_0001), b(0b0000_0010), b(0b0000_0011),
                    b(0b0000_0000), b(0b0000_0100), b(0b0000_1000), b(0b0000_1100)
            });
            put(CF_FLAG_MEANINGS_NAME,
                "bands_used_none bands_used_1-7 bands_used_8-14 bands_used_15-21 " +
                "spectral_tests_used_none spectral_tests_used_1-3 spectral_tests_used_4-6 spectral_tests_used_7-9");
        }});

        qualAssurFlagDef.setDefinitionForLayer(7, new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, new byte[]{
                    b(0b0000_0011), b(0b0000_0011), b(0b0000_0011), b(0b0000_0011),
                    b(0b0000_1100), b(0b0000_1100), b(0b0000_1100), b(0b0000_1100),
                    b(0b0011_0000), b(0b0011_0000), b(0b0011_0000), b(0b0011_0000),
                    b(0b1100_0000), b(0b1100_0000), b(0b1100_0000), b(0b1100_0000)
            });
            put(CF_FLAG_VALUES_NAME, new byte[]{
                    b(0b0000_0000), b(0b0000_0001), b(0b0000_0010), b(0b0000_0011),
                    b(0b0000_0000), b(0b0000_0100), b(0b0000_1000), b(0b0000_1100),
                    b(0b0000_0000), b(0b0001_0000), b(0b0010_0000), b(0b0011_0000),
                    b(0b0000_0000), b(0b0100_0000), b(0b1000_0000), b(0b1100_0000)
            });
            put(CF_FLAG_MEANINGS_NAME,
                "clear_rad_ori_mod35 clear_rad_ori_modFwdCalc clear_rad_ori_other clear_rad_ori_notUsed " +
                "surf_temp_land_NCEP_GDAS surf_temp_land_DAO surf_temp_land_mod11 surf_temp_land_other " +
                "surf_temp_ocean_ReynBlend surf_temp_ocean_DAO surf_temp_ocean_mod28 surf_temp_ocean_other " +
                "surf_winds_NCEP_GDAS surf_winds_DAO surf_winds_other surf_winds_notUsed");
        }});

        qualAssurFlagDef.setDefinitionForLayer(8, new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, new byte[]{
                    b(0b0000_0011), b(0b0000_0011), b(0b0000_0011), b(0b0000_0011),
                    b(0b0000_1100), b(0b0000_1100), b(0b0000_1100), b(0b0000_1100),
                    b(0b0011_0000), b(0b0011_0000), b(0b0011_0000), b(0b0011_0000),
                    b(0b1100_0000), b(0b1100_0000), b(0b1100_0000), b(0b1100_0000)
            });
            put(CF_FLAG_VALUES_NAME, new byte[]{
                    b(0b0000_0000), b(0b0000_0001), b(0b0000_0010), b(0b0000_0011),
                    b(0b0000_0000), b(0b0000_0100), b(0b0000_1000), b(0b0000_1100),
                    b(0b0000_0000), b(0b0001_0000), b(0b0010_0000), b(0b0011_0000),
                    b(0b0000_0000), b(0b0100_0000), b(0b1000_0000), b(0b1100_0000)
            });
            put(CF_FLAG_MEANINGS_NAME,
                "ecosys_map_LoLaNA1km ecosys_map_OlsonEco ecosys_map_mod12 ecosys_map_other " +
                "snow_mask_mod33 snow_mask_SSMI snow_mask_other snow_mask_notUsed " +
                "ice_cover_mod42 ice_cover_SSMI ice_cover_other ice_cover_notUsed " +
                "land/sea_mask_USGS1km6level land/sea_mask_USGS1kmBinary land/sea_mask_other land/sea_mask_notUsed");
        }});

        qualAssurFlagDef.setDefinitionForLayer(9, new HashMap<String, Object>() {{
            put(CF_FLAG_MASKS_NAME, new byte[]{
                    b(0b0000_0001),
                    b(0b0000_0110), b(0b0000_0110), b(0b0000_0110), b(0b0000_0110)
            });
            put(CF_FLAG_VALUES_NAME, new byte[]{
                    b(0b0000_0001),
                    b(0b0000_0000), b(0b0000_0010), b(0b0000_0100), b(0b0000_0110)
            });
            put(CF_FLAG_MEANINGS_NAME,
                "digital_elevation_model_notUsed " +
                "precipitable_water_NCEP_GDAS precipitable_water_DAO precipitable_water_mod07 precipitable_water_other");
        }});

        FLAG_DEFINITIONS.put("Cloud_Mask", cloudMaskFlagDef);
        FLAG_DEFINITIONS.put("Quality_Assurance", qualAssurFlagDef);
    }

    private static byte b(int i) {
        return (byte) i;
    }

    boolean packageLocalPropertyForUnitLevelTestsOnly_toSimulate_correspondingMod03FileNotAvailable = false;

    private final GeometryFactory geometryFactory;
    private final Dimension size1Km;
    private final Dimension size5km;
    private TimeLocator timeLocator;
    private PixelLocator pixelLocator;
    private Dimension _productSize;
    private MxD03_Reader mxD03Reader;
    private List<String> mxd03Names;
    private String fileName;
    private final ReaderContext readerContext;
    private Path filePath;
    private List<Variable> _variables;
    private Map<String, Variable> _variablesLUT;

    MxD35_Reader(ReaderContext readerContext) {
        this.readerContext = readerContext;
        this.size1Km = new Dimension("size", 1354, 0);
        this.size5km = new Dimension("size", 270, 0);
        this.geometryFactory = readerContext.getGeometryFactory();

    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
        timeLocator = null;
        this.fileName = file.getName();
        filePath = file.toPath();
    }

    @Override
    public void close() throws IOException {
        timeLocator = null;
        _productSize = null;

        if (pixelLocator != null && pixelLocator instanceof BowTiePixelLocator) {
            ((BowTiePixelLocator) pixelLocator).dispose();
            pixelLocator = null;
        }
        if (mxD03Reader != null) {
            mxD03Reader.close();
            mxD03Reader = null;
        }
        _variables = null;
        _variablesLUT = null;
        super.close();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        getPixelLocator();
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        HdfEOSUtil.extractAcquisitionTimes(acquisitionInfo, netcdfFile);
        extractGeometries(acquisitionInfo);
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public String getLongitudeVariableName() {
        return LONGITUDE_VAR_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE_VAR_NAME;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator != null) {
            return pixelLocator;
        }
        initVariables();
        if (loadCorrespondingMod03File()) {
            pixelLocator = mxD03Reader.getPixelLocator();
            final List<Variable> variables = mxD03Reader.getVariables();
            final String[] varNames = {"Longitude", "Latitude"};
            for (String varName : varNames) {
                final Variable var = variables.stream().filter(v -> varName.equals(v.getShortName())).findFirst().get();
                _variablesLUT.put(varName, var);
                arrayCache.inject(var);
                _variables.replaceAll(listVar -> {
                    if (varName.equals(listVar.getShortName())) {
                        return var;
                    } else {
                        return listVar;
                    }
                });
            }
        } else {
            // fallback
            try {
                pixelLocator = createPixelLocator();
            } catch (InvalidRangeException e) {
                throw new IOException(e);
            }
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            try {
                createTimeLocator();
            } catch (InvalidRangeException e) {
                throw new IOException(e);
            }
        }

        return timeLocator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        return ModisUtils.extractYearMonthDayFromFilename(fileName);
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        getPixelLocator();
        final Variable variable = _variablesLUT.get(variableName);
        final Array array = arrayCache.get(variableName);
        final Number fillValue = variable.findAttribute(CF_FILL_VALUE_NAME).getNumericValue();
        if (fillValue == null) {
            throw new RuntimeException("implement fill value handling here.");
        }

        return RawDataReader.read(centerX, centerY, interval, fillValue, array, getProductSize());
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array rawData = readRaw(centerX, centerY, interval, variableName);
        final String groupName = getGroupName(variableName);

        final double scaleFactor = getScaleFactorCf(groupName, variableName);
        final double offset = getOffset(groupName, variableName);
        if (ReaderUtils.mustScale(scaleFactor, offset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            return MAMath.convert2Unpacked(rawData, scaleOffset);
        }

        return rawData;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException {
        return (ArrayInt.D2) acquisitionTimeFromTimeLocator(y, interval);
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        getPixelLocator();
        return _variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        if (_productSize == null) {
            final List<ucar.nc2.Dimension> dimensions = netcdfFile.getDimensions();
            final List<ucar.nc2.Dimension> km1 = dimensions.stream().filter(d -> d.getShortName().contains("1km")).collect(Collectors.toList());
            final ucar.nc2.Dimension across = km1.stream().filter(d -> d.getShortName().contains("Across")).findFirst().get();
            final ucar.nc2.Dimension along = km1.stream().filter(d -> d.getShortName().contains("Along")).findFirst().get();
            _productSize = new Dimension("shape",
                                         across.getLength(),
                                         along.getLength()
            );
        }
        return _productSize;
    }

    private void initVariables() throws IOException {
        if (_variables != null) {
            return;
        }
        final List<Variable> variablesInFile = netcdfFile.getVariables();
        final Dimension productSize = getProductSize();
        final int sceneWidth = productSize.getNx();
        final int sceneHeight = productSize.getNy();
        _variables = new ArrayList<>();
        for (Variable variable : variablesInFile) {
            final String variableName = variable.getShortName();
            if (variableName.contains("StructMetadata")
                || variableName.contains("CoreMetadata")
                || variableName.contains("ArchiveMetadata")
                || variableName.contains("Cell_Across_Swath_1km")
                || variableName.contains("Cell_Along_Swath_1km")
                || variableName.contains("Byte_Segment")
            ) {
                continue;
            }

            if (variableName.equals("Latitude")
                || variableName.equals("Longitude")
                || variableName.equals("Solar_Zenith")
                || variableName.equals("Solar_Azimuth")
                || variableName.equals("Sensor_Zenith")
                || variableName.equals("Sensor_Azimuth")
            ) {
                variable = new MxD35BowTieVariable(variable, sceneWidth, sceneHeight);
            }

            if (variableName.equals("Scan_Start_Time")) {
                variable = new MxD35ScanTimeVariable(variable, sceneWidth, sceneHeight);
            }

            if (variableName.equals("Cloud_Mask")
                || variableName.equals("Quality_Assurance")
                || variableName.contains("Cloud_Mask_SPI")
            ) {
                final FlagDefinition flagDefinition = FLAG_DEFINITIONS.get(variableName);
                final int[] slicingConditions = findSlicingConditions(variable);
                for (int i = 0; i < slicingConditions[SMALLEST_DIM_SIZE]; i++) {
                    final Variable slice;
                    try {
                        slice = variable.slice(slicingConditions[SMALLEST_DIM_IDX], i);
                    } catch (InvalidRangeException e) {
                        throw new IOException(e);
                    }
                    slice.setShortName(slice.getShortName() + "_" + i);
                    if (flagDefinition != null) {
                        slice.removeAttribute(CF_VALID_RANGE_NAME);
                        slice.removeAttribute(CF_ADD_OFFSET_NAME);
                        slice.removeAttribute(CF_SCALE_FACTOR_NAME);
                        final Map<String, Object> definition = flagDefinition.getDefinitionFor(i);
                        if (definition != null) {
                            for (String key : definition.keySet()) {
                                final Object o = definition.get(key);
                                if (o instanceof String) {
                                    slice.addAttribute(new Attribute(key, (String) o));
                                } else {
                                    slice.addAttribute(new Attribute(key, Array.makeFromJavaArray(o)));
                                }
                            }
                        }
                    }
                    _variables.add(slice);
                }
                continue;
            }
            _variables.add(variable);
        }
        _variablesLUT = new HashMap<>();
        for (Variable variable : _variables) {
            arrayCache.inject(variable);
            _variablesLUT.put(variable.getShortName(), variable);
        }
    }

    // package access for testing only tb 2017-08-28
    static String getGroupName(String variableName) {
        if (LONGITUDE_VAR_NAME.equals(variableName) || LATITUDE_VAR_NAME.equals(variableName)) {
            return GEOLOCATION_GROUP;
        }
        return DATA_GROUP;
    }

    private boolean loadCorrespondingMod03File() throws IOException {
        if (packageLocalPropertyForUnitLevelTestsOnly_toSimulate_correspondingMod03FileNotAvailable) {
            return false;
        }
        if (mxD03Reader == null) {
            final int[] ymd = extractYearMonthDayFromFilename(fileName);
            final String fileType = extractGeoFileType(fileName);
            final String timePattern = extractTimePattern(fileName);

            final Archive archive = readerContext.getArchive();
            final String version = archive.getVersion(extractFileType(fileName), filePath);
            final Path productPath = archive.createValidProductPath(version, fileType, ymd[0], ymd[1], ymd[2]);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(productPath)) {
                for (Path path : stream) {
                    final Path fileName = path.getFileName();
                    if (fileName.toString().contains(timePattern)) {
                        mxD03Reader = new MxD03_Reader(readerContext);
                        mxD03Reader.open(path.toFile());

                        final List<Variable> variables = mxD03Reader.getVariables();
                        mxd03Names = new ArrayList<>(variables.size());
                        for (final Variable variable : variables) {
                            mxd03Names.add(variable.getShortName());
                        }

                        break;
                    }
                }

                return mxD03Reader != null;
            }
        }
        return false;
    }

    // package access for testing only tb 2020-05-26
    static String extractGeoFileType(String fileName) throws IOException {
        if (fileName.contains("MOD35")) {
            return "mod03-te";
        } else if (fileName.contains("MYD35")) {
            return "myd03-aq";
        }
        throw new IOException("invalid file name: " + fileName);
    }

    // package access for testing only tb 2020-06-03
    static String extractFileType(String fileName) throws IOException {
        if (fileName.contains("MOD35")) {
            return "mod35-te";
        } else if (fileName.contains("MYD35")) {
            return "myd35-aq";
        }
        throw new IOException("invalid file name: " + fileName);
    }

    // package access for testing only tb 2020-05-26
    static String extractTimePattern(String fileName) throws IOException {
        if (fileName.length() < 23) {
            throw new IOException("invalid file name: " + fileName);
        }

        final String timePattern = fileName.substring(17, 23);

        // check if we have the .xxxx. pattern
        try {
            Integer.parseInt(timePattern.substring(1, 5));
        } catch (NumberFormatException e) {
            throw new IOException("invalid file name: " + fileName);
        }

        return timePattern;
    }

    private int[] findSlicingConditions(Variable variable) {
        final int[] shape = variable.getShape();
        final int[] conditions = new int[2];
        conditions[SMALLEST_DIM_IDX] = -1;
        conditions[SMALLEST_DIM_SIZE] = Integer.MAX_VALUE;
        for (int k = 0; k < shape.length; k++) {
            int dimSize = shape[k];
            if (dimSize < conditions[SMALLEST_DIM_SIZE]) {
                conditions[SMALLEST_DIM_IDX] = k;
                conditions[SMALLEST_DIM_SIZE] = dimSize;
            }
        }
        return conditions;
    }

    private void extractGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(new Interval(250, 250), geometryFactory);
        final Array longitude = arrayCache.get(GEOLOCATION_GROUP, LONGITUDE_VAR_NAME);
        final Array latitude = arrayCache.get(GEOLOCATION_GROUP, LATITUDE_VAR_NAME);
        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitude, latitude);
        if (!boundingGeometry.isValid()) {
            throw new RuntimeException("Detected invalid bounding geometry");
        }
        acquisitionInfo.setBoundingGeometry(boundingGeometry);

        final Geometries geometries = new Geometries();
        geometries.setBoundingGeometry(boundingGeometry);
        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitude, latitude);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);
    }

    private void createTimeLocator() throws IOException, InvalidRangeException {
        getPixelLocator();
        final Variable variable = _variablesLUT.get("Scan_Start_Time");
        final int[] offsets = new int[]{0, 0};
        final int[] shape = variable.getShape();
        shape[1] = 1;
        final Array section = variable.read(offsets, shape);
        timeLocator = new TimeLocator_TAI1993Vector(section);
    }

    private PixelLocator createPixelLocator() throws IOException, InvalidRangeException {
        return new BowTiePixelLocator(arrayCache.get(LONGITUDE_VAR_NAME), arrayCache.get(LATITUDE_VAR_NAME), geometryFactory, 10);
    }

    private static class FlagDefinition {
        private final Map<Integer, Map<String, Object>> definitions = new HashMap<>();

        public void setDefinitionForLayer(int layer, Map<String, Object> cfConformDefinition) {
            definitions.put(layer, cfConformDefinition);
        }

        public Map<String, Object> getDefinitionFor(int layer) {
            if (definitions.containsKey(layer)) {
                return Collections.unmodifiableMap(definitions.get(layer));
            }
            return null;
        }
    }
}
