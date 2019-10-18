package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ConditionEngineContextTest {

    private ConditionEngineContext conditionEngineContext;
    private Date startDate;
    private Date endDate;

    @Before
    public void setUp() {
        conditionEngineContext = new ConditionEngineContext();
        startDate = new Date();
        endDate = new Date(startDate.getTime() + 100);
    }

    @Test
    public void testValidateTime_afterInitializing() {
        try {
            conditionEngineContext.validateTime();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("End date and/or start date are not valid.", expected.getMessage());
        }
    }

    @Test
    public void testValidateTime_endDateIsMissing() {
        conditionEngineContext.setStartDate(startDate);
        try {
            conditionEngineContext.validateTime();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("End date and/or start date are not valid.", expected.getMessage());
        }
    }

    @Test
    public void testValidateTime_startDateIsMissing() {
        conditionEngineContext.setEndDate(endDate);
        try {
            conditionEngineContext.validateTime();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("End date and/or start date are not valid.", expected.getMessage());
        }
    }

    @Test
    public void testValidateTime_endDateIsBeforeStartDate() {
        conditionEngineContext.setStartDate(endDate);
        conditionEngineContext.setEndDate(startDate);
        try {
            conditionEngineContext.validateTime();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("End date and/or start date are not valid.", expected.getMessage());
        }
    }

    @Test
    public void testValidateTime_valid() {
        conditionEngineContext.setStartDate(startDate);
        conditionEngineContext.setEndDate(endDate);

        conditionEngineContext.validateTime();
    }

    @Test
    public void testSetGetPrimaryExtractSize() {
        final Dimension dimension = new Dimension("prim", 3, 5);
        conditionEngineContext.setPrimaryExtractSize(dimension);

        final Dimension result = conditionEngineContext.getPrimaryExtractSize();
        assertEquals("prim", result.getName());
    }

    @Test
    public void testSetGetSecondaryExtractSize() {
        final Dimension dimension = new Dimension("seco", 5, 3);
        conditionEngineContext.setSecondaryExtractSize(dimension, SampleSet.getOnlyOneSecondaryKey());

        final Dimension result = conditionEngineContext.getSecondaryExtractSize(SampleSet.getOnlyOneSecondaryKey());
        assertEquals("seco", result.getName());
    }
}