package org.robolectric.shadows;

import android.media.AudioAttributes;
import android.media.session.MediaController.PlaybackInfo;
import android.os.Build.VERSION_CODES;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link PlaybackInfo} */
public class PlaybackInfoBuilder {
  private int volumeType;
  private int volumeControl;
  private int maxVolume;
  private int currentVolume;
  private AudioAttributes audioAttrs;

  private PlaybackInfoBuilder() {}

  public static PlaybackInfoBuilder newBuilder() {
    return new PlaybackInfoBuilder();
  }

  public PlaybackInfoBuilder setVolumeType(int volumeType) {
    this.volumeType = volumeType;
    return this;
  }

  public PlaybackInfoBuilder setVolumeControl(int volumeControl) {
    this.volumeControl = volumeControl;
    return this;
  }

  public PlaybackInfoBuilder setMaxVolume(int maxVolume) {
    this.maxVolume = maxVolume;
    return this;
  }

  public PlaybackInfoBuilder setCurrentVolume(int currentVolume) {
    this.currentVolume = currentVolume;
    return this;
  }

  public PlaybackInfoBuilder setAudioAttributes(AudioAttributes audioAttrs) {
    this.audioAttrs = audioAttrs;
    return this;
  }

  public PlaybackInfo build() {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel < VERSION_CODES.Q) {
      return ReflectionHelpers.callConstructor(
          PlaybackInfo.class,
          ClassParameter.from(int.class, volumeType),
          ClassParameter.from(AudioAttributes.class, audioAttrs),
          ClassParameter.from(int.class, volumeControl),
          ClassParameter.from(int.class, maxVolume),
          ClassParameter.from(int.class, currentVolume));
    } else if (apiLevel == VERSION_CODES.Q) {
      return ReflectionHelpers.callConstructor(
          PlaybackInfo.class,
          ClassParameter.from(int.class, volumeType),
          ClassParameter.from(int.class, volumeControl),
          ClassParameter.from(int.class, maxVolume),
          ClassParameter.from(int.class, currentVolume),
          ClassParameter.from(AudioAttributes.class, audioAttrs));
    } else {
      return new PlaybackInfo(
          volumeType, volumeControl, maxVolume, currentVolume, audioAttrs, null);
    }
  }
}
