package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static com.google.common.truth.Truth.assertThat;

import android.os.Looper;
import android.view.InputChannel;
import android.view.InputEventReceiver;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dalvik.system.CloseGuard;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowInputEventReceiver}. */
@RunWith(AndroidJUnit4.class)
public class ShadowInputEventReceiverTest {

  static class MyInputEventReceiver extends InputEventReceiver {

    public MyInputEventReceiver(InputChannel inputChannel, Looper looper) {
      super(inputChannel, looper);
    }

    @Override
    protected void finalize() throws Throwable {
      super.finalize();
    }
  }

  @Test
  @Config(minSdk = JELLY_BEAN)
  public void closeGuard_autoCloses() throws Throwable {
    final AtomicBoolean closeGuardWarned = new AtomicBoolean(false);
    CloseGuard.Reporter originalReporter = CloseGuard.getReporter();
    try {
      CloseGuard.setReporter((s, throwable) -> closeGuardWarned.set(true));
      new MyInputEventReceiver(new InputChannel(), Looper.getMainLooper()).finalize();
      assertThat(closeGuardWarned.get()).isFalse();
    } finally {
      CloseGuard.setReporter(originalReporter);
    }
  }
}
