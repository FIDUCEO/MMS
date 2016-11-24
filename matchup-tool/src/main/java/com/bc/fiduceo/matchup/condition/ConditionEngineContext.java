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

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.core.Dimension;

import java.util.Date;

public class ConditionEngineContext {

    private Date startDate;
    private Date endDate;
    private Dimension primarySize;
    private Dimension secondarySize;
    private Dimension primaryExtractSize;
    private Dimension secondaryExtractSize;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Dimension getPrimarySize() {
        return primarySize;
    }

    public void setPrimarySize(Dimension primarySize) {
        this.primarySize = primarySize;
    }

    public Dimension getSecondarySize() {
        return secondarySize;
    }

    public void setSecondarySize(Dimension secondarySize) {
        this.secondarySize = secondarySize;
    }

    public void validateTime() {
        if (endDate == null || startDate == null || endDate.before(startDate))
        throw new RuntimeException("End date and/or start date are not valid.");
    }

    public void setPrimaryExtractSize(Dimension primaryExtractSize) {
        this.primaryExtractSize = primaryExtractSize;
    }

    public Dimension getPrimaryExtractSize() {
        return primaryExtractSize;
    }

    public void setSecondaryExtractSize(Dimension secondaryExtractSize) {
        this.secondaryExtractSize = secondaryExtractSize;
    }

    public Dimension getSecondaryExtractSize() {
        return secondaryExtractSize;
    }
}
