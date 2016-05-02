package com.bc.fiduceo.matchup.condition;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.Date;

/**
 * Created by Sabine on 29.04.2016.
 */
public class ConditionsContextTest {

    private ConditionsContext conditionsContext;
    private Date startDate;
    private Date endDate;

    @Before
    public void setUp() throws Exception {
        conditionsContext = new ConditionsContext();
        startDate = new Date();
        endDate = new Date(startDate.getTime() + 100);
    }

    @Test
    public void testValidateTime_afterInitializing() {
        try {
            conditionsContext.validateTime();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("End date and/or start date are not valid.", expected.getMessage());
        }
    }

    @Test
    public void testValidateTime_endDateIsMissing() {
        conditionsContext.setStartDate(startDate);
        try {
            conditionsContext.validateTime();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("End date and/or start date are not valid.", expected.getMessage());
        }
    }

    @Test
    public void testValidateTime_startDateIsMissing() {
        conditionsContext.setEndDate(endDate);
        try {
            conditionsContext.validateTime();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("End date and/or start date are not valid.", expected.getMessage());
        }
    }

    @Test
    public void testValidateTime_endDateIsBeforeStartDate() {
        conditionsContext.setStartDate(endDate);
        conditionsContext.setEndDate(startDate);
        try {
            conditionsContext.validateTime();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("End date and/or start date are not valid.", expected.getMessage());
        }
    }

    @Test
    public void testValidateTime_valid() {
        conditionsContext.setStartDate(startDate);
        conditionsContext.setEndDate(endDate);

        conditionsContext.validateTime();
    }
}