package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.media.audiofx.AudioEffect;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowAudioEffectTest {

  @Test public void queryEffects() {

    AudioEffect.Descriptor descriptor = new AudioEffect.Descriptor();
    descriptor.type = AudioEffect.EFFECT_TYPE_AEC;
    ShadowAudioEffect.addEffect(descriptor);

    AudioEffect.Descriptor[] descriptors = AudioEffect.queryEffects();

    assertThat(descriptors).asList().hasSize(1);
    assertThat(descriptors[0].type).isEqualTo(AudioEffect.EFFECT_TYPE_AEC);
  }
}
