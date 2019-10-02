package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
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

  /** Saves the package name for use inside the shadow. */
  public void setPackageName(String packageName) {
    ReflectionHelpers.setField(realMediaController, "mPackageName", packageName);
  }

  /** Executes all registered onPlaybackStateChanged callbacks. */
  public void executeOnPlaybackStateChanged(PlaybackState playbackState) {
    int messageId = ReflectionHelpers.getStaticField(MediaController.class,
        "MSG_UPDATE_PLAYBACK_STATE");

    ReflectionHelpers.callInstanceMethod(MediaController.class, realMediaController, "postMessage",
          ClassParameter.from(int.class, messageId),
          ClassParameter.from(Object.class, playbackState),
          ClassParameter.from(Bundle.class, new Bundle()));
  }
}
