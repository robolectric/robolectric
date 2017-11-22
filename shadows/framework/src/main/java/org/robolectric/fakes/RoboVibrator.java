package org.robolectric.fakes;

import android.media.AudioAttributes;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Robolectric implementation of {@link android.os.Vibrator}.
 */
@DoNotInstrument
public class RoboVibrator extends Vibrator {
  private boolean vibrating;
  private boolean cancelled;
  private long milliseconds;
  private long[] pattern;
  private int repeat;
  private Handler handler = new Handler(Looper.myLooper());
  private Runnable stopVibratingRunnable = new Runnable() {
    @Override public void run() {
      vibrating = false;
    }
  };

  @Override public boolean hasVibrator() {
    return true;
  }

  public void vibrate(long milliseconds) {
    vibrating = true;
    this.milliseconds = milliseconds;
    handler.removeCallbacks(stopVibratingRunnable);
    handler.postDelayed(stopVibratingRunnable, milliseconds);
  }

  public void vibrate(long[] pattern, int repeat) {
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

  public void vibrate(int i, String s, long l, AudioAttributes audioAttributes) {

  }

  public void vibrate(int i, String s, long[] longs, int i1, AudioAttributes audioAttributes) {

  }

  @Override
  public void vibrate(int i, String s, VibrationEffect effect, AudioAttributes audioAttributes) {

  }

  public void vibrate(int i, String s, long l) {

  }

  public void vibrate(int i, String s, long[] l, int i1) {

  }

  @Override public void cancel() {
    cancelled = true;
    vibrating = false;
    handler.removeCallbacks(stopVibratingRunnable);
  }

  public boolean isVibrating() {
    return vibrating;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public long getMilliseconds() {
    return milliseconds;
  }

  public long[] getPattern() {
    return pattern;
  }

  public int getRepeat() {
    return repeat;
  }

  @Override
  public boolean hasAmplitudeControl() {
    return false;
  }
}
