package org.robolectric.shadows.support.v4;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Very basic implementation that only supports {@link MediaControllerCompat.getPlaybackState},
 * and {@link MediaMetadataCompat.getMetadata} by returning empty {@link PlaybackStateCompat} and
 * {@link MediaMetadataCompat} respectively.
 */
@Implements(MediaControllerCompat.class)
public class ShadowMediaControllerCompat {

  /** Returns an empty {@link PlaybackStateCompat}. */
  @Implementation
  protected PlaybackStateCompat getPlaybackState() {
    return new PlaybackStateCompat.Builder().build();
  }

  /** Returns an empty {@link MediaMetadataCompat}. */
  @Implementation
  protected MediaMetadataCompat getMetadata() {
    return new MediaMetadataCompat.Builder().build();
  }
}
