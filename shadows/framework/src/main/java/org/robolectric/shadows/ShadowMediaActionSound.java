package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.media.MediaActionSound;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** A shadow implementation of {@link android.media.MediaActionSound}. */
@Implements(value = MediaActionSound.class)
public class ShadowMediaActionSound {
  @RealObject MediaActionSound realObject;

  private static final int[] ALL_SOUNDS = {
    MediaActionSound.SHUTTER_CLICK,
    MediaActionSound.FOCUS_COMPLETE,
    MediaActionSound.START_VIDEO_RECORDING,
    MediaActionSound.STOP_VIDEO_RECORDING
  };
  private static final int NUM_SOUNDS = ALL_SOUNDS.length;
  private static final Map<Integer, AtomicInteger> playCount = initializePlayCountMap();

  @SuppressWarnings("NonFinalStaticField")
  private static boolean mustPlayShutterSoundInternal = false;

  private static final HashMap<Integer, AtomicInteger> initializePlayCountMap() {
    HashMap<Integer, AtomicInteger> playCount = new HashMap<>();
    for (int sound : ALL_SOUNDS) {
      playCount.put(sound, new AtomicInteger(0));
    }
    return playCount;
  }

  /** Get the number of times a sound has been played. */
  public static int getPlayCount(int soundName) {
    if (soundName < 0 || soundName >= NUM_SOUNDS) {
      throw new RuntimeException("Invalid sound name: " + soundName);
    }

    return playCount.get(soundName).get();
  }

  /** Sets the value returned by {@link MediaActionSound#mustPlayShutterSound()}. */
  public static void setMustPlayShutterSound(boolean mustPlayShutterSound) {
    mustPlayShutterSoundInternal = mustPlayShutterSound;
  }

  @Resetter
  public static void reset() {
    synchronized (playCount) {
      for (AtomicInteger soundCount : playCount.values()) {
        soundCount.set(0);
      }
    }
  }

  /** Instrumented call to {@link android.media.MediaActionSound#play} */
  @Implementation
  protected void play(int soundName) {
    reflector(MediaActionSoundReflector.class, realObject).play(soundName);

    playCount.get(soundName).incrementAndGet();
  }

  @Implementation(minSdk = TIRAMISU)
  protected static boolean mustPlayShutterSound() {
    return mustPlayShutterSoundInternal;
  }

  @ForType(MediaActionSound.class)
  interface MediaActionSoundReflector {

    @Direct
    void play(int soundName);
  }
}
