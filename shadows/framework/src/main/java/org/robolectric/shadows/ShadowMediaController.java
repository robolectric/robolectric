package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.annotation.NonNull;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaController.Callback;
import android.media.session.PlaybackState;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Implementation of {@link android.media.session.MediaController}.
 */
@Implements(value = MediaController.class, minSdk = LOLLIPOP)
public class ShadowMediaController {
  @RealObject
  private MediaController realMediaController;
  private PlaybackState playbackState;
  private MediaMetadata mediaMetadata;
  private final List<Callback> callbacks = new ArrayList<>();

  /** Saves the package name for use inside the shadow. */
  public void setPackageName(String packageName) {
    ReflectionHelpers.setField(realMediaController, "mPackageName", packageName);
  }

  /**
   * Saves the playbackState to control the return value of {@link
   * MediaController#getPlaybackState()}.
   */
  public void setPlaybackState(PlaybackState playbackState) {
    this.playbackState = playbackState;
  }

  /** Gets the playbackState set via {@link #setPlaybackState}. */
  @Implementation
  protected PlaybackState getPlaybackState() {
    return playbackState;
  }

  /**
   * Saves the mediaMetadata to control the return value of {@link MediaController#getMetadata()}.
   */
  public void setMetadata(MediaMetadata mediaMetadata) {
    this.mediaMetadata = mediaMetadata;
  }

  /** Gets the mediaMetadata set via {@link #setMetadata}. */
  @Implementation
  protected MediaMetadata getMetadata() {
    return mediaMetadata;
  }

  /**
   * Register callback and store it in the shadow to make it easier to check the state of the
   * registered callbacks.
   */
  @Implementation
  protected void registerCallback(@NonNull Callback callback) {
    callbacks.add(callback);
    directlyOn(realMediaController, MediaController.class).registerCallback(callback);
  }

  /**
   * Unregister callback and remove it from the shadow to make it easier to check the state of the
   * registered callbacks.
   */
  @Implementation
  protected void unregisterCallback(@NonNull Callback callback) {
    callbacks.remove(callback);
    directlyOn(realMediaController, MediaController.class).unregisterCallback(callback);
  }

  /** Gets the callbacks registered to MediaController. */
  public List<Callback> getCallbacks() {
    return callbacks;
  }

  /** Executes all registered onPlaybackStateChanged callbacks. */
  public void executeOnPlaybackStateChanged(PlaybackState playbackState) {
    setPlaybackState(playbackState);

    int messageId = ReflectionHelpers.getStaticField(MediaController.class,
        "MSG_UPDATE_PLAYBACK_STATE");
    ReflectionHelpers.callInstanceMethod(MediaController.class, realMediaController, "postMessage",
          ClassParameter.from(int.class, messageId),
          ClassParameter.from(Object.class, playbackState),
          ClassParameter.from(Bundle.class, new Bundle()));
  }

  /** Executes all registered onMetadataChanged callbacks. */
  public void executeOnMetadataChanged(MediaMetadata metadata) {
    setMetadata(metadata);

    int messageId = ReflectionHelpers.getStaticField(MediaController.class, "MSG_UPDATE_METADATA");
    ReflectionHelpers.callInstanceMethod(
        MediaController.class,
        realMediaController,
        "postMessage",
        ClassParameter.from(int.class, messageId),
        ClassParameter.from(Object.class, metadata),
        ClassParameter.from(Bundle.class, new Bundle()));
  }
}
