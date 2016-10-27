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
 */

package com.bc.fiduceo;

import com.bc.fiduceo.geometry.GeometryUtil;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class InsituConverter {

    public static void main(String[] args) throws IOException {
        final String format = args[0];
        final Path srcDirPath = Paths.get(args[1]);
        convertToKML(srcDirPath);
    }

    private static void convertToKML(Path srcDirPath) throws IOException {
        final Iterator<Path> iterator = Files.list(srcDirPath).iterator();
        while (iterator.hasNext()) {
            NetcdfFile netcdfFile = null;
            try {
                Path next = iterator.next();
                if (next.toString().endsWith(".kml")) {
                    Files.delete(next);
                    continue;
                }
                netcdfFile = NetcdfFile.open(next.toString());
                final Group rootGroup = netcdfFile.getRootGroup();
                final Variable latVar = netcdfFile.findVariable(rootGroup, "insitu.lat");
                final Variable lonVar = netcdfFile.findVariable(rootGroup, "insitu.lon");
                final float[] lats = (float[]) latVar.read().getStorage();
                final float[] lons = (float[]) lonVar.read().getStorage();
                final String s = GeometryUtil.toKml(lats, lons);
                final String fileName = next.getFileName().toString();
                final String outName = fileName.substring(0, fileName.lastIndexOf("."))+".kml";
                final Path parent = next.getParent();
                final Path outPath = parent.resolve(outName);
                final FileOutputStream outputStream = new FileOutputStream(outPath.toFile());
                final PrintWriter pw = new PrintWriter(outputStream);
                pw.print(s);
                pw.flush();
                pw.close();
            } finally {
                if (netcdfFile != null) {
                    netcdfFile.close();
                }
            }
        }
    }
}
