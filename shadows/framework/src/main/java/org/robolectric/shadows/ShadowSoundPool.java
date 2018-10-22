package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;

import android.content.Context;
import android.media.IAudioService;
import android.media.SoundPool;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(SoundPool.class)
public class ShadowSoundPool {
  @RealObject
  SoundPool realObject;

  /** Generates sound ids when they are loaded. */
  private final AtomicInteger soundIds = new AtomicInteger();

  /** Tracks mapping between sound id and the paths they refer too. */
  private final SparseArray<String> idToPaths = new SparseArray<>();

  /** Tracks mapping between sound ids and the resource id they refer too. */
  private final SparseIntArray idToRes = new SparseIntArray();

  private final List<Integer> playedSounds = new ArrayList<>();

  @Implementation(minSdk = N, maxSdk = N_MR1)
  protected static IAudioService getService() {
    return ReflectionHelpers.createNullProxy(IAudioService.class);
  }

  // Pre api 23, the SoundPool holds an internal delegate rather than directly been used itself.
  // Because of this it's necessary to override the public method, rather than the internal
  // native method.
  @Implementation(maxSdk = LOLLIPOP_MR1)
  protected int play(
      int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate) {
    playedSounds.add(soundID);
    return 1;
  }

  @Implementation(minSdk = M)
  protected int _play(
      int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate) {
    playedSounds.add(soundID);
    return 1;
  }

  // It's not possible to override the native _load method as that would only give access to a
  // FileDescriptor which would make it difficult to check if a given sound has been placed.
  @Implementation
  protected int load(String path, int priority) {
    int soundId = soundIds.getAndIncrement();
    idToPaths.put(soundId, path);
    return soundId;
  }

  @Implementation
  protected int load(Context context, int resId, int priority) {
    int soundId = soundIds.getAndIncrement();
    idToRes.put(soundId, resId);
    return soundId;
  }

  /** Returns {@code true} if the given path was played. */
  public boolean wasPathPlayed(String path) {
    for (int id : playedSounds) {
      if (idToPaths.indexOfKey(id) >= 0 && idToPaths.get(id).equals(path)) {
        return true;
      }
    }
    return false;
  }

  /** Returns {@code true} if the given resource was played. */
  public boolean wasResourcePlayed(int resId) {
    for (int id : playedSounds) {
      if (idToRes.indexOfKey(id) >= 0 && idToRes.get(id) == resId) {
        return true;
      }
    }
    return false;
  }

  /** Clears the sounds played by this SoundPool. */
  public void clearPlayed() {
    playedSounds.clear();
  }
}
