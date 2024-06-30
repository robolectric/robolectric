package org.robolectric.shadows;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.Surface;
import com.google.common.base.Preconditions;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(MediaRecorder.class)
public class ShadowMediaRecorder {
  @SuppressWarnings("UnusedDeclaration")
  @Implementation
  protected static void __staticInitializer__() {
    // don't bind the JNI library
  }

  // Recording machine state, as per:
  // http://developer.android.com/reference/android/media/MediaRecorder.html
  public static final int STATE_ERROR = -1;
  public static final int STATE_INITIAL = 1;
  public static final int STATE_INITIALIZED = 2;
  public static final int STATE_DATA_SOURCE_CONFIGURED = 3;
  public static final int STATE_PREPARED = 4;
  public static final int STATE_RECORDING = 5;
  public static final int STATE_RELEASED = 6;

  private int state;

  private Camera camera;
  private int audioChannels;
  private int audioEncoder;
  private int audioBitRate;
  private int audioSamplingRate;
  private int audioSource;
  private int maxDuration;
  private long maxFileSize;
  private String outputPath;
  private int outputFormat;
  private int videoEncoder;
  private int videoBitRate;
  private int videoFrameRate;
  private int videoWidth;
  private int videoHeight;
  private int videoSource;

  private Surface previewDisplay;
  private Surface recordingSurface;
  private SurfaceTexture recordingSurfaceTexture;
  private MediaRecorder.OnErrorListener errorListener;
  private MediaRecorder.OnInfoListener infoListener;

  @Implementation
  protected void __constructor__() {
    state = STATE_INITIAL;
  }

  @Implementation
  protected void setAudioChannels(int numChannels) {
    audioChannels = numChannels;
  }

  @Implementation
  protected void setAudioEncoder(int audio_encoder) {
    audioEncoder = audio_encoder;
    state = STATE_DATA_SOURCE_CONFIGURED;
  }

  @Implementation
  protected void setAudioEncodingBitRate(int bitRate) {
    audioBitRate = bitRate;
  }

  @Implementation
  protected void setAudioSamplingRate(int samplingRate) {
    audioSamplingRate = samplingRate;
  }

  @Implementation
  protected void setAudioSource(int audio_source) {
    audioSource = audio_source;
    state = STATE_INITIALIZED;
  }

  @Implementation
  protected void setCamera(Camera c) {
    camera = c;
  }

  @Implementation
  protected void setMaxDuration(int max_duration_ms) {
    maxDuration = max_duration_ms;
  }

  @Implementation
  protected void setMaxFileSize(long max_filesize_bytes) {
    maxFileSize = max_filesize_bytes;
  }

  @Implementation
  protected void setOnErrorListener(MediaRecorder.OnErrorListener l) {
    errorListener = l;
  }

  @Implementation
  protected void setOnInfoListener(MediaRecorder.OnInfoListener listener) {
    infoListener = listener;
  }

  @Implementation
  protected void setOutputFile(String path) {
    outputPath = path;
    state = STATE_DATA_SOURCE_CONFIGURED;
  }

  @Implementation
  protected void setOutputFormat(int output_format) {
    outputFormat = output_format;
    state = STATE_DATA_SOURCE_CONFIGURED;
  }

  @Implementation
  protected void setPreviewDisplay(Surface sv) {
    previewDisplay = sv;
    state = STATE_DATA_SOURCE_CONFIGURED;
  }

  @Implementation
  protected void setVideoEncoder(int video_encoder) {
    videoEncoder = video_encoder;
    state = STATE_DATA_SOURCE_CONFIGURED;
  }

  @Implementation
  protected void setVideoEncodingBitRate(int bitRate) {
    videoBitRate = bitRate;
  }

  @Implementation
  protected void setVideoFrameRate(int rate) {
    videoFrameRate = rate;
    state = STATE_DATA_SOURCE_CONFIGURED;
  }

  @Implementation
  protected void setVideoSize(int width, int height) {
    videoWidth = width;
    videoHeight = height;
    state = STATE_DATA_SOURCE_CONFIGURED;
  }

  @Implementation
  protected void setVideoSource(int video_source) {
    videoSource = video_source;
    state = STATE_INITIALIZED;
  }

  @Implementation
  protected void prepare() {
    state = STATE_PREPARED;
  }

  @Implementation
  protected void start() {
    state = STATE_RECORDING;
  }

  @Implementation
  protected void stop() {
    state = STATE_INITIAL;
  }

  @Implementation
  protected void reset() {
    state = STATE_INITIAL;
  }

  @Implementation
  protected void release() {
    state = STATE_RELEASED;
    if (recordingSurface != null) {
      recordingSurface.release();
      recordingSurface = null;
    }
    if (recordingSurfaceTexture != null) {
      recordingSurfaceTexture.release();
      recordingSurfaceTexture = null;
    }
  }

  @Implementation
  protected Surface getSurface() {
    Preconditions.checkState(
        getVideoSource() == MediaRecorder.VideoSource.SURFACE,
        "getSurface can only be called when setVideoSource is set to SURFACE");
    // There is a diagram of the MediaRecorder state machine here:
    // https://developer.android.com/reference/android/media/MediaRecorder
    Preconditions.checkState(
        state == STATE_PREPARED || state == STATE_RECORDING,
        "getSurface must be called after prepare() and before stop()");

    if (recordingSurface == null) {
      recordingSurfaceTexture = new SurfaceTexture(/* texName= */ 0);
      recordingSurface = new Surface(recordingSurfaceTexture);
    }

    return recordingSurface;
  }

  public Camera getCamera() {
    return camera;
  }

  public int getAudioChannels() {
    return audioChannels;
  }

  public int getAudioEncoder() {
    return audioEncoder;
  }

  public int getAudioEncodingBitRate() {
    return audioBitRate;
  }

  public int getAudioSamplingRate() {
    return audioSamplingRate;
  }

  public int getAudioSource() {
    return audioSource;
  }

  public int getMaxDuration() {
    return maxDuration;
  }

  public long getMaxFileSize() {
    return maxFileSize;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public int getOutputFormat() {
    return outputFormat;
  }

  public int getVideoEncoder() {
    return videoEncoder;
  }

  public int getVideoEncodingBitRate() {
    return videoBitRate;
  }

  public int getVideoFrameRate() {
    return videoFrameRate;
  }

  public int getVideoWidth() {
    return videoWidth;
  }

  public int getVideoHeight() {
    return videoHeight;
  }

  public int getVideoSource() {
    return videoSource;
  }

  public Surface getPreviewDisplay() {
    return previewDisplay;
  }

  public MediaRecorder.OnErrorListener getErrorListener() {
    return errorListener;
  }

  public MediaRecorder.OnInfoListener getInfoListener() {
    return infoListener;
  }

  public int getState() {
    return state;
  }
}
