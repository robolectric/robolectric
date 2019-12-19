package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.Q;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.AudioCapabilities;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecInfo.VideoCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.SparseIntArray;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.ShadowMediaCodec.CodecConfig;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Implementation of {@link MediaCodecList} to return a list of predefined decoders. Four MIME types
 * can be added to the list: AAC, Opus, AVC, and VP9. Supported features and configurations, e.g.
 * profile level, color format, audio capabilities, video capabilities, etc., for these decoders are
 * pre-set to values based on on-device software and/or hardware decoder of the corresponding type.
 */
@Implements(value = MediaCodecList.class, minSdk = LOLLIPOP)
public class ShadowMediaCodecList {

  private static final ImmutableMap<String, SparseIntArray> CODEC_PROFILE_LEVELS =
      ImmutableMap.of(
          MediaFormat.MIMETYPE_AUDIO_AAC,
          new SparseIntArray() {
            {
              put(CodecProfileLevel.AACObjectELD, 0);
              put(CodecProfileLevel.AACObjectERScalable, 0);
              put(CodecProfileLevel.AACObjectHE, 0);
              put(CodecProfileLevel.AACObjectHE_PS, 0);
              put(CodecProfileLevel.AACObjectLC, 0);
              put(CodecProfileLevel.AACObjectLD, 0);
              put(CodecProfileLevel.AACObjectXHE, 0);
            }
          },
          MediaFormat.MIMETYPE_VIDEO_AVC,
          new SparseIntArray() {
            {
              put(CodecProfileLevel.AVCProfileBaseline, CodecProfileLevel.AVCLevel51);
              put(CodecProfileLevel.AVCProfileConstrainedBaseline, CodecProfileLevel.AVCLevel51);
              put(CodecProfileLevel.AVCProfileMain, CodecProfileLevel.AVCLevel51);
              put(CodecProfileLevel.AVCProfileHigh, CodecProfileLevel.AVCLevel51);
              put(CodecProfileLevel.AVCProfileConstrainedHigh, CodecProfileLevel.AVCLevel51);
            }
          },
          MediaFormat.MIMETYPE_VIDEO_VP9,
          new SparseIntArray() {
            {
              put(CodecProfileLevel.VP9Profile0, CodecProfileLevel.VP9Level5);
              put(CodecProfileLevel.VP9Profile2HDR, CodecProfileLevel.VP9Level5);
              put(CodecProfileLevel.VP9Profile2HDR10Plus, CodecProfileLevel.VP9Level5);
            }
          });

  private static final List<MediaCodecInfo> mediaCodecInfos =
      Collections.synchronizedList(new ArrayList<>());

  /**
   * Add a predefined decoder to the list of MediaCodecInfos.
   *
   * @param name Decoder name. May need to be the same as "type" in {@link
   *     ShadowMediaCodec#addDecoder(String, CodecConfig)} if use together.
   * @param mime one of MIMETYPE_* from {@link MediaFormat}. Throws {@link IllegalArgumentException}
   *     if the given mime is not supported.
   */
  @TargetApi(Q)
  public static void addDecoder(String name, String mime) {
    switch (mime) {
      case MediaFormat.MIMETYPE_AUDIO_AAC:
        mediaCodecInfos.add(createAudioCodecInfo(name, mime));
        break;
      case MediaFormat.MIMETYPE_AUDIO_OPUS:
        mediaCodecInfos.add(createAudioCodecInfo(name, mime));
        break;
      case MediaFormat.MIMETYPE_VIDEO_AVC:
        mediaCodecInfos.add(createVideoCodecInfo(name, mime));
        break;
      case MediaFormat.MIMETYPE_VIDEO_VP9:
        mediaCodecInfos.add(createVideoCodecInfo(name, mime));
        break;
      default:
        throw new IllegalArgumentException(mime + " is not supported.");
    }
  }

  @Resetter
  public static void reset() {
    mediaCodecInfos.clear();
  }

  @Implementation
  protected static int native_getCodecCount() {
    return mediaCodecInfos.size();
  }

  @Implementation
  protected static MediaCodecInfo getNewCodecInfoAt(int index) {
    return mediaCodecInfos.get(index);
  }

  /**
   * Create a sample {@link MediaCodecInfo} for an audio decoder with the predefined profile levels
   * for the mime in {@link #CODEC_PROFILE_LEVELS}.
   *
   * @param name codec name
   * @param mime one of MIMETYPE_* from {@link MediaFormat}
   */
  @TargetApi(Q)
  private static MediaCodecInfo createAudioCodecInfo(String name, String mime) {
    return createAudioCodecInfo(
        name,
        mime,
        getCodecProfileLevelArray(CODEC_PROFILE_LEVELS.getOrDefault(mime, new SparseIntArray())));
  }

  /**
   * Create a sample {@link MediaCodecInfo} for an audio decoder.
   *
   * @param name codec name
   * @param mime one of MIMETYPE_* from {@link MediaFormat}
   * @param profileLevels {@link MediaCodecInfo.CodecProfileLevel} supported by the codec
   */
  @TargetApi(Q)
  private static MediaCodecInfo createAudioCodecInfo(
      String name, String mime, CodecProfileLevel[] profileLevels) {
    MediaFormat mediaFormat = getMediaFormat(mime);

    CodecCapabilities codecCapabilities =
        createDefaultCodecCapabilities(
            mime,
            profileLevels,
            /* colorFormats= */ new int[0],
            /* flagsSupported= */ 0,
            mediaFormat);

    AudioCapabilities audioCapabilities =
        createDefaultAudioCapabilities(codecCapabilities, mediaFormat);
    ReflectionHelpers.setField(codecCapabilities, "mAudioCaps", audioCapabilities);

    return createMediaCodecInfo(name, new CodecCapabilities[] {codecCapabilities}, /* flags= */ 4);
  }

