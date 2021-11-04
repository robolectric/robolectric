package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.media.AudioSystem;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link AudioSystem}. */
@Implements(value = AudioSystem.class, isInAndroidSdk = false)
public class ShadowAudioSystem {

  // from frameworks/base/core/jni/android_media_AudioSystem.cpp
  private static final int MAX_CHANNEL_COUNT = 8;
  private static final int MAX_SAMPLE_RATE = 192000;
  private static final int MIN_SAMPLE_RATE = 4000;

  @Implementation(minSdk = S)
  protected static int native_getMaxChannelCount() {
    return MAX_CHANNEL_COUNT;
  }

  @Implementation(minSdk = S)
  protected static int native_getMaxSampleRate() {
    return MAX_SAMPLE_RATE;
  }

  @Implementation(minSdk = S)
  protected static int native_getMinSampleRate() {
    return MIN_SAMPLE_RATE;
  }

  @Implementation(minSdk = Q, maxSdk = R)
  protected static int native_get_FCC_8() {
    // Return the value hard-coded in native code:
    // https://cs.android.com/android/platform/superproject/+/master:system/media/audio/include/system/audio-base.h;l=197;drc=c84ca89fa5d660046364897482b202c797c8595e
    return 8;
  }
}
