package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.media.AudioAttributes;
import android.os.Vibrator;
import android.os.vibrator.PrimitiveSegment;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(Vibrator.class)
public class ShadowVibrator {
  static boolean vibrating;
  static boolean cancelled;
  static long milliseconds;
  protected static long[] pattern;
  protected static final List<Object> vibrationEffectSegments = new ArrayList<>();
  protected static final List<PrimitiveEffect> primitiveEffects = new ArrayList<>();
  protected static final List<Integer> supportedPrimitives = new ArrayList<>();
  protected static final SparseArray<Integer> primitiveidsToDurationMillis = new SparseArray<>();

  @Nullable protected static Object vibrationAttributesFromLastVibration;
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

  /** Returns the last list of {@link PrimitiveSegment} vibrations in {@link PrimitiveEffect}. */
  @SuppressWarnings("JdkCollectors") // toImmutableList is only supported in Java 8+.
  public List<PrimitiveEffect> getPrimitiveSegmentsInPrimitiveEffects() {
    return vibrationEffectSegments.stream()
        .filter(segment -> segment instanceof PrimitiveSegment)
        .map(
            segment ->
                new PrimitiveEffect(
                    ReflectionHelpers.getField(segment, "mPrimitiveId"),
                    ReflectionHelpers.getField(segment, "mScale"),
                    ReflectionHelpers.getField(segment, "mDelay")))
        .collect(Collectors.toList());
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

  @Implementation(minSdk = S)
  protected int[] getPrimitiveDurations(int... primitiveIds) {
    int[] durations = new int[primitiveIds.length];
    for (int i = 0; i < primitiveIds.length; i++) {
      durations[i] = primitiveidsToDurationMillis.get(primitiveIds[i], /* valueIfKeyNotFound= */ 0);
    }
    return durations;
  }

  /** Set a custom duration in milliseconds for the given vibration primitive. */
  public void setPrimitiveDurations(int primitiveId, int durationMillis) {
    ShadowVibrator.primitiveidsToDurationMillis.put(primitiveId, durationMillis);
  }

  /** Returns the {@link android.os.VibrationAttributes} from the last vibration. */
  @Nullable
  public Object getVibrationAttributesFromLastVibration() {
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
    primitiveidsToDurationMillis.clear();
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
