package org.robolectric.simulator;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.robolectric.simulator.pluginapi.ScreenRecorder;

public class ScreenRecorderFrameListener implements FrameListener {

  private static final long STOP_TIMEOUT_SECONDS = 5;

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

    Runtime.getRuntime().addShutdownHook(new Thread(this::stopRecording));
  }

  /** Stops the screen recorder once any in-progress frame is written. Runs at JVM shutdown. */
  void stopRecording() {
    // Make sure that we don't stop the screen recorder in the middle of a write, but never block
    // JVM shutdown forever on a frame that does not finish.
    try {
      if (!writingFrame.tryAcquire(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        System.err.println("Timed out waiting for a frame to be written; not stopping recorder");
        return;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    }
    try {
      screenRecorder.stop();
      screenRecorder = null;
    } finally {
      writingFrame.release();
    }
  }

  @Override
  public void onFrame(Bitmap bitmap) {
    writingFrame.acquireUninterruptibly();
    try {
      if (screenRecorder != null) {
        screenRecorder.recordFrame(bitmap);
      } else {
        bitmap.recycle();
      }
    } finally {
      writingFrame.release();
    }
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
