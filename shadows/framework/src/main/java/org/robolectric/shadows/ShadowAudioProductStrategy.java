package org.robolectric.shadows;

import static org.robolectric.versioning.VersionCalculator.POST_BAKLAVA;

import android.media.AudioAttributes;
import android.media.AudioSystem;
import android.media.audiopolicy.AudioProductStrategy;
import java.util.Objects;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Minimal shadow for {@link AudioProductStrategy} native implementation. */
@Implements(value = AudioProductStrategy.class, minSdk = POST_BAKLAVA, isInAndroidSdk = false)
public class ShadowAudioProductStrategy {

  @Implementation(minSdk = POST_BAKLAVA)
  protected static int native_get_legacy_stream_for_audio_attributes(
      AudioAttributes audioAttributes) {
    Objects.requireNonNull(audioAttributes, "AudioAttributes must not be null");
    // this method's java equivalent would always return STREAM_MUSIC since Robolectric has no audio
    // product strategies
    return AudioSystem.STREAM_MUSIC;
  }
}
