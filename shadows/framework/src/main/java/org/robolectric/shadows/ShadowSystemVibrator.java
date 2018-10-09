package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;

import android.media.AudioAttributes;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemVibrator;
import android.os.VibrationEffect;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = SystemVibrator.class, isInAndroidSdk = false)
public class ShadowSystemVibrator extends ShadowVibrator {

  private Handler handler = new Handler(Looper.myLooper());
  private Runnable stopVibratingRunnable = () -> vibrating = false;

  @Implementation
  protected boolean hasVibrator() {
    return hasVibrator;
  }

  @Implementation(minSdk = O)
  protected boolean hasAmplitudeControl() {
    return hasAmplitudeControl;
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected void vibrate(long[] pattern, int repeat) {
    recordVibratePattern(pattern, repeat);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT_WATCH)
  protected void vibrate(int owningUid, String owningPackage, long[] pattern, int repeat) {
    recordVibratePattern(pattern, repeat);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected void vibrate(int uid, String opPkg, long[] pattern, int repeat, AudioAttributes attributes) {
    recordVibratePattern(pattern, repeat);
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  public void vibrate(long milliseconds) {
    recordVibrate(milliseconds);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT_WATCH)
  public void vibrate(int owningUid, String owningPackage, long milliseconds) {
    recordVibrate(milliseconds);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected void vibrate(int uid, String opPkg, long milliseconds, AudioAttributes attributes) {
    recordVibrate(milliseconds);
  }

  @Implementation(minSdk = O, maxSdk = VERSION_CODES.P)
  protected void vibrate(int uid, String opPkg, VibrationEffect effect, AudioAttributes attributes) {
    vibrate(uid, opPkg, effect, null, attributes);
  }

  @Implementation(minSdk = VERSION_CODES.CUR_DEVELOPMENT)
  protected void vibrate(int uid, String opPkg, VibrationEffect effect,
      String reason, AudioAttributes attributes) {
    if (effect instanceof VibrationEffect.Waveform) {
      VibrationEffect.Waveform waveform = (VibrationEffect.Waveform) effect;
      recordVibratePattern(waveform.getTimings(), waveform.getRepeatIndex());

    } else {
      VibrationEffect.OneShot oneShot = (VibrationEffect.OneShot) effect;

      long timing;

      if (RuntimeEnvironment.getApiLevel() >= P) {
        timing = oneShot.getDuration();
      } else {
        timing = ReflectionHelpers.callInstanceMethod(oneShot, "getTiming");
      }

      recordVibrate(timing);
    }
  }

  private void recordVibrate(long milliseconds) {
    vibrating = true;
    this.milliseconds = milliseconds;
    handler.removeCallbacks(stopVibratingRunnable);
    handler.postDelayed(stopVibratingRunnable, this.milliseconds);
  }

  private void recordVibratePattern(long[] pattern, int repeat) {
    vibrating = true;
    this.pattern = pattern;
    this.repeat = repeat;
    handler.removeCallbacks(stopVibratingRunnable);
    if (repeat < 0) {
      long endDelayMillis = 0;
      for (long t : pattern) {
        endDelayMillis += t;
      }
      handler.postDelayed(stopVibratingRunnable, endDelayMillis);
    }
  }

  @Implementation
  protected void cancel() {
    cancelled = true;
    vibrating = false;
    handler.removeCallbacks(stopVibratingRunnable);
  }
}
