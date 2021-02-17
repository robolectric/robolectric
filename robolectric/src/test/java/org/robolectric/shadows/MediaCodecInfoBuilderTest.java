package org.robolectric.shadows;

import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;
import static android.media.MediaFormat.MIMETYPE_AUDIO_OPUS;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_VP9;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Tests for {@link MediaCodecInfoBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class MediaCodecInfoBuilderTest {

  private static final String AAC_ENCODER_NAME = "test.encoder.aac";
  private static final String VP9_DECODER_NAME = "test.decoder.vp9";
  private static final String MULTIFORMAT_ENCODER_NAME = "test.encoder.multiformat";

  private static final MediaFormat AAC_MEDIA_FORMAT =
      createMediaFormat(
          MIMETYPE_AUDIO_AAC, new String[] {CodecCapabilities.FEATURE_DynamicTimestamp});
  private static final MediaFormat OPUS_MEDIA_FORMAT =
      createMediaFormat(
          MIMETYPE_AUDIO_OPUS, new String[] {CodecCapabilities.FEATURE_AdaptivePlayback});
  private static final MediaFormat AVC_MEDIA_FORMAT =
      createMediaFormat(MIMETYPE_VIDEO_AVC, new String[] {CodecCapabilities.FEATURE_IntraRefresh});
  private static final MediaFormat VP9_MEDIA_FORMAT =
      createMediaFormat(
          MIMETYPE_VIDEO_VP9,
          new String[] {
            CodecCapabilities.FEATURE_SecurePlayback, CodecCapabilities.FEATURE_MultipleFrames
          });

  private static final CodecProfileLevel[] AAC_PROFILE_LEVELS =
      new CodecProfileLevel[] {
        createCodecProfileLevel(CodecProfileLevel.AACObjectELD, 0),
        createCodecProfileLevel(CodecProfileLevel.AACObjectHE, 1)
      };
  private static final CodecProfileLevel[] AVC_PROFILE_LEVELS =
      new CodecProfileLevel[] {
        createCodecProfileLevel(CodecProfileLevel.AVCProfileMain, CodecProfileLevel.AVCLevel12)
      };
  private static final CodecProfileLevel[] VP9_PROFILE_LEVELS =
      new CodecProfileLevel[] {
        createCodecProfileLevel(CodecProfileLevel.VP9Profile3, CodecProfileLevel.VP9Level52)
      };

  private static final int[] AVC_COLOR_FORMATS =
      new int[] {
        CodecCapabilities.COLOR_FormatYUV420Flexible, CodecCapabilities.COLOR_FormatYUV420Planar
      };
  private static final int[] VP9_COLOR_FORMATS =
      new int[] {
        CodecCapabilities.COLOR_FormatYUV422Flexible, CodecCapabilities.COLOR_Format32bitABGR8888
      };

  @Test
  @Config(minSdk = Q)
  public void canCreateAudioEncoderCapabilities() {
    CodecCapabilities codecCapabilities =
        MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
            .setMediaFormat(AAC_MEDIA_FORMAT)
            .setIsEncoder(true)
            .setProfileLevels(AAC_PROFILE_LEVELS)
            .build();

    assertThat(codecCapabilities.getMimeType()).isEqualTo(MIMETYPE_AUDIO_AAC);
    assertThat(codecCapabilities.getAudioCapabilities()).isNotNull();
    assertThat(codecCapabilities.getVideoCapabilities()).isNull();
    assertThat(codecCapabilities.getEncoderCapabilities()).isNotNull();
    assertThat(codecCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_DynamicTimestamp))
        .isTrue();
    assertThat(codecCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_FrameParsing))
        .isFalse();
    assertThat(codecCapabilities.profileLevels).hasLength(AAC_PROFILE_LEVELS.length);
    assertThat(codecCapabilities.profileLevels).isEqualTo(AAC_PROFILE_LEVELS);
  }

  @Test
  @Config(minSdk = Q)
  public void canCreateAudioDecoderCapabilities() {
    CodecCapabilities codecCapabilities =
        MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
            .setMediaFormat(OPUS_MEDIA_FORMAT)
            .setProfileLevels(new CodecProfileLevel[0])
            .build();

    assertThat(codecCapabilities.getMimeType()).isEqualTo(MIMETYPE_AUDIO_OPUS);
    assertThat(codecCapabilities.getAudioCapabilities()).isNotNull();
    assertThat(codecCapabilities.getVideoCapabilities()).isNull();
    assertThat(codecCapabilities.getEncoderCapabilities()).isNull();
    assertThat(codecCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_AdaptivePlayback))
        .isTrue();
    assertThat(codecCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_MultipleFrames))
        .isFalse();
    assertThat(codecCapabilities.profileLevels).hasLength(0);
  }

  @Test
  @Config(minSdk = Q)
  public void canCreateVideoEncoderCapabilities() {
    CodecCapabilities codecCapabilities =
        MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
            .setMediaFormat(AVC_MEDIA_FORMAT)
            .setIsEncoder(true)
            .setProfileLevels(AVC_PROFILE_LEVELS)
            .setColorFormats(AVC_COLOR_FORMATS)
            .build();

    assertThat(codecCapabilities.getMimeType()).isEqualTo(MIMETYPE_VIDEO_AVC);
    assertThat(codecCapabilities.getAudioCapabilities()).isNull();
    assertThat(codecCapabilities.getVideoCapabilities()).isNotNull();
    assertThat(codecCapabilities.getEncoderCapabilities()).isNotNull();
    assertThat(codecCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_IntraRefresh))
        .isTrue();
    assertThat(codecCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_MultipleFrames))
        .isFalse();
    assertThat(codecCapabilities.profileLevels).hasLength(AVC_PROFILE_LEVELS.length);
    assertThat(codecCapabilities.profileLevels).isEqualTo(AVC_PROFILE_LEVELS);
    assertThat(codecCapabilities.colorFormats).hasLength(AVC_COLOR_FORMATS.length);
    assertThat(codecCapabilities.colorFormats).isEqualTo(AVC_COLOR_FORMATS);
  }

  @Test
  @Config(minSdk = Q)
  public void canCreateVideoDecoderCapabilities() {
    CodecCapabilities codecCapabilities =
        MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
            .setMediaFormat(VP9_MEDIA_FORMAT)
            .setProfileLevels(VP9_PROFILE_LEVELS)
            .setColorFormats(VP9_COLOR_FORMATS)
            .build();

    assertThat(codecCapabilities.getMimeType()).isEqualTo(MIMETYPE_VIDEO_VP9);
    assertThat(codecCapabilities.getAudioCapabilities()).isNull();
    assertThat(codecCapabilities.getVideoCapabilities()).isNotNull();
    assertThat(codecCapabilities.getEncoderCapabilities()).isNull();
    assertThat(codecCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_SecurePlayback))
        .isTrue();
    assertThat(codecCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_MultipleFrames))
        .isTrue();
    assertThat(codecCapabilities.isFeatureSupported(CodecCapabilities.FEATURE_DynamicTimestamp))
        .isFalse();
    assertThat(codecCapabilities.profileLevels).hasLength(VP9_PROFILE_LEVELS.length);
    assertThat(codecCapabilities.profileLevels).isEqualTo(VP9_PROFILE_LEVELS);
    assertThat(codecCapabilities.colorFormats).hasLength(VP9_COLOR_FORMATS.length);
    assertThat(codecCapabilities.colorFormats).isEqualTo(VP9_COLOR_FORMATS);
  }

  @Test(expected = NullPointerException.class)
  public void setMediaFormatToNullThrowsException() {
    MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder().setMediaFormat(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setMediaFormatWithoutMimeThrowsException() {
    MediaFormat mediaFormat = new MediaFormat();
    MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder().setMediaFormat(mediaFormat);
  }

  @Test(expected = NullPointerException.class)
  public void setProfileLevelsToNullThrowsException() {
    MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder().setProfileLevels(null);
  }

  @Test(expected = NullPointerException.class)
  public void buildWithoutSettingMediaFormatThrowsException() {
    MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder().build();
  }

  @Test(expected = NullPointerException.class)
  public void buildWithoutSettingColorFormatThrowsException() {
    MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
        .setMediaFormat(AVC_MEDIA_FORMAT)
        .setProfileLevels(AVC_PROFILE_LEVELS)
        .build();
  }

  @Test
  @Config(minSdk = Q)
  public void canCreateMediaCodecInfoForEncoder() {
    CodecCapabilities codecCapabilities =
        MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
            .setMediaFormat(AAC_MEDIA_FORMAT)
            .setIsEncoder(true)
            .setProfileLevels(AAC_PROFILE_LEVELS)
            .build();

    MediaCodecInfo mediaCodecInfo =
        MediaCodecInfoBuilder.newBuilder()
            .setName(AAC_ENCODER_NAME)
            .setIsEncoder(true)
            .setIsVendor(true)
            .setCapabilities(codecCapabilities)
            .build();

    assertThat(mediaCodecInfo.getName()).isEqualTo(AAC_ENCODER_NAME);
    assertThat(mediaCodecInfo.isEncoder()).isTrue();
    assertThat(mediaCodecInfo.isVendor()).isTrue();
    assertThat(mediaCodecInfo.isSoftwareOnly()).isFalse();
    assertThat(mediaCodecInfo.isHardwareAccelerated()).isFalse();
    assertThat(mediaCodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_AUDIO_AAC);
    assertThat(mediaCodecInfo.getCapabilitiesForType(MIMETYPE_AUDIO_AAC)).isNotNull();
  }

  @Test
  @Config(minSdk = Q)
  public void isVendor_properlySet() {
    MediaCodecInfo mediaCodecInfo =
        MediaCodecInfoBuilder.newBuilder()
            .setName(AAC_ENCODER_NAME)
            .setIsEncoder(false)
            .setIsVendor(true)
            .build();

    assertThat(mediaCodecInfo.getName()).isEqualTo(AAC_ENCODER_NAME);
    assertThat(mediaCodecInfo.isEncoder()).isFalse();
    assertThat(mediaCodecInfo.isVendor()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void canCreateMediaCodecInfoForDecoder() {
    CodecCapabilities codecCapabilities =
        MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
            .setMediaFormat(VP9_MEDIA_FORMAT)
            .setProfileLevels(VP9_PROFILE_LEVELS)
            .setColorFormats(VP9_COLOR_FORMATS)
            .build();

    MediaCodecInfo mediaCodecInfo =
        MediaCodecInfoBuilder.newBuilder()
            .setName(VP9_DECODER_NAME)
            .setIsSoftwareOnly(true)
            .setIsHardwareAccelerated(true)
            .setCapabilities(codecCapabilities)
            .build();

    assertThat(mediaCodecInfo.getName()).isEqualTo(VP9_DECODER_NAME);
    assertThat(mediaCodecInfo.isEncoder()).isFalse();
    assertThat(mediaCodecInfo.isVendor()).isFalse();
    assertThat(mediaCodecInfo.isSoftwareOnly()).isTrue();
    assertThat(mediaCodecInfo.isHardwareAccelerated()).isTrue();
    assertThat(mediaCodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_VIDEO_VP9);
    assertThat(mediaCodecInfo.getCapabilitiesForType(MIMETYPE_VIDEO_VP9)).isNotNull();
  }

  @Test
  @Config(minSdk = Q)
  public void canCreateMediaCodecInfoWithMultipleFormats() {
    CodecCapabilities avcEncoderCapabilities =
        MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
            .setMediaFormat(AVC_MEDIA_FORMAT)
            .setIsEncoder(true)
            .setProfileLevels(AVC_PROFILE_LEVELS)
            .setColorFormats(AVC_COLOR_FORMATS)
            .build();

    CodecCapabilities vp9EncoderCapabilities =
        MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
            .setMediaFormat(VP9_MEDIA_FORMAT)
            .setIsEncoder(true)
            .setProfileLevels(VP9_PROFILE_LEVELS)
            .setColorFormats(VP9_COLOR_FORMATS)
            .build();

    MediaCodecInfo mediaCodecInfo =
        MediaCodecInfoBuilder.newBuilder()
            .setName(MULTIFORMAT_ENCODER_NAME)
            .setIsEncoder(true)
            .setCapabilities(avcEncoderCapabilities, vp9EncoderCapabilities)
            .build();

    assertThat(mediaCodecInfo.getName()).isEqualTo(MULTIFORMAT_ENCODER_NAME);
    assertThat(mediaCodecInfo.isEncoder()).isTrue();
    assertThat(mediaCodecInfo.isVendor()).isFalse();
    assertThat(mediaCodecInfo.isSoftwareOnly()).isFalse();
    assertThat(mediaCodecInfo.isHardwareAccelerated()).isFalse();
    assertThat(mediaCodecInfo.getSupportedTypes()).asList().contains(MIMETYPE_VIDEO_AVC);
    assertThat(mediaCodecInfo.getSupportedTypes()).asList().contains(MIMETYPE_VIDEO_VP9);
    assertThat(mediaCodecInfo.getCapabilitiesForType(MIMETYPE_VIDEO_AVC)).isNotNull();
    assertThat(mediaCodecInfo.getCapabilitiesForType(MIMETYPE_VIDEO_VP9)).isNotNull();
  }

  @Test(expected = NullPointerException.class)
  public void setNameToNullThrowsException() {
    MediaCodecInfoBuilder.newBuilder().setName(null);
  }

  @Test(expected = NullPointerException.class)
  public void buildWithoutSettingNameThrowsException() {
    MediaCodecInfoBuilder.newBuilder().build();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void mediaCodecInfo_preQ() {
    if (RuntimeEnvironment.getApiLevel() <= M) {
      MediaCodecList.getCodecCount();
    }
    CodecCapabilities codecCapabilities =
        MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
            .setMediaFormat(AAC_MEDIA_FORMAT)
            .setIsEncoder(true)
            .setProfileLevels(AAC_PROFILE_LEVELS)
            .build();

    MediaCodecInfo mediaCodecInfo =
        MediaCodecInfoBuilder.newBuilder()
            .setName(AAC_ENCODER_NAME)
            .setIsEncoder(true)
            .setCapabilities(codecCapabilities)
            .build();

    assertThat(mediaCodecInfo.getName()).isEqualTo(AAC_ENCODER_NAME);
    assertThat(mediaCodecInfo.isEncoder()).isTrue();
    assertThat(mediaCodecInfo.getSupportedTypes()).asList().containsExactly(MIMETYPE_AUDIO_AAC);
    assertThat(mediaCodecInfo.getCapabilitiesForType(MIMETYPE_AUDIO_AAC)).isNotNull();
  }

  /** Create a sample {@link CodecProfileLevel}. */
  private static CodecProfileLevel createCodecProfileLevel(int profile, int level) {
    CodecProfileLevel profileLevel = new CodecProfileLevel();
    profileLevel.profile = profile;
    profileLevel.level = level;
    return profileLevel;
  }

  /**
   * Create a sample {@link MediaFormat}.
   *
   * @param mime one of MIMETYPE_* from {@link MediaFormat}.
   * @param features an array of CodecCapabilities.FEATURE_ features to be enabled.
   */
  private static MediaFormat createMediaFormat(String mime, String[] features) {
    MediaFormat mediaFormat = new MediaFormat();
    mediaFormat.setString(MediaFormat.KEY_MIME, mime);
    for (String feature : features) {
      mediaFormat.setFeatureEnabled(feature, true);
    }
    return mediaFormat;
  }
}
