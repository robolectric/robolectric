package org.robolectric.shadows;

import android.media.audiofx.AudioEffect;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

import java.util.LinkedList;
import java.util.List;

/**
 * Shadow for {@link android.media.audiofx.AudioEffect}.
 */
@Implements(AudioEffect.class)
public class ShadowAudioEffect {

  private static List<AudioEffect.Descriptor> DESCRIPTORS = new LinkedList<>();

  public static void addEffect(AudioEffect.Descriptor descriptor) {
    DESCRIPTORS.add(descriptor);
  }

  @Implementation
  public static AudioEffect.Descriptor[] queryEffects() {
    return DESCRIPTORS.toArray(new AudioEffect.Descriptor[DESCRIPTORS.size()]);
  }

  @Resetter
  public static void reset() {
    DESCRIPTORS.clear();
  }
}
