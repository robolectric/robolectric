package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowMediaCodecList}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public class ShadowMediaCodecListTest {

  private static final MediaCodecInfo AAC_ENCODER_INFO =
      MediaCodecInfoBuilder.newBuilder()
          .setName("shadow.test.decoder.aac")
          .setIsEncoder(true)
          .setIsVendor(true)
          .setCapabilities(
              MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
                  .setMediaFormat(createMediaFormat(MediaFormat.MIMETYPE_AUDIO_AAC))
                  .setIsEncoder(true)
                  .setProfileLevels(
                      new CodecProfileLevel[] {
                        createCodecProfileLevel(CodecProfileLevel.AACObjectELD, 0),
                        createCodecProfileLevel(CodecProfileLevel.AACObjectHE, 0)
                      })
                  .build())
          .build();

  private static final MediaCodecInfo VP9_DECODER_INFO =
      MediaCodecInfoBuilder.newBuilder()
          .setName("shadow.test.decoder.vp9")
          .setIsHardwareAccelerated(true)
          .setCapabilities(
              MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
                  .setMediaFormat(createMediaFormat(MediaFormat.MIMETYPE_VIDEO_VP9))
                  .setIsEncoder(true)
                  .setProfileLevels(
                      new CodecProfileLevel[] {
                        createCodecProfileLevel(
                            CodecProfileLevel.VP9Profile3, CodecProfileLevel.VP9Level52)
                      })
                  .setColorFormats(
                      new int[] {
                        CodecCapabilities.COLOR_FormatYUV420Flexible,
                        CodecCapabilities.COLOR_FormatYUV420Planar
                      })
                  .build())
          .build();

  private static MediaFormat createMediaFormat(String mime) {
    MediaFormat mediaFormat = new MediaFormat();
    mediaFormat.setString(MediaFormat.KEY_MIME, mime);
    return mediaFormat;
  }

  private static CodecProfileLevel createCodecProfileLevel(int profile, int level) {
    CodecProfileLevel profileLevel = new CodecProfileLevel();
    profileLevel.profile = profile;
    profileLevel.level = level;
    return profileLevel;
  }

  @Before
  public void setUp() throws Exception {
    ShadowMediaCodecList.addCodec(AAC_ENCODER_INFO);
    ShadowMediaCodecList.addCodec(VP9_DECODER_INFO);
  }

  @Test
  public void getCodecInfosLength() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    assertThat(mediaCodecList.getCodecInfos()).hasLength(2);
  }

  @Test
  public void aacEncoderInfo() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    assertThat(mediaCodecList.getCodecInfos()[0]).isEqualTo(AAC_ENCODER_INFO);
  }

  @Test
  public void vp9DecoderInfo() {
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    assertThat(mediaCodecList.getCodecInfos()[1]).isEqualTo(VP9_DECODER_INFO);
  }

  @Test
  public void testReset() {
    ShadowMediaCodecList.reset();
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    assertThat(mediaCodecList.getCodecInfos()).hasLength(0);
  }

  @Test
  public void codecCapabilities_createFromProfileLevel() {
    CodecCapabilities codecCapabilities =
        CodecCapabilities.createFromProfileLevel(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            CodecProfileLevel.AVCProfileBaseline,
            CodecProfileLevel.AVCLevel2);
    CodecProfileLevel expected = new CodecProfileLevel();
    expected.profile = CodecProfileLevel.AVCProfileBaseline;
    expected.level = CodecProfileLevel.AVCLevel2;
    assertThat(codecCapabilities.getMimeType()).isEqualTo(MediaFormat.MIMETYPE_VIDEO_AVC);
    assertThat(codecCapabilities.profileLevels).hasLength(1);
    assertThat(codecCapabilities.profileLevels[0]).isEqualTo(expected);
  }
}
