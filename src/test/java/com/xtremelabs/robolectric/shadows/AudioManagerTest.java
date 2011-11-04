package com.xtremelabs.robolectric.shadows;


import android.media.AudioManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class AudioManagerTest {
    private AudioManager audioManager;
    private ShadowAudioManager shadowAudioManager;
    private AudioManager.OnAudioFocusChangeListener listener;

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
        shadowAudioManager.setStreamMaxVolume(45);

        assertEquals(45, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
    }

    @Test
    public void shouldGetVolume() throws Exception {
        shadowAudioManager.setStreamVolume(5);

        assertEquals(5, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    @Test
    public void shouldSetVolume() throws Exception {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 8, 0);

        assertEquals(8, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        assertEquals(8, shadowAudioManager.getStreamVolume());
    }

}
