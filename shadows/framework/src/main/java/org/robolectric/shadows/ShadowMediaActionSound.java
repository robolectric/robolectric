package org.robolectric.shadows;

import android.media.MediaActionSound;
import android.os.Build;
import java.util.Arrays;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** A shadow implementation of {@link android.media.MediaActionSound}. */
@Implements(value = MediaActionSound.class, minSdk = Build.VERSION_CODES.JELLY_BEAN)
public class ShadowMediaActionSound {
  private final SoundState[] sounds;
  private static final int NUM_SOUNDS = 4;

  private static final int[] playCount = {0, 0, 0, 0};

  // a class (rather than array of booleans) is used so that we can lock per sound
  private static class SoundState {
    public boolean loaded;

    public SoundState(int name) {
      loaded = false;
    }
  }

  /** Construct a new ShadowMediaActionSound. */
  public ShadowMediaActionSound() {
    sounds = new SoundState[NUM_SOUNDS];
    Arrays.setAll(sounds, i -> new SoundState(i));
  }

  /**
   * Sets a sound to loaded. Valid soundName values are 0-3 inclusive; see ex. {@link
   * MediaActionSound#SHUTTER_CLICK}. Load must only be called once for a sound before releasing the
   * sound; calling it twice will throw an error.
   */
  @Implementation
  public void load(int soundName) {
    if (soundName < 0 || soundName >= NUM_SOUNDS) {
      throw new RuntimeException("Invalid sound load requested: " + soundName);
    }
    SoundState sound = sounds[soundName];
    synchronized (sound) {
      if (sound.loaded) {
        throw new IllegalStateException("Sound loaded twice: " + soundName);
      } else {
        sound.loaded = true;
      }
    }
  }

  /*
   * Get the number of times a sound has been played. It is recommended to reset the play count at
   * the start of the test so that this count is for a single test. See also
   * {@link #resetPlayCount}.
   */
  public static int getPlayCount(int soundName) {
    if (soundName < 0 || soundName >= NUM_SOUNDS) {
      throw new RuntimeException("Invalid sound play count requested: " + soundName);
    }
    synchronized (playCount) {
      return playCount[soundName];
    }
  }

  /** Resets the play counts to 0 for all sounds. */
  public static void resetPlayCounts() {
    synchronized (playCount) {
      for (int i = 0; i < NUM_SOUNDS; i++) {
        playCount[i] = 0;
      }
    }
  }

  /**
   * Increment the play counter for a sound. Does not actually play a sound. soundName must be 0-3,
   * inclusive.
   */
  @Implementation
  public void play(int soundName) {
    if (soundName < 0 || soundName >= NUM_SOUNDS) {
      throw new RuntimeException("Invalid sound load requested: " + soundName);
    }
    SoundState sound = sounds[soundName];
    synchronized (sound) {
      if (!sound.loaded) {
        sound.loaded = true;
      }
      synchronized (playCount) {
        playCount[soundName] += 1;
      }
    }
  }

  /** Unloads the sounds. */
  @Implementation
  public void release() {
    for (SoundState sound : sounds) {
      synchronized (sound) {
        sound.loaded = false;
      }
    }
  }
}
