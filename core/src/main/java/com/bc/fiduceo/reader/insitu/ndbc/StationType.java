package com.bc.fiduceo.reader.insitu.ndbc;

enum StationType {
    OCEAN_BUOY,     // > 5km distance to land
    COAST_BUOY,     // <= 5km distance to land
    LAKE_BUOY,      // inland water
    OCEAN_STATION,  // > 5km distance to land
    COAST_STATION,  // <= 5km distance to land
    LAKE_STATION    // inland water
}
