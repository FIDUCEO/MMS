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
package com.bc.fiduceo.reader;

public class AMSU_MHS_L1B_ReaderPlugin implements ReaderPlugin {

    // @todo 2 tb/mb where are the MHS sensor-keys? Please either implement here or rename reader class (and the plugin) 2016-03-11
    private static final String[] SENSOR_KEYS = {"amsub-tn", "amsub-n06", "amsub-n07", "amsub-n08", "amsub-n09",
                                                 "amsub-n10", "amsub-n11", "amsub-n12", "amsub-n14", "amsub-n15", "amsub-n16", "amsub-n17", "amsub-n18", "amsub-n19"};


    public String[] getSupportedSensorKeys() {
        return SENSOR_KEYS;
    }

    @Override
    public Reader createReader() {
        return new AMSU_MHS_L1B_Reader();
    }
}
