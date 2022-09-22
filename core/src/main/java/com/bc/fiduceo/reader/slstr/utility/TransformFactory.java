package com.bc.fiduceo.reader.slstr.utility;

import com.bc.fiduceo.reader.slstr.VariableType;

import static com.bc.fiduceo.reader.slstr.VariableType.*;

public class TransformFactory {

    private final int rasterWidth;
    private final int rasterHeight;
    private final int obliqueRasterOffset;

    public TransformFactory(int rasterWidth, int rasterHeight, int obliqueRasterOffset) {
        this.rasterWidth = rasterWidth;
        this.rasterHeight = rasterHeight;
        this.obliqueRasterOffset = obliqueRasterOffset;
    }

    public Transform get(VariableType variableType) {
        if (variableType == NADIR_1km) {
            return new Nadir1kmTransform(rasterWidth, rasterHeight);
        } else if (variableType == NADIR_500m) {
            return new Nadir500mTransform(rasterWidth, rasterHeight);
        } else if (variableType == OBLIQUE_1km) {
            return new Oblique1kmTransform(rasterWidth, rasterHeight, obliqueRasterOffset);
        } else if (variableType == OBLIQUE_500m) {
            return new Oblique500mTransform(rasterWidth, rasterHeight, obliqueRasterOffset);
        }

        throw new RuntimeException("Invalid transform type requested");
    }
}
