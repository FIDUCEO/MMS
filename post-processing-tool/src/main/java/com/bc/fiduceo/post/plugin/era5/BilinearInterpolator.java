package com.bc.fiduceo.post.plugin.era5;

class BilinearInterpolator {

    private final double a;
    private final double b;
    private final int xMin;
    private final int yMin;

    BilinearInterpolator(double a, double b, int xMin, int yMin) {
        this.a = a;
        this.b = b;
        this.xMin = xMin;
        this.yMin = yMin;
    }

    double interpolate(float c00, float c10, float c01, float c11) {
        final double interp0 = lerp(c00, c10, a);
        final double interp1 = lerp(c01, c11, a);
        return lerp(interp0, interp1, b);
    }

    int getXMin() {
        return xMin;
    }

    int getYMin() {
        return yMin;
    }

    double getA() {return a;}

    double getB() {return b;}

    private static double lerp(double c0, double c1, double t) {
        return c0 + (c1 - c0) * t;
    }

    // probably clever to add the reading coordinates in the rea-5 raster
}
