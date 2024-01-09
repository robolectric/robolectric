package org.robolectric.shadows;

import android.media.AudioProfile;
import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link AudioProfile}. */
@RequiresApi(VERSION_CODES.S)
public class AudioProfileBuilder {

  private int format;
  private int[] samplingRates = new int[0];
  private int[] channelMasks = new int[0];
  private int[] channelIndexMasks = new int[0];
  private int encapsulationType;

  public static AudioProfileBuilder newBuilder() {
    return new AudioProfileBuilder();
  }

  @CanIgnoreReturnValue
  public AudioProfileBuilder setFormat(int format) {
    this.format = format;
    return this;
  }

  @CanIgnoreReturnValue
  public AudioProfileBuilder setSamplingRates(int[] samplingRates) {
    this.samplingRates = samplingRates;
    return this;
  }

  @CanIgnoreReturnValue
  public AudioProfileBuilder setChannelMasks(int[] channelMasks) {
    this.channelMasks = channelMasks;
    return this;
  }

  @CanIgnoreReturnValue
  public AudioProfileBuilder setChannelIndexMasks(int[] channelIndexMasks) {
    this.channelIndexMasks = channelIndexMasks;
    return this;
  }

  @CanIgnoreReturnValue
  public AudioProfileBuilder setEncapsulationType(int encapsulationType) {
    this.encapsulationType = encapsulationType;
    return this;
  }

  public AudioProfile build() {
    return ReflectionHelpers.callConstructor(
        AudioProfile.class,
        ClassParameter.from(int.class, format),
        ClassParameter.from(int[].class, samplingRates),
        ClassParameter.from(int[].class, channelMasks),
        ClassParameter.from(int[].class, channelIndexMasks),
        ClassParameter.from(int.class, encapsulationType));
  }
}
