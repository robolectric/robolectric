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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
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
   * AudioAttributes, int)}, and {@link #getDirectPlaybackSupport(AudioFormat, AudioAttributes)}.
   */
  private static final Table<AudioFormat, Integer, Integer> directPlaybackSupportTable =
      Tables.synchronizedTable(HashBasedTable.create());

  /**
   * Table to store pair of {@link OffloadSupportFormat} and {@link
   * AudioAttributes#getVolumeControlStream()} with a value of Offload Playback support. Used with
   * {@link #native_get_offload_support}. The table uses {@link OffloadSupportFormat} rather than
   * {@link AudioFormat} because {@link #native_get_offload_support} does not pass all the fields
   * needed to reliably reconstruct {@link AudioFormat} instances.
   */
  private static final Table<OffloadSupportFormat, Integer, Integer> offloadPlaybackSupportTable =
      Tables.synchronizedTable(HashBasedTable.create());

  /**
   * Multimap to store whether a pair of {@link OffloadSupportFormat} and {@link
   * AudioAttributes#getVolumeControlStream()} ()} support offloaded playback. Used with {@link
   * #native_is_offload_supported}. The map uses {@link OffloadSupportFormat} keys rather than
   * {@link AudioFormat} because {@link #native_is_offload_supported} does not pass all the fields
   * needed to reliably reconstruct {@link AudioFormat} instances.
   */
  private static final Multimap<OffloadSupportFormat, Integer> offloadSupportedMap =
      Multimaps.synchronizedMultimap(HashMultimap.create());

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
   * As a result, calling {@link #getDirectPlaybackSupport} with the same pair of {@link
   * AudioFormat} and {@link AudioAttributes} values will return the cached support value.
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
      @NonNull AudioFormat format, @NonNull AudioAttributes attr, int directPlaybackSupport) {
    checkNotNull(format, "Illegal null AudioFormat");
    checkNotNull(attr, "Illegal null AudioAttributes");
    directPlaybackSupportTable.put(format, attr.getUsage(), directPlaybackSupport);
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
    return Optional.ofNullable(directPlaybackSupportTable.get(format, attr.getUsage()))
        .orElse(AudioSystem.DIRECT_NOT_SUPPORTED);
  }

  /**
   * Sets offload playback support for a key-pair of {@link AudioFormat} and {@link
   * AudioAttributes}. As a result, calling {@link AudioSystem#getOffloadSupport} with the same pair
   * of {@link AudioFormat} and {@link AudioAttributes} values will return the cached support value.
   *
   * @param format the audio format (codec, sample rate, channels)
   * @param attr the {@link AudioAttributes} to be used for playback
   * @param offloadSupport the level of offload playback support to save for the format and
   *     attribute pair. Must be one of {@link AudioSystem#OFFLOAD_NOT_SUPPORTED}, {@link
   *     AudioSystem#OFFLOAD_SUPPORTED} or {@link AudioSystem#OFFLOAD_GAPLESS_SUPPORTED}.
   */
  public static void setOffloadPlaybackSupport(
      @NonNull AudioFormat format, @NonNull AudioAttributes attr, int offloadSupport) {
    checkNotNull(format, "Illegal null AudioFormat");
    checkNotNull(attr, "Illegal null AudioAttributes");
    offloadPlaybackSupportTable.put(
        new OffloadSupportFormat(
            format.getEncoding(),
            format.getSampleRate(),
            format.getChannelMask(),
            format.getChannelIndexMask()),
        attr.getVolumeControlStream(),
        offloadSupport);
  }

  /**
   * Sets whether offload playback is supported for a key-pair of {@link AudioFormat} and {@link
   * AudioAttributes}. As a result, calling {@link AudioSystem#isOffloadSupported} with the same
   * pair of {@link AudioFormat} and {@link AudioAttributes} values will return {@code supported}.
   *
   * @param format the audio format (codec, sample rate, channels)
   * @param attr the {@link AudioAttributes} to be used for playback
   */
  public static void setOffloadSupported(
      @NonNull AudioFormat format, @NonNull AudioAttributes attr, boolean supported) {
    OffloadSupportFormat offloadSupportFormat =
        new OffloadSupportFormat(
            format.getEncoding(),
            format.getSampleRate(),
            format.getChannelMask(),
            format.getChannelIndexMask());
    if (supported) {
      offloadSupportedMap.put(offloadSupportFormat, attr.getVolumeControlStream());
    } else {
      offloadSupportedMap.remove(offloadSupportFormat, attr.getVolumeControlStream());
    }
  }

  @Implementation(minSdk = Q, maxSdk = R)
  protected static boolean native_is_offload_supported(
      int encoding, int sampleRate, int channelMask, int channelIndexMask, int streamType) {
    return offloadSupportedMap.containsEntry(
        new OffloadSupportFormat(encoding, sampleRate, channelMask, channelIndexMask), streamType);
  }

  @Implementation(minSdk = S)
  protected static int native_get_offload_support(
      int encoding, int sampleRate, int channelMask, int channelIndexMask, int streamType) {
    return Optional.ofNullable(
            offloadPlaybackSupportTable.get(
                new OffloadSupportFormat(encoding, sampleRate, channelMask, channelIndexMask),
                streamType))
        .orElse(AudioSystem.OFFLOAD_NOT_SUPPORTED);
  }

  @Resetter
  public static void reset() {
    directPlaybackSupportTable.clear();
    offloadPlaybackSupportTable.clear();
    offloadSupportedMap.clear();
  }

  /**
   * Struct to hold specific values from {@link AudioFormat} which are used in {@link
   * #native_get_offload_support} and {@link #native_is_offload_supported}.
   */
  private static class OffloadSupportFormat {
    public final int encoding;
    public final int sampleRate;
    public final int channelMask;
    public final int channelIndexMask;

    public OffloadSupportFormat(
        int encoding, int sampleRate, int channelMask, int channelIndexMask) {
      this.encoding = encoding;
      this.sampleRate = sampleRate;
      this.channelMask = channelMask;
      this.channelIndexMask = channelIndexMask;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof OffloadSupportFormat)) {
        return false;
      }
      OffloadSupportFormat that = (OffloadSupportFormat) o;
      return encoding == that.encoding
          && sampleRate == that.sampleRate
          && channelMask == that.channelMask
          && channelIndexMask == that.channelIndexMask;
    }

    @Override
    public int hashCode() {
      int result = encoding;
      result = 31 * result + sampleRate;
      result = 31 * result + channelMask;
      result = 31 * result + channelIndexMask;
      return result;
    }
  }
}
