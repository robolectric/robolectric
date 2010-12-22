package com.xtremelabs.robolectric.shadows;


import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.Surface;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class MediaRecorderTest {

    private MediaRecorder mediaRecorder;
    private ShadowMediaRecorder shadowMediaRecorder;

    @Before
    public void setUp() throws Exception {
        mediaRecorder = new MediaRecorder();
        shadowMediaRecorder = Robolectric.shadowOf(mediaRecorder);
    }

    @Test
    public void testAudioChannels() throws Exception {
        assertThat(shadowMediaRecorder.getAudioChannels(), not(equalTo(2)));
        mediaRecorder.setAudioChannels(2);
        assertThat(shadowMediaRecorder.getAudioChannels(), equalTo(2));
    }

    @Test
    public void testAudioEncoder() throws Exception {
        assertThat(shadowMediaRecorder.getAudioEncoder(), not(equalTo(MediaRecorder.AudioEncoder.AMR_NB)));
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED)));
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        assertThat(shadowMediaRecorder.getAudioEncoder(), equalTo(MediaRecorder.AudioEncoder.AMR_NB));
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED));
    }

    @Test
    public void testAudioEncodingBitRate() throws Exception {
        assertThat(shadowMediaRecorder.getAudioEncodingBitRate(), not(equalTo(128000)));
        mediaRecorder.setAudioEncodingBitRate(128000);
        assertThat(shadowMediaRecorder.getAudioEncodingBitRate(), equalTo(128000));
    }

    @Test
    public void testAudioSamplingRate() throws Exception {
        assertThat(shadowMediaRecorder.getAudioSamplingRate(), not(equalTo(22050)));
        mediaRecorder.setAudioSamplingRate(22050);
        assertThat(shadowMediaRecorder.getAudioSamplingRate(), equalTo(22050));
    }

    @Test
    public void testAudioSource() throws Exception {
        assertThat(shadowMediaRecorder.getAudioSource(), not(equalTo(MediaRecorder.AudioSource.CAMCORDER)));
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_INITIALIZED)));
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        assertThat(shadowMediaRecorder.getAudioSource(), equalTo(MediaRecorder.AudioSource.CAMCORDER));
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_INITIALIZED));
    }

    @Test
    public void testCamera() throws Exception {
        assertThat(shadowMediaRecorder.getCamera(), nullValue());
        Camera c = Robolectric.newInstanceOf(Camera.class);
        mediaRecorder.setCamera(c);
        assertThat(shadowMediaRecorder.getCamera(), notNullValue());
        assertThat(shadowMediaRecorder.getCamera(), sameInstance(c));
    }

    @Test
    public void testMaxDuration() throws Exception {
        assertThat(shadowMediaRecorder.getMaxDuration(), not(equalTo(30000)));
        mediaRecorder.setMaxDuration(30000);
        assertThat(shadowMediaRecorder.getMaxDuration(), equalTo(30000));
    }

    @Test
    public void testMaxFileSize() throws Exception {
        assertThat(shadowMediaRecorder.getMaxFileSize(), not(equalTo(512000L)));
        mediaRecorder.setMaxFileSize(512000);
        assertThat(shadowMediaRecorder.getMaxFileSize(), equalTo(512000L));
    }

    @Test
    public void testOnErrorListener() throws Exception {
        assertThat(shadowMediaRecorder.getErrorListener(), nullValue());
        TestErrorListener listener = new TestErrorListener();
        mediaRecorder.setOnErrorListener(listener);
        assertThat(shadowMediaRecorder.getErrorListener(), notNullValue());
        assertThat(shadowMediaRecorder.getErrorListener(), sameInstance((MediaRecorder.OnErrorListener) listener));
    }

    @Test
    public void testOnInfoListener() throws Exception {
        assertThat(shadowMediaRecorder.getInfoListener(), nullValue());
        TestInfoListener listener = new TestInfoListener();
        mediaRecorder.setOnInfoListener(listener);
        assertThat(shadowMediaRecorder.getInfoListener(), notNullValue());
        assertThat(shadowMediaRecorder.getInfoListener(), sameInstance((MediaRecorder.OnInfoListener) listener));
    }

    @Test
    public void testOutputFile() throws Exception {
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED)));
        assertThat(shadowMediaRecorder.getOutputPath(), nullValue());
        mediaRecorder.setOutputFile("/dev/null");
        assertThat(shadowMediaRecorder.getOutputPath(), equalTo("/dev/null"));
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED));
    }

    @Test
    public void testOutputFormat() throws Exception {
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED)));
        assertThat(shadowMediaRecorder.getOutputFormat(), not(equalTo(MediaRecorder.OutputFormat.MPEG_4)));
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        assertThat(shadowMediaRecorder.getOutputFormat(), equalTo(MediaRecorder.OutputFormat.MPEG_4));
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED));
    }

    @Test
    public void testPreviewDisplay() throws Exception {
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED)));
        assertThat(shadowMediaRecorder.getPreviewDisplay(), nullValue());
        Surface surface = Robolectric.newInstanceOf(Surface.class);
        mediaRecorder.setPreviewDisplay(surface);
        assertThat(shadowMediaRecorder.getPreviewDisplay(), notNullValue());
        assertThat(shadowMediaRecorder.getPreviewDisplay(), sameInstance(surface));
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED));
    }

    @Test
    public void testVideoEncoder() throws Exception {
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED)));
        assertThat(shadowMediaRecorder.getVideoEncoder(), not(equalTo(MediaRecorder.VideoEncoder.H264)));
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        assertThat(shadowMediaRecorder.getVideoEncoder(), equalTo(MediaRecorder.VideoEncoder.H264));
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED));
    }

    @Test
    public void testVideoEncodingBitRate() throws Exception {
        assertThat(shadowMediaRecorder.getVideoEncodingBitRate(), not(equalTo(320000)));
        mediaRecorder.setVideoEncodingBitRate(320000);
        assertThat(shadowMediaRecorder.getVideoEncodingBitRate(), equalTo(320000));
    }

    @Test
    public void testVideoFrameRate() throws Exception {
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED)));
        assertThat(shadowMediaRecorder.getVideoFrameRate(), not(equalTo(30)));
        mediaRecorder.setVideoFrameRate(30);
        assertThat(shadowMediaRecorder.getVideoFrameRate(), equalTo(30));
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED));
    }

    @Test
    public void testVideoSize() throws Exception {
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED)));
        assertThat(shadowMediaRecorder.getVideoWidth(), not(equalTo(640)));
        assertThat(shadowMediaRecorder.getVideoHeight(), not(equalTo(480)));
        mediaRecorder.setVideoSize(640, 480);
        assertThat(shadowMediaRecorder.getVideoWidth(), equalTo(640));
        assertThat(shadowMediaRecorder.getVideoHeight(), equalTo(480));
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_DATA_SOURCE_CONFIGURED));
    }

    @Test
    public void testVideoSource() throws Exception {
        assertThat(shadowMediaRecorder.getVideoSource(), not(equalTo(MediaRecorder.VideoSource.CAMERA)));
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_INITIALIZED)));
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        assertThat(shadowMediaRecorder.getVideoSource(), equalTo(MediaRecorder.VideoSource.CAMERA));
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_INITIALIZED));
    }

    @Test
    public void testPrepare() throws Exception {
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_PREPARED)));
        mediaRecorder.prepare();
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_PREPARED));
    }

    @Test
    public void testStart() throws Exception {
        mediaRecorder.prepare();
        assertThat(shadowMediaRecorder.getState(), not(equalTo(ShadowMediaRecorder.STATE_RECORDING)));
        mediaRecorder.start();
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_RECORDING));
    }

    @Test
    public void testStop() throws Exception {
        mediaRecorder.start();
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_RECORDING));
        mediaRecorder.stop();
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_INITIAL));
    }

    @Test
    public void testReset() throws Exception {
        mediaRecorder.start();
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_RECORDING));
        mediaRecorder.reset();
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_INITIAL));
    }

    @Test
    public void testRelease() throws Exception {
        mediaRecorder.start();
        mediaRecorder.reset();
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_INITIAL));
        mediaRecorder.release();
        assertThat(shadowMediaRecorder.getState(), equalTo(ShadowMediaRecorder.STATE_RELEASED));
    }

    private class TestErrorListener implements MediaRecorder.OnErrorListener {
        @Override
        public void onError(MediaRecorder arg0, int arg1, int arg2) {
        }
    }

    private class TestInfoListener implements MediaRecorder.OnInfoListener {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
        }
    }

}
