package com.bc.fiduceo.qc;

import org.esa.snap.core.datamodel.GeoPos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

class GlobalPlot {

    static byte[] imageBuffer;

    private BufferedImage image;

    private GlobalPlot() throws IOException {
        image = ImageIO.read(new ByteArrayInputStream(imageBuffer));
    }

    static GlobalPlot create() throws IOException {
        if (imageBuffer == null) {
            final InputStream is = GlobalPlot.class.getResourceAsStream("bluemarble-2048.png");
            if (is == null) {
                throw new IllegalStateException("The internal resource file could not be read.");
            }
            final DataInputStream dis = new DataInputStream(is);
            final int fileSize = dis.available();
            imageBuffer = new byte[fileSize];
            dis.readFully(imageBuffer);
        }

        return new GlobalPlot();
    }

    void plot(ArrayList<GeoPos> pointList) {
        final Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.magenta);

        for (final GeoPos point : pointList) {
            final double x = (point.getLon() + 180.0) / 360.0;
            final double y = (90.0 - point.getLat()) / 180.0;
            final int i = (int) (y * 1024);
            final int k = (int) (x * 2048);
            graphics.fill(new Rectangle(k, i, 1, 1));
        }

        graphics.dispose();
    }

    void writeTo(String pngFilePath) throws IOException {
        ImageIO.write(image, "png", new File(pngFilePath));
    }

    public void dispose() {
        image = null;
    }
}
