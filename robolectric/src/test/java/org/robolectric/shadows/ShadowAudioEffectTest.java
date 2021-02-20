package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.media.audiofx.AudioEffect;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowAudioEffect}. */
@Config(maxSdk = VERSION_CODES.Q)
@RunWith(AndroidJUnit4.class)
public class ShadowAudioEffectTest {
  private static final UUID EFFECT_TYPE_NULL =
      UUID.fromString("ec7178ec-e5e1-4432-a3f4-4657e6795210");

  @Test
  public void queryEffects() {

    AudioEffect.Descriptor descriptor = new AudioEffect.Descriptor();
    descriptor.type = AudioEffect.EFFECT_TYPE_AEC;
    ShadowAudioEffect.addEffect(descriptor);

    AudioEffect.Descriptor[] descriptors = AudioEffect.queryEffects();

    assertThat(descriptors).asList().hasSize(1);
    assertThat(descriptors[0].type).isEqualTo(AudioEffect.EFFECT_TYPE_AEC);
  }

  @Test
  public void getAudioEffects_noAudioEffects_returnsNoEffects() {
    assertThat(ShadowAudioEffect.getAudioEffects()).isEmpty();
  }

  @Test
  public void getAudioEffects_newAudioEffect_returnsAudioEffect() {
    int priority = 100;
    int audioSession = 500;
    new AudioEffect(
        AudioEffect.EFFECT_TYPE_AEC, /* uuid= */ EFFECT_TYPE_NULL, priority, audioSession);

    ImmutableList<AudioEffect> actualEffects = ShadowAudioEffect.getAudioEffects();

    assertThat(actualEffects).hasSize(1);
    ShadowAudioEffect actualEffect = shadowOf(actualEffects.get(0));
    assertThat(actualEffect.getPriority()).isEqualTo(priority);
    assertThat(actualEffect.getAudioSession()).isEqualTo(audioSession);
  }

  @Test
  public void getAudioEffects_audioEffectReleased_returnsNoEffect() {
    AudioEffect effect = createAudioEffect();

    effect.release();

    assertThat(ShadowAudioEffect.getAudioEffects()).isEmpty();
  }

  @Test
  public void getPriority_returnsPriorityFromCtor() {
    int priority = 100;
    AudioEffect audioEffect =
        new AudioEffect(
            AudioEffect.EFFECT_TYPE_AEC, EFFECT_TYPE_NULL, priority, /* audioSession= */ 0);

    assertThat(shadowOf(audioEffect).getPriority()).isEqualTo(priority);
  }

  @Test
  public void getAudioSession_returnsAudioSessionFromCtor() {
    int audioSession = 100;
    AudioEffect audioEffect =
        new AudioEffect(
            AudioEffect.EFFECT_TYPE_AEC, EFFECT_TYPE_NULL, /* priority= */ 0, audioSession);

    assertThat(shadowOf(audioEffect).getAudioSession()).isEqualTo(audioSession);
  }

  @Test
  public void getEnabled_returnsFalseByDefault() {
    AudioEffect audioEffect = createAudioEffect();

    assertThat(audioEffect.getEnabled()).isFalse();
  }

  @Test
  public void getEnabled_setEnabledTrue_returnsTrue() {
    AudioEffect audioEffect = createAudioEffect();

    audioEffect.setEnabled(true);

    assertThat(audioEffect.getEnabled()).isTrue();
  }

  @Test
  public void getEnabled_setEnabledTrueThenFalse_returnsFalse() {
    AudioEffect audioEffect = createAudioEffect();

    audioEffect.setEnabled(true);
    audioEffect.setEnabled(false);

    assertThat(audioEffect.getEnabled()).isFalse();
  }

  @Test
  public void setEnabled_errorCodeSet_returnsError() {
    AudioEffect audioEffect = createAudioEffect();
    shadowOf(audioEffect).setErrorCode(AudioEffect.ERROR);

    assertThat(audioEffect.setEnabled(true)).isEqualTo(AudioEffect.ERROR);
  }

  @Test
  public void getParameter_errorCodeSet_returnsError() {
    AudioEffect audioEffect = createAudioEffect();
    shadowOf(audioEffect).setErrorCode(AudioEffect.ERROR);

    assertThat(audioEffect.getParameter(/* param= */ 1, /* value= */ new int[1]))
        .isEqualTo(AudioEffect.ERROR);
  }

  @Test
  public void setParameter_errorCodeSet_returnsError() {
    AudioEffect audioEffect = createAudioEffect();
    shadowOf(audioEffect).setErrorCode(AudioEffect.ERROR);

    assertThat(audioEffect.setParameter(/* param= */ 1, /* value= */ 2))
        .isEqualTo(AudioEffect.ERROR);
  }

  @Test
  public void getEnabled_audioEffectUninitialized_throwsException() {
    AudioEffect audioEffect = createAudioEffect();
    shadowOf(audioEffect).setInitialized(false);

    assertThrows(IllegalStateException.class, audioEffect::getEnabled);
  }

  @Test
  public void setEnabled_audioEffectUninitialized_throwsException() {
    AudioEffect audioEffect = createAudioEffect();
    shadowOf(audioEffect).setInitialized(false);

    assertThrows(IllegalStateException.class, () -> audioEffect.setEnabled(true));
  }

  @Test
  public void release_callSetEnabledAfterwards_throwsException() {
    AudioEffect audioEffect = createAudioEffect();
    audioEffect.release();

    assertThrows(IllegalStateException.class, () -> audioEffect.setEnabled(true));
  }

  @Test
  public void release_callGetEnabledAfterwards_throwsException() {
    AudioEffect audioEffect = createAudioEffect();
    audioEffect.release();

    assertThrows(IllegalStateException.class, audioEffect::getEnabled);
  }

  @Test
  public void ctor_nullType_throwsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new AudioEffect(
                /* type= */ null, EFFECT_TYPE_NULL, /* priority= */ 0, /* audioSession= */ 0));
  }

  @Test
  public void ctor_nullUuid_throwsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new AudioEffect(
                AudioEffect.EFFECT_TYPE_AEC,
                /* uuid= */ null,
                /* priority= */ 0,
                /* audioSession= */ 0));
  }

  private static AudioEffect createAudioEffect() {
    return new AudioEffect(
        AudioEffect.EFFECT_TYPE_AEC, EFFECT_TYPE_NULL, /* priority= */ 0, /* audioSession= */ 0);
  }
}
