package org.robolectric.shadows;

import android.media.MediaPlayer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class MediaPlayerTest {

  private MediaPlayer mediaPlayer;
  private ShadowMediaPlayer shadowMediaPlayer;

  @Before
  public void setUp() throws Exception {
    mediaPlayer = Robolectric.newInstanceOf(MediaPlayer.class);
    shadowMediaPlayer = Robolectric.shadowOf(mediaPlayer);
  }

  @Test
  public void testCurrentPosition() {
    int[] positions = {0, 1, 2, 1024};

    for (int position : positions) {
      shadowMediaPlayer.setCurrentPosition(position);
      assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(position);
    }
  }

  @Test
  public void testErrorListenerCalled() {
    MediaPlayer.OnErrorListener err = Mockito.mock(MediaPlayer.OnErrorListener.class);
    shadowMediaPlayer.setOnErrorListener(err);
    shadowMediaPlayer.invokeErrorListener(0,0);
    Mockito.verify(err).onError(mediaPlayer,0,0);
  }

  @Test
  public void testErrorListenerCalledNoOnCompleteCalledWhenReturnTrue() {
    MediaPlayer.OnErrorListener err = Mockito.mock(MediaPlayer.OnErrorListener.class);
    MediaPlayer.OnCompletionListener complete =  Mockito.mock(MediaPlayer.OnCompletionListener.class);
    Mockito.when(err.onError(mediaPlayer,0,0)).thenReturn(true);
    shadowMediaPlayer.setOnErrorListener(err);
    shadowMediaPlayer.setOnCompletionListener(complete);

    shadowMediaPlayer.invokeErrorListener(0, 0);

    Mockito.verify(err).onError(mediaPlayer,0,0);
    Mockito.verifyZeroInteractions(complete);
  }

  @Test
  public void testErrorListenerCalledOnCompleteCalledWhenReturnFalse() {
    MediaPlayer.OnErrorListener err = Mockito.mock(MediaPlayer.OnErrorListener.class);
    MediaPlayer.OnCompletionListener complete =  Mockito.mock(MediaPlayer.OnCompletionListener.class);
    Mockito.when(err.onError(mediaPlayer,0,0)).thenReturn(false);
    shadowMediaPlayer.setOnErrorListener(err);
    shadowMediaPlayer.setOnCompletionListener(complete);

    shadowMediaPlayer.invokeErrorListener(0, 0);

    Mockito.verify(err).onError(mediaPlayer,0,0);
    Mockito.verify(complete).onCompletion(mediaPlayer);
  }
}
