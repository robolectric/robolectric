package org.robolectric.shadows;


import android.media.AudioManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.WithDefaults.class)
public class AudioManagerTest {

  /**the default max volume of the music and dtmf stream*/
  private static final int MAX_VOLUME_MUSIC_DTMF = 15;

  /**the default max volume of the rest of the stream*/
  private static final int DEFAULT_MAX_VOLUME = 7;

  private AudioManager        audioManager;
  private ShadowAudioManager  shadowAudioManager;
  private AudioManager.OnAudioFocusChangeListener listener;

  private int[] allStreams = {
                       AudioManager.STREAM_ALARM,
                       AudioManager.STREAM_NOTIFICATION,
                       AudioManager.STREAM_RING,
                       AudioManager.STREAM_SYSTEM,
                       AudioManager.STREAM_VOICE_CALL,
                       AudioManager.STREAM_MUSIC,
                       AudioManager.STREAM_DTMF
                       };

  @Before
  public void setUp() throws Exception {
    audioManager = Robolectric.newInstanceOf(AudioManager.class);
    shadowAudioManager = Robolectric.shadowOf(audioManager);
    listener = new AudioManager.OnAudioFocusChangeListener() {
      @Override
      public void onAudioFocusChange(int focusChange) {
      }
    };
  }

  @Test
  public void requestAudioFocus_shouldRecordArgumentsOfMostRecentCall() {
    assertNull(shadowAudioManager.getLastAudioFocusRequest());
    audioManager.requestAudioFocus(listener, 999, 888);
    assertSame(listener, shadowAudioManager.getLastAudioFocusRequest().listener);
    assertEquals(999, shadowAudioManager.getLastAudioFocusRequest().streamType);
    assertEquals(888, shadowAudioManager.getLastAudioFocusRequest().durationHint);
  }

  @Test
  public void requestAudioFocus_shouldReturnTheSpecifiedValue() {
    int value = audioManager.requestAudioFocus(listener, 999, 888);
    assertEquals(AudioManager.AUDIOFOCUS_REQUEST_GRANTED, value);

    shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED);

    value = audioManager.requestAudioFocus(listener, 999, 888);
    assertEquals(AudioManager.AUDIOFOCUS_REQUEST_FAILED, value);
  }

  @Test
  public void abandonAudioFocus_shouldRecordTheListenerOfTheMostRecentCall() {
    audioManager.abandonAudioFocus(null);
    assertNull(shadowAudioManager.getLastAbandonedAudioFocusListener());

    audioManager.abandonAudioFocus(listener);
    assertSame(listener, shadowAudioManager.getLastAbandonedAudioFocusListener());
  }

  @Test
  public void shouldGetStreamMaxVolume() throws Exception {

    for (int stream : allStreams) {

      switch(stream) {
        case AudioManager.STREAM_MUSIC: /*fall through*/
        case AudioManager.STREAM_DTMF:
          assertEquals(MAX_VOLUME_MUSIC_DTMF, shadowAudioManager.getStreamMaxVolume(stream));
          break;

        case AudioManager.STREAM_ALARM:         /*fall through*/
        case AudioManager.STREAM_NOTIFICATION:  /*fall through*/
        case AudioManager.STREAM_RING:          /*fall through*/
        case AudioManager.STREAM_SYSTEM:        /*fall through*/
        case AudioManager.STREAM_VOICE_CALL:
          assertEquals(DEFAULT_MAX_VOLUME, shadowAudioManager.getStreamMaxVolume(stream));
          break;

        default:
          throw new Exception("Unexpected audio stream requested.");
      }
    }
  }

  @Test
  public void shouldGetAndSetVolume() {

    int vol = 1;
    for (int stream : allStreams) {
      shadowAudioManager.setStreamVolume(stream, vol, 0);

      vol++;
      if (vol > DEFAULT_MAX_VOLUME) {
        vol = 1;
      }
    }

    vol = 1;
    for (int stream : allStreams) {

      assertEquals(vol, shadowAudioManager.getStreamVolume(stream));

      vol++;
      if (vol > DEFAULT_MAX_VOLUME) {
        vol = 1;
      }

    }
  }

  @Test
  public void shouldSetAllMaxVolume() {

    int newMaxVol = 31;

    shadowAudioManager.setStreamMaxVolume(newMaxVol);
    for (int stream : allStreams) {
        assertEquals(newMaxVol, shadowAudioManager.getStreamMaxVolume(stream));
      }
  }

  @Test
  public void shouldSetAllVolume() {

    int newVol = 3;

    shadowAudioManager.setStreamVolume(newVol);
    for (int stream : allStreams) {
      assertEquals(newVol, shadowAudioManager.getStreamVolume(stream));
    }
  }

  @Test
  public void shouldNotExceedMaxVolume() throws Exception {

    int newVol = 31;

    shadowAudioManager.setStreamVolume(newVol);

    for (int stream : allStreams) {

      switch(stream) {
        case AudioManager.STREAM_MUSIC: /*fall through*/
        case AudioManager.STREAM_DTMF:
          assertEquals(MAX_VOLUME_MUSIC_DTMF, shadowAudioManager.getStreamMaxVolume(stream));
          break;

        case AudioManager.STREAM_ALARM:         /*fall through*/
        case AudioManager.STREAM_NOTIFICATION:  /*fall through*/
        case AudioManager.STREAM_RING:          /*fall through*/
        case AudioManager.STREAM_SYSTEM:        /*fall through*/
        case AudioManager.STREAM_VOICE_CALL:
          assertEquals(DEFAULT_MAX_VOLUME, shadowAudioManager.getStreamMaxVolume(stream));
          break;

        default:
          throw new Exception("Unexpected audio stream requested.");
      }
    }

  }

  @Test
  public void shouldNotBeBelowZeroVolume() {

    int newVol = -3;

    shadowAudioManager.setStreamVolume(newVol);

    for (int stream : allStreams) {
      assertEquals(0, shadowAudioManager.getStreamVolume(stream));
    }
  }


}
