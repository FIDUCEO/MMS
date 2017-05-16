/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.matchup.strategy;

import static org.junit.Assert.*;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.core.UseCaseConfigBuilder;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AbstractMatchupStrategyTest {

    @Test
    public void testAssignSensor() {
        final Sensor sensor = new Sensor("Klaus", "v1.0");
        final QueryParameter parameter = new QueryParameter();

        AbstractMatchupStrategy.assignSensor(parameter, sensor);

        assertEquals("Klaus", parameter.getSensorName());
        assertEquals("v1.0", parameter.getVersion());
    }

    @Test
    public void testAssignSensor_withoutVersion() {
        final Sensor sensor = new Sensor("Marie", null);
        final QueryParameter parameter = new QueryParameter();

        AbstractMatchupStrategy.assignSensor(parameter, sensor);

        assertEquals("Marie", parameter.getSensorName());
        assertEquals(null, parameter.getVersion());
    }

    @Test
    public void testGetPrimarySensorParameter() {
        final ToolContext context = new ToolContext();
        context.setStartDate(TimeUtils.parseDOYBeginOfDay("2002-23"));
        context.setEndDate(TimeUtils.parseDOYEndOfDay("2002-23"));

        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor sensor = new Sensor("amsub-n16");
        sensor.setPrimary(true);
        sensorList.add(sensor);
        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder.build("name")
                .withSensors(sensorList)
                .createConfig();
        context.setUseCaseConfig(useCaseConfig);

        final QueryParameter parameter = AbstractMatchupStrategy.getPrimarySensorParameter(context);
        assertNotNull(parameter);
        assertEquals("amsub-n16", parameter.getSensorName());
        assertNull(parameter.getVersion());
        TestUtil.assertCorrectUTCDate(2002, 1, 23, 0, 0, 0, 0, parameter.getStartTime());
        TestUtil.assertCorrectUTCDate(2002, 1, 23, 23, 59, 59, 999, parameter.getStopTime());
    }

    @Test
    public void testGetPrimarySensorParameter_withDataVersion() {
        final ToolContext context = new ToolContext();

        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor sensor = new Sensor("amsub-n16");
        sensor.setPrimary(true);
        sensor.setDataVersion("v23.5");
        sensorList.add(sensor);
        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder.build("name")
                .withSensors(sensorList)
                .createConfig();
        context.setUseCaseConfig(useCaseConfig);

        final QueryParameter parameter = AbstractMatchupStrategy.getPrimarySensorParameter(context);
        assertNotNull(parameter);
        assertEquals("amsub-n16", parameter.getSensorName());
        assertEquals("v23.5", parameter.getVersion());
    }

    @Test
    public void testGetPrimarySensorParameter_missingPrimarySensor() {
        final ToolContext context = new ToolContext();

        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor sensor = new Sensor("amsub-n16");
        sensorList.add(sensor);

        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder.build("testName")
                .withSensors(sensorList)
                .createConfig();
        context.setUseCaseConfig(useCaseConfig);

        try {
            AbstractMatchupStrategy.getPrimarySensorParameter(context);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetSecondarySensorParameter() {
        final UseCaseConfig config = mock(UseCaseConfig.class);

        final List<Sensor> additionalSensors = new ArrayList<>();
        additionalSensors.add(new Sensor("the sensor"));
        when(config.getSecondarySensors()).thenReturn(additionalSensors);

        final Date startDate = TimeUtils.parseDOYBeginOfDay("1997-34");
        final Date endDate = TimeUtils.parseDOYEndOfDay("1997-34");

        final List<QueryParameter> parameters = AbstractMatchupStrategy.getSecondarySensorParameter(config, startDate, endDate);
        assertNotNull(parameters);
        assertEquals(1, parameters.size());
        final QueryParameter parameter = parameters.get(0);
        assertNotNull(parameter);
        assertEquals("the sensor", parameter.getSensorName());
        assertNull(parameter.getVersion());
        TestUtil.assertCorrectUTCDate(1997, 2, 3, 0, 0, 0, parameter.getStartTime());
        TestUtil.assertCorrectUTCDate(1997, 2, 3, 23, 59, 59, parameter.getStopTime());
    }

    @Test
    public void testGetSecondarySensorParameter_withDataVersion() {
        final UseCaseConfig config = mock(UseCaseConfig.class);

        final List<Sensor> additionalSensors = new ArrayList<>();
        additionalSensors.add(new Sensor("the sensor", "version_string"));
        when(config.getSecondarySensors()).thenReturn(additionalSensors);

        final Date startDate = TimeUtils.parseDOYBeginOfDay("1997-35");
        final Date endDate = TimeUtils.parseDOYEndOfDay("1997-35");

        final List<QueryParameter> parameters = AbstractMatchupStrategy.getSecondarySensorParameter(config, startDate, endDate);
        assertNotNull(parameters);
        assertEquals(1, parameters.size());
        final QueryParameter parameter = parameters.get(0);
        assertNotNull(parameter);
        assertEquals("the sensor", parameter.getSensorName());
        assertEquals("version_string", parameter.getVersion());
        TestUtil.assertCorrectUTCDate(1997, 2, 4, 0, 0, 0, parameter.getStartTime());
        TestUtil.assertCorrectUTCDate(1997, 2, 4, 23, 59, 59, parameter.getStopTime());
    }

    @Test
    public void testGetPixelLocator_notSegmented() throws Exception {
        final Reader reader = mock(Reader.class);
        final PixelLocator locator = mock(PixelLocator.class);
        when(reader.getPixelLocator()).thenReturn(locator);
        final Polygon polygon = mock(Polygon.class);
        final boolean segmented = false;

        final PixelLocator pixelLocator = AbstractMatchupStrategy.getPixelLocator(reader, segmented, polygon);

        verify(reader, times(1)).getPixelLocator();
        verifyNoMoreInteractions(reader);
        verifyNoMoreInteractions(polygon);
        assertNotNull(pixelLocator);
        assertSame(locator, pixelLocator);
    }

    @Test
    public void testGetPixelLocator_segmented() throws Exception {
        final Reader reader = mock(Reader.class);
        final PixelLocator locator = mock(PixelLocator.class);
        final Polygon polygon = mock(Polygon.class);
        when(reader.getSubScenePixelLocator(polygon)).thenReturn(locator);
        final boolean segmented = true;

        final PixelLocator pixelLocator = AbstractMatchupStrategy.getPixelLocator(reader, segmented, polygon);

        verify(reader, times(1)).getSubScenePixelLocator(same(polygon));
        verifyNoMoreInteractions(reader);
        verifyNoMoreInteractions(polygon);
        assertNotNull(pixelLocator);
        assertSame(locator, pixelLocator);
    }

    @Test
    public void testIsSegmented() throws Exception {
        final GeometryCollection collection = mock(GeometryCollection.class);

        when(collection.getGeometries()).thenReturn(new Geometry[1]);
        assertEquals(false, AbstractMatchupStrategy.isSegmented(collection));

        when(collection.getGeometries()).thenReturn(new Geometry[2]);
        assertEquals(true, AbstractMatchupStrategy.isSegmented(collection));

        verify(collection, times(2)).getGeometries();
        verifyNoMoreInteractions(collection);
    }
}
