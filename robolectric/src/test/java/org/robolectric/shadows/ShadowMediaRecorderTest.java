package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.Surface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowMediaRecorderTest {

  private MediaRecorder mediaRecorder;
  private ShadowMediaRecorder shadowMediaRecorder;

  @Before
  public void setUp() throws Exception {
    mediaRecorder = new MediaRecorder();
    shadowMediaRecorder = Shadows.shadowOf(mediaRecorder);
  }

  @Test
  public void testAudioChannels() throws Exception {
    assertThat(shadowMediaRecorder.getAudioChannels()).isNotEqualTo(2);
    mediaRecorder.setAudioChannels(2);
    assertThat(shadowMediaRecorder.getAudioChannels()).isEqualTo(2);
  }

  @Test
  public void testAudioEncoder() throws Exception {
    assertThat(shadowMediaRecorder.getAudioEncoder()).isNotEqualTo(MediaRecorder.AudioEncoder.AMR_NB);
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    assertThat(shadowMediaRecorder.getAudioEncoder()).isEqualTo(MediaRecorder.AudioEncoder.AMR_NB);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testAudioEncodingBitRate() throws Exception {
    assertThat(shadowMediaRecorder.getAudioEncodingBitRate()).isNotEqualTo(128000);
    mediaRecorder.setAudioEncodingBitRate(128000);
    assertThat(shadowMediaRecorder.getAudioEncodingBitRate()).isEqualTo(128000);
  }

  @Test
  public void testAudioSamplingRate() throws Exception {
    assertThat(shadowMediaRecorder.getAudioSamplingRate()).isNotEqualTo(22050);
    mediaRecorder.setAudioSamplingRate(22050);
    assertThat(shadowMediaRecorder.getAudioSamplingRate()).isEqualTo(22050);
  }

  @Test
  public void testAudioSource() throws Exception {
    assertThat(shadowMediaRecorder.getAudioSource()).isNotEqualTo(MediaRecorder.AudioSource.CAMCORDER);
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_INITIALIZED);
    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
    assertThat(shadowMediaRecorder.getAudioSource()).isEqualTo(MediaRecorder.AudioSource.CAMCORDER);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_INITIALIZED);
  }

  @Test
  public void testCamera() throws Exception {
    assertThat(shadowMediaRecorder.getCamera()).isNull();
    Camera c = Shadow.newInstanceOf(Camera.class);
    mediaRecorder.setCamera(c);
    assertThat(shadowMediaRecorder.getCamera()).isNotNull();
    assertThat(shadowMediaRecorder.getCamera()).isSameAs(c);
  }

  @Test
  public void testMaxDuration() throws Exception {
    assertThat(shadowMediaRecorder.getMaxDuration()).isNotEqualTo(30000);
    mediaRecorder.setMaxDuration(30000);
    assertThat(shadowMediaRecorder.getMaxDuration()).isEqualTo(30000);
  }

  @Test
  public void testMaxFileSize() throws Exception {
    assertThat(shadowMediaRecorder.getMaxFileSize()).isNotEqualTo(512000L);
    mediaRecorder.setMaxFileSize(512000);
    assertThat(shadowMediaRecorder.getMaxFileSize()).isEqualTo(512000L);
  }

  @Test
  public void testOnErrorListener() throws Exception {
    assertThat(shadowMediaRecorder.getErrorListener()).isNull();
    TestErrorListener listener = new TestErrorListener();
    mediaRecorder.setOnErrorListener(listener);
    assertThat(shadowMediaRecorder.getErrorListener()).isNotNull();
    assertThat(shadowMediaRecorder.getErrorListener()).isSameAs((MediaRecorder.OnErrorListener) listener);
  }

  @Test
  public void testOnInfoListener() throws Exception {
    assertThat(shadowMediaRecorder.getInfoListener()).isNull();
    TestInfoListener listener = new TestInfoListener();
    mediaRecorder.setOnInfoListener(listener);
    assertThat(shadowMediaRecorder.getInfoListener()).isNotNull();
    assertThat(shadowMediaRecorder.getInfoListener()).isSameAs((MediaRecorder.OnInfoListener) listener);
  }

  @Test
  public void testOutputFile() throws Exception {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getOutputPath()).isNull();
    mediaRecorder.setOutputFile("/dev/null");
    assertThat(shadowMediaRecorder.getOutputPath()).isEqualTo("/dev/null");
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testOutputFormat() throws Exception {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getOutputFormat()).isNotEqualTo(MediaRecorder.OutputFormat.MPEG_4);
    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    assertThat(shadowMediaRecorder.getOutputFormat()).isEqualTo(MediaRecorder.OutputFormat.MPEG_4);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testPreviewDisplay() throws Exception {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getPreviewDisplay()).isNull();
    Surface surface = Shadow.newInstanceOf(Surface.class);
    mediaRecorder.setPreviewDisplay(surface);
    assertThat(shadowMediaRecorder.getPreviewDisplay()).isNotNull();
    assertThat(shadowMediaRecorder.getPreviewDisplay()).isSameAs(surface);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testVideoEncoder() throws Exception {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getVideoEncoder()).isNotEqualTo(MediaRecorder.VideoEncoder.H264);
    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    assertThat(shadowMediaRecorder.getVideoEncoder()).isEqualTo(MediaRecorder.VideoEncoder.H264);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testVideoEncodingBitRate() throws Exception {
    assertThat(shadowMediaRecorder.getVideoEncodingBitRate()).isNotEqualTo(320000);
    mediaRecorder.setVideoEncodingBitRate(320000);
    assertThat(shadowMediaRecorder.getVideoEncodingBitRate()).isEqualTo(320000);
  }

  @Test
  public void testVideoFrameRate() throws Exception {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getVideoFrameRate()).isNotEqualTo(30);
    mediaRecorder.setVideoFrameRate(30);
    assertThat(shadowMediaRecorder.getVideoFrameRate()).isEqualTo(30);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testVideoSize() throws Exception {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getVideoWidth()).isNotEqualTo(640);
    assertThat(shadowMediaRecorder.getVideoHeight()).isNotEqualTo(480);
    mediaRecorder.setVideoSize(640, 480);
    assertThat(shadowMediaRecorder.getVideoWidth()).isEqualTo(640);
    assertThat(shadowMediaRecorder.getVideoHeight()).isEqualTo(480);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testVideoSource() throws Exception {
    assertThat(shadowMediaRecorder.getVideoSource()).isNotEqualTo(MediaRecorder.VideoSource.CAMERA);
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_INITIALIZED);
    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    assertThat(shadowMediaRecorder.getVideoSource()).isEqualTo(MediaRecorder.VideoSource.CAMERA);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_INITIALIZED);
  }

  @Test
  public void testPrepare() throws Exception {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_PREPARED);
    mediaRecorder.prepare();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_PREPARED);
  }

  @Test
  public void testStart() throws Exception {
    mediaRecorder.prepare();
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_RECORDING);
    mediaRecorder.start();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_RECORDING);
  }

  @Test
  public void testStop() throws Exception {
    mediaRecorder.start();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_RECORDING);
    mediaRecorder.stop();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_INITIAL);
  }

  @Test
  public void testReset() throws Exception {
    mediaRecorder.start();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_RECORDING);
    mediaRecorder.reset();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_INITIAL);
  }

  @Test
  public void testRelease() throws Exception {
    mediaRecorder.start();
    mediaRecorder.reset();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_INITIAL);
    mediaRecorder.release();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_RELEASED);
  }

  private static class TestErrorListener implements MediaRecorder.OnErrorListener {
    @Override
    public void onError(MediaRecorder arg0, int arg1, int arg2) {
    }
  }

  private static class TestInfoListener implements MediaRecorder.OnInfoListener {
    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
    }
  }

}
