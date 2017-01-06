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

package com.bc.fiduceo.matchup.writer;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.matchup.MatchupToolUseCaseConfigBuilder;
import org.junit.*;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.iosp.netcdf3.N3iosp;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AbstractMmdWriterTest {

    private static final String fillValueName = "_FillValue";

    @Test
    public void testCreateUseCaseAttributesGroupInMmdFile() throws Exception {
        final Sensor primarySensor = new Sensor("SensorName1");
        primarySensor.setPrimary(true);
        final List<Sensor> sensorList = Arrays.asList(
                primarySensor,
                new Sensor("SensorName2"),
                new Sensor("SensorName3")
        );

        final UseCaseConfig useCaseConfig = new MatchupToolUseCaseConfigBuilder("NameOfTheUseCase")
                .withTimeDeltaSeconds(234)
                .withMaxPixelDistanceKm(12.34f)
                .withSensors(sensorList)
                .withDimensions(Arrays.asList(
                        new Dimension("SensorName1", 1, 2),
                        new Dimension("SensorName2", 3, 4),
                        new Dimension("SensorName3", 5, 6)
                ))
                .createConfig();

        final NetcdfFileWriter mockWriter = mock(NetcdfFileWriter.class);

        //test
        AbstractMmdWriter.createUseCaseAttributes(mockWriter, useCaseConfig);

        //verification
        final String useCaseAttributeName = "use-case-configuration";
        final String expectedCommentText = "This MMD file is created based on the use case configuration " +
                "documented in the attribute '" + useCaseAttributeName + "'.";
        verify(mockWriter).addGroupAttribute(isNull(Group.class), eq(new Attribute("comment", expectedCommentText)));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        useCaseConfig.store(outputStream);
        verify(mockWriter).addGroupAttribute(isNull(Group.class), eq(new Attribute(useCaseAttributeName, outputStream.toString())));
        verifyNoMoreInteractions(mockWriter);
    }

    @Test
    public void testEnsureFillValue_Double() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.DOUBLE.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(N3iosp.NC_FILL_DOUBLE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Float() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.FLOAT.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(N3iosp.NC_FILL_FLOAT, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Long() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.LONG.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(N3iosp.NC_FILL_LONG, attribute.getNumericValue().longValue());
    }

    @Test
    public void testEnsureFillValue_Integer() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.INT.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(N3iosp.NC_FILL_INT, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Short() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.SHORT.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(N3iosp.NC_FILL_SHORT, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Byte() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.BYTE.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(N3iosp.NC_FILL_BYTE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Double_existing() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.DOUBLE.name());
        final double fillValue = 1234.5678;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Float_existing() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.FLOAT.name());
        final float fillValue = 1234.5678f;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Long_existing() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.LONG.name());
        final long fillValue = 12345678912345678L;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getValue(0));
    }

    @Test
    public void testEnsureFillValue_Integer_existing() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.INT.name());
        final int fillValue = 123456789;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Short_existing() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.SHORT.name());
        final short fillValue = 12345;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Byte_existing() throws Exception {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setDataType(DataType.BYTE.name());
        final byte fillValue = 123;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }
}
