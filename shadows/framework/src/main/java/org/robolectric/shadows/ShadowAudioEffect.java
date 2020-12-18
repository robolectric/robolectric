package org.robolectric.shadows;

import static android.media.audiofx.AudioEffect.STATE_INITIALIZED;
import static android.media.audiofx.AudioEffect.STATE_UNINITIALIZED;
import static android.media.audiofx.AudioEffect.SUCCESS;
import static com.google.common.base.Preconditions.checkArgument;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.media.audiofx.AudioEffect;
import android.os.Build.VERSION_CODES;
import com.google.common.collect.ImmutableList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Implements {@link AudioEffect} by shadowing its native methods. */
@Implements(value = AudioEffect.class)
public class ShadowAudioEffect {
  private static final List<AudioEffect.Descriptor> descriptors = new ArrayList<>();
  private static final List<AudioEffect> audioEffects = new ArrayList<>();

  private final Map<ByteBuffer, ByteBuffer> parameters = new HashMap<>();

  @RealObject AudioEffect audioEffect;

  private int priority;
  private int audioSession;
  private boolean isEnabled = false;
  private int errorCode = SUCCESS;

  @Implementation(minSdk = VERSION_CODES.JELLY_BEAN, maxSdk = VERSION_CODES.LOLLIPOP_MR1)
  protected int native_setup(
      Object audioEffectThis,
      String type,
      String uuid,
      int priority,
      int audioSession,
      int[] id,
      Object[] desc) {
    return native_setup(
        audioEffectThis, type, uuid, priority, audioSession, id, desc, /* opPackageName= */ null);
  }

  @Implementation(minSdk = VERSION_CODES.M, maxSdk = VERSION_CODES.Q)
  protected int native_setup(
      Object audioEffectThis,
      String type,
      String uuid,
      int priority,
      int audioSession,
      int[] id,
      Object[] desc,
      String opPackageName) {
    audioEffects.add(audioEffect);
    this.priority = priority;
    this.audioSession = audioSession;
    return SUCCESS;
  }

  /** Marks the {@link AudioEffect} as enabled, and always returns {@code SUCCESS}. */
  @Implementation
  protected int native_setEnabled(boolean enabled) {
    if (errorCode != SUCCESS) {
      return errorCode;
    }
    isEnabled = enabled;
    return SUCCESS;
  }

  /** Returns whether the {@link AudioEffect} is enabled (as per {@link #native_setEnabled}). */
  @Implementation
  protected boolean native_getEnabled() {
    return isEnabled;
  }

  /**
   * Sets the parameter with the given key {@code param} to the given value {@code value}.
   *
   * @return always {@code SUCCESS}
   */
  @Implementation
  protected int native_setParameter(int psize, byte[] param, int vsize, byte[] value) {
    if (errorCode != SUCCESS) {
      return errorCode;
    }
    ByteBuffer parameterKey = createReadOnlyByteBuffer(param, psize);
    ByteBuffer parameterValue = createReadOnlyByteBuffer(value, vsize);
    parameters.put(parameterKey, parameterValue);
    return SUCCESS;
  }

  /**
   * Gets the value of the parameter with key {@code param}, by putting its value in {@code value}.
   *
   * <p>Note: Sub-classes of {@link ShadowAudioEffect} can declare default values for any
   * parameters. Note: If the given parameter has not been set, and there is no default value for
   * that parameter, then we "return" (set {@code value} to) a single integer 0.
   *
   * @return the size of the returned value, in bytes, or an error code in case of failure.
   */
  @Implementation
  protected int native_getParameter(int psize, byte[] param, int vsize, byte[] value) {
    if (errorCode != SUCCESS) {
      return errorCode;
    }

    ByteBuffer parameterKey = ByteBuffer.wrap(Arrays.copyOf(param, psize));
    if (parameters.containsKey(parameterKey)) {
      ByteBuffer parameterValue = parameters.get(parameterKey);
      return copyByteBufferToArrayAndReturnSize(parameterValue, value);
    }

    Optional<ByteBuffer> defaultValue = getDefaultParameter(parameterKey);
    if (defaultValue.isPresent()) {
      return copyByteBufferToArrayAndReturnSize(defaultValue.get(), value);
    }

    byte[] val = AudioEffect.intToByteArray(0);
    System.arraycopy(val, 0, value, 0, 4);
    return 4; // The number of meaningful bytes in the value array
  }

  private static int copyByteBufferToArrayAndReturnSize(ByteBuffer byteBuffer, byte[] array) {
    checkArgument(byteBuffer.position() == 0);
    for (int i = 0; i < byteBuffer.limit(); i++) {
      array[i] = byteBuffer.get(i);
    }
    return byteBuffer.limit();
  }

  /**
   * Allows sub-classes to provide default parameters.
   *
   * <p>Override this method to provide default parameters.
   */
  protected Optional<ByteBuffer> getDefaultParameter(ByteBuffer parameter) {
    return Optional.empty();
  }

  /** Returns the priority set in the {@link AudioEffect} ctor. */
  public int getPriority() {
    return priority;
  }

  /** Returns the audio session set in the {@link AudioEffect} ctor. */
  public int getAudioSession() {
    return audioSession;
  }

  /**
   * Updates the state of the {@link AudioEffect} itself.
   *
   * <p>This can be used e.g. to put the AudioEffect in an unexpected state and cause an exception
   * the next time the Visualizer is used.
   */
  public void setInitialized(boolean initialized) {
    reflector(ReflectorAudioEffect.class, audioEffect)
        .setState(initialized ? STATE_INITIALIZED : STATE_UNINITIALIZED);
  }

  /**
   * Sets the error code to override setter methods in this class.
   *
   * <p>When the error code is set to anything other than {@link SUCCESS} setters in the AudioEffect
   * will early-out and return that error code.
   */
  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * Adds an effect represented by an {@link AudioEffect.Descriptor}, only to be queried from {@link
   * #queryEffects()}.
   */
  public static void addEffect(AudioEffect.Descriptor descriptor) {
    descriptors.add(descriptor);
  }

  /**
   * Returns the set of audio effects added through {@link #addEffect}.
   *
   * <p>Note: in the original {@link AudioEffect} implementation this method returns all the
   * existing unique AudioEffects created through an {@link AudioEffect} ctor. In this
   * implementation only the effects added through {@link #addEffect} are returned here.
   */
  @Implementation
  protected static AudioEffect.Descriptor[] queryEffects() {
    return descriptors.toArray(new AudioEffect.Descriptor[descriptors.size()]);
  }

  /** Returns all effects created with an {@code AudioEffect} constructor. */
  public static ImmutableList<AudioEffect> getAudioEffects() {
    return ImmutableList.copyOf(audioEffects);
  }

  /** Removes this audio effect from the set of active audio effects. */
  @Implementation
  protected void native_release() {
    audioEffects.remove(audioEffect);
  }

  static ByteBuffer createReadOnlyByteBuffer(byte[] array) {
    return createReadOnlyByteBuffer(array, array.length);
  }

  static ByteBuffer createReadOnlyByteBuffer(byte[] array, int length) {
    return ByteBuffer.wrap(Arrays.copyOf(array, length)).asReadOnlyBuffer();
  }

  @Resetter
  public static void reset() {
    descriptors.clear();
    audioEffects.clear();
  }

  /** Accessor interface for {@link AudioEffect}'s internals. */
  @ForType(AudioEffect.class)
  private interface ReflectorAudioEffect {
    @Accessor("mState")
    void setState(int state);
  }
}
