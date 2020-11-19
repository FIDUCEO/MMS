package com.bc.fiduceo.post.plugin.era5;

class BilinearInterpolator {

    private final double a;
    private final double b;

    BilinearInterpolator(double a, double b) {
        this.a = a;
        this.b = b;
    }

    double interpolate(float c00, float c10, float c01, float c11) {
        final double interp0 = lerp(c00, c10, a);
        final double interp1 = lerp(c01, c11, a);
        return lerp(interp0, interp1, b);
    }

    static double lerp(double c0, double c1, double t) {
        return c0 + (c1 - c0) * t;
    }
}
