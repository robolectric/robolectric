package org.robolectric.fakes;

import android.os.Vibrator;
import android.media.AudioAttributes;
import org.robolectric.annotation.internal.DoNotInstrument;

@DoNotInstrument
public class RoboVibrator extends Vibrator {
  private boolean vibrating;
  private boolean cancelled;
  private long milliseconds;
  private long[] pattern;
  private int repeat;

  @Override public boolean hasVibrator() {
    return true;
  }

  public void vibrate(long milliseconds) {
    vibrating = true;
    this.milliseconds = milliseconds;
  }

  public void vibrate(long[] pattern, int repeat) {
    vibrating = true;
    this.pattern = pattern;
    this.repeat = repeat;
  }

  @Override
  public void vibrate(int i, String s, long l, AudioAttributes audioAttributes) {

  }

  @Override
  public void vibrate(int i, String s, long[] longs, int i1, AudioAttributes audioAttributes) {

  }

  public void cancel() {
    cancelled = true;
    vibrating = false;
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
}