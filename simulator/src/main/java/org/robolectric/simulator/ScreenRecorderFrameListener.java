package org.robolectric.simulator;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;
import org.robolectric.simulator.pluginapi.ScreenRecorder;

public class ScreenRecorderFrameListener implements FrameListener {

  private final float displayWidth;
  private final float displayHeight;
  private ScreenRecorder screenRecorder = null;
  private final Semaphore writingFrame = new Semaphore(1);

  public ScreenRecorderFrameListener(
      ScreenRecorder screenRecorder, float displayWidth, float displayHeight) {
    this.displayWidth = displayWidth;
    this.displayHeight = displayHeight;
    this.screenRecorder = screenRecorder;
  }

  @Override
  public void onInitialize() {
    Path videoPath;
    try {
      videoPath = getVideoPath();
    } catch (IOException e) {
      Log.i("Simulator", "Failed to get a path for screen recording!", e);
      return;
    }
    // Use the class loader for an Android class.
    screenRecorder.start(
        videoPath,
        (int) this.displayWidth,
        (int) this.displayHeight,
        new ScreenRecorder.FrameRate(24, 1));

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  // Make sure that we don't stop the screen recorder in the middle of a write!
                  writingFrame.acquireUninterruptibly();
                  screenRecorder.stop();
                  screenRecorder = null;
                  writingFrame.release();
                }));
  }

  @Override
  public void onFrame(Bitmap bitmap) {
    writingFrame.acquireUninterruptibly();
    if (screenRecorder != null) {
      screenRecorder.recordFrame(bitmap);
    } else {
      bitmap.recycle();
    }
    writingFrame.release();
  }

  private Path getVideoPath() throws IOException {
    if (System.getProperty("robolectric.videoPath") != null) {
      Path videoPath = Path.of(System.getProperty("robolectric.videoPath"));
      Files.createDirectories(videoPath.getParent());
      return videoPath;
    } else {
      return Files.createTempFile(
          "robolectric", System.getProperty("robolectric.videoExtension", "webm"));
    }
  }
}
