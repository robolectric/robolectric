package org.robolectric.shadows;

import static org.robolectric.versioning.VersionCalculator.CINNAMON_BUN;

import android.media.AudioAttributes;
import android.media.AudioSystem;
import android.media.audiopolicy.AudioProductStrategy;
import java.util.Objects;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Minimal shadow for {@link AudioProductStrategy} native implementation. */
@Implements(value = AudioProductStrategy.class, minSdk = CINNAMON_BUN, isInAndroidSdk = false)
public class ShadowAudioProductStrategy {

  @Implementation(minSdk = CINNAMON_BUN)
  protected static int native_get_legacy_stream_for_audio_attributes(
      AudioAttributes audioAttributes) {
    Objects.requireNonNull(audioAttributes, "AudioAttributes must not be null");
    // The real native method would return STREAM_DEFAULT since Robolectric has no audio product
    // strategies.
    return AudioSystem.STREAM_DEFAULT;
  }
}
