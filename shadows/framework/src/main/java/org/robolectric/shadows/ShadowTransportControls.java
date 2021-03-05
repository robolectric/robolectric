package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.media.Rating;
import android.media.session.MediaController.TransportControls;
import android.media.session.PlaybackState;
import android.os.Bundle;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Implementation of {@link android.media.session.MediaController.TransportControls}. */
@Implements(value = TransportControls.class, minSdk = LOLLIPOP)
public class ShadowTransportControls {
  /**
   * Stores the list of called TransportControls functions along with their arguments. The first
   * member of each item in the list is the name of the function called in string format. After the
   * name, each member corresponds to the in-order arguments that were passed with the function.
   */
  private List<Object> listTransportControls = new ArrayList<>();

  @Implementation
  public void fastForward() {
    listTransportControls.add(ImmutableList.of("fastForward"));
  }

  @Implementation
  public void pause() {
    listTransportControls.add(ImmutableList.of("pause"));
  }

  @Implementation
  public void play() {
    listTransportControls.add(ImmutableList.of("play"));
  }

  @Implementation
  public void playFromMediaId(String mediaId, Bundle extras) {
    listTransportControls.add(ImmutableList.of("playFromMediaId", mediaId, extras));
  }

  @Implementation
  public void playFromSearch(String query, Bundle extras) {
    listTransportControls.add(ImmutableList.of("playFromSearch", query, extras));
  }

  @Implementation
  public void rewind() {
    listTransportControls.add(ImmutableList.of("rewind"));
  }

  @Implementation
  public void seekTo(long pos) {
    listTransportControls.add(ImmutableList.of("seekTo", pos));
  }

  @Implementation
  public void sendCustomAction(PlaybackState.CustomAction customAction, Bundle args) {
    listTransportControls.add(ImmutableList.of("sendCustomAction", customAction, args));
  }

  @Implementation
  public void sendCustomAction(String action, Bundle args) {
    listTransportControls.add(ImmutableList.of("sendCustomAction", action, args));
  }

  @Implementation
  public void setRating(Rating rating) {
    listTransportControls.add(ImmutableList.of("setRating", rating));
  }

  @Implementation
  public void skipToNext() {
    listTransportControls.add(ImmutableList.of("skipToNext"));
  }

  @Implementation
  public void skipToPrevious() {
    listTransportControls.add(ImmutableList.of("skipToPrevious"));
  }

  @Implementation
  public void skipToQueueItem(long id) {
    listTransportControls.add(ImmutableList.of("skipToQueueItem", id));
  }

  @Implementation
  public void stop() {
    listTransportControls.add(ImmutableList.of("stop"));
  }

  public void setRepeatMode(int repeatMode) {
    listTransportControls.add(ImmutableList.of("setRepeatMode", repeatMode));
  }

  public void setShuffleMode(int shuffleMode) {
    listTransportControls.add(ImmutableList.of("setShuffleMode", shuffleMode));
  }

  /**
   * Gets the list of all transport control functions called along with the arguments passed with
   * them.
   */
  public List<Object> getListTransportControls() {
    return listTransportControls;
  }
}
