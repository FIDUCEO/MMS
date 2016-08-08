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

package com.bc.fiduceo.core;


import java.util.Date;

public class TimeRange {

    private Date startDate;
    private Date stopDate;

    public TimeRange(Date startDate, Date stopDate) {
        this.startDate = startDate;
        this.stopDate = stopDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getStopDate() {
        return stopDate;
    }

    // @todo 3 tb/** these methods are unused - delete if no-one shouts :-) 2016-08-08
//    public boolean includes(long actualTime) {
//        final long startTime = startDate.getTime();
//        final long stopTime = stopDate.getTime();
//
//        return (startTime <= actualTime && actualTime < stopTime);
//    }
//
//    public boolean intersectsWith(TimeRange other) {
//        return includes(other.getStartDate().getTime()) || includes(other.getStopDate().getTime()) || other.includes(
//                startDate.getTime()) || other.includes(stopDate.getTime());
//    }
}
