package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.os.Vibrator;
import android.os.vibrator.VibrationEffectSegment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Vibrator.class)
public class ShadowVibrator {
  boolean vibrating;
  boolean cancelled;
  long milliseconds;
  protected long[] pattern;
  protected final List<VibrationEffectSegment> vibrationEffectSegments = new ArrayList<>();
  protected final List<Integer> supportedPrimitives = new ArrayList<>();
  int repeat;
  boolean hasVibrator = true;
  boolean hasAmplitudeControl = false;
  int effectId;

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
   * Returns the last vibration effect ID of a {@link VibrationEffect#Prebaked} (e.g. {@link
   * VibrationEffect#EFFECT_CLICK}).
   *
   * <p>This field is non-zero only if a {@link VibrationEffect#Prebaked} was ever requested.
   */
  public int getEffectId() {
    return effectId;
  }

  /**
   * Returns the last vibration repeat times.
   */
  public int getRepeat() {
    return repeat;
  }

  /** Returns the last list of {@link VibrationEffectSegment}. */
  public List<VibrationEffectSegment> getVibrationEffectSegments() {
    return vibrationEffectSegments;
  }

  @Implementation(minSdk = R)
  protected boolean areAllPrimitivesSupported(int... primitiveIds) {
    for (int i = 0; i < primitiveIds.length; i++) {
      if (!supportedPrimitives.contains(primitiveIds[i])) {
        return false;
      }
    }
    return true;
  }

  /** Adds supported vibration primitives. */
  public void setSupportedPrimitives(Collection<Integer> primitives) {
    supportedPrimitives.clear();
    supportedPrimitives.addAll(primitives);
  }
}
