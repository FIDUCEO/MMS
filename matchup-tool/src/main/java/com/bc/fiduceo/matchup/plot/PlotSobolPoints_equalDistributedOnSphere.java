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
package com.bc.fiduceo.matchup.plot;

import com.bc.fiduceo.core.SamplingPoint;
import com.bc.fiduceo.util.SobolSamplingPointGenerator;

import java.io.IOException;
import java.util.List;

public class PlotSobolPoints_equalDistributedOnSphere {

    public static void main(String[] args) throws IOException {
        final List<SamplingPoint> samples = new SobolSamplingPointGenerator(SobolSamplingPointGenerator.Distribution.FLAT).createSamples(100000, 874658237, 0, 100);
        new SamplingPointPlotter(1800, 900).show(true).samples(samples).filePath("/fs1/temp/Tom/flat.png").plot();
    }
}
