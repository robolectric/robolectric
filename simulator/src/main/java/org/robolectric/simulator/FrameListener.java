package org.robolectric.simulator;

import android.graphics.Bitmap;

/** Receives frames of screen captures formatted as {@link Bitmap}s. */
public interface FrameListener {

  /**
   * Callback for the simulator starting. Implementations should perform any necessary initialize
   * here
   */
  default void onInitialize() {}

  /**
   * Receives one frame.
   *
   * <p>Implementations are responsible for recycling the provided Bitmap.
   *
   * <p>This is called from main Looper thread. Implementations should carefully consider whether
   * they should do processing work synchronously or asynchronously
   */
  void onFrame(Bitmap bitmap);
}
