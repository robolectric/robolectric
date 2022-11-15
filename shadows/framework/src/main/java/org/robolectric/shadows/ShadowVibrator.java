package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.media.AudioAttributes;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.vibrator.VibrationEffectSegment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(Vibrator.class)
public class ShadowVibrator {
  static boolean vibrating;
  static boolean cancelled;
  static long milliseconds;
  protected static long[] pattern;
  protected static final List<VibrationEffectSegment> vibrationEffectSegments = new ArrayList<>();
  protected static final List<PrimitiveEffect> primitiveEffects = new ArrayList<>();
  protected static final List<Integer> supportedPrimitives = new ArrayList<>();
  @Nullable protected static VibrationAttributes vibrationAttributesFromLastVibration;
  @Nullable protected static AudioAttributes audioAttributesFromLastVibration;
  static int repeat;
  static boolean hasVibrator = true;
  static boolean hasAmplitudeControl = false;
  static int effectId;

  /** Controls the return value of {@link Vibrator#hasVibrator()} the default is true. */
  public void setHasVibrator(boolean hasVibrator) {
    ShadowVibrator.hasVibrator = hasVibrator;
  }

  /** Controls the return value of {@link Vibrator#hasAmplitudeControl()} the default is false. */
  public void setHasAmplitudeControl(boolean hasAmplitudeControl) {
    ShadowVibrator.hasAmplitudeControl = hasAmplitudeControl;
  }

  /**
   * Returns true if the Vibrator is currently vibrating as controlled by {@link
   * Vibrator#vibrate(long)}
   */
  @Implementation(minSdk = R)
  public boolean isVibrating() {
    return vibrating;
  }

  /** Returns true if the Vibrator has been cancelled. */
  public boolean isCancelled() {
    return cancelled;
  }

  /** Returns the last vibration duration in MS. */
  public long getMilliseconds() {
    return milliseconds;
  }

  /** Returns the last vibration pattern. */
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

  /** Returns the last vibration repeat times. */
  public int getRepeat() {
    return repeat;
  }

  /** Returns the last list of {@link VibrationEffectSegment}. */
  public List<VibrationEffectSegment> getVibrationEffectSegments() {
    return vibrationEffectSegments;
  }

  /** Returns the last list of {@link PrimitiveEffect}. */
  @Nullable
  public List<PrimitiveEffect> getPrimitiveEffects() {
    return primitiveEffects;
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

  /** Returns the {@link VibrationAttributes} from the last vibration. */
  @Nullable
  public VibrationAttributes getVibrationAttributesFromLastVibration() {
    return vibrationAttributesFromLastVibration;
  }

  /** Returns the {@link AudioAttributes} from the last vibration. */
  @Nullable
  public AudioAttributes getAudioAttributesFromLastVibration() {
    return audioAttributesFromLastVibration;
  }

  @Resetter
  public static void reset() {
    vibrating = false;
    cancelled = false;
    milliseconds = 0;
    pattern = null;
    vibrationEffectSegments.clear();
    primitiveEffects.clear();
    supportedPrimitives.clear();
    vibrationAttributesFromLastVibration = null;
    audioAttributesFromLastVibration = null;
    repeat = 0;
    hasVibrator = true;
    hasAmplitudeControl = false;
    effectId = 0;
  }

  /**
   * A data class for exposing {@link VibrationEffect.Composition$PrimitiveEffect}, which is a
   * hidden non TestApi class introduced in Android R.
   */
  public static class PrimitiveEffect {
    public final int id;
    public final float scale;
    public final int delay;

    public PrimitiveEffect(int id, float scale, int delay) {
      this.id = id;
      this.scale = scale;
      this.delay = delay;
    }

    @Override
    public String toString() {
      return "PrimitiveEffect{" + "id=" + id + ", scale=" + scale + ", delay=" + delay + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || !getClass().isInstance(o)) {
        return false;
      }
      PrimitiveEffect that = (PrimitiveEffect) o;
      return id == that.id && Float.compare(that.scale, scale) == 0 && delay == that.delay;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, scale, delay);
    }
  }
}
