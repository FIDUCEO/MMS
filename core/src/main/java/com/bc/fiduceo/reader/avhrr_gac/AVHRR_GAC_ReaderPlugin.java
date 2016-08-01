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
package com.bc.fiduceo.reader.avhrr_gac;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderPlugin;

public class AVHRR_GAC_ReaderPlugin implements ReaderPlugin {

    private static final String[] SENSOR_KEYS = {"avhrr-n06", "avhrr-n07", "avhrr-n08", "avhrr-n09", "avhrr-n10", "avhrr-n11", "avhrr-n12", "avhrr-n13", "avhrr-n14", "avhrr-n15", "avhrr-n16", "avhrr-n17", "avhrr-n18", "avhrr-n19", "avhrr-m01", "avhrr-m02"};

    @Override
    public Reader createReader(GeometryFactory geometryFactory) {
        return new AVHRR_GAC_Reader(geometryFactory);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SENSOR_KEYS;
    }
}
