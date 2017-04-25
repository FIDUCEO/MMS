package com.bc.fiduceo.reader.iasi;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.ProductData;

import java.util.Calendar;

class HeaderUtil {

    public static MetadataAttribute createAttribute(String name, int bitField, int field, String offValue, String onValue) {
        final int bitValue = (bitField & (1 << field));
        final String stringValue;
        if (bitValue == 0) {
            stringValue = offValue;
        } else {
            stringValue = onValue;
        }
        return createAttribute(name, stringValue);
    }

    public static MetadataAttribute createAttribute(String name, int intData) {
        ProductData data = ProductData.createInstance(new int[]{intData});
        return new MetadataAttribute(name, data, true);
    }

    public static MetadataAttribute createAttribute(String name, int intData, String unit) {
        return createAttribute(name, intData, unit, null);
    }

    public static MetadataAttribute createAttribute(String name, int intData, String unit, String description) {
        MetadataAttribute attribute = createAttribute(name, intData);
        extendAttribute(attribute, unit, description);
        return attribute;
    }

    public static MetadataAttribute createAttribute(String name, float floatData) {
        ProductData data = ProductData.createInstance(new float[]{floatData});
        return new MetadataAttribute(name, data, true);
    }

    public static MetadataAttribute createAttribute(String name, float floatData, String unit) {
        return createAttribute(name, floatData, unit, null);
    }

    public static MetadataAttribute createAttribute(String name, float floatData, String unit, String description) {
        MetadataAttribute attribute = createAttribute(name, floatData);
        extendAttribute(attribute, unit, description);
        return attribute;
    }

    public static MetadataAttribute createAttribute(String name, String stringData) {
        ProductData data = ProductData.createInstance(stringData);
        return new MetadataAttribute(name, data, true);
    }

    public static MetadataAttribute createAttribute(String name, String stringData, String unit) {
        return createAttribute(name, stringData, unit, null);
    }

    public static MetadataAttribute createAttribute(String name, String stringData, String unit, String description) {
        MetadataAttribute attribute = createAttribute(name, stringData);
        extendAttribute(attribute, unit, description);
        return attribute;
    }

    public static ProductData.UTC createUTCDate(int year, int dayOfYear, int millisInDay) {
        Calendar calendar = ProductData.UTC.createCalendar();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
        calendar.add(Calendar.MILLISECOND, millisInDay);

        return ProductData.UTC.create(calendar.getTime(), 0);
    }

    private static void extendAttribute(MetadataAttribute attribute, String unit, String description) {
        if (unit != null) {
            attribute.setUnit(unit);
        }
        if (description != null) {
            attribute.setDescription(description);
        }
    }
}
