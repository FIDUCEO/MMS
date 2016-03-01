package com.bc.fiduceo.math;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public final class Rotator {
    private final double a11;
    private final double a12;
    private final double a13;
    private final double a21;
    private final double a22;
    private final double a23;
    private final double a31;
    private final double a32;
    private final double a33;
    private final double lon;
    private final double lat;
    private final double alpha;

    public Rotator(double lon, double lat) {
        this(lon, lat, 0.0D);
    }

    public Rotator(double lon, double lat, double alpha) {
        this.lon = lon;
        this.lat = lat;
        this.alpha = alpha;
        double u = Math.toRadians(lon);
        double v = Math.toRadians(lat);
        double w = Math.toRadians(alpha);
        double cu = Math.cos(u);
        double cv = Math.cos(v);
        double cw = Math.cos(w);
        double su = Math.sin(u);
        double sv = Math.sin(v);
        double sw = Math.sin(w);
        this.a11 = cu * cv;
        this.a12 = su * cv;
        this.a13 = sv;
        this.a21 = sw * cu * sv - su * cw;
        this.a22 = cw * cu + sw * su * sv;
        this.a23 = -sw * cv;
        this.a31 = sw * -su - cw * cu * sv;
        this.a32 = sw * cu - cw * su * sv;
        this.a33 = cw * cv;
    }

    public double getLon() {
        return this.lon;
    }

    public double getLat() {
        return this.lat;
    }

    public double getAlpha() {
        return this.alpha;
    }

    public Rotator(Point2D point) {
        this(point, 0.0D);
    }

    public Rotator(Point2D point, double alpha) {
        this(point.getX(), point.getY(), alpha);
    }

    public static Point2D calculateCenter(double[][] data, int lonIndex, int latIndex) {
        int size = data.length;
        double[] x = new double[size];
        double[] y = new double[size];
        double[] z = new double[size];
        calculateXYZ(data, x, y, z, lonIndex, latIndex);
        double xc = 0.0D;
        double yc = 0.0D;
        double zc = 0.0D;

        for(int length = 0; length < size; ++length) {
            xc += x[length];
            yc += y[length];
            zc += z[length];
        }

        double var19 = Math.sqrt(xc * xc + yc * yc + zc * zc);
        xc /= var19;
        yc /= var19;
        zc /= var19;
        double lat = Math.toDegrees(Math.asin(zc));
        double lon = Math.toDegrees(Math.atan2(yc, xc));
        return new Double(lon, lat);
    }

    static void calculateXYZ(double[][] data, double[] x, double[] y, double[] z, int lonIndex, int latIndex) {
        for(int i = 0; i < data.length; ++i) {
            double lon = data[i][lonIndex];
            double lat = data[i][latIndex];
            double u = Math.toRadians(lon);
            double v = Math.toRadians(lat);
            double w = Math.cos(v);
            x[i] = Math.cos(u) * w;
            y[i] = Math.sin(u) * w;
            z[i] = Math.sin(v);
        }

    }

    public void transform(Point2D point) {
        double lon = point.getX();
        double lat = point.getY();
        double u = Math.toRadians(lon);
        double v = Math.toRadians(lat);
        double w = Math.cos(v);
        double x = Math.cos(u) * w;
        double y = Math.sin(u) * w;
        double z = Math.sin(v);
        double x2 = this.a11 * x + this.a12 * y + this.a13 * z;
        double y2 = this.a21 * x + this.a22 * y + this.a23 * z;
        double z2 = this.a31 * x + this.a32 * y + this.a33 * z;
        lat = Math.toDegrees(Math.asin(z2));
        lon = Math.toDegrees(Math.atan2(y2, x2));
        point.setLocation(lon, lat);
    }

    public void transform(double[] lons, double[] lats) {
        for(int i = 0; i < lats.length; ++i) {
            double u = Math.toRadians(lons[i]);
            double v = Math.toRadians(lats[i]);
            double w = Math.cos(v);
            double x = Math.cos(u) * w;
            double y = Math.sin(u) * w;
            double z = Math.sin(v);
            double x2 = this.a11 * x + this.a12 * y + this.a13 * z;
            double y2 = this.a21 * x + this.a22 * y + this.a23 * z;
            double z2 = this.a31 * x + this.a32 * y + this.a33 * z;
            lats[i] = Math.toDegrees(Math.asin(z2));
            lons[i] = Math.toDegrees(Math.atan2(y2, x2));
        }

    }

    void transform(double[][] data, int lonIndex, int latIndex) {
        double[][] var4 = data;
        int var5 = data.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            double[] p = var4[var6];
            double u = Math.toRadians(p[lonIndex]);
            double v = Math.toRadians(p[latIndex]);
            double w = Math.cos(v);
            double x = Math.cos(u) * w;
            double y = Math.sin(u) * w;
            double z = Math.sin(v);
            double x2 = this.a11 * x + this.a12 * y + this.a13 * z;
            double y2 = this.a21 * x + this.a22 * y + this.a23 * z;
            double z2 = this.a31 * x + this.a32 * y + this.a33 * z;
            p[lonIndex] = Math.toDegrees(Math.atan2(y2, x2));
            p[latIndex] = Math.toDegrees(Math.asin(z2));
        }

    }

    public void transformInversely(Point2D point) {
        double lon = point.getX();
        double lat = point.getY();
        double u = Math.toRadians(lon);
        double v = Math.toRadians(lat);
        double w = Math.cos(v);
        double x = Math.cos(u) * w;
        double y = Math.sin(u) * w;
        double z = Math.sin(v);
        double x2 = this.a11 * x + this.a21 * y + this.a31 * z;
        double y2 = this.a12 * x + this.a22 * y + this.a32 * z;
        double z2 = this.a13 * x + this.a23 * y + this.a33 * z;
        lat = Math.toDegrees(Math.asin(z2));
        lon = Math.toDegrees(Math.atan2(y2, x2));
        point.setLocation(lon, lat);
    }

    public void transformInversely(double[] lons, double[] lats) {
        for(int i = 0; i < lats.length; ++i) {
            double u = Math.toRadians(lons[i]);
            double v = Math.toRadians(lats[i]);
            double w = Math.cos(v);
            double x = Math.cos(u) * w;
            double y = Math.sin(u) * w;
            double z = Math.sin(v);
            double x2 = this.a11 * x + this.a21 * y + this.a31 * z;
            double y2 = this.a12 * x + this.a22 * y + this.a32 * z;
            double z2 = this.a13 * x + this.a23 * y + this.a33 * z;
            lats[i] = Math.toDegrees(Math.asin(z2));
            lons[i] = Math.toDegrees(Math.atan2(y2, x2));
        }

    }
}
