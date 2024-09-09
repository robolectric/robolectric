package android.media;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Compatibility test for {@link android.media.MediaPlayer} */
@RunWith(AndroidJUnit4.class)
public class MediaPlayerTest {

  /**
   * Checks that a MediaPlayer in the IDLE state does not throw an exception when calling {@link
   * MediaPlayer#getCurrentPosition()}.
   */
  @Test
  public void newMediaPlayer_getCurrentPosition_doesNotThrow() {
    MediaPlayer mediaPlayer = new MediaPlayer();
    int currentPosition = mediaPlayer.getCurrentPosition();
    // currentPosition appears to be inconsistent garbage when run on emulators for SDKs < 30
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      assertThat(currentPosition).isEqualTo(0);
    }
  }
}
