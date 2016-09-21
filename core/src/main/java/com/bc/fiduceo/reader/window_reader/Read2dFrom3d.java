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
package com.bc.fiduceo.reader.window_reader;

public abstract class Read2dFrom3d extends WindowReader {

    protected final Number fillValue;
    protected final int[] initialIndexPos;
    protected int xIndex;
    protected int yIndex;

    public Read2dFrom3d(Number fillValue, String[] offsetMapping) {
        this.fillValue = fillValue;
        initialIndexPos = new int[3];
        initializeIndex(offsetMapping);
    }

    private void initializeIndex(String[] offsetMapping) {
        for (int i = 0; i < offsetMapping.length; i++) {
            String s = offsetMapping[i];
            if ("x".equalsIgnoreCase(s)) {
                xIndex = i;
            } else if ("y".equalsIgnoreCase(s)) {
                yIndex = i;
            } else {
                initialIndexPos[i] = Integer.parseInt(s);
            }
        }
    }
}
