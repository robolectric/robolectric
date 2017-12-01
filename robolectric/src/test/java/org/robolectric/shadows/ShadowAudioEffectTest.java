package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.media.audiofx.AudioEffect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowAudioEffectTest {

  @Test public void queryEffects() {

    AudioEffect.Descriptor descriptor = new AudioEffect.Descriptor();
    descriptor.type = AudioEffect.EFFECT_TYPE_AEC;
    ShadowAudioEffect.addEffect(descriptor);

    AudioEffect.Descriptor[] descriptors = AudioEffect.queryEffects();

    assertThat(descriptors).hasSize(1);
    assertThat(descriptors[0].type).isEqualTo(AudioEffect.EFFECT_TYPE_AEC);
  }
}
