/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.matchup.plot;

import com.bc.fiduceo.core.SamplingPoint;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

final class SamplingPointPlotter {

    public static final String TIME_LAT = "timlat";
    public static final String LON_LAT = "lonlat";
    private List<SamplingPoint> samples;
    private String filePath;
    private boolean show = false;
    private boolean live = false;
    private boolean series = false;
    private String windowTitle;
    private String mapStrategyName;
    private final int width;
    private final int height;

    public SamplingPointPlotter() {
        width = 800;
        height = 400;
    }

    public SamplingPointPlotter(int width, int height) {
        this.width = width;
        this.height = height;
    }

    SamplingPointPlotter samples(List<SamplingPoint> samples) {
        this.samples = samples;
        return this;
    }

    SamplingPointPlotter filePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public SamplingPointPlotter live(boolean live) {
        this.live = live;
        return this;
    }

    private SamplingPointPlotter series(boolean series) {
        this.series = series;
        return this;
    }

    public SamplingPointPlotter show(boolean show) {
        this.show = show;
        return this;
    }

    public SamplingPointPlotter windowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
        return this;
    }

    public SamplingPointPlotter mapStrategyName(String mapStrategyName) {
        this.mapStrategyName = mapStrategyName;
        return this;
    }

    BufferedImage plot() throws IOException {
        final MapStrategy strategy = getMapStrategy();
        strategy.initialize(samples);

        final BufferedImage image = drawImage(strategy);
        if (!live && show) {
            showImage(image);
        }
        if (filePath != null) {
            writeImage(image);
        }

        return image;
    }

    private BufferedImage drawImage(MapStrategy strategy) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        final JComponent component;
        if (live) {
            component = showImage(image);
        } else {
            component = null;
        }
        final Graphics2D graphics = image.createGraphics();

        for (int i = 0, k = 0; i < samples.size(); i++) {
            final SamplingPoint p = samples.get(i);
            final PlotPoint mapPoint = strategy.map(p);

            graphics.fill(new Rectangle(mapPoint.getX(), mapPoint.getY(), 1, 1));
            if (series) {
                if (i % 50 == 0 || i == samples.size() - 1) {
                    try {
                        final File file = createImageFile(k, false);
                        ImageIO.write(image, "png", file);
                        k++;
                    } catch (IOException ignored) {
                    }
                }
            }

            if (component != null) {
                component.repaint();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
        return image;
    }

    private File createImageFile(int count, boolean deleteOnExit) throws IOException {
        final String filename = String.format("%04d.png", count);
        final File file = new File(filename);
        if (deleteOnExit) {
            file.deleteOnExit();
        }
        return file;
    }

    private JComponent showImage(BufferedImage image) {
        final JLabel label = new JLabel(new ImageIcon(image));
        final JFrame frame = new JFrame();
        frame.setTitle(windowTitle);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(label);
        frame.setSize(image.getWidth(), image.getHeight());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
            }
        });

        return label;
    }

    private void writeImage(BufferedImage image) throws IOException {
        ImageIO.write(image, "png", new File(filePath));
    }

    private MapStrategy getMapStrategy() {
        if (TIME_LAT.equals(mapStrategyName)) {
            return new TimeLatMapStrategy(width, height);
        } else if (LON_LAT.equals(mapStrategyName)) {
            return new LonLatMapStrategy(width, height);
        } else {
            return new LonLatMapStrategy(width, height);
        }
    }
}

