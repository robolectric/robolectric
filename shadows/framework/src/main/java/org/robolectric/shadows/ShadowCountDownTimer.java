package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.CountDownTimer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(CountDownTimer.class)
public class ShadowCountDownTimer {
  static final String PROPERTY_USE_REAL_IMPL = "robolectric.useRealCountDownTimer";

  private boolean started;
  @RealObject private CountDownTimer realCountDownTimer;

  @Implementation
  protected synchronized CountDownTimer start() {
    started = true;

    if (useRealCountDownTimer()) {
      return reflector(CountDownTimerReflector.class, realCountDownTimer).start();
    } else {
      return realCountDownTimer;
    }
  }

  @Implementation
  protected synchronized void cancel() {
    started = false;

    reflector(CountDownTimerReflector.class, realCountDownTimer).cancel();
  }

  public void invokeTick(long millisUntilFinished) {
    realCountDownTimer.onTick(millisUntilFinished);
  }

  public void invokeFinish() {
    realCountDownTimer.onFinish();
  }

  public boolean hasStarted() {
    return started;
  }

  public boolean isCancelled() {
    return reflector(CountDownTimerReflector.class, realCountDownTimer).getCancelled();
  }

  public long getCountDownInterval() {
    return reflector(CountDownTimerReflector.class, realCountDownTimer).getCountDownInterval();
  }

  public long getMillisInFuture() {
    return reflector(CountDownTimerReflector.class, realCountDownTimer).getMillisInFuture();
  }

  /**
   * If the {@code robolectric.useRealCountDownTimer} system property is {@code true}, the real
   * framework code of {@link CountDownTimer} is used.
   *
   * <p>If it is {@code false}, the {@link #start()} and {@link #cancel()} methods are no-ops.
   *
   * <p>This allows tests to use the old behavior (ie. the no-op version) during a migration.
   */
  private static boolean useRealCountDownTimer() {
    return Boolean.parseBoolean(System.getProperty(PROPERTY_USE_REAL_IMPL, "true"));
  }

  @ForType(CountDownTimer.class)
  interface CountDownTimerReflector {
    @Direct
    CountDownTimer start();

    @Direct
    void cancel();

    @Accessor("mCancelled")
    boolean getCancelled();

    @Accessor("mCountdownInterval")
    long getCountDownInterval();

    @Accessor("mMillisInFuture")
    long getMillisInFuture();
  }
}
