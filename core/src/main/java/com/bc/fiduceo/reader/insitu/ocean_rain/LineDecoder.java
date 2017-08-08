package com.bc.fiduceo.reader.insitu.ocean_rain;

class LineDecoder {

    Object get(Line line){
        throw new RuntimeException("not implemented, override!");
    }

    static class Lon extends LineDecoder {
        @Override
        Object get(Line line) {
            return line.getLon();
        }
    }

    static class Lat extends LineDecoder {
        @Override
        Object get(Line line) {
            return line.getLat();
        }
    }

    static class Time extends LineDecoder {
        @Override
        Object get(Line line) {
            return line.getTime();
        }
    }

    static class Sst extends LineDecoder {
        @Override
        Object get(Line line) {
            return line.getSst();
        }
    }
}
