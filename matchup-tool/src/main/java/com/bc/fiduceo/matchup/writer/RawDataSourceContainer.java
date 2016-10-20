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

import com.bc.fiduceo.reader.RawDataSource;

import java.io.IOException;
import java.nio.file.Path;

class RawDataSourceContainer {

    private RawDataSource source;
    private Path sourcePath;

    public void setSource(RawDataSource source) {
        this.source = source;
    }

    public RawDataSource getSource() {
        return source;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(Path sourcePath) throws IOException {
        this.sourcePath = sourcePath;
        final RawDataSource source = getSource();
        if (source != null) {
            source.close();
            source.open(sourcePath.toFile());
        }
    }
}
