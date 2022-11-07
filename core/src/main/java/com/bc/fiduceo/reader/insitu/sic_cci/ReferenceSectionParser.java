package com.bc.fiduceo.reader.insitu.sic_cci;

import org.esa.snap.core.datamodel.ProductData;

import java.text.ParseException;
import java.util.Date;

abstract class ReferenceSectionParser extends AbstractSectionParser {

    static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    Date parseTime(String line) throws ParseException {
        // @todo 2 add some tests for negative indices tb 2022-11-03
        int first = line.indexOf(",");
        int startIndex = line.indexOf(",", first + 1) + 1;
        int stopIndex = line.indexOf(",", startIndex);

        final String timeString = line.substring(startIndex, stopIndex);
        final ProductData.UTC utcTime = ProductData.UTC.parse(timeString, DATE_PATTERN);
        return utcTime.getAsDate();
    }
}
