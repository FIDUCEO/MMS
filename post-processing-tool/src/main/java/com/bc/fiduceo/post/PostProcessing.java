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

import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.archive.ArchiveConfig;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.ReaderCache;
import com.bc.fiduceo.reader.ReaderFactory;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class PostProcessing {

    private PostProcessingContext context;
    private ReaderCache readerCache;

    public PostProcessingContext getContext() {
        return context;
    }

    public void setContext(PostProcessingContext context) {
        this.context = context;
        initReaderCache();
    }

    /**
     * Is called after PostProcessingContext is set. Override this method only if the
     * concrete PostProcessing implementation needs a {@link ReaderCache}
     * Don't forget to close and clean up the {@link ReaderCache} instance on dispose.
     */
    protected void initReaderCache() {
        // the default implementation does nothing. Plugins may override to implement their clean-up chores. tb 2017-07-17
    }

    /**
     * Allows the plugin to define a list of variable names which shall not be copied to the
     * target MMD. The default implementation returns an empty list. Overwrite to add variable names.
     *
     * @return the list of variable names to remove
     */
    protected List<String> getVariableNamesToRemove() {
        return new ArrayList<>();
    }

    /**
     * Invokes the plugin preparation phase. Plugins shall add target variables, attributes,  dimensions and whatever
     * else is required to the NetcdfFileWriter. The NetcdfFileWriter has created a new file and is in "define-mode" when
     * passed in. The NetcdfFile "reader" gives access to the input MMD, this reader is open and in read-only mode.
     *
     * @param reader the NetcdfFile opened on the input MMD file
     * @param writer the NetcdfFileWriter opened on the target MMD, in define-mode
     *
     * @throws IOException           on disk access failures
     * @throws InvalidRangeException on other occasions
     */
    protected abstract void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException;

    /**
     * Invokes the plugin processing phase. Plugins shall compute the target data for all variables they have defined and
     * write the data to the variables acquired from the NetcdfFileWriter.
     * The NetcdfFileWriter has written the new file and is in "write-mode" when
     * passed in. The NetcdfFile "reader" gives access to the input MMD, this reader is open and in read-only mode.
     *
     * @param reader the NetcdfFile opened on the input MMD file
     * @param writer the NetcdfFileWriter opened on the target MMD, in write-mode
     *
     * @throws IOException           on disk access failures
     * @throws InvalidRangeException on other occasions
     */
    protected abstract void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException;

    /**
     * Is called by the engine when the post-processing job is done to allow cleanup actions.
     */
    protected void dispose() {
        // the default implementation does nothing. Plugins may override to implement their clean-up chores. tb 2017-07-17
    }

    public static ReaderCache createReaderCache(PostProcessingContext context) {
        // todo se/** 3 put maxCacheSize to system configuration file
        final int maxCacheSize = 70;
        final SystemConfig systemConfig = context.getSystemConfig();
        final ArchiveConfig archiveConfig = systemConfig.getArchiveConfig();
        final Archive archive = new Archive(archiveConfig);
        final String geometryLibraryType = systemConfig.getGeometryLibraryType();
        final ReaderFactory readerFactory = ReaderFactory.get(new GeometryFactory(geometryLibraryType));
        return new ReaderCache(maxCacheSize, readerFactory, archive);
    }
}
