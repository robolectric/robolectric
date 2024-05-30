package org.robolectric.shadows;

import static android.media.session.PlaybackState.ACTION_PAUSE;
import static android.media.session.PlaybackState.ACTION_PLAY;
import static android.media.session.PlaybackState.ACTION_PLAY_FROM_SEARCH;
import static android.media.session.PlaybackState.ACTION_PLAY_FROM_URI;
import static android.media.session.PlaybackState.ACTION_PREPARE_FROM_SEARCH;
import static android.media.session.PlaybackState.ACTION_PREPARE_FROM_URI;
import static android.media.session.PlaybackState.ACTION_SEEK_TO;
import static android.media.session.PlaybackState.ACTION_SET_RATING;
import static android.media.session.PlaybackState.ACTION_SKIP_TO_NEXT;
import static android.media.session.PlaybackState.ACTION_SKIP_TO_PREVIOUS;
import static android.media.session.PlaybackState.ACTION_SKIP_TO_QUEUE_ITEM;
import static android.media.session.PlaybackState.ACTION_STOP;
import static android.media.session.PlaybackState.STATE_NONE;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.Nullable;
import android.media.Rating;
import android.media.session.MediaController.TransportControls;
import android.net.Uri;
import android.os.Bundle;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow class for using {@link TransportControls} in tests.
 *
 * <p>TransportControls should always be created by first creating a corresponding MediaController;
 * *NOT*, for instance, via Shadows.newInstanceOf(TransportControls.class).
 */
@Implements(value = TransportControls.class)
public class ShadowTransportControls {
  @RealObject protected TransportControls realTransportControls;

  private long lastPerformedAction = STATE_NONE;

  @Nullable private String customAction;
  @Nullable private Bundle customActionArgs;

  /** The current item id in playlist set by {@link TransportControls#skipToQueueItem(long)}. */
  private long queueItemId;

  /** The rating value set by last call of {@link TransportControls#setRating(Rating)}. */
  @Nullable private Rating rating;

  /** The current position in milliseconds set by {@link TransportControls#seekTo(long)} method. */
  private long seekToPositionMs;

  /**
   * URI argument provided when {@link TransportControls#prepareFromUri(Uri, Bundle)} or {@link
   * TransportControls#playFromUri(Uri, Bundle)} was called.
   */
  @Nullable private Uri uri;

  @Implementation
  protected void pause() {
    lastPerformedAction = ACTION_PAUSE;
    reflector(TransportControlsReflector.class, realTransportControls).pause();
  }

  @Implementation
  protected void play() {
    lastPerformedAction = ACTION_PLAY;
    reflector(TransportControlsReflector.class, realTransportControls).play();
  }

  @Implementation
  protected void playFromSearch(String query, Bundle extras) {
    lastPerformedAction = ACTION_PLAY_FROM_SEARCH;
    reflector(TransportControlsReflector.class, realTransportControls)
        .playFromSearch(query, extras);
  }

  @Implementation(minSdk = M)
  protected void playFromUri(Uri uri, Bundle extras) {
    lastPerformedAction = ACTION_PLAY_FROM_URI;
    this.uri = uri;
    reflector(TransportControlsReflector.class, realTransportControls).playFromUri(uri, extras);
  }

  @Implementation(minSdk = N)
  protected void prepareFromSearch(String query, Bundle extras) {
    lastPerformedAction = ACTION_PREPARE_FROM_SEARCH;
    reflector(TransportControlsReflector.class, realTransportControls)
        .prepareFromSearch(query, extras);
  }

  @Implementation(minSdk = N)
  protected void prepareFromUri(Uri uri, Bundle extras) {
    lastPerformedAction = ACTION_PREPARE_FROM_URI;
    this.uri = uri;
    reflector(TransportControlsReflector.class, realTransportControls).prepareFromUri(uri, extras);
  }

  @Implementation
  protected void seekTo(long pos) {
    lastPerformedAction = ACTION_SEEK_TO;
    seekToPositionMs = pos;
    reflector(TransportControlsReflector.class, realTransportControls).seekTo(pos);
  }

  @Implementation
  protected void sendCustomAction(String action, Bundle args) {
    customAction = action;
    customActionArgs = args;
    reflector(TransportControlsReflector.class, realTransportControls)
        .sendCustomAction(action, args);
  }

  @Implementation
  protected void setRating(Rating rating) {
    lastPerformedAction = ACTION_SET_RATING;
    this.rating = rating;
    reflector(TransportControlsReflector.class, realTransportControls).setRating(rating);
  }

  @Implementation
  protected void skipToNext() {
    lastPerformedAction = ACTION_SKIP_TO_NEXT;
    reflector(TransportControlsReflector.class, realTransportControls).skipToNext();
  }

  @Implementation
  protected void skipToPrevious() {
    lastPerformedAction = ACTION_SKIP_TO_PREVIOUS;
    reflector(TransportControlsReflector.class, realTransportControls).skipToPrevious();
  }

  @Implementation
  protected void skipToQueueItem(long id) {
    lastPerformedAction = ACTION_SKIP_TO_QUEUE_ITEM;
    queueItemId = id;
    reflector(TransportControlsReflector.class, realTransportControls).skipToQueueItem(id);
  }

  @Implementation
  protected void stop() {
    lastPerformedAction = ACTION_STOP;
    reflector(TransportControlsReflector.class, realTransportControls).stop();
  }

  public long getLastPerformedAction() {
    return lastPerformedAction;
  }

  @Nullable
  public String getCustomAction() {
    return customAction;
  }

  @Nullable
  public Bundle getCustomActionArgs() {
    return customActionArgs;
  }

  public long getSeekToPositionMs() {
    return seekToPositionMs;
  }

  @Nullable
  public Uri getUri() {
    return uri;
  }

  @Nullable
  public Rating getRating() {
    return rating;
  }

  public long getQueueItemId() {
    return queueItemId;
  }

  @ForType(TransportControls.class)
  private interface TransportControlsReflector {
    @Direct
    void pause();

    @Direct
    void play();

    @Direct
    void playFromSearch(String query, Bundle extras);

    @Direct
    void playFromUri(Uri uri, Bundle extras);

    @Direct
    void prepareFromSearch(String query, Bundle extras);

    @Direct
    void prepareFromUri(Uri uri, Bundle extras);

    @Direct
    void seekTo(long pos);

    @Direct
    void sendCustomAction(String action, Bundle args);

    @Direct
    void setRating(Rating rating);

    @Direct
    void skipToNext();

    @Direct
    void skipToPrevious();

    @Direct
    void skipToQueueItem(long id);

    @Direct
    void stop();
  }
}
