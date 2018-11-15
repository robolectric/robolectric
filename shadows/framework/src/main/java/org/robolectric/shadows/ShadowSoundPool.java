package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;

import android.content.Context;
import android.media.IAudioService;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(SoundPool.class)
public class ShadowSoundPool {
  @RealObject SoundPool realObject;

  /** Generates sound ids when they are loaded. */
  private final AtomicInteger soundIds = new AtomicInteger();

  /** Tracks mapping between sound id and the paths they refer too. */
  private final SparseArray<String> idToPaths = new SparseArray<>();

  /** Tracks mapping between sound ids and the resource id they refer too. */
  private final SparseIntArray idToRes = new SparseIntArray();

  private final List<Playback> playedSounds = new ArrayList<>();

  private OnLoadCompleteListener listener;

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
    playedSounds.add(new Playback(soundID, leftVolume, rightVolume, priority, loop, rate));
    return 1;
  }

  @Implementation(minSdk = M)
  protected int _play(
      int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate) {
    playedSounds.add(new Playback(soundID, leftVolume, rightVolume, priority, loop, rate));
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

  @Implementation
  protected void setOnLoadCompleteListener(OnLoadCompleteListener listener) {
    this.listener = listener;
  }

  /** Notify the {@link OnLoadCompleteListener}, if present, that the given path was loaded. */
  public void notifyPathLoaded(String path, boolean success) {
    boolean soundIsKnown = false;
    for (int pathIdx = 0; pathIdx < idToPaths.size(); ++pathIdx) {
      if (idToPaths.valueAt(pathIdx).equals(path)) {
        if (listener != null) {
          listener.onLoadComplete(realObject, idToPaths.keyAt(pathIdx), success ? 0 : 1);
        }
        soundIsKnown = true;
      }
    }
    if (!soundIsKnown) {
      throw new IllegalArgumentException("Unknown sound. You need to call load() first");
    }
  }

  /** Notify the {@link OnLoadCompleteListener}, if present, that the given resource was loaded. */
  public void notifyResourceLoaded(int resId, boolean success) {
    boolean soundIsKnown = false;
    for (int resIdx = 0; resIdx < idToRes.size(); ++resIdx) {
      if (idToRes.valueAt(resIdx) == resId) {
        if (listener != null) {
          listener.onLoadComplete(realObject, idToRes.keyAt(resIdx), success ? 0 : 1);
        }
        soundIsKnown = true;
      }
    }
    if (!soundIsKnown) {
      throw new IllegalArgumentException("Unknown sound. You need to call load() first");
    }
  }

  /** Returns {@code true} if the given path was played. */
  public boolean wasPathPlayed(String path) {
    for (Playback playback : playedSounds) {
      if (idIsForPath(playback.soundId, path)) {
        return true;
      }
    }
    return false;
  }

  /** Returns {@code true} if the given resource was played. */
  public boolean wasResourcePlayed(int resId) {
    for (Playback playback : playedSounds) {
      if (idIsForResource(playback.soundId, resId)) {
        return true;
      }
    }
    return false;
  }

  /** Return a list of calls to {@code play} made for the given path. */
  public List<Playback> getPathPlaybacks(String path) {
    ImmutableList.Builder<Playback> playbacks = ImmutableList.builder();
    for (Playback playback : playedSounds) {
      if (idIsForPath(playback.soundId, path)) {
        playbacks.add(playback);
      }
    }
    return playbacks.build();
  }

  /** Return a list of calls to {@code play} made for the given resource. */
  public List<Playback> getResourcePlaybacks(int resId) {
    ImmutableList.Builder<Playback> playbacks = ImmutableList.builder();
    for (Playback playback : playedSounds) {
      if (idIsForResource(playback.soundId, resId)) {
        playbacks.add(playback);
      }
    }
    return playbacks.build();
  }

  private boolean idIsForPath(int soundId, String path) {
    return idToPaths.indexOfKey(soundId) >= 0 && idToPaths.get(soundId).equals(path);
  }

  private boolean idIsForResource(int soundId, int resId) {
    return idToRes.indexOfKey(soundId) >= 0 && idToRes.get(soundId) == resId;
  }

  /** Clears the sounds played by this SoundPool. */
  public void clearPlayed() {
    playedSounds.clear();
  }

  /** Record of a single call to {@link SoundPool#play }. */
  public static final class Playback {
    public final int soundId;
    public final float leftVolume;
    public final float rightVolume;
    public final int priority;
    public final int loop;
    public final float rate;

    public Playback(
        int soundId, float leftVolume, float rightVolume, int priority, int loop, float rate) {
      this.soundId = soundId;
      this.leftVolume = leftVolume;
      this.rightVolume = rightVolume;
      this.priority = priority;
      this.loop = loop;
      this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Playback)) {
        return false;
      }
      Playback that = (Playback) o;
      return this.soundId == that.soundId
          && this.leftVolume == that.leftVolume
          && this.rightVolume == that.rightVolume
          && this.priority == that.priority
          && this.loop == that.loop
          && this.rate == that.rate;
    }

    @Override
    public int hashCode() {
      return Objects.hash(soundId, leftVolume, rightVolume, priority, loop, rate);
    }

    @Override
    public String toString() {
      return Arrays.asList(soundId, leftVolume, rightVolume, priority, loop, rate).toString();
    }
  }
}
