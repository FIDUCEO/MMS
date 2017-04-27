/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.reader.Reader;

import java.nio.file.Path;

public class ReaderContainer {

    private Reader reader;
    private Path sourcePath;
    private String processingVersion;

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Reader getReader() {
        return reader;
    }

    public void setProcessingVersion(String processingVersion) {
        this.processingVersion = processingVersion;
    }

    public String getProcessingVersion() {
        return processingVersion;
    }

    Path getSourcePath() {
        return sourcePath;
    }

    void setSourcePath(Path sourcePath) {
        this.sourcePath = sourcePath;
    }
}
