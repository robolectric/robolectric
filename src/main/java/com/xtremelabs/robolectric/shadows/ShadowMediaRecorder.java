package com.xtremelabs.robolectric.shadows;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.Surface;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadows the {@code MediaRecorder} class.
 */
@Implements(MediaRecorder.class)
public class ShadowMediaRecorder {

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
    private MediaRecorder.OnErrorListener errorListener;
    private MediaRecorder.OnInfoListener infoListener;

    public void __constructor__() {
        state = STATE_INITIAL;
    }

    @Implementation
    public void setAudioChannels(int numChannels) {
        audioChannels = numChannels;
    }

    @Implementation
    public void setAudioEncoder(int audio_encoder) {
        audioEncoder = audio_encoder;
        state = STATE_DATA_SOURCE_CONFIGURED;
    }

    @Implementation
    public void setAudioEncodingBitRate(int bitRate) {
        audioBitRate = bitRate;
    }

    @Implementation
    public void setAudioSamplingRate(int samplingRate) {
        audioSamplingRate = samplingRate;
    }

    @Implementation
    public void setAudioSource(int audio_source) {
        audioSource = audio_source;
        state = STATE_INITIALIZED;
    }

    @Implementation
    public void setCamera(Camera c) {
        camera = c;
    }

    @Implementation
    public void setMaxDuration(int max_duration_ms) {
        maxDuration = max_duration_ms;
    }

    @Implementation
    public void setMaxFileSize(long max_filesize_bytes) {
        maxFileSize = max_filesize_bytes;
    }

    @Implementation
    public void setOnErrorListener(MediaRecorder.OnErrorListener l) {
        errorListener = l;
    }

    @Implementation
    public void setOnInfoListener(MediaRecorder.OnInfoListener listener) {
        infoListener = listener;
    }

    @Implementation
    public void setOutputFile(String path) {
        outputPath = path;
        state = STATE_DATA_SOURCE_CONFIGURED;
    }

    @Implementation
    public void setOutputFormat(int output_format) {
        outputFormat = output_format;
        state = STATE_DATA_SOURCE_CONFIGURED;
    }

    @Implementation
    public void setPreviewDisplay(Surface sv) {
        previewDisplay = sv;
        state = STATE_DATA_SOURCE_CONFIGURED;
    }

    @Implementation
    public void setVideoEncoder(int video_encoder) {
        videoEncoder = video_encoder;
        state = STATE_DATA_SOURCE_CONFIGURED;
    }

    @Implementation
    public void setVideoEncodingBitRate(int bitRate) {
        videoBitRate = bitRate;
    }

    @Implementation
    public void setVideoFrameRate(int rate) {
        videoFrameRate = rate;
        state = STATE_DATA_SOURCE_CONFIGURED;
    }

    @Implementation
    public void setVideoSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        state = STATE_DATA_SOURCE_CONFIGURED;
    }

    @Implementation
    public void setVideoSource(int video_source) {
        videoSource = video_source;
        state = STATE_INITIALIZED;
    }

    @Implementation
    public void prepare() {
        state = STATE_PREPARED;
    }

    @Implementation
    public void start() {
        state = STATE_RECORDING;
    }

    @Implementation
    public void stop() {
        state = STATE_INITIAL;
    }

    @Implementation
    public void reset() {
        state = STATE_INITIAL;
    }

    @Implementation
    public void release() {
        state = STATE_RELEASED;
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
