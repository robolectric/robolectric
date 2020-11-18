package org.robolectric.shadows;

import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.AudioCapabilities;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecInfo.EncoderCapabilities;
import android.media.MediaCodecInfo.VideoCapabilities;
import android.media.MediaFormat;
import com.google.common.base.Preconditions;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;
import org.robolectric.util.reflector.Static;

/** Builder for {@link MediaCodecInfo}. */
public class MediaCodecInfoBuilder {

  private String name;
  private boolean isEncoder;
  private boolean isVendor;
  private boolean isSoftwareOnly;
  private boolean isHardwareAccelerated;
  private CodecCapabilities[] capabilities = new CodecCapabilities[0];

  private MediaCodecInfoBuilder() {}

  /** Create a new {@link MediaCodecInfoBuilder}. */
  public static MediaCodecInfoBuilder newBuilder() {
    return new MediaCodecInfoBuilder();
  }

  /**
   * Sets the codec name.
   *
   * @param name codec name.
   * @throws NullPointerException if name is null.
   */
  public MediaCodecInfoBuilder setName(String name) {
    this.name = Preconditions.checkNotNull(name);
    return this;
  }

  /**
   * Sets the codec role.
   *
   * @param isEncoder a boolean to indicate whether the codec is an encoder {@code true} or a
   *     decoder {@code false}. Default value is {@code false}.
   */
  public MediaCodecInfoBuilder setIsEncoder(boolean isEncoder) {
    this.isEncoder = isEncoder;
    return this;
  }

  /**
   * Sets the codec provider.
   *
   * @param isVendor a boolean to indicate whether the codec is provided by the device manufacturer
   *     {@code true} or by the Android platform {@code false}. Default value is {@code false}.
   */
  public MediaCodecInfoBuilder setIsVendor(boolean isVendor) {
    this.isVendor = isVendor;
    return this;
  }

  /**
   * Sets whether the codec is softwrare only or not.
   *
   * @param isSoftwareOnly a boolean to indicate whether the codec is software only {@code true} or
   *     not {@code false}. Default value is {@code false}.
   */
  public MediaCodecInfoBuilder setIsSoftwareOnly(boolean isSoftwareOnly) {
    this.isSoftwareOnly = isSoftwareOnly;
    return this;
  }

  /**
   * Sets whether the codec is hardware accelerated or not.
   *
   * @param isHardwareAccelerated a boolean to indicate whether the codec is hardware accelerated
   *     {@code true} or not {@code false}. Default value is {@code false}.
   */
  public MediaCodecInfoBuilder setIsHardwareAccelerated(boolean isHardwareAccelerated) {
    this.isHardwareAccelerated = isHardwareAccelerated;
    return this;
  }

  /**
   * Sets codec capabilities.
   *
   * <p>Use {@link CodecCapabilitiesBuilder} can be to create an instance of {@link
   * CodecCapabilities}.
   *
   * @param capabilities one or multiple {@link CodecCapabilities}.
   * @throws NullPointerException if capabilities is null.
   */
  public MediaCodecInfoBuilder setCapabilities(CodecCapabilities... capabilities) {
    this.capabilities = capabilities;
    return this;
  }

  public MediaCodecInfo build() {
    Preconditions.checkNotNull(name, "Codec name is not set.");

    int flags = getCodecFlags();

    return ReflectionHelpers.callConstructor(
        MediaCodecInfo.class,
        ClassParameter.from(String.class, name),
        ClassParameter.from(String.class, name), // canonicalName
        ClassParameter.from(int.class, flags),
        ClassParameter.from(CodecCapabilities[].class, capabilities));
  }

  /** Accessor interface for {@link MediaCodecInfo}'s internals. */
  @ForType(MediaCodecInfo.class)
  interface MediaCodecInfoReflector {

    @Static
    @Accessor("FLAG_IS_ENCODER")
    int getIsEncoderFlagValue();

    @Static
    @Accessor("FLAG_IS_VENDOR")
    int getIsVendorFlagValue();

    @Static
    @Accessor("FLAG_IS_SOFTWARE_ONLY")
    int getIsSoftwareOnlyFlagValue();

