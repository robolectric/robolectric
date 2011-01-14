package com.xtremelabs.robolectric.shadows;


import android.media.AudioManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class AudioManagerTest {
    private AudioManager audioManager;
    private ShadowAudioManager shadowAudioManager;

    @Before
    public void setUp() throws Exception {
        audioManager = Robolectric.newInstanceOf(AudioManager.class);
        shadowAudioManager = Robolectric.shadowOf(audioManager);
    }

    @After
    public void tearDown() throws Exception {
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
