package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat.Token;
import android.support.v4.media.session.PlaybackStateCompat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/** Tests for {@link org.robolectric.shadows.ShadowMediaControllerCompat}. */
@RunWith(RobolectricTestRunner.class)
public final class ShadowMediaControllerCompatTest {

  private Context context = RuntimeEnvironment.application;
  private MediaControllerCompat mediaControllerCompat;

  @Before
  public void setUp() {
    Token token = Token.CREATOR.createFromParcel(Parcel.obtain());
    mediaControllerCompat = new MediaControllerCompat(context, token);
  }

  @Test
  public void getPlaybackState() {
    PlaybackStateCompat playbackState = mediaControllerCompat.getPlaybackState();

    assertThat(playbackState.getPlaybackState()).isEqualTo(PlaybackStateCompat.STATE_NONE);
  }

  @Test
  public void getMetadata() {
    MediaMetadataCompat metadata = mediaControllerCompat.getMetadata();

    assertThat(metadata.size()).isEqualTo(0);
  }
}
