/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.nwp;

class SensorExtractConfiguration {
    private String an_CI_name;
    private String an_ASN_name;
    private String an_SSTK_name;
    private String an_TCWV_name;
    private String an_MSL_name;
    private String an_TCC_name;
    private String an_U10_name;
    private String an_V10_name;
    private String an_T2_name;
    private String an_D2_name;
    private String an_AL_name;
    private String an_SKT_name;
    private String an_LNSP_name;
    private String an_T_name;
    private String an_Q_name;
    private String an_O3_name;
    private String an_CLWC_name;
    private String an_CIWC_name;
    private String an_TP_name;
    private String timeVariableName;
    private int x_Dimension;
    private int y_Dimension;
    private int z_Dimension;
    private String x_DimensionName;
    private String y_DimensionName;
    private String z_DimensionName;

    SensorExtractConfiguration() {
        an_CI_name = "amsre.nwp.cloud_ice_water";
        an_ASN_name = "amsre.nwp.snow_albedo";
        an_SSTK_name = "amsre.nwp.sea_surface_temperature";
        an_TCWV_name = "amsre.nwp.total_column_water_vapour";
        an_MSL_name = "amsre.nwp.mean_sea_level_pressure";
        an_TCC_name = "amsre.nwp.total_cloud_cover";
        an_U10_name = "amsre.nwp.10m_east_wind_component";
        an_V10_name = "amsre.nwp.10m_north_wind_component";
        an_T2_name = "amsre.nwp.2m_temperature";
        an_D2_name = "amsre.nwp.2m_dew_point";
        an_AL_name = "amsre.nwp.albedo";
        an_SKT_name = "amsre.nwp.skin_temperature";
        an_LNSP_name = "amsre.nwp.log_surface_pressure";
        an_T_name = "amsre.nwp.temperature_profile";
        an_Q_name = "amsre.nwp.water_vapour_profile";
        an_O3_name = "amsre.nwp.ozone_profile";
        an_CLWC_name = "amsre.nwp.cloud_liquid_water";
        an_CIWC_name = "amsre.nwp.cloud_ice_water";
        an_TP_name = "amsre.nwp.total_precip";

        x_DimensionName = "amsre.nwp.nx";
        y_DimensionName = "amsre.nwp.ny";
        z_DimensionName = "amsre.nwp.nz";
    }

    void setAn_CI_name(String an_CI_name) {
        this.an_CI_name = an_CI_name;
    }

    String getAn_CI_name() {
        return an_CI_name;
    }

    void setAn_ASN_name(String an_ASN_name) {
        this.an_ASN_name = an_ASN_name;
    }

    String getAn_ASN_name() {
        return an_ASN_name;
    }

    void setAn_SSTK_name(String an_SSTK_name) {
        this.an_SSTK_name = an_SSTK_name;
    }

    String getAn_SSTK_name() {
        return an_SSTK_name;
    }

    void setAn_TCWV_name(String an_TCWV_name) {
        this.an_TCWV_name = an_TCWV_name;
    }

    String getAn_TCWV_name() {
        return an_TCWV_name;
    }

    void setAn_MSL_name(String an_MSL_name) {
        this.an_MSL_name = an_MSL_name;
    }

    String getAn_MSL_name() {
        return an_MSL_name;
    }

    void setAn_TCC_name(String an_TCC_name) {
        this.an_TCC_name = an_TCC_name;
    }

    String getAn_TCC_name() {
        return an_TCC_name;
    }

    void setAn_U10_name(String an_U10_name) {
        this.an_U10_name = an_U10_name;
    }

    String getAn_U10_name() {
        return an_U10_name;
    }

    void setAn_V10_name(String an_V10_name) {
        this.an_V10_name = an_V10_name;
    }

    String getAn_V10_name() {
        return an_V10_name;
    }

    void setAn_T2_name(String an_T2_name) {
        this.an_T2_name = an_T2_name;
    }

    String getAn_T2_name() {
        return an_T2_name;
    }

    void setAn_D2_name(String an_D2_name) {
        this.an_D2_name = an_D2_name;
    }

    String getAn_D2_name() {
        return an_D2_name;
    }

    void setAn_AL_name(String an_AL_name) {
        this.an_AL_name = an_AL_name;
    }

    String getAn_AL_name() {
        return an_AL_name;
    }

    void setAn_SKT_name(String an_SKT_name) {
        this.an_SKT_name = an_SKT_name;
    }

    String getAn_SKT_name() {
        return an_SKT_name;
    }

    void setAn_LNSP_name(String an_LNSP_name) {
        this.an_LNSP_name = an_LNSP_name;
    }

    String getAn_LNSP_name() {
        return an_LNSP_name;
    }

    void setAn_T_name(String an_T_name) {
        this.an_T_name = an_T_name;
    }

    String getAn_T_name() {
        return an_T_name;
    }

    void setAn_Q_name(String an_Q_name) {
        this.an_Q_name = an_Q_name;
    }

    String getAn_Q_name() {
        return an_Q_name;
    }

    void setAn_O3_name(String an_O3_name) {
        this.an_O3_name = an_O3_name;
    }

    String getAn_O3_name() {
        return an_O3_name;
    }

    void setAn_CLWC_name(String an_CLWC_name) {
        this.an_CLWC_name = an_CLWC_name;
    }

    String getAn_CLWC_name() {
        return an_CLWC_name;
    }

    void setAn_CIWC_name(String an_CIWC_name) {
        this.an_CIWC_name = an_CIWC_name;
    }

    String getAn_CIWC_name() {
        return an_CIWC_name;
    }

    void setAn_TP_name(String an_TP_name) {
        this.an_TP_name = an_TP_name;
    }

    String getAn_TP_name() {
        return an_TP_name;
    }

    void setTimeVariableName(String timeVariableName) {
        this.timeVariableName = timeVariableName;
    }

    String getTimeVariableName() {
        return timeVariableName;
    }

    void setX_Dimension(int x_Dimension) {
        this.x_Dimension = x_Dimension;
    }

    int getX_Dimension() {
        return x_Dimension;
    }

    void setY_Dimension(int y_Dimension) {
        this.y_Dimension = y_Dimension;
    }

    int getY_Dimension() {
        return y_Dimension;
    }

    void setZ_Dimension(int z_Dimension) {
        this.z_Dimension = z_Dimension;
    }

    int getZ_Dimension() {
        return z_Dimension;
    }

     void setX_DimensionName(String x_DimensionName) {
        this.x_DimensionName = x_DimensionName;
    }

    String getX_DimensionName() {
        return x_DimensionName;
    }

    void setY_DimensionName(String y_DimensionName) {
        this.y_DimensionName = y_DimensionName;
    }

    String getY_DimensionName() {
        return y_DimensionName;
    }

    void setZ_DimensionName(String z_DimensionName) {
        this.z_DimensionName = z_DimensionName;
    }

    String getZ_DimensionName() {
        return z_DimensionName;
    }
}
