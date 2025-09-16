package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;

import android.media.AudioAttributes;
import android.media.AudioSystem;
import android.media.audiopolicy.AudioProductStrategy;
import java.util.Objects;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.PostBaklava;

/** Minimal shadow for {@link AudioProductStrategy} native implementation. */
@Implements(
    value = AudioProductStrategy.class,
    minSdk = PostBaklava.SDK_INT,
    isInAndroidSdk = false)
public class ShadowAudioProductStrategy {

  // TODO: minSdk should be PostBaklava.SDK_INT
  @Implementation(minSdk = BAKLAVA)
  protected static int native_get_legacy_stream_for_audio_attributes(
      AudioAttributes audioAttributes) {
    Objects.requireNonNull(audioAttributes, "AudioAttributes must not be null");
    // this method's java equivalent would always return STREAM_MUSIC since Robolectric has no audio
    // product strategies
    return AudioSystem.STREAM_MUSIC;
  }
}
