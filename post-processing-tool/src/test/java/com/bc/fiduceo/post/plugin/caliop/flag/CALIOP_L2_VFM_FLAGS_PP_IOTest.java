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

package com.bc.fiduceo.post.plugin.caliop.flag;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.reader.ReaderCache;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(IOTestRunner.class)
public class CALIOP_L2_VFM_FLAGS_PP_IOTest {

    private CALIOP_L2_VFM_FLAGS_PP pp;

    @Before
    public void setUp() throws Exception {
        ReaderFactory.create(new GeometryFactory(GeometryFactory.Type.S2), null);

        pp = new CALIOP_L2_VFM_FLAGS_PP("caliop_vfm.file_name",
                "caliop_vfm.processing_version",
                "caliop_vfm.y",
                "caliop_vfm.Center_Feature_Classification_Flags");
        // In regular usage the PostProcessingContext will be set by post processing framework.
        pp.setContext(createPostProcessingContext());
        // a call setContext(...) generates an framework call to initReaderCache() method
    }

    @After
    public void tearDown() {
        pp.forTestsOnly_dispose();
    }

    @Test
    public void prepare() throws Exception {
        final Path testDirPath = TestUtil.getTestDataDirectory().toPath();
        final Path relMmd15sst = Paths.get("post-processing", "mmd15sst", "mmd15_sst_drifter-sst_amsre-aq_caliop_vfm-cal_2008-149_2008-155.nc");
        final Path absMmd15sst = testDirPath.resolve(relMmd15sst);

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // open the file with NetCDFUtils is needed
        // because the standard open mechanism changes the file size
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        final NetcdfFile reader = NetCDFUtils.openReadOnly(absMmd15sst.toAbsolutePath().toString());

        final String numFlagsDimName = "center-fcf-flags";
        final String dimStr = reader.findVariable(NetcdfFile.makeValidCDLName("caliop_vfm.Latitude")).getDimensionsString() + " " + numFlagsDimName;

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final Variable variable = mock(Variable.class);
        when(writer.addVariable(null, "caliop_vfm.Center_Feature_Classification_Flags", DataType.SHORT, dimStr)).thenReturn(variable);

        //execution
        try {
            pp.prepare(reader, writer);
        } finally {
            reader.close();
        }

        //verification
        verify(writer, times(1)).addDimension(null, numFlagsDimName, 545);
        verify(writer, times(1)).addVariable(null, "caliop_vfm.Center_Feature_Classification_Flags", DataType.SHORT, dimStr);
        verify(variable, times(1)).addAttribute(eq(new Attribute("_Unsigned", "true")));
        verify(variable, times(1)).addAttribute(eq(new Attribute("units", "NoUnits")));
        verify(variable, times(1)).addAttribute(eq(new Attribute("format", "UInt_16")));
        verify(variable, times(1)).addAttribute(eq(new Attribute("valid_range", "1...49146")));
        verifyNoMoreInteractions(variable);
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void compute() throws Exception {
        pp.filenameFieldSize = 200;
        pp.processingVersionSize = 30;
        pp.targetFlagsVariable = mock(Variable.class);

        pp.processingVersionVariable = mock(Variable.class);
        when(pp.processingVersionVariable.read(new int[]{0, 0}, new int[]{1, 30})).thenReturn(NetCDFUtils.create("   4.10   ".toCharArray()));
        when(pp.processingVersionVariable.read(new int[]{1, 0}, new int[]{1, 30})).thenReturn(NetCDFUtils.create("   v4   ".toCharArray()));

        pp.fileNameVariable = mock(Variable.class);
        when(pp.fileNameVariable.read(new int[]{0, 0}, new int[]{1, 200})).thenReturn(NetCDFUtils.create("   CAL_LID_L2_VFM-Standard-V4-10.2008-06-02T10-39-30ZD.hdf   ".toCharArray()));
        when(pp.fileNameVariable.read(new int[]{1, 0}, new int[]{1, 200})).thenReturn(NetCDFUtils.create("   CAL_LID_L2_VFM-Standard-V4-10.2011-01-02T23-37-04ZD.hdf   ".toCharArray()));

        final Variable yVar = mock(Variable.class);
        when(yVar.read()).thenReturn(NetCDFUtils.create(new int[]{3, 33}));

        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.findDimension("caliop_vfm-cal_ny")).thenReturn(new Dimension("name", 3));
        when(reader.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(new Dimension("name", 2));
        when(reader.findVariable("caliop_vfm\\.y")).thenReturn(yVar);

        final CapturingWriter writer = new CapturingWriter();

        //execution
        pp.compute(reader, writer);

        //verification
        verify(yVar, times(1)).read();
        verify(reader, times(1)).findDimension("caliop_vfm-cal_ny");
        verify(reader, times(1)).findVariable("caliop_vfm\\.y");
        verifyNoMoreInteractions(yVar, reader);

        assertEquals(1, writer.variables.size());
        assertEquals(6, writer.origins.size());
        assertArrayEquals(new int[]{0, 0, 0, 0}, writer.origins.get(0));
        assertArrayEquals(new int[]{0, 1, 0, 0}, writer.origins.get(1));
        assertArrayEquals(new int[]{0, 2, 0, 0}, writer.origins.get(2));
        assertArrayEquals(new int[]{1, 0, 0, 0}, writer.origins.get(3));
        assertArrayEquals(new int[]{1, 1, 0, 0}, writer.origins.get(4));
        assertArrayEquals(new int[]{1, 2, 0, 0}, writer.origins.get(5));
        assertEquals(6, writer.arrays.size());
        ArrayList<Array> arrays = writer.arrays;
        for (Array array : arrays) {
            assertEquals(545, array.getSize());
        }
    }

    @Test
    public void testReaderCacheIsInitialized() {
        assertNotNull(pp.forTestsOnly_getReaderCache());
    }

    @Test
    public void testDisposeIsCallingReaderCacheClose() throws Exception {
        //preparation
        final ReaderCache readerCache = mock(ReaderCache.class);
        pp.forTestsOnly_setReaderCache(readerCache);

        //execution
        pp.forTestsOnly_dispose();

        //verification
        verify(readerCache, times(1)).close();
        verifyNoMoreInteractions(readerCache);
    }

    private PostProcessingContext createPostProcessingContext() throws IOException {
        final PostProcessingContext pp_context = new PostProcessingContext();
        pp_context.setSystemConfig(createSystemConfig());
        pp_context.setReaderFactory(ReaderFactory.get());
        return pp_context;
    }

    private SystemConfig createSystemConfig() throws IOException {
        final String archivePath = TestUtil.getTestDataDirectory().getAbsolutePath();
        return SystemConfig.load(new ByteArrayInputStream(
                ("<system-config>" +
                        "    <geometry-library name=\"S2\"/>" +
                        "    <reader-cache-size>24</reader-cache-size>" +
                        "    <archive>" +
                        "        <root-path>" + archivePath + "</root-path>" +
                        "        <rule sensors=\"drifter-sst, ship-sst\">insitu/SENSOR/VERSION</rule>" +
                        "    </archive>" +
                        "</system-config>").getBytes()
        ));
    }

    class CapturingWriter extends NetcdfFileWriter {

        final ArrayList<int[]> origins;
        final HashSet<Variable> variables;
        final ArrayList<Array> arrays;

        protected CapturingWriter() throws IOException {
            super(Version.netcdf3, "test", false, null);
            origins = new ArrayList<>();
            variables = new HashSet<>();
            arrays = new ArrayList<>();
        }

        @Override
        public void write(Variable v, int[] origin, Array values) {
            variables.add(v);
            origins.add(origin.clone());
            arrays.add(values);
        }
    }
}