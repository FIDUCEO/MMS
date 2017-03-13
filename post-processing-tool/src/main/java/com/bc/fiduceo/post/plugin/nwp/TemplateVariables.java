
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

import ucar.ma2.DataType;

import java.util.ArrayList;
import java.util.List;

class TemplateVariables {

    private final List<TemplateVariable> analysisVariables;
    private final List<TemplateVariable> forecastVariables;

    TemplateVariables(Configuration configuration) {
        analysisVariables = createAnalysisVariables(configuration);
        forecastVariables = createForecastVariables(configuration);
    }

    List<TemplateVariable> getAnalysisVariables() {
        return analysisVariables;
    }

    List<TemplateVariable> getForecastVariables() {
        return forecastVariables;
    }

    List<TemplateVariable> getAllVariables() {
        final ArrayList<TemplateVariable> allVariables = new ArrayList<>();

        allVariables.addAll(analysisVariables);
        allVariables.addAll(forecastVariables);

        return allVariables;
    }

    private ArrayList<TemplateVariable> createAnalysisVariables(Configuration configuration) {
        final ArrayList<TemplateVariable> variables = new ArrayList<>();
        final String anDimensions = "matchup_count matchup.nwp.an.time";

        final TemplateVariable seaIceFractionVariable = new TemplateVariable(configuration.getAnSeaIceFractionName(), "CI", DataType.FLOAT, anDimensions);
        seaIceFractionVariable.addAttribute("long_name", "Sea-ice cover");
        seaIceFractionVariable.addAttribute("_FillValue", 2.0E20);
        seaIceFractionVariable.addAttribute("source", "GRIB data");
        variables.add(seaIceFractionVariable);

        final TemplateVariable sstVariable = new TemplateVariable(configuration.getAnSSTName(), "SSTK", DataType.FLOAT, anDimensions);
        sstVariable.addAttribute("long_name", "Sea surface temperature");
        sstVariable.addAttribute("units", "K");
        sstVariable.addAttribute("_FillValue", 2.0E20);
        sstVariable.addAttribute("source", "GRIB data");
        variables.add(sstVariable);

        final TemplateVariable eastWindVariable = new TemplateVariable(configuration.getAnEastWindName(), "U10", DataType.FLOAT, anDimensions);
        eastWindVariable.addAttribute("standard_name", "eastward_wind");
        eastWindVariable.addAttribute("long_name", "10 metre U wind component");
        eastWindVariable.addAttribute("units", "m s**-1");
        eastWindVariable.addAttribute("_FillValue", 2.0E20);
        eastWindVariable.addAttribute("source", "GRIB data");
        variables.add(eastWindVariable);

        final TemplateVariable northWindVariable = new TemplateVariable(configuration.getAnNorthWindName(), "V10", DataType.FLOAT, anDimensions);
        northWindVariable.addAttribute("standard_name", "northward_wind");
        northWindVariable.addAttribute("long_name", "10 metre V wind component");
        northWindVariable.addAttribute("units", "m s**-1");
        northWindVariable.addAttribute("_FillValue", 2.0E20);
        northWindVariable.addAttribute("source", "GRIB data");
        variables.add(northWindVariable);

        final TemplateVariable totalColumnWaterVapourVariable = new TemplateVariable(configuration.getAnTotalColumnWaterVapourName(), "TCWV", DataType.FLOAT, anDimensions);
        totalColumnWaterVapourVariable.addAttribute("standard_name", "lwe_thickness_of_atmosphere_water_vapour_content");
        totalColumnWaterVapourVariable.addAttribute("long_name", "Total column water vapour");
        totalColumnWaterVapourVariable.addAttribute("units", "kg m**-2");
        totalColumnWaterVapourVariable.addAttribute("_FillValue", 2.0E20);
        totalColumnWaterVapourVariable.addAttribute("source", "GRIB data");
        variables.add(totalColumnWaterVapourVariable);

        return variables;
    }

