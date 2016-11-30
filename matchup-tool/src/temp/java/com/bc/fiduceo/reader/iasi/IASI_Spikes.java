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

package com.bc.fiduceo.reader.iasi;

import static java.lang.System.exit;

import com.bc.ceres.binio.CollectionType;
import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.Type;
import com.bc.ceres.binio.internal.CompoundMemberImpl;
import com.bc.ceres.binio.internal.CompoundTypeImpl;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

public class IASI_Spikes {

    public static void main(String[] args) throws Exception {

//        final File logfile = new File("F:/Development Temp Dirs/Fiduceo/IASI/IASI_" + System.currentTimeMillis() + ".log");
//        final FileOutputStream logOut = new FileOutputStream(logfile);
//        System.setOut(new PrintStream(logOut));

        final File file = new File("F:/Development Temp Dirs/Fiduceo/IASI/IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat");
        final IasiFile iasiFile = new IasiFile(file);
        final int mdrCount = iasiFile.getMdrCount();
        for (int i = 0; i < 1; i++) {
            final IasiSample sample = iasiFile.createSample(i);
            final Point2D point2D = iasiFile.readGeoPosition(0);
            System.out.println("point2D = " + point2D);

//            final Point2D geoPosition = sample.getGeoPosition();
//            System.out.println("geoPosition = " + geoPosition);
            //        final IasiSpectrum iasiSpectrum = sample.getIasiSpectrum();
            //        System.out.println("iasiSpectrum = " + iasiSpectrum);

            final RadianceAnalysis radianceAnalysis = sample.getRadianceAnalysis();
            System.out.println("radianceAnalysis = " + radianceAnalysis);

//            final double solarAzimuthAngle = sample.getSolarAzimuthAngle();
//            System.out.println("solarAzimuthAngle = " + solarAzimuthAngle);
//            final double solarZenithAngle = sample.getSolarZenithAngle();
//            System.out.println("solarZenithAngle = " + solarZenithAngle);
//            final double viewAzimuthAngle = sample.getViewAzimuthAngle();
//            System.out.println("viewAzimuthAngle = " + viewAzimuthAngle);
//            final double viewZenithAngle = sample.getViewZenithAngle();
//            System.out.println("viewZenithAngle = " + viewZenithAngle);
        }

//        printData(iasiFile.getMdrData());
//        printData(iasiFile.getMetopData());
//        final int mdrCount = iasiFile.getMdrCount();
//        System.out.println("mdrCount = " + mdrCount);
//        final int mdrElementCount = iasiFile.getMdrData().getElementCount();
//        System.out.println("mdrElementCount = " + mdrElementCount);
//        final long mdrSize = iasiFile.getMdrData().getSize();
//        System.out.println("mdrSize = " + mdrSize);

        exit(0);
    }

    private static void printData(SequenceData sequenceData) throws IOException {
        final CollectionType type = sequenceData.getType();
        System.out.println("********************************************************************");
        System.out.println("********************************************************************");
        System.out.println("CollectionDataTypeName: " + type.getName());
        System.out.println("CollectionDataTypeSize: " + type.getSize());
        System.out.println("********************************************************************");
        final int count = sequenceData.getElementCount();
        System.out.println("Elements: " + count);
        System.out.println("*********");
        for (int i = 0; i < count; i++) {
            final CompoundData compound = sequenceData.getCompound(i);
            final CompoundType type1 = compound.getType();
            if (type1 instanceof CompoundTypeImpl) {
                final CompoundTypeImpl compoundType = (CompoundTypeImpl) type1;
                final String name = compoundType.getName();
                System.out.println("    " + i + " name = " + name);
                final CompoundMember[] members = compoundType.getMembers();
                printMembers("        ", members);
            }
        }
    }

    private static void printData(CompoundData collectionData) throws IOException {
        final CollectionType type = collectionData.getType();
        System.out.println("********************************************************************");
        System.out.println("********************************************************************");
        System.out.println("CollectionDataTypeName: " + type.getName());
        System.out.println("CollectionDataTypeSize: " + type.getSize());
        System.out.println("********************************************************************");
        final int count = collectionData.getElementCount();
        System.out.println("Elements: " + count);
        System.out.println("*********");
        for (int i = 0; i < count; i++) {
            final CompoundMember member = collectionData.getType().getMember(i);
            final String name = member.getName();
            System.out.print("    " + i + " name = " + name);
            System.out.println("    Size = " + member.getSize());
            final CompoundMember[] members = ((CompoundTypeImpl) member.getType()).getMembers();
            printMembers("        ", members);
        }
    }

    private static void printMembers(String indent, CompoundMember[] members) {
        System.out.println(indent + "Member: " + members.length);
        System.out.println(indent + "*********");
        final String nextIndent = indent + "    ";
        for (int j = 0; j < members.length; j++) {
            CompoundMember member = members[j];
            System.out.print(indent);
            final String name = member.getName();
            System.out.print("" + j + " " + name);
//            System.out.print("        " + j + " name = " + member.getName());
            System.out.print("   size = " + member.getSize());
            System.out.print("   type = " + member.getType());
            if (member instanceof CompoundMemberImpl) {
                final CompoundMemberImpl cMember = (CompoundMemberImpl) member;
                final Object metadata = cMember.getMetadata();
                System.out.print("   metadata = " + metadata);
            }
            System.out.println();
            final Type type = member.getType();
            if (type instanceof CompoundTypeImpl) {
                final CompoundTypeImpl compoundType = (CompoundTypeImpl) type;
                final CompoundMember[] compMembers = compoundType.getMembers();
                printMembers(nextIndent, compMembers);
            }
        }
    }

}
