package com.bc.fiduceo.matchup.condition;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.Date;

public class ConditionEngineContextTest {

    private ConditionEngineContext conditionEngineContext;
    private Date startDate;
    private Date endDate;

    @Before
    public void setUp() throws Exception {
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
}