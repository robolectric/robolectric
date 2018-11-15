package org.robolectric.shadows;

import android.os.CountDownTimer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(CountDownTimer.class)
public class ShadowCountDownTimer {
  private boolean started;
  private long countDownInterval;
  private long millisInFuture;

  @RealObject CountDownTimer countDownTimer;

  @Implementation
  protected void __constructor__(long millisInFuture, long countDownInterval) {
    this.countDownInterval = countDownInterval;
    this.millisInFuture = millisInFuture;
    this.started = false;
  }

  @Implementation
  protected final synchronized CountDownTimer start() {
    started = true;
    return countDownTimer;
  }

  @Implementation
  protected final void cancel() {
    started = false;
  }

  public void invokeTick(long millisUntilFinished) {
    countDownTimer.onTick(millisUntilFinished);
  }

  public void invokeFinish() {
    countDownTimer.onFinish();
  }

  public boolean hasStarted() {
    return started;
  }

  public long getCountDownInterval() {
    return countDownInterval;
  }

  public long getMillisInFuture() {
    return millisInFuture;
  }
}
