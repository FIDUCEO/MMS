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
 */

package com.bc.fiduceo.post;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Arrays;

public class PostProcessingTest {

    private PostProcessing postProcessing;

    @Before
    public void setUp() throws Exception {
        postProcessing = new PostProcessing() {
            @Override
            protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) {

            }

            @Override
            protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {

            }
        };
    }

    @Test
    public void testContextProperty() throws Exception {
        final PostProcessingContext context = new PostProcessingContext();

        postProcessing.setContext(context);

        assertSame(context, postProcessing.getContext());
    }

    @Test
    public void getFileNameVariable_Success() throws IOException {
        final NetcdfFile reader = mock(NetcdfFile.class);
        final Variable expectedVariable = mock(Variable.class);
        when(reader.findVariable(null, "sensor-name_file_name")).thenReturn(expectedVariable);

        //action
        final Variable fileNameVariable = PostProcessing.getFileNameVariable(reader, "sensor-name");

        assertSame(expectedVariable, fileNameVariable);
    }

    @Test
    public void getFileNameVariable_VariableDoesNotExist() throws IOException {
        final NetcdfFile reader = mock(NetcdfFile.class);

        try {
            PostProcessing.getFileNameVariable(reader, "sensor-name");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), is(equalTo("Input Variable 'sensor-name_file_name' not present in input file")));
            assertThat(expected.getClass(), is(equalTo(RuntimeException.class)));
        }
    }

    @Test
    public void getProcessingVersionVariable_Success() throws IOException {
        final NetcdfFile reader = mock(NetcdfFile.class);
        final Variable expectedVariable = mock(Variable.class);
        when(reader.findVariable(null, "sensor-name_processing_version")).thenReturn(expectedVariable);

        //action
        final Variable processingVersionVariable = PostProcessing.getProcessingVersionVariable(reader, "sensor-name");

        assertSame(expectedVariable, processingVersionVariable);
    }

    @Test
    public void getProcessingVersionVariable_VariableDoesNotExist() throws IOException {
        final NetcdfFile reader = mock(NetcdfFile.class);

        try {
            PostProcessing.getProcessingVersionVariable(reader, "sensor-name");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), is(equalTo("Input Variable 'sensor-name_processing_version' not present in input file")));
            assertThat(expected.getClass(), is(equalTo(RuntimeException.class)));
        }
    }

    @Test
    public void getSourceFileName_Success() throws Exception {
        final String fileNamePattern = ".*_\\d{8}_\\d{8}.nc";
        final Array array = mock(Array.class);

        final String validSourceFileName = "file_name_12345678_12345678.nc";
        when(array.getStorage()).thenReturn(Arrays.copyOf(validSourceFileName.toCharArray(), 180));

        final Variable fileNameVariable = mock(Variable.class);
        when(fileNameVariable.read(new int[]{0, 0}, new int[]{1, 180})).thenReturn(array);

        final String sourceFileName = PostProcessing.getSourceFileName(fileNameVariable, 0, 180, fileNamePattern);

        assertEquals(validSourceFileName, sourceFileName);
    }

    @Test
    public void getSourceFileName_ThrowsRuntimeException_BecauseTheFileNameDoesNotMatchTheExpectedPattern() throws Exception {
        final String fileNamePattern = ".*_\\d{8}_\\d{8}.nc";
        final String invalidFileName = "invalid_file_name_12345678.nc";
        final String expectedErrorMessage =
                    "The file name '" + invalidFileName + "' does not match the regular expression '" + fileNamePattern + "'";

        final Array array = mock(Array.class);
        final Variable fileNameVariable = mock(Variable.class);
        when(array.getStorage()).thenReturn(Arrays.copyOf(invalidFileName.toCharArray(), 180));
        when(fileNameVariable.read(new int[]{0, 0}, new int[]{1, 180})).thenReturn(array);

        try {
            PostProcessing.getSourceFileName(fileNameVariable, 0, 180, fileNamePattern);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(expectedErrorMessage, expected.getMessage());
        }
    }

    @Test
    public void getSourceFileName_VariableThrowsInvalidRangeException_IsNotCatched() throws Exception {
        final Variable fileNameVariable = mock(Variable.class);
        when(fileNameVariable.read(new int[]{0, 0}, new int[]{1, 180})).thenThrow(new InvalidRangeException("mess"));

        try {
            PostProcessing.getSourceFileName(fileNameVariable, 0, 180, "");
            fail("InvalidRangeException expected");
        } catch (InvalidRangeException expected) {
            assertEquals("mess", expected.getMessage());
        }
    }

    @Test
    public void getSourceFileName_VariableThrowsIOException_IsNotCatched() throws Exception {
        final Variable fileNameVariable = mock(Variable.class);
        when(fileNameVariable.read(new int[]{0, 0}, new int[]{1, 180})).thenThrow(new IOException("mess"));

        try {
            PostProcessing.getSourceFileName(fileNameVariable, 0, 180, "");
            fail("IOException expected");
        } catch (IOException expected) {
            assertEquals("mess", expected.getMessage());
        }
    }


}
