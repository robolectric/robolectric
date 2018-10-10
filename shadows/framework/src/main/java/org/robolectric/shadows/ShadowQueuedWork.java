package org.robolectric.shadows;

import android.app.QueuedWork;
import android.os.Build;
import android.os.Handler;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = QueuedWork.class, isInAndroidSdk = false)
public class ShadowQueuedWork {

  @Resetter
  public static void reset() {
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O) {
      resetStateApi26();
    } else {
      QueuedWork.waitToFinish();
      ReflectionHelpers.setStaticField(QueuedWork.class, "sSingleThreadExecutor", null);
    }
  }

  private static void resetStateApi26() {
    Handler queuedWorkHandler = ReflectionHelpers.getStaticField(QueuedWork.class, "sHandler");
    if (queuedWorkHandler != null) {
      queuedWorkHandler.removeCallbacksAndMessages(null);
    }
    ((List) ReflectionHelpers.getStaticField(QueuedWork.class, "sFinishers")).clear();
    ((List) ReflectionHelpers.getStaticField(QueuedWork.class, "sWork")).clear();
    ReflectionHelpers.setStaticField(QueuedWork.class, "mNumWaits", 0);
  }
}
