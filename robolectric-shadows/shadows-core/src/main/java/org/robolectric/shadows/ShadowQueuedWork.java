package org.robolectric.shadows;

import android.app.QueuedWork;
import android.os.Build;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = QueuedWork.class, isInAndroidSdk = false)
public class ShadowQueuedWork {

  @Resetter
  public static void reset() {
    QueuedWork.waitToFinish();
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O ||
        (RuntimeEnvironment.getApiLevel() == 26 && Build.VERSION_CODES.O == 10000)) {
      ReflectionHelpers.setStaticField(QueuedWork.class, "sHandler", null);
    } else {
      ReflectionHelpers.setStaticField(QueuedWork.class, "sSingleThreadExecutor", null);
    }
  }
}
