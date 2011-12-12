package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.VideoView;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class VideoViewTest {
	
    private VideoView view;

    @Before public void setUp() throws Exception {
        view = new VideoView(null);
    }
    
    @Test
    public void shouldSetOnPreparedListener() throws Exception {
    	TestPreparedListener l = new TestPreparedListener();
    	view.setOnPreparedListener(l);
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat((TestPreparedListener)(shadowVideoView.getOnPreparedListener()), sameInstance(l));
    }
    
    @Test
    public void shouldSetOnErrorListener() throws Exception {
    	TestErrorListener l = new TestErrorListener();
    	view.setOnErrorListener(l);
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat((TestErrorListener)(shadowVideoView.getOnErrorListener()), sameInstance(l));
    }
    
    @Test
    public void shouldSetOnCompletionListener() throws Exception {
    	TestCompletionListener l = new TestCompletionListener();
    	view.setOnCompletionListener(l);
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat((TestCompletionListener)(shadowVideoView.getOnCompletionListener()), sameInstance(l));
    }
    
    @Test
    public void shouldSetVideoPath() throws Exception {
    	view.setVideoPath("video.mp4");
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat(shadowVideoView.getVideoPath(), equalTo("video.mp4"));
    	view.setVideoPath(null);
    	assertThat(shadowVideoView.getVideoPath(), nullValue());
    }
    
    @Test
    public void shouldSetVideoURI() throws Exception {
    	view.setVideoURI(Uri.parse("video.mp4"));
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat(shadowVideoView.getVideoURIString(), equalTo("video.mp4"));
    	view.setVideoURI(null);
    	assertThat(shadowVideoView.getVideoURIString(), nullValue());
    }
    
    @Test
    public void shoulDetermineIsPlaying() throws Exception {
    	assertThat(view.isPlaying(), equalTo(false));
    	view.start();
    	assertThat(view.isPlaying(), equalTo(true));
    	view.stopPlayback();
    	assertThat(view.isPlaying(), equalTo(false));
    }
    
    @Test
    public void shouldStartPlaying() throws Exception {
    	view.start();
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat(shadowVideoView.getCurrentVideoState(), equalTo(ShadowVideoView.START));
    }
    
    @Test
    public void shouldStopPlayback() throws Exception {
    	view.stopPlayback();
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat(shadowVideoView.getCurrentVideoState(), equalTo(ShadowVideoView.STOP));
    }
    
    @Test
    public void shouldSuspendPlaying() throws Exception {
    	view.start();
    	view.suspend();
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat(shadowVideoView.getPrevVideoState(), equalTo(ShadowVideoView.START));
    	assertThat(shadowVideoView.getCurrentVideoState(), equalTo(ShadowVideoView.SUSPEND));
    }
    
    @Test
    public void shouldResumePlaying() throws Exception {
    	view.start();
    	view.suspend();
    	view.resume();
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat(shadowVideoView.getPrevVideoState(), equalTo(ShadowVideoView.SUSPEND));
    	assertThat(shadowVideoView.getCurrentVideoState(), equalTo(ShadowVideoView.RESUME));
    }
    
    
    @Test
    public void shouldPausePlaying() throws Exception {
    	view.start();
    	view.pause();
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat(shadowVideoView.getPrevVideoState(), equalTo(ShadowVideoView.START));
    	assertThat(shadowVideoView.getCurrentVideoState(), equalTo(ShadowVideoView.PAUSE));
    }
    
    @Test
    public void shouldDetermineIfPausable() throws Exception {
    	view.start();
    	assertThat(view.canPause(), equalTo(true));

    	view.pause();
    	assertThat(view.canPause(), equalTo(false));
    	
    	view.resume();
    	assertThat(view.canPause(), equalTo(true));
    	
    	view.suspend();
    	assertThat(view.canPause(), equalTo(false));
    }
         
    /**
     * Helper classes
     */
    
	private class TestPreparedListener implements MediaPlayer.OnPreparedListener {		
		@Override
		public void onPrepared(MediaPlayer mp) {}
	}
	
	private class TestErrorListener implements MediaPlayer.OnErrorListener  {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {			
			return false;
		}
	}

	private class TestCompletionListener implements MediaPlayer.OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer mp) {}
	}
}
