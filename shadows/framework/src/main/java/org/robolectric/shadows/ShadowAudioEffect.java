package org.robolectric.shadows;

import android.media.audiofx.AudioEffect;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(AudioEffect.class)
public class ShadowAudioEffect {

  private static List<AudioEffect.Descriptor> DESCRIPTORS = new ArrayList<>();

  public static void addEffect(AudioEffect.Descriptor descriptor) {
    DESCRIPTORS.add(descriptor);
  }

  @Implementation
  protected static AudioEffect.Descriptor[] queryEffects() {
    return DESCRIPTORS.toArray(new AudioEffect.Descriptor[DESCRIPTORS.size()]);
  }

  @Resetter
  public static void reset() {
    DESCRIPTORS.clear();
  }
}
