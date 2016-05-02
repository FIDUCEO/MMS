package com.bc.fiduceo.matchup;

import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.*;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class UseCaseConfigTest {

    @Test
    public void testStore() throws IOException {

        final UseCaseConfig useCaseConfig = new MatchupToolUseCaseConfigBuilder("test_use_case")
                    .withTimeDeltaSeconds(12345)
                    .withMaxPixelDistanceKm(14.8f)
                    .withSensors(Arrays.asList(
                                new Sensor("first"),
                                new Sensor("second")))
                    .withDimensions(Arrays.asList(
                                new Dimension("first", 11, 15),
                                new Dimension("second", 3, 5)))
                    .withOutputPath("wherever/you/want/it")
                    .createConfig();

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        useCaseConfig.store(outputStream);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<use-case-config name=\"test_use_case\">");
        pw.println("  <conditions>");
        pw.println("    <time-delta>");
        pw.println("      <time-delta-seconds>12345</time-delta-seconds>");
        pw.println("    </time-delta>");
        pw.println("    <spherical-distance>");
        pw.println("      <max-pixel-distance-km>14.8</max-pixel-distance-km>");
        pw.println("    </spherical-distance>");
        pw.println("  </conditions>");
        pw.println("  <sensors>");
        pw.println("    <sensor>");
        pw.println("      <name>first</name>");
        pw.println("      <primary>false</primary>");
        pw.println("    </sensor>");
        pw.println("    <sensor>");
        pw.println("      <name>second</name>");
        pw.println("      <primary>false</primary>");
        pw.println("    </sensor>");
        pw.println("  </sensors>");
        pw.println("  <dimensions>");
        pw.println("    <dimension name=\"first\">");
        pw.println("      <nx>11</nx>");
        pw.println("      <ny>15</ny>");
        pw.println("    </dimension>");
        pw.println("    <dimension name=\"second\">");
        pw.println("      <nx>3</nx>");
        pw.println("      <ny>5</ny>");
        pw.println("    </dimension>");
        pw.println("  </dimensions>");
        pw.println("  <output-path>wherever/you/want/it</output-path>");
        pw.println("</use-case-config>");
        pw.flush();

        assertThat(sw.toString(), equalToIgnoringWhiteSpace(outputStream.toString()));
    }

}
