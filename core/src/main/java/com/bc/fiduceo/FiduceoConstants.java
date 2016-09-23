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
package com.bc.fiduceo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FiduceoConstants {

    private static final String TIME_STAMP;

    public static final String VERSION;
    public static final String VERSION_NUMBER;

    static {
        InputStream in = FiduceoConstants.class.getResourceAsStream("/fiduceo-version.properties");
        final Properties fiduceoVersionProperties = new Properties();
        try {
            fiduceoVersionProperties.load(in);
            VERSION_NUMBER = (String) fiduceoVersionProperties.get("version");
            TIME_STAMP = (String) fiduceoVersionProperties.get("timestamp");
            VERSION = String.format("Fiduceo version %s (built %s)", VERSION_NUMBER, TIME_STAMP);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
