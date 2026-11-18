package org.robolectric.simulator;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.graphics.Bitmap;
import java.nio.file.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.simulator.pluginapi.ScreenRecorder;

@RunWith(JUnit4.class)
public class ScreenRecorderFrameListenerTest {

  @Test(timeout = 2_000)
  public void onFrame_recordFrameThrows_laterFramesAndStopDoNotBlock() {
    FailOnceScreenRecorder recorder = new FailOnceScreenRecorder();
    ScreenRecorderFrameListener listener = new ScreenRecorderFrameListener(recorder, 100, 100);

    assertThrows(LinkageError.class, () -> listener.onFrame(null));
    listener.onFrame(null);
    listener.stopRecording();

    assertThat(recorder.framesRecorded).isEqualTo(1);
    assertThat(recorder.stopped).isTrue();
  }

  private static class FailOnceScreenRecorder implements ScreenRecorder {
    boolean failed;
    int framesRecorded;
    boolean stopped;

    @Override
    public void start(Path output, int width, int height, FrameRate frameRate) {}

    @Override
    public void recordFrame(Bitmap frame) {
      if (!failed) {
        failed = true;
        throw new LinkageError("recordFrame failed");
      }
      framesRecorded++;
    }

    @Override
    public void stop() {
      stopped = true;
    }
  }
}
