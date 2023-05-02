package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.base.Preconditions.checkNotNull;

import android.annotation.NonNull;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioSystem;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.Optional;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link AudioSystem}. */
@Implements(value = AudioSystem.class, isInAndroidSdk = false)
public class ShadowAudioSystem {

  // from frameworks/base/core/jni/android_media_AudioSystem.cpp
  private static final int MAX_CHANNEL_COUNT = 8;
  private static final int MAX_SAMPLE_RATE = 192000;
  private static final int MIN_SAMPLE_RATE = 4000;

  /**
   * Table to store key-pair of {@link AudioFormat} and {@link AudioAttributes#getUsage()} with
   * value of support for Direct Playback. Used with {@link #setDirectPlaybackSupport(AudioFormat,
   * AudioAttributes, int)}, {@link #getOffloadSupport(AudioFormat, AudioAttributes)}, and {@link
   * #getDirectPlaybackSupport(AudioFormat, AudioAttributes)}.
   */
  private static final Table<AudioFormat, Integer, Integer> directPlaybackSupportTable =
      Tables.synchronizedTable(HashBasedTable.create());

  @Implementation(minSdk = S)
  protected static int native_getMaxChannelCount() {
    return MAX_CHANNEL_COUNT;
  }

  @Implementation(minSdk = S)
  protected static int native_getMaxSampleRate() {
    return MAX_SAMPLE_RATE;
  }

  @Implementation(minSdk = S)
  protected static int native_getMinSampleRate() {
    return MIN_SAMPLE_RATE;
  }

  @Implementation(minSdk = Q, maxSdk = R)
  protected static int native_get_FCC_8() {
    // Return the value hard-coded in native code:
    // https://cs.android.com/android/platform/superproject/+/master:system/media/audio/include/system/audio-base.h;l=197;drc=c84ca89fa5d660046364897482b202c797c8595e
    return 8;
  }

  /**
   * Sets direct playback support for a key-pair of {@link AudioFormat} and {@link AudioAttributes}.
   * As a result, calling {@link #getOffloadSupport} or {@link #getDirectPlaybackSupport} with the
   * same pair of {@link AudioFormat} and {@link AudioAttributes} values will return the cached
   * support value.
   *
   * <p>Note that calling this method mutates static state so after use one must call {@link
   * #reset()} (e.g. in a JUnit @After method).
   *
   * @param format the audio format (codec, sample rate, channels)
   * @param attr the {@link AudioAttributes} to be used for playback
   * @param directPlaybackSupport the level of direct playback support to save for the format and
   *     attribute pair. Must be one of {@link AudioSystem#DIRECT_NOT_SUPPORTED}, {@link
   *     AudioSystem#OFFLOAD_NOT_SUPPORTED}, {@link AudioSystem#OFFLOAD_SUPPORTED}, {@link
   *     AudioSystem#OFFLOAD_GAPLESS_SUPPORTED}, or a combination of {@link
   *     AudioSystem#DIRECT_OFFLOAD_SUPPORTED}, {@link AudioSystem#DIRECT_OFFLOAD_GAPLESS_SUPPORTED}
   *     and {@link AudioSystem#DIRECT_BITSTREAM_SUPPORTED}
   */
  public static void setDirectPlaybackSupport(
      @NonNull AudioFormat format,
      @NonNull AudioAttributes attr,
      @NonNull int directPlaybackSupport) {
    checkNotNull(format, "Illegal null AudioFormat");
    checkNotNull(attr, "Illegal null AudioAttributes");
    synchronized (directPlaybackSupportTable) {
      directPlaybackSupportTable.put(format, attr.getUsage(), directPlaybackSupport);
    }
  }

  /**
   * Retrieves the stored direct playback support for the {@link AudioFormat} and {@link
   * AudioAttributes}. If no value was stored for the key-pair then {@link
   * AudioSystem#OFFLOAD_NOT_SUPPORTED} is returned.
   *
   * @param format the audio format (codec, sample rate, channels) to be used for playback
   * @param attr the {@link AudioAttributes} to be used for playback
   * @return the level of offload playback support for the format and attributes.
   */
  @Implementation(minSdk = S)
  protected static int getOffloadSupport(
      @NonNull AudioFormat format, @NonNull AudioAttributes attr) {
    synchronized (directPlaybackSupportTable) {
      return Optional.ofNullable(directPlaybackSupportTable.get(format, attr.getUsage()))
          .orElse(AudioSystem.OFFLOAD_NOT_SUPPORTED);
    }
  }

  /**
   * Retrieves the stored direct playback support for the {@link AudioFormat} and {@link
   * AudioAttributes}. If no value was stored for the key-pair then {@link
   * AudioSystem#DIRECT_NOT_SUPPORTED} is returned.
   *
   * @param format the audio format (codec, sample rate, channels) to be used for playback
   * @param attr the {@link AudioAttributes} to be used for playback
   * @return the level of direct playback playback support for the format and attributes.
   */
  @Implementation(minSdk = TIRAMISU)
  protected static int getDirectPlaybackSupport(
      @NonNull AudioFormat format, @NonNull AudioAttributes attr) {
    synchronized (directPlaybackSupportTable) {
      return Optional.ofNullable(directPlaybackSupportTable.get(format, attr.getUsage()))
          .orElse(AudioSystem.DIRECT_NOT_SUPPORTED);
    }
  }

  @Resetter
  public static void reset() {
    synchronized (directPlaybackSupportTable) {
      directPlaybackSupportTable.clear();
    }
  }
}
