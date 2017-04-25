package com.bc.fiduceo.reader.iasi;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class MainProductHeaderRecord extends AsciiRecord {

    private static final int NUM_FIELDS = 72;

    private static final DateFormat generalTimeFormat = new SimpleDateFormat(
            "yyyyMMddHHmmss'Z'");

    private static final DateFormat longGeneralTimeFormat = new SimpleDateFormat(
            "yyyyMMddHHmmssSSS'Z'");

    MainProductHeaderRecord() {
        super(NUM_FIELDS);
    }

    MetadataElement getMetaData() {
        final MetadataElement element = new MetadataElement("MPH");

        element.addElement(getProductDetails());
        element.addElement(getAscendingNodeOrbitParameters());
        element.addElement(getLocationSummary());
        element.addElement(getLeapSecondInformation());
        element.addElement(getRecordCounts());
        element.addElement(getRecordBasedGenericQualityFlags());
        element.addElement(getTimeBasedGenericQualityFlags());
        element.addElement(getRegionalProductInformation());

        return element;
    }

    private MetadataElement getProductDetails() {
        final MetadataElement element = new MetadataElement("PRODUCT_DETAILS");
        element.addAttribute(HeaderUtil.createAttribute("PRODUCT_NAME",
                getValue("PRODUCT_NAME"), null,
                "Complete name of the product"));
        element.addAttribute(HeaderUtil.createAttribute(
                "PARENT_PRODUCT_NAME_1", getValue("PARENT_PRODUCT_NAME_1"),
                null, "Name of the parent product"));
        element.addAttribute(HeaderUtil.createAttribute(
                "PARENT_PRODUCT_NAME_2", getValue("PARENT_PRODUCT_NAME_2"),
                null, "Name of the parent product"));
        element.addAttribute(HeaderUtil.createAttribute(
                "PARENT_PRODUCT_NAME_3", getValue("PARENT_PRODUCT_NAME_3"),
                null, "Name of the parent product"));
        element.addAttribute(HeaderUtil.createAttribute(
                "PARENT_PRODUCT_NAME_4", getValue("PARENT_PRODUCT_NAME_4"),
                null, "Name of the parent product"));
        element.addAttribute(HeaderUtil.createAttribute("INSTRUMENT_ID",
                getValue("INSTRUMENT_ID")));
        element.addAttribute(HeaderUtil.createAttribute("INSTRUMENT_MODEL",
                getValue("INSTRUMENT_MODEL")));
        element.addAttribute(HeaderUtil.createAttribute("PRODUCT_TYPE",
                getValue("PRODUCT_TYPE")));
        element.addAttribute(HeaderUtil.createAttribute("PROCESSING_LEVEL",
                getValue("PROCESSING_LEVEL")));
        element.addAttribute(HeaderUtil.createAttribute("SPACECRAFT_ID",
                getValue("SPACECRAFT_ID")));
        element.addAttribute(createDateAttribute("SENSING_START",
                generalTimeFormat));
        element.addAttribute(createDateAttribute("SENSING_END",
                generalTimeFormat));
        element.addAttribute(createDateAttribute("SENSING_START_THEORETICAL",
                generalTimeFormat));
        element.addAttribute(createDateAttribute("SENSING_END_THEORETICAL",
                generalTimeFormat));
        element.addAttribute(HeaderUtil.createAttribute("PROCESSING_CENTRE",
                getValue("PROCESSING_CENTRE")));
        element.addAttribute(HeaderUtil.createAttribute(
                "PROCESSOR_MAJOR_VERSION",
                getIntValue("PROCESSOR_MAJOR_VERSION")));
        element.addAttribute(HeaderUtil.createAttribute(
                "PROCESSOR_MINOR_VERSION",
                getIntValue("PROCESSOR_MINOR_VERSION")));
        element.addAttribute(HeaderUtil.createAttribute("FORMAT_MAJOR_VERSION",
                getIntValue("FORMAT_MAJOR_VERSION")));
        element.addAttribute(HeaderUtil.createAttribute("FORMAT_MINOR_VERSION",
                getIntValue("FORMAT_MINOR_VERSION")));
        element.addAttribute(createDateAttribute("PROCESSING_TIME_START",
                generalTimeFormat));
        element.addAttribute(createDateAttribute("PROCESSING_TIME_END",
                generalTimeFormat));
        element.addAttribute(HeaderUtil.createAttribute("PROCESSING_MODE",
                getValue("PROCESSING_MODE")));
        element.addAttribute(HeaderUtil.createAttribute("DISPOSITION_MODE",
                getValue("DISPOSITION_MODE")));
        element.addAttribute(HeaderUtil.createAttribute(
                "RECEIVING_GROUND_STATION", getValue("DISPOSITION_MODE")));
        element.addAttribute(createDateAttribute("RECEIVE_TIME_START",
                generalTimeFormat));
        element.addAttribute(createDateAttribute("RECEIVE_TIME_END",
                generalTimeFormat));
        element.addAttribute(HeaderUtil.createAttribute("ORBIT_START",
                getIntValue("ORBIT_START"), null,
                "Start Orbit Number, counted incrementally since launch"));
        element.addAttribute(HeaderUtil.createAttribute("ORBIT_END",
                getIntValue("ORBIT_END"), null, "Stop Orbit Number"));
        element.addAttribute(HeaderUtil.createAttribute("ACTUAL_PRODUCT_SIZE",
                getIntValue("ACTUAL_PRODUCT_SIZE"), EpsMetopConstants.UNIT_BYTES,
                "Size of the complete product"));
        return element;
    }

    private MetadataElement getAscendingNodeOrbitParameters() {
        final MetadataElement element = new MetadataElement(
                "ASCENDING_NODE_ORBIT_PARAMETERS");
        element.addAttribute(createDateAttribute("STATE_VECTOR_TIME",
                longGeneralTimeFormat));
        element.addAttribute(HeaderUtil.createAttribute("SEMI_MAJOR_AXIS",
                getValue("SEMI_MAJOR_AXIS"), EpsMetopConstants.UNIT_MM));
        element.addAttribute(HeaderUtil.createAttribute("ECCENTRICITY",
                (float) (getLongValue("ECCENTRICITY") * 1E-6)));
        element.addAttribute(HeaderUtil.createAttribute("INCLINATION",
                (float) (getLongValue("INCLINATION") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        element.addAttribute(HeaderUtil.createAttribute("PERIGEE_ARGUMENT",
                (float) (getLongValue("PERIGEE_ARGUMENT") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        element.addAttribute(HeaderUtil.createAttribute("RIGHT_ASCENSION",
                (float) (getLongValue("RIGHT_ASCENSION") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        element.addAttribute(HeaderUtil.createAttribute("MEAN_ANOMALY",
                (float) (getLongValue("MEAN_ANOMALY") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        element.addAttribute(HeaderUtil.createAttribute("X_POSITION",
                (float) (getLongValue("X_POSITION") * 1E-3),
                EpsMetopConstants.UNIT_M));
        element.addAttribute(HeaderUtil.createAttribute("Y_POSITION",
                (float) (getLongValue("Y_POSITION") * 1E-3),
                EpsMetopConstants.UNIT_M));
        element.addAttribute(HeaderUtil.createAttribute("Z_POSITION",
                (float) (getLongValue("Z_POSITION") * 1E-3),
                EpsMetopConstants.UNIT_M));
        element.addAttribute(HeaderUtil.createAttribute("X_VELOCITY",
                (float) (getLongValue("X_VELOCITY") * 1E-3),
                EpsMetopConstants.UNIT_M_PER_S));
        element.addAttribute(HeaderUtil.createAttribute("Y_VELOCITY",
                (float) (getLongValue("Y_VELOCITY") * 1E-3),
                EpsMetopConstants.UNIT_M_PER_S));
        element.addAttribute(HeaderUtil.createAttribute("Z_VELOCITY",
                (float) (getLongValue("Z_VELOCITY") * 1E-3),
                EpsMetopConstants.UNIT_M_PER_S));
        element.addAttribute(HeaderUtil.createAttribute(
                "EARTH_SUN_DISTANCE_RATIO",
                getIntValue("EARTH_SUN_DISTANCE_RATIO")));
        element.addAttribute(HeaderUtil
                .createAttribute("LOCATION_TOLERANCE_RADIAL",
                        getIntValue("LOCATION_TOLERANCE_RADIAL"),
                        EpsMetopConstants.UNIT_M));
        element.addAttribute(HeaderUtil.createAttribute(
                "LOCATION_TOLERANCE_CROSSTRACK",
                getIntValue("LOCATION_TOLERANCE_CROSSTRACK"),
                EpsMetopConstants.UNIT_M));
        element.addAttribute(HeaderUtil.createAttribute(
                "LOCATION_TOLERANCE_ALONGTRACK",
                getIntValue("LOCATION_TOLERANCE_ALONGTRACK"),
                EpsMetopConstants.UNIT_M));
        element.addAttribute(HeaderUtil.createAttribute("YAW_ERROR",
                (float) (getLongValue("YAW_ERROR") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        element.addAttribute(HeaderUtil.createAttribute("ROLL_ERROR",
                (float) (getLongValue("ROLL_ERROR") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        element.addAttribute(HeaderUtil.createAttribute("PITCH_ERROR",
                (float) (getLongValue("PITCH_ERROR") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        return element;
    }

    private MetadataElement getLocationSummary() {
        final MetadataElement element = new MetadataElement("LOCATION_SUMMARY");
        element.addAttribute(HeaderUtil.createAttribute(
                "SUBSAT_LATITUDE_START",
                (float) (getLongValue("SUBSAT_LATITUDE_START") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        element.addAttribute(HeaderUtil.createAttribute(
                "SUBSAT_LONGITUDE_START",
                (float) (getLongValue("SUBSAT_LONGITUDE_START") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        element.addAttribute(HeaderUtil.createAttribute("SUBSAT_LATITUDE_END",
                (float) (getLongValue("SUBSAT_LATITUDE_END") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        element.addAttribute(HeaderUtil.createAttribute("SUBSAT_LONGITUDE_END",
                (float) (getLongValue("SUBSAT_LONGITUDE_END") * 1E-3),
                EpsMetopConstants.UNIT_DEG));
        return element;
    }

    private MetadataElement getLeapSecondInformation() {
        final MetadataElement element = new MetadataElement(
                "LEAP_SECOND_INFORMATION");
        final int leapSecond = getIntValue("LEAP_SECOND");
        element.addAttribute(HeaderUtil.createAttribute("LEAP_SECOND",
                leapSecond));
        element.addAttribute(createDateAttribute("LEAP_SECOND_UTC",
                generalTimeFormat));
        return element;
    }

    private MetadataElement getRecordCounts() {
        final MetadataElement element = new MetadataElement("RECORD_COUNTS");
        element.addAttribute(HeaderUtil.createAttribute("TOTAL_RECORDS",
                getIntValue("TOTAL_RECORDS")));
        element.addAttribute(HeaderUtil.createAttribute("TOTAL_MPHR",
                getIntValue("TOTAL_MPHR")));
        element.addAttribute(HeaderUtil.createAttribute("TOTAL_SPHR",
                getIntValue("TOTAL_SPHR")));
        element.addAttribute(HeaderUtil.createAttribute("TOTAL_IPR",
                getIntValue("TOTAL_IPR")));
        element.addAttribute(HeaderUtil.createAttribute("TOTAL_GEADR",
                getIntValue("TOTAL_GEADR")));
        element.addAttribute(HeaderUtil.createAttribute("TOTAL_GIADR",
                getIntValue("TOTAL_GIADR")));
        element.addAttribute(HeaderUtil.createAttribute("TOTAL_VEADR",
                getIntValue("TOTAL_VEADR")));
        element.addAttribute(HeaderUtil.createAttribute("TOTAL_VIADR",
                getIntValue("TOTAL_VIADR")));
        element.addAttribute(HeaderUtil.createAttribute("TOTAL_MDR",
                getIntValue("TOTAL_MDR")));
        return element;
    }

    private MetadataElement getRecordBasedGenericQualityFlags() {
        final MetadataElement element = new MetadataElement(
                "RECORD_BASED_GENERIC_QUALITY_FLAGS");
        element.addAttribute(HeaderUtil.createAttribute(
                "COUNT_DEGRADED_INST_MDR",
                getIntValue("COUNT_DEGRADED_INST_MDR")));
        element.addAttribute(HeaderUtil.createAttribute(
                "COUNT_DEGRADED_PROC_MDR",
                getIntValue("COUNT_DEGRADED_PROC_MDR")));
        element.addAttribute(HeaderUtil.createAttribute(
                "COUNT_DEGRADED_INST_MDR_BLOCKS",
                getIntValue("COUNT_DEGRADED_INST_MDR_BLOCKS")));
        element.addAttribute(HeaderUtil.createAttribute(
                "COUNT_DEGRADED_PROC_MDR_BLOCKS",
                getIntValue("COUNT_DEGRADED_PROC_MDR_BLOCKS")));
        return element;
    }

    private MetadataElement getTimeBasedGenericQualityFlags() {
        final MetadataElement element = new MetadataElement(
                "TIME_BASED_GENERIC_RECORD_FLAGS");
        element.addAttribute(HeaderUtil.createAttribute("DURATION_OF_PRODUCT",
                getIntValue("DURATION_OF_PRODUCT"), EpsMetopConstants.UNIT_MS));
        element.addAttribute(HeaderUtil.createAttribute(
                "MILLISECONDS_OF_DATA_PRESENT",
                getIntValue("MILLISECONDS_OF_DATA_PRESENT"),
                EpsMetopConstants.UNIT_MS));
        element.addAttribute(HeaderUtil.createAttribute(
                "MILLISECONDS_OF_DATA_MISSING",
                getIntValue("MILLISECONDS_OF_DATA_MISSING"),
                EpsMetopConstants.UNIT_MS));
        return element;
    }

    private MetadataElement getRegionalProductInformation() {
        final MetadataElement element = new MetadataElement("REGIONAL_PRODUCT_INFORMATION");

        element.addAttribute(HeaderUtil.createAttribute("SUBSETTED_PRODUCT",
                getValue("SUBSETTED_PRODUCT")));

        return element;
    }

    private MetadataAttribute createDateAttribute(String key, DateFormat dateFormat) {
        final String dateString = getValue(key);
        MetadataAttribute attribute;
        try {
            final Date date = dateFormat.parse(dateString);
            ProductData.UTC utc = ProductData.UTC.create(date, 0);
            attribute = new MetadataAttribute(key, utc, true);
        } catch (ParseException e) {
            ProductData data = ProductData.createInstance(dateString);
            attribute = new MetadataAttribute(key, data, true);
        }
        attribute.setUnit(EpsMetopConstants.UNIT_DATE);
        return attribute;
    }
}

