package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.media.AudioAttributes;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

/** Class to build {@link VibrationAttributes} */
public final class VibrationAttributesBuilder {

  private AudioAttributes audioAttributes;
  private VibrationEffect vibrationEffect;

  private VibrationAttributesBuilder() {}

  public static VibrationAttributesBuilder newBuilder() {
    return new VibrationAttributesBuilder();
  }

  public VibrationAttributesBuilder setAudioAttributes(AudioAttributes audioAttributes) {
    this.audioAttributes = audioAttributes;
    return this;
  }

  public VibrationAttributesBuilder setVibrationEffect(VibrationEffect vibrationEffect) {
    this.vibrationEffect = vibrationEffect;
    return this;
  }

  public VibrationAttributes build() {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel >= S && apiLevel <= S_V2) {
      return ReflectionHelpers.callConstructor(
              VibrationAttributes.Builder.class,
              from(AudioAttributes.class, audioAttributes),
              from(VibrationEffect.class, vibrationEffect))
          .build();

    } else if (apiLevel >= TIRAMISU) {
      return new VibrationAttributes.Builder(audioAttributes).build();
    }
    throw new IllegalStateException("VibrationAttributes hidden constructor not found");
  }
}
