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
package com.bc.fiduceo.reader.amsu_mhs;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.reader.Reader;

public class ForReaderFactoryTest_AMSUB_MHS_L1C_Reader {
    public static void checkInstance(Reader reader) {
        assertThat(reader, is(instanceOf(AMSUB_MHS_L1C_Reader.class)));
    }
}
