package com.bc.fiduceo.matchup;

public class Sample implements Comparable<Sample> {

    public int x;
    public int y;
    public double lon;
    public double lat;
    public long time;

    public Sample(int x, int y, double lon, double lat, long time) {
        this.x = x;
        this.y = y;
        this.lon = lon;
        this.lat = lat;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Sample sample = (Sample) o;
        return /*x == sample.x &&
               y == sample.y &&
               Double.compare(sample.lon, lon) == 0 &&
               Double.compare(sample.lat, lat) == 0 &&*/
               time == sample.time;
    }

    @Override
    public int compareTo(Sample sample) {
        return Long.compare(time, sample.time);
    }
}
