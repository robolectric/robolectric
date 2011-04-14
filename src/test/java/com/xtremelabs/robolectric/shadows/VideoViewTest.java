package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
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
    	PreparedListenerTest l = new PreparedListenerTest();
    	view.setOnPreparedListener(l);
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat((PreparedListenerTest)(shadowVideoView.getOnPreparedListener()), sameInstance(l));
    }
    
    @Test
    public void shouldSetOnErrorListener() throws Exception {
    	ErrorListenerTest l = new ErrorListenerTest();
    	view.setOnErrorListener(l);
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat((ErrorListenerTest)(shadowVideoView.getOnErrorListener()), sameInstance(l));
    }
    
    @Test
    public void shouldSetOnCompletionListener() throws Exception {
    	CompletionListenerTest l = new CompletionListenerTest();
    	view.setOnCompletionListener(l);
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat((CompletionListenerTest)(shadowVideoView.getOnCompletionListener()), sameInstance(l));
    }
    
    @Test
    public void shouldSetVideoURI() throws Exception {
    	view.setVideoURI(Uri.parse("video.mp4"));
    	ShadowVideoView shadowVideoView = Robolectric.shadowOf(view);
    	assertThat(shadowVideoView.getVideoURIString(), equalTo("video.mp4"));
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
        
        
    /**
     * Helper class
     * @author zoodles
     */
    
	private class PreparedListenerTest implements MediaPlayer.OnPreparedListener {		
		@Override
		public void onPrepared(MediaPlayer mp) {}
	}
	
	private class ErrorListenerTest implements MediaPlayer.OnErrorListener  {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {			
			return false;
		}
	}

	private class CompletionListenerTest implements MediaPlayer.OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer mp) {}
	}
    
}
