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

package com.bc.fiduceo.tool;


import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.util.TempFileUtils;

import java.util.Date;

public class ToolContext {

    private Date startDate;
    private Date endDate;
    private Storage storage;
    private SystemConfig systemConfig;
    private UseCaseConfig useCaseConfig;
    private GeometryFactory geometryFactory;
    private ReaderFactory readerFactory;
    private TempFileUtils tempFileUtils;
    private Archive archive;

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Storage getStorage() {
        return storage;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public void setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    public void setUseCaseConfig(UseCaseConfig useCaseConfig) {
        this.useCaseConfig = useCaseConfig;
    }

    public UseCaseConfig getUseCaseConfig() {
        return useCaseConfig;
    }

    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public ReaderFactory getReaderFactory() {
        return readerFactory;
    }

    public void setReaderFactory(ReaderFactory readerFactory) {
        this.readerFactory = readerFactory;
    }

    public void setTempFileUtils(TempFileUtils tempFileUtils) {
        this.tempFileUtils = tempFileUtils;
    }

    public TempFileUtils getTempFileUtils() {
        return tempFileUtils;
    }

    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }
}