  /**
   * Create a sample {@link MediaCodecInfo} for a video decoder with the predefined profile levels
   * for the mime in {@link #CODEC_PROFILE_LEVELS} and {@link
   * CodecCapabilities#COLOR_FormatYUV420Flexible} as the default color format.
   *
   * @param name codec name
   * @param mime one of MIMETYPE_* from {@link MediaFormat}
   */
  @TargetApi(Q)
  private static MediaCodecInfo createVideoCodecInfo(String name, String mime) {
    return createVideoCodecInfo(
        name,
        mime,
        getCodecProfileLevelArray(CODEC_PROFILE_LEVELS.getOrDefault(mime, new SparseIntArray())),
        new int[] {CodecCapabilities.COLOR_FormatYUV420Flexible});
  }

  /**
   * Create a sample {@link MediaCodecInfo} for a video decoder.
   *
   * @param name codec name
   * @param mime one of MIMETYPE_* from {@link MediaFormat}
   * @param profileLevels {@link MediaCodecInfo.CodecProfileLevel} supported by the codec
   * @param colorFormats color formats supported by the codec, refer to {@link CodecCapabilities}
   *     for possible values
   */
  @TargetApi(Q)
  private static MediaCodecInfo createVideoCodecInfo(
      String name, String mime, CodecProfileLevel[] profileLevels, int[] colorFormats) {
    MediaFormat mediaFormat = getMediaFormat(mime);
    mediaFormat.setFeatureEnabled(CodecCapabilities.FEATURE_AdaptivePlayback, true);

    CodecCapabilities codecCapabilities =
        createDefaultCodecCapabilities(
            mime, profileLevels, colorFormats, /* flagsSupported= */ 1, mediaFormat);

    VideoCapabilities videoCapabilities =
        createDefaultVideoCapabilities(codecCapabilities, mediaFormat);
    ReflectionHelpers.setField(codecCapabilities, "mVideoCaps", videoCapabilities);

    return createMediaCodecInfo(name, new CodecCapabilities[] {codecCapabilities}, /* flags= */ 10);
  }

  /** Create a {@link MediaFormat} with the specified mime type. */
  private static MediaFormat getMediaFormat(String mime) {
    MediaFormat mediaFormat = new MediaFormat();
    mediaFormat.setString(MediaFormat.KEY_MIME, mime);
    return mediaFormat;
  }

  /**
   * Turn codec profile level in a {@link SparseIntArray} into an array of {@link
   * CodecProfileLevel}.
   */
  private static CodecProfileLevel[] getCodecProfileLevelArray(SparseIntArray profileLevels) {
    List<CodecProfileLevel> profileLevelList = new ArrayList<>();
    for (int i = 0; i < profileLevels.size(); i++) {
      CodecProfileLevel profileLevel = new CodecProfileLevel();
      profileLevel.profile = profileLevels.keyAt(i);
      profileLevel.level = profileLevels.valueAt(i);
      profileLevelList.add(profileLevel);
    }
    return profileLevelList.toArray(new CodecProfileLevel[0]);
  }

  /**
   * Create a sample {@link CodecCapabilities}.
   *
   * @param mime one of MIMETYPE_* from {@link MediaFormat}
   * @param profileLevels {@link MediaCodecInfo.CodecProfileLevel} supported by the codec
   * @param colorFormats color formats supported by the codec, refer to {@link CodecCapabilities}
   *     for possible values
   * @param flagsSupported a flag to indicate supported features by the codec, refer to FEATURE_*
   *     constants in {@link MediaCodecInfo.CodecCapabilities}
   * @param mediaFormat a {@link MediaFormat} with default configurations for this codec
   */
  private static CodecCapabilities createDefaultCodecCapabilities(
      String mime,
      CodecProfileLevel[] profileLevels,
      int[] colorFormats,
      int flagsSupported,
      MediaFormat mediaFormat) {
    CodecCapabilities codecCapabilities = new CodecCapabilities();
    codecCapabilities.profileLevels = profileLevels;
    codecCapabilities.colorFormats = colorFormats;
    ReflectionHelpers.setField(codecCapabilities, "mMime", mime);
    ReflectionHelpers.setField(codecCapabilities, "mFlagsSupported", flagsSupported);
    ReflectionHelpers.setField(codecCapabilities, "mMaxSupportedInstances", 32);
    ReflectionHelpers.setField(codecCapabilities, "mCapabilitiesInfo", mediaFormat);
    ReflectionHelpers.setField(codecCapabilities, "mDefaultFormat", mediaFormat);
    return codecCapabilities;
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

  /**
   * Create a {@link MediaCodecInfo}.
   *
   * @param name codec name
   * @param capabilities an array of {@link MediaCodecInfo.CodecCapabilities} to indicate the
   *     capabilities of the codec
   * @param flags a flag to indicate characteristics of the codec, refer to {@link MediaCodecInfo}
   *     for the available characteristics
   */
  @TargetApi(Q)
  private static MediaCodecInfo createMediaCodecInfo(
      String name, CodecCapabilities[] capabilities, int flags) {
    return ReflectionHelpers.callConstructor(
        MediaCodecInfo.class,
        ClassParameter.from(String.class, name),
        ClassParameter.from(String.class, name), // canonicalName
        ClassParameter.from(int.class, flags),
        ClassParameter.from(CodecCapabilities[].class, capabilities));
  }
}