    @Static
    @Accessor("FLAG_IS_HARDWARE_ACCELERATED")
    int getIsHardwareAcceleratedFlagValue();
  }

  /** Convert the boolean flags describing codec to values recognized by {@link MediaCodecInfo}. */
  private int getCodecFlags() {
    MediaCodecInfoReflector mediaCodecInfoReflector =
        Reflector.reflector(MediaCodecInfoReflector.class);

    int flags = 0;

    if (isEncoder) {
      flags |= mediaCodecInfoReflector.getIsEncoderFlagValue();
    }
    if (isVendor) {
      flags |= mediaCodecInfoReflector.getIsVendorFlagValue();
    }
    if (isSoftwareOnly) {
      flags |= mediaCodecInfoReflector.getIsSoftwareOnlyFlagValue();
    }
    if (isHardwareAccelerated) {
      flags |= mediaCodecInfoReflector.getIsHardwareAcceleratedFlagValue();
    }

    return flags;
  }

  /** Builder for {@link CodecCapabilities}. */
  public static class CodecCapabilitiesBuilder {
    private MediaFormat mediaFormat;
    private boolean isEncoder;
    private CodecProfileLevel[] profileLevels = new CodecProfileLevel[0];
    private int[] colorFormats;

    private CodecCapabilitiesBuilder() {}

    /** Creates a new {@link CodecCapabilitiesBuilder}. */
    public static CodecCapabilitiesBuilder newBuilder() {
      return new CodecCapabilitiesBuilder();
    }

    /**
     * Sets media format.
     *
     * @param mediaFormat a {@link MediaFormat} supported by the codec. It is a requirement for
     *     mediaFormat to have {@link MediaFormat.KEY_MIME} set. Other keys are optional.
     * @throws {@link NullPointerException} if mediaFormat is null.
     * @throws {@link IllegalArgumentException} if mediaFormat does not have {@link
     *     MediaFormat.KEY_MIME}.
     */
    public CodecCapabilitiesBuilder setMediaFormat(MediaFormat mediaFormat) {
      Preconditions.checkNotNull(mediaFormat);
      Preconditions.checkArgument(
          mediaFormat.getString(MediaFormat.KEY_MIME) != null,
          "MIME type of the format is not set.");
      this.mediaFormat = mediaFormat;
      return this;
    }

    /**
     * Sets codec role.
     *
     * @param isEncoder a boolean to indicate whether the codec is an encoder or a decoder. Default
     *     value is false.
     */
    public CodecCapabilitiesBuilder setIsEncoder(boolean isEncoder) {
      this.isEncoder = isEncoder;
      return this;
    }

    /**
     * Sets profiles and levels.
     *
     * @param profileLevels an array of {@link MediaCodecInfo.CodecProfileLevel} supported by the
     *     codec.
     * @throws {@link NullPointerException} if profileLevels is null.
     */
    public CodecCapabilitiesBuilder setProfileLevels(CodecProfileLevel[] profileLevels) {
      this.profileLevels = Preconditions.checkNotNull(profileLevels);
      return this;
    }

    /**
     * Sets color formats.
     *
     * @param colorFormats an array of color formats supported by the video codec. Refer to {@link
     *     CodecCapabilities} for possible values.
     */
    public CodecCapabilitiesBuilder setColorFormats(int[] colorFormats) {
      this.colorFormats = colorFormats;
      return this;
    }

    /** Accessor interface for {@link CodecCapabilities}'s internals. */
    @ForType(CodecCapabilities.class)
    interface CodecCapabilitiesReflector {

      @Accessor("mMime")
      void setMime(String mime);

      @Accessor("mMaxSupportedInstances")
      void setMaxSupportedInstances(int maxSupportedInstances);

      @Accessor("mDefaultFormat")
      void setDefaultFormat(MediaFormat mediaFormat);

      @Accessor("mCapabilitiesInfo")
      void setCapabilitiesInfo(MediaFormat mediaFormat);

      @Accessor("mVideoCaps")
      void setVideoCaps(VideoCapabilities videoCaps);

      @Accessor("mAudioCaps")
      void setAudioCaps(AudioCapabilities audioCaps);

      @Accessor("mEncoderCaps")
      void setEncoderCaps(EncoderCapabilities encoderCaps);

