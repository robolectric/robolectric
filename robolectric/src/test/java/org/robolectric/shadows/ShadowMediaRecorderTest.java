package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build.VERSION_CODES;
import android.view.Surface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
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
  public void testAudioChannels() {
    assertThat(shadowMediaRecorder.getAudioChannels()).isNotEqualTo(2);
    mediaRecorder.setAudioChannels(2);
    assertThat(shadowMediaRecorder.getAudioChannels()).isEqualTo(2);
  }

  @Test
  public void testAudioEncoder() {
    assertThat(shadowMediaRecorder.getAudioEncoder()).isNotEqualTo(MediaRecorder.AudioEncoder.AMR_NB);
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    assertThat(shadowMediaRecorder.getAudioEncoder()).isEqualTo(MediaRecorder.AudioEncoder.AMR_NB);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testAudioEncodingBitRate() {
    assertThat(shadowMediaRecorder.getAudioEncodingBitRate()).isNotEqualTo(128000);
    mediaRecorder.setAudioEncodingBitRate(128000);
    assertThat(shadowMediaRecorder.getAudioEncodingBitRate()).isEqualTo(128000);
  }

  @Test
  public void testAudioSamplingRate() {
    assertThat(shadowMediaRecorder.getAudioSamplingRate()).isNotEqualTo(22050);
    mediaRecorder.setAudioSamplingRate(22050);
    assertThat(shadowMediaRecorder.getAudioSamplingRate()).isEqualTo(22050);
  }

  @Test
  public void testAudioSource() {
    assertThat(shadowMediaRecorder.getAudioSource()).isNotEqualTo(MediaRecorder.AudioSource.CAMCORDER);
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_INITIALIZED);
    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
    assertThat(shadowMediaRecorder.getAudioSource()).isEqualTo(MediaRecorder.AudioSource.CAMCORDER);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_INITIALIZED);
  }

  @Test
  public void testCamera() {
    assertThat(shadowMediaRecorder.getCamera()).isNull();
    Camera c = Shadow.newInstanceOf(Camera.class);
    mediaRecorder.setCamera(c);
    assertThat(shadowMediaRecorder.getCamera()).isNotNull();
    assertThat(shadowMediaRecorder.getCamera()).isSameInstanceAs(c);
  }

  @Test
  public void testMaxDuration() {
    assertThat(shadowMediaRecorder.getMaxDuration()).isNotEqualTo(30000);
    mediaRecorder.setMaxDuration(30000);
    assertThat(shadowMediaRecorder.getMaxDuration()).isEqualTo(30000);
  }

  @Test
  public void testMaxFileSize() {
    assertThat(shadowMediaRecorder.getMaxFileSize()).isNotEqualTo(512000L);
    mediaRecorder.setMaxFileSize(512000);
    assertThat(shadowMediaRecorder.getMaxFileSize()).isEqualTo(512000L);
  }

  @Test
  public void testOnErrorListener() {
    assertThat(shadowMediaRecorder.getErrorListener()).isNull();
    TestErrorListener listener = new TestErrorListener();
    mediaRecorder.setOnErrorListener(listener);
    assertThat(shadowMediaRecorder.getErrorListener()).isNotNull();
    assertThat(shadowMediaRecorder.getErrorListener()).isSameInstanceAs(listener);
  }

  @Test
  public void testOnInfoListener() {
    assertThat(shadowMediaRecorder.getInfoListener()).isNull();
    TestInfoListener listener = new TestInfoListener();
    mediaRecorder.setOnInfoListener(listener);
    assertThat(shadowMediaRecorder.getInfoListener()).isNotNull();
    assertThat(shadowMediaRecorder.getInfoListener()).isSameInstanceAs(listener);
  }

  @Test
  public void testOutputFile() {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getOutputPath()).isNull();
    mediaRecorder.setOutputFile("/dev/null");
    assertThat(shadowMediaRecorder.getOutputPath()).isEqualTo("/dev/null");
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testOutputFormat() {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getOutputFormat()).isNotEqualTo(MediaRecorder.OutputFormat.MPEG_4);
    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    assertThat(shadowMediaRecorder.getOutputFormat()).isEqualTo(MediaRecorder.OutputFormat.MPEG_4);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testPreviewDisplay() {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getPreviewDisplay()).isNull();
    Surface surface = Shadow.newInstanceOf(Surface.class);
    mediaRecorder.setPreviewDisplay(surface);
    assertThat(shadowMediaRecorder.getPreviewDisplay()).isNotNull();
    assertThat(shadowMediaRecorder.getPreviewDisplay()).isSameInstanceAs(surface);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testVideoEncoder() {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getVideoEncoder()).isNotEqualTo(MediaRecorder.VideoEncoder.H264);
    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    assertThat(shadowMediaRecorder.getVideoEncoder()).isEqualTo(MediaRecorder.VideoEncoder.H264);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testVideoEncodingBitRate() {
    assertThat(shadowMediaRecorder.getVideoEncodingBitRate()).isNotEqualTo(320000);
    mediaRecorder.setVideoEncodingBitRate(320000);
    assertThat(shadowMediaRecorder.getVideoEncodingBitRate()).isEqualTo(320000);
  }

  @Test
  public void testVideoFrameRate() {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getVideoFrameRate()).isNotEqualTo(30);
    mediaRecorder.setVideoFrameRate(30);
    assertThat(shadowMediaRecorder.getVideoFrameRate()).isEqualTo(30);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testVideoSize() {
    assertThat(shadowMediaRecorder.getState()).isNotEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
    assertThat(shadowMediaRecorder.getVideoWidth()).isNotEqualTo(640);
    assertThat(shadowMediaRecorder.getVideoHeight()).isNotEqualTo(480);
    mediaRecorder.setVideoSize(640, 480);
    assertThat(shadowMediaRecorder.getVideoWidth()).isEqualTo(640);
    assertThat(shadowMediaRecorder.getVideoHeight()).isEqualTo(480);
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED);
  }

  @Test
  public void testVideoSource() {
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
  public void testStop() {
    mediaRecorder.start();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_RECORDING);
    mediaRecorder.stop();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_INITIAL);
  }

  @Test
  public void testReset() {
    mediaRecorder.start();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_RECORDING);
    mediaRecorder.reset();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_INITIAL);
  }

  @Test
  public void testRelease() {
    mediaRecorder.start();
    mediaRecorder.reset();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_INITIAL);
    mediaRecorder.release();
    assertThat(shadowMediaRecorder.getState()).isEqualTo(ShadowMediaRecorder.STATE_RELEASED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void testGetSurface() throws Exception {
    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mediaRecorder.prepare();
    assertThat(mediaRecorder.getSurface()).isNotNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void testGetSurface_beforePrepare() {
    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    assertThrows(IllegalStateException.class, () -> mediaRecorder.getSurface());
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void testGetSurface_afterStop() throws Exception {
    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mediaRecorder.prepare();
    mediaRecorder.start();
    mediaRecorder.stop();
    assertThrows(IllegalStateException.class, () -> mediaRecorder.getSurface());
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void testGetSurface_wrongVideoSource() throws Exception {
    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    mediaRecorder.prepare();
    assertThrows(IllegalStateException.class, () -> mediaRecorder.getSurface());
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
