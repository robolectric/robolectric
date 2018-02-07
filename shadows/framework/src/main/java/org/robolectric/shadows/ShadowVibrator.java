package org.robolectric.shadows;

import android.media.AudioAttributes;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.internal.DoNotInstrument;

@Implements(Vibrator.class)
public class ShadowVibrator {
  boolean vibrating;
  boolean cancelled;
  long milliseconds;
  protected long[] pattern;
  int repeat;
  boolean hasVibrator = true;
  boolean hasAmplitudeControl = false;

  /**
   * Controls the return value of {@link Vibrator#hasVibrator()} the default is true.
   */
  public void setHasVibrator(boolean hasVibrator) {
    this.hasVibrator = hasVibrator;
  }

  /**
   * Controls the return value of {@link Vibrator#hasAmplitudeControl()} the default is false.
   */
  public void setHasAmplitudeControl(boolean hasAmplitudeControl) {
    this.hasAmplitudeControl = hasAmplitudeControl;
  }

  /**
   * Returns true if the Vibrator is currently vibrating as controlled by {@link Vibrator#vibrate(long)}
   */
  public boolean isVibrating() {
    return vibrating;
  }

  /**
   * Returns true if the Vibrator has been cancelled.
   */
  public boolean isCancelled() {
    return cancelled;
  }

  /**
   * Returns the last vibration duration in MS.
   */
  public long getMilliseconds() {
    return milliseconds;
  }

  /**
   * Returns the last vibration pattern.
   */
  public long[] getPattern() {
    return pattern;
  }

  /**
   * Returns the last vibration repeat times.
   */
  public int getRepeat() {
    return repeat;
  }

}
