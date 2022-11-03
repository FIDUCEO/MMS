package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.TestUtil;
import org.junit.Test;

import java.text.ParseException;

public class ReferenceDataSectionTest {

    @Test
    public void testParse() throws ParseException {
        final String lineStart = "-59.000,+090.000,2016-01-01T08:00:00Z,ICECHART_DMI,0.0,-59.000,+090.000, ...";

        final ReferenceDataSection section = new ReferenceDataSection();
        section.parseTime(lineStart);

        TestUtil.assertCorrectUTCDate(2016, 1, 1, 8, 0, 0, section.getTime());
    }
}
