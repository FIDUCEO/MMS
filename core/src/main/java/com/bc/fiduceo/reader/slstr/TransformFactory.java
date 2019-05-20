package com.bc.fiduceo.reader.slstr;

import static com.bc.fiduceo.reader.slstr.VariableType.*;

class TransformFactory {

    private final int rasterWidth;
    private final int rasterHeight;

    TransformFactory(int rasterWidth, int rasterHeight) {
        this.rasterWidth = rasterWidth;
        this.rasterHeight = rasterHeight;
    }

    public Transform get(VariableType variableType) {
        if (variableType == NADIR_1km) {
            return new Nadir1kmTransform();
        } else if (variableType == NADIR_500m) {
            return new Nadir500mTransform();
        } else if (variableType == OBLIQUE_1km) {
            return new Oblique1kmTransform();
        } else if (variableType == OBLIQUE_500m) {
            return new Oblique500mTransform();
        }

        throw new RuntimeException("Invalid transform type requested");
    }
}
