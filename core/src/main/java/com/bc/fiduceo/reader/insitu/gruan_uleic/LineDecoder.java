package com.bc.fiduceo.reader.insitu.gruan_uleic;


class LineDecoder {

    private static final String SEPARATOR = ",";

    Object get(String line) {
        throw new RuntimeException("not implemented, override!");
    }

    static class Lon extends LineDecoder {
        @Override
        Object get(String line) {
            final String[] tokens = line.split(SEPARATOR);

            return Float.parseFloat(tokens[1].trim());
        }
    }

    static class Lat extends LineDecoder {
        @Override
        Object get(String line) {
            final String[] tokens = line.split(SEPARATOR);

            return Float.parseFloat(tokens[2].trim());
        }
    }

    static class Time extends LineDecoder {
        @Override
        Object get(String line) {
            final String[] tokens = line.split(SEPARATOR);

            final double floatTime = Math.round(Double.parseDouble(tokens[0].trim()));
            return (int) floatTime;
        }
    }

    static class Source extends LineDecoder {
        @Override
        Object get(String line) {
            final String[] tokens = line.split(SEPARATOR);

            return tokens[3].trim();
        }
    }
}
