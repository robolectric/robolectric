package org.robolectric.simulator.pluginapi;

import android.graphics.Bitmap;
import java.nio.file.Path;

/** A screen recorder for the Robolectric Simulator. */
public interface ScreenRecorder {
  /** Frame rates are rational numbers. */
  class FrameRate { // Java 16: "record FrameRate(int numerator, int denominator) {}"
    public final int numerator;
    public final int denominator;

    public FrameRate(int numerator, int denominator) {
      this.numerator = numerator;
      this.denominator = denominator;
    }
  }

  /** Start the screen recorder. */
  void start(Path output, int width, int height, FrameRate frameRate);

  /** Record a frame. */
  void recordFrame(Bitmap frame);

  /** Stop the screen recorder. */
  void stop();
}
