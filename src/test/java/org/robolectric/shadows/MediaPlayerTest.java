package org.robolectric.shadows;

import android.media.MediaPlayer;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
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
}
