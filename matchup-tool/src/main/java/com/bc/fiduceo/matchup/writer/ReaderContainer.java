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

import java.io.IOException;
import java.nio.file.Path;

class ReaderContainer {

    private Reader reader;
    private Path sourcePath;

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Reader getReader() {
        return reader;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(Path sourcePath) throws IOException {
        this.sourcePath = sourcePath;
        if (reader != null) {
            reader.close();
            reader.open(sourcePath.toFile());
        }
    }
}