      @Accessor("mFlagsSupported")
      void setFlagsSupported(int flagsSupported);
    }

    public CodecCapabilities build() {
      Preconditions.checkNotNull(mediaFormat, "mediaFormat is not set.");
      Preconditions.checkNotNull(profileLevels, "profileLevels is not set.");

      final String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
      final boolean isVideoCodec = mime.startsWith("video/");

      CodecCapabilities caps = new CodecCapabilities();
      CodecCapabilitiesReflector capsReflector =
          Reflector.reflector(CodecCapabilitiesReflector.class, caps);

      caps.profileLevels = profileLevels;
      if (isVideoCodec) {
        Preconditions.checkNotNull(colorFormats, "colorFormats should not be null for video codec");
        caps.colorFormats = colorFormats;
      } else {
        Preconditions.checkArgument(
            colorFormats == null || colorFormats.length == 0,
            "colorFormats should not be set for audio codec");
        caps.colorFormats = new int[0]; // To prevet crash in CodecCapabilities.dup().
      }

      capsReflector.setMime(mime);
      capsReflector.setMaxSupportedInstances(32);
      capsReflector.setDefaultFormat(mediaFormat);
      capsReflector.setCapabilitiesInfo(mediaFormat);

      if (isVideoCodec) {
        VideoCapabilities videoCaps = createDefaultVideoCapabilities(caps, mediaFormat);
        capsReflector.setVideoCaps(videoCaps);
      } else {
        AudioCapabilities audioCaps = createDefaultAudioCapabilities(caps, mediaFormat);
        capsReflector.setAudioCaps(audioCaps);
      }

      if (isEncoder) {
        EncoderCapabilities encoderCaps = createDefaultEncoderCapabilities(caps, mediaFormat);
        capsReflector.setEncoderCaps(encoderCaps);
      }

      int flagsSupported = getSupportedFeatures(caps, mediaFormat);
      capsReflector.setFlagsSupported(flagsSupported);

      return caps;
    }

    /** Create a default {@link AudioCapabilities} for a given {@link MediaFormat}. */
    private static AudioCapabilities createDefaultAudioCapabilities(
        CodecCapabilities parent, MediaFormat mediaFormat) {
      return ReflectionHelpers.callStaticMethod(
          AudioCapabilities.class,
          "create",
          ClassParameter.from(MediaFormat.class, mediaFormat),
          ClassParameter.from(CodecCapabilities.class, parent));
    }

    /** Create a default {@link VideoCapabilities} for a given {@link MediaFormat}. */
    private static VideoCapabilities createDefaultVideoCapabilities(
        CodecCapabilities parent, MediaFormat mediaFormat) {
      return ReflectionHelpers.callStaticMethod(
          VideoCapabilities.class,
          "create",
          ClassParameter.from(MediaFormat.class, mediaFormat),
          ClassParameter.from(CodecCapabilities.class, parent));
    }

    /** Create a default {@link EncoderCapabilities} for a given {@link MediaFormat}. */
    private static EncoderCapabilities createDefaultEncoderCapabilities(
        CodecCapabilities parent, MediaFormat mediaFormat) {
      return ReflectionHelpers.callStaticMethod(
          EncoderCapabilities.class,
          "create",
          ClassParameter.from(MediaFormat.class, mediaFormat),
          ClassParameter.from(CodecCapabilities.class, parent));
    }

    /**
     * Read codec features from a given {@link MediaFormat} and convert them to values recognized by
     * {@link CodecCapabilities}.
     */
    private static int getSupportedFeatures(CodecCapabilities parent, MediaFormat mediaFormat) {
      int flagsSupported = 0;
      Object[] validFeatures = ReflectionHelpers.callInstanceMethod(parent, "getValidFeatures");
      for (Object validFeature : validFeatures) {
        String featureName = (String) ReflectionHelpers.getField(validFeature, "mName");
        int featureValue = (int) ReflectionHelpers.getField(validFeature, "mValue");
        if (mediaFormat.containsFeature(featureName)
            && mediaFormat.getFeatureEnabled(featureName)) {
          flagsSupported |= featureValue;
        }
      }
      return flagsSupported;
    }
  }
}
