package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.QueuedWork;
import android.os.Build;
import android.os.Handler;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(value = QueuedWork.class, isInAndroidSdk = false)
public class ShadowQueuedWork {

  @Resetter
  public static void reset() {

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O) {
      resetStateApi26();
    } else {
      QueuedWork.waitToFinish();
      reflector(QueuedWorkReflector.class).setSingleThreadExecutor(null);
    }
  }

  private static void resetStateApi26() {
    Handler queuedWorkHandler = ReflectionHelpers.getStaticField(QueuedWork.class, "sHandler");
    if (queuedWorkHandler != null) {
      queuedWorkHandler.removeCallbacksAndMessages(null);
    }
    QueuedWorkReflector queuedWorkReflector = reflector(QueuedWorkReflector.class);
    queuedWorkReflector.getFinishers().clear();
    queuedWorkReflector.getWork().clear();
    queuedWorkReflector.setNumWaits(0);
  }

  /** Accessor interface for {@link QueuedWork}'s internals. */
  @ForType(QueuedWork.class)
  interface QueuedWorkReflector {

    @Static
    @Accessor("sFinishers")
    LinkedList<Runnable> getFinishers();

    @Static
    @Accessor("sSingleThreadExecutor")
    void setSingleThreadExecutor(ExecutorService o);

    @Static
    @Accessor("sWork")
    LinkedList<Runnable> getWork();

    // yep, it starts with 'm' but it's static
    @Static
    @Accessor("mNumWaits")
    void setNumWaits(int i);

    @Static
    @Accessor("sHandler")
    void setHandler(Handler handler);

    @Static
    void add(Runnable runnable);
  }
}
