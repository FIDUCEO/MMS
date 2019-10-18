package com.bc.fiduceo.reader.caliop;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

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
 */public class CaliopUtilsTest {

    private CaliopUtils caliopUtils;


    /*
    #######################################################################################################
    ###                                                                                                 ###
    ###     ##    ##   #####   ######   ######      ######  ######   #####   ######   #####      ##     ###
    ###     ###  ###  ##   ##  ##   ##  ##            ##    ##      ##    #    ##    ##    #     ##     ###
    ###     ## ## ##  ##   ##  #####    ######        ##    ######    ###      ##      ###       ##     ###
    ###     ##    ##  ##   ##  ##  ##   ##            ##    ##      #    ##    ##    #    ##            ###
    ###     ##    ##   #####   ##   ##  ######        ##    ######   #####     ##     #####      ##     ###
    ###                                                                                                 ###
    #######################################################################################################
     */


    @Before
    public void setUp() {
        caliopUtils = new CaliopUtils();
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        String caliopFileName;
        int[] ymd;

        caliopFileName = "CAL_LID_L2_VFM-Standard-V4-10.2008-05-31T00-11-58ZN.hdf";
        ymd = caliopUtils.extractYearMonthDayFromFilename(caliopFileName);
        assertArrayEquals(new int[]{2008, 5, 31}, ymd);

        caliopFileName = "CAL_LID_L2_05kmCLay-Standard-V4-10.2010-05-28T00-27-29ZD.hdf";
        ymd = caliopUtils.extractYearMonthDayFromFilename(caliopFileName);
        assertArrayEquals(new int[]{2010, 5, 28}, ymd);
    }
}