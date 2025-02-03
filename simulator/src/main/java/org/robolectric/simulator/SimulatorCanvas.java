package org.robolectric.simulator;

import android.graphics.Bitmap;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

/** A {@link Canvas} that draws screenshots taken with RNG. */
public class SimulatorCanvas extends Canvas {
  private int[] pixels;

  public SimulatorCanvas() {
    MouseHandler mouseHandler = new MouseHandler();
    addMouseListener(mouseHandler);
    addMouseMotionListener(mouseHandler);
  }

  private BufferedImage image;

  @Override
  public Color getBackground() {
    return Color.BLACK;
  }

  @Override
  public void paint(Graphics graphics) {
    // Required override
  }

  public void drawBitmap(Bitmap bitmap) {
    BufferStrategy bufferStrategy = getBufferStrategy();
    if (bufferStrategy == null) {
      createBufferStrategy(2);
      bufferStrategy = getBufferStrategy();
    }
    Graphics graphics = bufferStrategy.getDrawGraphics();

    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    if (pixels == null || pixels.length != width * height) {
      pixels = new int[width * height];
    }
    if (image == null || image.getWidth() != width || image.getHeight() != height) {
      image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
    bitmap.getPixels(pixels, /* offset= */ 0, /* stride= */ width, 0, 0, width, height);
    image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();
    bufferStrategy.show();
  }
}
