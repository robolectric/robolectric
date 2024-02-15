package org.robolectric.shadows;

import android.annotation.RequiresApi;
import android.media.AudioFormat;
import android.media.AudioProfile;
import android.os.Build.VERSION_CODES;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link AudioProfile}. */
@RequiresApi(VERSION_CODES.S)
public class AudioProfileBuilder {

  private int format = AudioFormat.ENCODING_PCM_16BIT;
  private int[] samplingRates = new int[] {48000};
  private int[] channelMasks = new int[] {AudioFormat.CHANNEL_OUT_STEREO};
  private int[] channelIndexMasks = new int[] {};
  private int encapsulationType = AudioProfile.AUDIO_ENCAPSULATION_TYPE_NONE;

  private AudioProfileBuilder() {}

  public static AudioProfileBuilder newBuilder() {
    return new AudioProfileBuilder();
  }

  /**
   * Sets the audio format.
   *
   * <p>The default is {@link AudioFormat#ENCODING_PCM_16BIT}.
   *
   * @param format The audio format. The possible values are the {@code ENCODING_} constants defined
   *     in {@link AudioFormat}.
   */
  @CanIgnoreReturnValue
  public AudioProfileBuilder setFormat(int format) {
    this.format = format;
    return this;
  }

  /**
   * Sets the sampling rates.
   *
   * <p>The default is a single-item array with 48000.
   *
   * @param samplingRates The array of supported sampling rates.
   */
  @CanIgnoreReturnValue
  public AudioProfileBuilder setSamplingRates(int[] samplingRates) {
    this.samplingRates = samplingRates;
    return this;
  }

  /**
   * Sets the channel masks.
   *
   * <p>The default is a single-item array with {@link AudioFormat#CHANNEL_OUT_STEREO}.
   *
   * @param channelMasks The array of supported channel masks. The possible values are the {@code
   *     CHANNEL_OUT_} constants defined in {@link AudioFormat}.
   */
  @CanIgnoreReturnValue
  public AudioProfileBuilder setChannelMasks(int[] channelMasks) {
    this.channelMasks = channelMasks;
    return this;
  }

  /**
   * Sets the channel index masks.
   *
   * <p>The default is an empty array.
   *
   * @param channelIndexMasks The array of supported channel index masks.
   */
  @CanIgnoreReturnValue
  public AudioProfileBuilder setChannelIndexMasks(int[] channelIndexMasks) {
    this.channelIndexMasks = channelIndexMasks;
    return this;
  }

  /**
   * Sets the encapsulation type.
   *
   * <p>The default is {@link AudioProfile#AUDIO_ENCAPSULATION_TYPE_NONE}.
   *
   * @param encapsulationType The encapsulation type. The possible values are the {@code
   *     AUDIO_ENCAPSULATION_TYPE_} constants defined in {@link AudioProfile}.
   */
  @CanIgnoreReturnValue
  public AudioProfileBuilder setEncapsulationType(int encapsulationType) {
    this.encapsulationType = encapsulationType;
    return this;
  }

  public AudioProfile build() {
    return ReflectionHelpers.callConstructor(
        AudioProfile.class,
        ClassParameter.from(Integer.TYPE, format),
        ClassParameter.from(int[].class, samplingRates),
        ClassParameter.from(int[].class, channelMasks),
        ClassParameter.from(int[].class, channelIndexMasks),
        ClassParameter.from(Integer.TYPE, encapsulationType));
  }
}
