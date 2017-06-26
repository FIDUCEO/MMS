package com.bc.fiduceo.matchup.strategy;

import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.tool.ToolContext;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Sabine on 16.03.2017.
 */
public class SeedPointMatchupStrategy_Errors_Test {

    private SeedPointMatchupStrategy matchupStrategy;
    private ToolContext toolContext;
    private UseCaseConfig useCaseConfig;

    @Before
    public void setUp() throws Exception {
        matchupStrategy = new SeedPointMatchupStrategy(Logger.getAnonymousLogger());
        toolContext = new ToolContext();
        useCaseConfig = new UseCaseConfig();
        toolContext.setUseCaseConfig(useCaseConfig);
    }

    @Test
    public void that_createMatchupCollection_throwsRuntimeException_ifNumRandomSeedPointLessThanOne() throws Exception {
        useCaseConfig.setNumRandomSeedPoints(0);

        try {
            matchupStrategy.createMatchupCollection(toolContext);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Number of random seed points greater than zero expected.", expected.getMessage());
        }
    }
}