    private ArrayList<TemplateVariable> createForecastVariables(Configuration configuration) {
        final ArrayList<TemplateVariable> variables = new ArrayList<>();
        final String fcDimensions = "matchup_count matchup.nwp.fc.time";

        final TemplateVariable evaporationVariable = new TemplateVariable(configuration.getFcEvaporationName(), "E", DataType.FLOAT, fcDimensions);
        evaporationVariable.addAttribute("standard_name", "lwe_thickness_of_water_evaporation_amount");
        evaporationVariable.addAttribute("long_name", "Evaporation");
        evaporationVariable.addAttribute("units", "m of water");
        evaporationVariable.addAttribute("_FillValue", 2.0E20);
        evaporationVariable.addAttribute("source", "GRIB data");
        variables.add(evaporationVariable);

        final TemplateVariable sshfVariable = new TemplateVariable(configuration.getFcSurfSensibleHeatFluxName(), "SSHF", DataType.FLOAT, fcDimensions);
        sshfVariable.addAttribute("standard_name", "surface_upward_sensible_heat_flux");
        sshfVariable.addAttribute("long_name", "Surface sensible heat flux");
        sshfVariable.addAttribute("units", "W m**-2 s");
        sshfVariable.addAttribute("_FillValue", 2.0E20);
        sshfVariable.addAttribute("source", "GRIB data");
        variables.add(sshfVariable);

        final TemplateVariable slhfVariable = new TemplateVariable(configuration.getFcSurfLatentHeatFluxName(), "SLHF", DataType.FLOAT, fcDimensions);
        slhfVariable.addAttribute("standard_name", "surface_upward_latent_heat_flux");
        slhfVariable.addAttribute("long_name", "Surface latent heat flux");
        slhfVariable.addAttribute("units", "W m**-2 s");
        slhfVariable.addAttribute("_FillValue", 2.0E20);
        slhfVariable.addAttribute("source", "GRIB data");
        variables.add(slhfVariable);

        final TemplateVariable ssrdVariable = new TemplateVariable(configuration.getFcDownSurfSolarRadiationName(), "SSRD", DataType.FLOAT, fcDimensions);
        ssrdVariable.addAttribute("standard_name", "surface_downwelling_shortwave_flux_in_air");
        ssrdVariable.addAttribute("long_name", "Surface solar radiation downwards");
        ssrdVariable.addAttribute("units", "W m**-2 s");
        ssrdVariable.addAttribute("_FillValue", 2.0E20);
        ssrdVariable.addAttribute("source", "GRIB data");
        variables.add(ssrdVariable);

        final TemplateVariable strdVariable = new TemplateVariable(configuration.getFcDownSurfThermalRadiationName(), "STRD", DataType.FLOAT, fcDimensions);
        strdVariable.addAttribute("long_name", "Surface thermal radiation downwards");
        strdVariable.addAttribute("units", "W m**-2 s");
        strdVariable.addAttribute("_FillValue", 2.0E20);
        strdVariable.addAttribute("source", "GRIB data");
        variables.add(strdVariable);

        final TemplateVariable ssrVariable = new TemplateVariable(configuration.getFcSurfSolarRadiationName(), "SSR", DataType.FLOAT, fcDimensions);
        ssrVariable.addAttribute("standard_name", "surface_net_upward_longwave_flux");
        ssrVariable.addAttribute("long_name", "Surface solar radiation");
        ssrVariable.addAttribute("units", "W m**-2 s");
        ssrVariable.addAttribute("_FillValue", 2.0E20);
        ssrVariable.addAttribute("source", "GRIB data");
        variables.add(ssrVariable);

        final TemplateVariable strVariable = new TemplateVariable(configuration.getFcSurfThermalRadiationName(), "STR", DataType.FLOAT, fcDimensions);
        strVariable.addAttribute("standard_name", "surface_net_upward_shortwave_flux");
        strVariable.addAttribute("long_name", "Surface thermal radiation");
        strVariable.addAttribute("units", "W m**-2 s");
        strVariable.addAttribute("_FillValue", 2.0E20);
        strVariable.addAttribute("source", "GRIB data");
        variables.add(strVariable);

        final TemplateVariable ewssVariable = new TemplateVariable(configuration.getFcTurbStressEastName(), "EWSS", DataType.FLOAT, fcDimensions);
        ewssVariable.addAttribute("standard_name", "surface_downward_eastward_stress");
        ewssVariable.addAttribute("long_name", "East-west surface stress");
        ewssVariable.addAttribute("units", "N m**-2 s");
        ewssVariable.addAttribute("_FillValue", 2.0E20);
        ewssVariable.addAttribute("source", "GRIB data");
        variables.add(ewssVariable);

        final TemplateVariable nsssVariable = new TemplateVariable(configuration.getFcTurbStressNorthName(), "NSSS", DataType.FLOAT, fcDimensions);
        nsssVariable.addAttribute("standard_name", "surface_downward_northward_stress");
        nsssVariable.addAttribute("long_name", "North-south surface stress");
        nsssVariable.addAttribute("units", "N m**-2 s");
        nsssVariable.addAttribute("_FillValue", 2.0E20);
        nsssVariable.addAttribute("source", "GRIB data");
        variables.add(nsssVariable);

        final TemplateVariable tpVariable = new TemplateVariable(configuration.getFcTotalPrecipName(), "TP", DataType.FLOAT, fcDimensions);
        tpVariable.addAttribute("long_name", "Total precipitation");
        tpVariable.addAttribute("units", "m");
        tpVariable.addAttribute("_FillValue", 2.0E20);
        tpVariable.addAttribute("source", "GRIB data");
        variables.add(tpVariable);

        final TemplateVariable u10Variable = new TemplateVariable(configuration.getFc10mEastWindName(), "U10", DataType.FLOAT, fcDimensions);
        u10Variable.addAttribute("standard_name", "eastward_wind");
        u10Variable.addAttribute("long_name", "10 metre U wind component");
        u10Variable.addAttribute("units", "m s**-1");
        u10Variable.addAttribute("_FillValue", 2.0E20);
        u10Variable.addAttribute("source", "GRIB data");
        variables.add(u10Variable);

        final TemplateVariable v10Variable = new TemplateVariable(configuration.getFc10mNorthWindName(), "V10", DataType.FLOAT, fcDimensions);
        v10Variable.addAttribute("standard_name", "northward_wind");
        v10Variable.addAttribute("long_name", "10 metre V wind component");
        v10Variable.addAttribute("units", "m s**-1");
        v10Variable.addAttribute("_FillValue", 2.0E20);
        v10Variable.addAttribute("source", "GRIB data");
        variables.add(v10Variable);

        final TemplateVariable d2Variable = new TemplateVariable(configuration.getFc2mDewPointName(), "D2", DataType.FLOAT, fcDimensions);
        d2Variable.addAttribute("standard_name", "dew_point_temperature");
        d2Variable.addAttribute("long_name", "2 metre dewpoint temperature");
        d2Variable.addAttribute("units", "K");
        d2Variable.addAttribute("_FillValue", 2.0E20);
        d2Variable.addAttribute("source", "GRIB data");
        variables.add(d2Variable);

        final TemplateVariable t2Variable = new TemplateVariable(configuration.getFc2mTemperatureName(), "T2", DataType.FLOAT, fcDimensions);
        t2Variable.addAttribute("standard_name", "air_temperature");
        t2Variable.addAttribute("long_name", "2 metre temperature");
        t2Variable.addAttribute("units", "K");
        t2Variable.addAttribute("_FillValue", 2.0E20);
        t2Variable.addAttribute("source", "GRIB data");
        variables.add(t2Variable);

        final TemplateVariable blhVariable = new TemplateVariable(configuration.getFcBoundaryLayerHeightName(), "BLH", DataType.FLOAT, fcDimensions);
        blhVariable.addAttribute("long_name", "Boundary layer height");
        blhVariable.addAttribute("units", "m");
        blhVariable.addAttribute("_FillValue", 2.0E20);
        blhVariable.addAttribute("source", "GRIB data");
        variables.add(blhVariable);

        final TemplateVariable mslVariable = new TemplateVariable(configuration.getFcMeanSeaLevelPressureName(), "MSL", DataType.FLOAT, fcDimensions);
        mslVariable.addAttribute("standard_name", "air_pressure_at_sea_level");
        mslVariable.addAttribute("long_name", "Mean sea-level pressure");
        mslVariable.addAttribute("units", "Pa");
        mslVariable.addAttribute("_FillValue", 2.0E20);
        mslVariable.addAttribute("source", "GRIB data");
        variables.add(mslVariable);

        final TemplateVariable sstkVariable = new TemplateVariable(configuration.getFcSSTName(), "SSTK", DataType.FLOAT, fcDimensions);
        sstkVariable.addAttribute("long_name", "Sea surface temperature");
        sstkVariable.addAttribute("units", "K");
        sstkVariable.addAttribute("_FillValue", 2.0E20);
        sstkVariable.addAttribute("source", "GRIB data");
        variables.add(sstkVariable);

        final TemplateVariable totalColumnWaterVapourVariable = new TemplateVariable(configuration.getFcTotalColumnWaterVapourName(), "TCWV", DataType.FLOAT, fcDimensions);
        totalColumnWaterVapourVariable.addAttribute("standard_name", "lwe_thickness_of_atmosphere_water_vapour_content");
        totalColumnWaterVapourVariable.addAttribute("long_name", "Total column water vapour");
        totalColumnWaterVapourVariable.addAttribute("units", "kg m**-2");
        totalColumnWaterVapourVariable.addAttribute("_FillValue", 2.0E20);
        totalColumnWaterVapourVariable.addAttribute("source", "GRIB data");
        variables.add(totalColumnWaterVapourVariable);

        return variables;
    }
}
