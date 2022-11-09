package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

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
