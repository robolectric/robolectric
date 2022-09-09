package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.app.PendingIntent;
import android.media.MediaMetadata;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaController.Callback;
import android.media.session.MediaController.PlaybackInfo;
import android.media.session.PlaybackState;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Implementation of {@link android.media.session.MediaController}. */
@Implements(value = MediaController.class, minSdk = LOLLIPOP)
public class ShadowMediaController {
  @RealObject private MediaController realMediaController;
  private PlaybackState playbackState;
  private PlaybackInfo playbackInfo;
  private MediaMetadata mediaMetadata;
  private PendingIntent sessionActivity;

  /**
   * A value of RATING_NONE for ratingType indicates that rating media is not supported by the media
   * session associated with the media controller
   */
  private int ratingType = Rating.RATING_NONE;

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
   * Saves the playbackInfo to control the return value of {@link
   * MediaController#getPlaybackInfo()}.
   *
   * <p>{@link PlaybackInfoBuilder} can be used to create PlaybackInfo instances.
   */
  public void setPlaybackInfo(PlaybackInfo playbackInfo) {
    this.playbackInfo = playbackInfo;
  }

  /** Gets the playbackInfo set via {@link #setPlaybackInfo}. */
  @Implementation
  protected PlaybackInfo getPlaybackInfo() {
    return playbackInfo;
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
   * Saves the rating type to control the return value of {@link MediaController#getRatingType()}.
   */
  public void setRatingType(int ratingType) {
    if (ratingType >= 0 && ratingType <= Rating.RATING_PERCENTAGE) {
      this.ratingType = ratingType;
    } else {
      throw new IllegalArgumentException(
          "Invalid RatingType value "
              + ratingType
              + ". The valid range is from 0 to "
              + Rating.RATING_PERCENTAGE);
    }
  }

  /** Gets the rating type set via {@link #setRatingType}. */
  @Implementation
  protected int getRatingType() {
    return ratingType;
  }

  /**
   * Saves the sessionActivty to control the return value of {@link
   * MediaController#getSessionActivity()}.
   */
  public void setSessionActivity(PendingIntent sessionActivity) {
    this.sessionActivity = sessionActivity;
  }

  /** Gets the playbackState set via {@link #setSessionActivity}. */
  @Implementation
  protected PendingIntent getSessionActivity() {
    return sessionActivity;
  }

  /**
   * Register callback and store it in the shadow to make it easier to check the state of the
   * registered callbacks.
   */
  @Implementation
  protected void registerCallback(@NonNull Callback callback) {
    callbacks.add(callback);
    reflector(MediaControllerReflector.class, realMediaController).registerCallback(callback);
  }

  /**
   * Unregister callback and remove it from the shadow to make it easier to check the state of the
   * registered callbacks.
   */
  @Implementation
  protected void unregisterCallback(@NonNull Callback callback) {
    callbacks.remove(callback);
    reflector(MediaControllerReflector.class, realMediaController).unregisterCallback(callback);
  }

  /** Gets the callbacks registered to MediaController. */
  public List<Callback> getCallbacks() {
    return callbacks;
  }

  /** Executes all registered onPlaybackStateChanged callbacks. */
  public void executeOnPlaybackStateChanged(PlaybackState playbackState) {
    setPlaybackState(playbackState);

    int messageId =
        ReflectionHelpers.getStaticField(MediaController.class, "MSG_UPDATE_PLAYBACK_STATE");
    ReflectionHelpers.callInstanceMethod(
        MediaController.class,
        realMediaController,
        "postMessage",
        ClassParameter.from(int.class, messageId),
        ClassParameter.from(Object.class, playbackState),
        ClassParameter.from(Bundle.class, new Bundle()));
  }

  /** Executes all registered onSessionDestroyed callbacks. */
  public void executeOnSessionDestroyed() {
    int messageId = ReflectionHelpers.getStaticField(MediaController.class, "MSG_DESTROYED");
    ReflectionHelpers.callInstanceMethod(
        MediaController.class,
        realMediaController,
        "postMessage",
        ClassParameter.from(int.class, messageId),
        ClassParameter.from(Object.class, null),
        ClassParameter.from(Bundle.class, null));
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

  @ForType(MediaController.class)
  interface MediaControllerReflector {

    @Direct
    void registerCallback(@NonNull Callback callback);

    @Direct
    void unregisterCallback(@NonNull Callback callback);
  }
}
