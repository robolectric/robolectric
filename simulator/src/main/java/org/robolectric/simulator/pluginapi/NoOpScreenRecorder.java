package org.robolectric.simulator.pluginapi;

import android.graphics.Bitmap;
import com.google.auto.service.AutoService;
import java.nio.file.Path;
import javax.annotation.Priority;

/** The default [ScreenRecorder]. */
@Priority(Integer.MIN_VALUE)
@AutoService(ScreenRecorder.class)
public final class NoOpScreenRecorder implements ScreenRecorder {
  @Override
  public void start(Path output, int width, int height, FrameRate frameRate) {
    // Do nothing
  }

  @Override
  public void recordFrame(Bitmap frame) {
    frame.recycle();
  }

  @Override
  public void stop() {
    // Do nothing
  }
}
