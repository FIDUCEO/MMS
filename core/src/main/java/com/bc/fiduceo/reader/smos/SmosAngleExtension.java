package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.reader.netcdf.LayerExtension;

class SmosAngleExtension implements LayerExtension {

    private static final String[] EXTENSIONS = new String[]{"_025", "_075", "_125", "_175", "_225", "_275", "_325", "_375", "_400", "_425", "_475", "_525", "_575", "_625"};

    @Override
    public String getExtension(int index) {
        if (index >= 0 && index < EXTENSIONS.length) {
            return EXTENSIONS[index];
        } else {
            throw new RuntimeException("invalid layer index");
        }
    }

    @Override
    public int getIndex(String extension) {
        for (int i = 0; i < EXTENSIONS.length; i++) {
            if (extension.equals(EXTENSIONS[i])) {
                return i;
            }
        }

        throw new RuntimeException("invalid extension string: " + extension);
    }
}
