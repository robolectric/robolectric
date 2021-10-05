package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.media.AudioAttributes;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemVibrator;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.vibrator.VibrationEffectSegment;
import java.util.List;
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
  protected void vibrate(
      int uid, String opPkg, long[] pattern, int repeat, AudioAttributes attributes) {
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

  @Implementation(minSdk = O, maxSdk = P)
  protected void vibrate(
      int uid, String opPkg, VibrationEffect effect, AudioAttributes attributes) {
    vibrate(uid, opPkg, effect, null, attributes);
  }

  @Implementation(minSdk = Q, maxSdk = R)
  protected void vibrate(
      int uid, String opPkg, VibrationEffect effect, String reason, AudioAttributes attributes) {
    try {
      Class<?> waveformClass = Class.forName("android.os.VibrationEffect$Waveform");
      Class<?> prebakedClass = Class.forName("android.os.VibrationEffect$Prebaked");
      Class<?> oneShotClass = Class.forName("android.os.VibrationEffect$OneShot");

      if (waveformClass.isInstance(effect)) {
        recordVibratePattern(
            (long[]) ReflectionHelpers.callInstanceMethod(effect, "getTimings"),
            ReflectionHelpers.callInstanceMethod(effect, "getRepeatIndex"));
      } else if (prebakedClass.isInstance(effect)) {
        recordVibratePredefined(
            ReflectionHelpers.callInstanceMethod(effect, "getDuration"),
            ReflectionHelpers.callInstanceMethod(effect, "getId"));
      } else if (oneShotClass.isInstance(effect)) {
        long timing;

        if (RuntimeEnvironment.getApiLevel() >= P) {
          timing = ReflectionHelpers.callInstanceMethod(effect, "getDuration");
        } else {
          timing = ReflectionHelpers.callInstanceMethod(effect, "getTiming");
        }

        recordVibrate(timing);
      } else {
        throw new UnsupportedOperationException(
            "unrecognized effect type " + effect.getClass().getName());
      }
    } catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException(
          "unrecognized effect type " + effect.getClass().getName(), e);
    }
  }

  @Implementation(minSdk = S)
  protected void vibrate(
      int uid,
      String opPkg,
      VibrationEffect effect,
      String reason,
      VibrationAttributes attributes) {
    if (effect instanceof VibrationEffect.Composed) {
      VibrationEffect.Composed composedEffect = (VibrationEffect.Composed) effect;

      recordVibratePattern(composedEffect.getSegments(), composedEffect.getRepeatIndex());
    } else {
      throw new UnsupportedOperationException(
          "unrecognized effect type " + effect.getClass().getName());
    }
  }

  private void recordVibratePattern(List<VibrationEffectSegment> segments, int repeatIndex) {
    long[] pattern = new long[segments.size()];
    int i = 0;
    for (VibrationEffectSegment segment : segments) {
      pattern[i] = segment.getDuration();
      i++;
    }
    recordVibratePattern(pattern, repeatIndex);
  }

  private void recordVibratePredefined(long milliseconds, int effectId) {
    vibrating = true;
    this.effectId = effectId;
    this.milliseconds = milliseconds;
    handler.removeCallbacks(stopVibratingRunnable);
    handler.postDelayed(stopVibratingRunnable, this.milliseconds);
  }

  private void recordVibrate(long milliseconds) {
    vibrating = true;
    this.milliseconds = milliseconds;
    handler.removeCallbacks(stopVibratingRunnable);
    handler.postDelayed(stopVibratingRunnable, this.milliseconds);
  }

  protected void recordVibratePattern(long[] pattern, int repeat) {
    vibrating = true;
    this.pattern = pattern;
    this.repeat = repeat;
    handler.removeCallbacks(stopVibratingRunnable);
    if (repeat < 0) {
      long endDelayMillis = 0;
      for (long t : pattern) {
        endDelayMillis += t;
      }
      this.milliseconds = endDelayMillis;
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
