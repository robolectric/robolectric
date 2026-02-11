package org.robolectric.simulator;

import android.graphics.Bitmap;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/** A {@link JPanel} that draws screenshots taken with RNG. */
public class SimulatorPanel extends JPanel {
  private int[] pixels;
  private BufferedImage image;

  public SimulatorPanel() {
    setFocusable(true); // Required for keyboard focus.
    setFocusTraversalKeysEnabled(false); // Send tab keys to the simulator.

    if (!GraphicsEnvironment.isHeadless()) {
      addKeyListener(new KeyboardHandler());
      MouseHandler mouseHandler = new MouseHandler();
      addMouseListener(mouseHandler);
      addMouseMotionListener(mouseHandler);
      addMouseWheelListener(mouseHandler);
    }
  }

  @Override
  public Color getBackground() {
    return Color.WHITE;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (image != null) {
      g.drawImage(image, 0, 0, this);
    }
  }

  public void drawBitmap(Bitmap bitmap) {
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
    repaint();
  }
}
