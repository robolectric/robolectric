package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.VibrationEffect.EFFECT_CLICK;
import static android.os.VibrationEffect.EFFECT_DOUBLE_CLICK;
import static android.os.VibrationEffect.EFFECT_HEAVY_CLICK;
import static android.os.VibrationEffect.EFFECT_TICK;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowVibrator.PrimitiveEffect;

@RunWith(AndroidJUnit4.class)
public class ShadowVibratorTest {
  private Vibrator vibrator;

  @Before
  public void before() {
    vibrator =
        (Vibrator)
            ApplicationProvider.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
  }

  @Test
  public void hasVibrator() {
    assertThat(vibrator.hasVibrator()).isTrue();

    shadowOf(vibrator).setHasVibrator(false);

    assertThat(vibrator.hasVibrator()).isFalse();
  }

  @Config(minSdk = O)
  @Test
  public void hasAmplitudeControl() {
    assertThat(vibrator.hasAmplitudeControl()).isFalse();

    shadowOf(vibrator).setHasAmplitudeControl(true);

    assertThat(vibrator.hasAmplitudeControl()).isTrue();
  }

  @Test
  public void vibrateMilliseconds() {
    vibrator.vibrate(5000);

    assertThat(shadowOf(vibrator).isVibrating()).isTrue();
    assertThat(shadowOf(vibrator).getMilliseconds()).isEqualTo(5000L);

    shadowMainLooper().idleFor(Duration.ofSeconds(5));
    assertThat(shadowOf(vibrator).isVibrating()).isFalse();
  }

  @Test
  public void vibratePattern() {
    long[] pattern = new long[] {0, 200};
    vibrator.vibrate(pattern, 1);

    assertThat(shadowOf(vibrator).isVibrating()).isTrue();
    assertThat(shadowOf(vibrator).getPattern()).isEqualTo(pattern);
    assertThat(shadowOf(vibrator).getRepeat()).isEqualTo(1);
    assertThat(shadowOf(vibrator).getPrimitiveEffects()).isEmpty();
  }

  @Config(minSdk = Q)
  @Test
  public void vibratePredefined() {
    vibrator.vibrate(VibrationEffect.createPredefined(EFFECT_CLICK));

    assertThat(shadowOf(vibrator).getEffectId()).isEqualTo(EFFECT_CLICK);
    assertThat(shadowOf(vibrator).getPrimitiveEffects()).isEmpty();
  }

  @Config(sdk = R)
  @Test
  public void getPrimitiveEffects_composeOnce_shouldReturnSamePrimitiveEffects() {
    vibrator.vibrate(
        VibrationEffect.startComposition()
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.5f, /* delay= */ 20)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.7f, /* delay= */ 50)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.9f, /* delay= */ 150)
            .compose());

    assertThat(shadowOf(vibrator).getPrimitiveEffects())
        .isEqualTo(
            ImmutableList.of(
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 0.5f, /* delay= */ 20),
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 0.7f, /* delay= */ 50),
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 0.9f, /* delay= */ 150)));
  }

  @Config(sdk = R)
  @Test
  public void getPrimitiveEffects_composeTwice_shouldReturnTheLastComposition() {
    vibrator.vibrate(
        VibrationEffect.startComposition()
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.5f, /* delay= */ 20)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.7f, /* delay= */ 50)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.9f, /* delay= */ 150)
            .compose());
    vibrator.vibrate(
        VibrationEffect.startComposition()
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.4f, /* delay= */ 120)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.9f, /* delay= */ 150)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 1f, /* delay= */ 2150)
            .compose());

    assertThat(shadowOf(vibrator).getPrimitiveEffects())
        .isEqualTo(
            ImmutableList.of(
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 0.4f, /* delay= */ 120),
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 0.9f, /* delay= */ 150),
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 1f, /* delay= */ 2150)));
  }

  @Config(minSdk = S)
  @Test
  public void getPrimitiveSegmentsInPrimitiveEffects_composeOnce_shouldReturnSameFragment() {
    vibrator.vibrate(
        VibrationEffect.startComposition()
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.5f, /* delay= */ 20)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.7f, /* delay= */ 50)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.9f, /* delay= */ 150)
            .compose());

    assertThat(shadowOf(vibrator).getPrimitiveSegmentsInPrimitiveEffects())
        .isEqualTo(
            ImmutableList.of(
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 0.5f, /* delay= */ 20),
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 0.7f, /* delay= */ 50),
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 0.9f, /* delay= */ 150)));
  }

  @Config(minSdk = S)
  @Test
  public void getPrimitiveSegmentsInPrimitiveEffects_composeTwice_shouldReturnTheLastComposition() {
    vibrator.vibrate(
        VibrationEffect.startComposition()
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.5f, /* delay= */ 20)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.7f, /* delay= */ 50)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.9f, /* delay= */ 150)
            .compose());
    vibrator.vibrate(
        VibrationEffect.startComposition()
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.4f, /* delay= */ 120)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 0.9f, /* delay= */ 150)
            .addPrimitive(EFFECT_CLICK, /* scale= */ 1f, /* delay= */ 2150)
            .compose());

    assertThat(shadowOf(vibrator).getPrimitiveSegmentsInPrimitiveEffects())
        .isEqualTo(
            ImmutableList.of(
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 0.4f, /* delay= */ 120),
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 0.9f, /* delay= */ 150),
                new PrimitiveEffect(EFFECT_CLICK, /* scale= */ 1f, /* delay= */ 2150)));
  }

  @Config(minSdk = R)
  @Test
  public void areAllPrimitivesSupported_oneSupportedPrimitive_shouldReturnTrue() {
    shadowOf(vibrator)
        .setSupportedPrimitives(ImmutableList.of(EFFECT_CLICK, EFFECT_TICK, EFFECT_HEAVY_CLICK));

    assertThat(vibrator.areAllPrimitivesSupported(EFFECT_CLICK)).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void areAllPrimitivesSupported_twoSupportedPrimitives_shouldReturnTrue() {
    shadowOf(vibrator)
        .setSupportedPrimitives(ImmutableList.of(EFFECT_CLICK, EFFECT_TICK, EFFECT_HEAVY_CLICK));

    assertThat(vibrator.areAllPrimitivesSupported(EFFECT_TICK, EFFECT_CLICK)).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void areAllPrimitivesSupported_twoSupportedPrimitivesOneUnsupported_shouldReturnFalse() {
    shadowOf(vibrator)
        .setSupportedPrimitives(ImmutableList.of(EFFECT_CLICK, EFFECT_TICK, EFFECT_HEAVY_CLICK));

    assertThat(vibrator.areAllPrimitivesSupported(EFFECT_TICK, EFFECT_CLICK, EFFECT_DOUBLE_CLICK))
        .isFalse();
  }

  @Config(minSdk = R)
  @Test
  public void areAllPrimitivesSupported_oneUnsupportedPrimitive_shouldReturnFalse() {
    shadowOf(vibrator)
        .setSupportedPrimitives(ImmutableList.of(EFFECT_CLICK, EFFECT_TICK, EFFECT_HEAVY_CLICK));

    assertThat(vibrator.areAllPrimitivesSupported(EFFECT_DOUBLE_CLICK)).isFalse();
  }

  @Test
  public void cancelled() {
    vibrator.vibrate(5000);
    assertThat(shadowOf(vibrator).isVibrating()).isTrue();
    assertThat(shadowOf(vibrator).isCancelled()).isFalse();
    vibrator.cancel();

    assertThat(shadowOf(vibrator).isVibrating()).isFalse();
    assertThat(shadowOf(vibrator).isCancelled()).isTrue();
  }

  @Config(minSdk = S)
  @Test
  public void vibratePattern_withVibrationAttributes() {
    AudioAttributes audioAttributes =
        new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
            .setFlags(AudioAttributes.FLAG_BYPASS_INTERRUPTION_POLICY)
            .build();

    vibrator.vibrate(VibrationEffect.createPredefined(EFFECT_CLICK), audioAttributes);

    assertThat(shadowOf(vibrator).getVibrationAttributesFromLastVibration())
        .isEqualTo(
            VibrationAttributesBuilder.newBuilder()
                .setAudioAttributes(audioAttributes)
                .setVibrationEffect(VibrationEffect.createPredefined(EFFECT_CLICK))
                .build());
  }

  @Config(minSdk = O, maxSdk = R)
  @Test
  public void getAudioAttributes_vibrateWithAudioAttributes_shouldReturnAudioAttributes() {
    AudioAttributes audioAttributes =
        new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
            .setFlags(AudioAttributes.FLAG_BYPASS_INTERRUPTION_POLICY)
            .build();

    vibrator.vibrate(/* delay= */ 200, audioAttributes);

    AudioAttributes actualAudioAttributes =
        shadowOf(vibrator).getAudioAttributesFromLastVibration();
    assertThat(actualAudioAttributes.getAllFlags()).isEqualTo(audioAttributes.getAllFlags());
    assertThat(actualAudioAttributes.getUsage()).isEqualTo(audioAttributes.getUsage());
  }

  @Config(minSdk = S)
  @Test
  public void getPrimitiveDurations_returnsDefaultValue() {
    assertThat(vibrator.getPrimitiveDurations(EFFECT_CLICK)).asList().containsExactly(0);
  }

  @Config(minSdk = S)
  @Test
  public void getPrimitiveDurations_multipleIds_returnsDefaultValues() {
    assertThat(vibrator.getPrimitiveDurations(EFFECT_CLICK, EFFECT_TICK))
        .asList()
        .containsExactly(0, 0);
  }

  @Config(minSdk = S)
  @Test
  public void getPrimitiveDurations_nonDefaultSupplied_returnsNonDefaultValue() {
    shadowOf(vibrator).setPrimitiveDurations(EFFECT_CLICK, 10);

    assertThat(vibrator.getPrimitiveDurations(EFFECT_CLICK)).asList().containsExactly(10);
  }

  @Config(minSdk = S)
  @Test
  public void getPrimitiveDurations_multipleNonDefaultsSupplied_returnsNonDefaultValues() {
    shadowOf(vibrator).setPrimitiveDurations(EFFECT_CLICK, 10);
    shadowOf(vibrator).setPrimitiveDurations(EFFECT_TICK, 20);

    assertThat(vibrator.getPrimitiveDurations(EFFECT_CLICK, EFFECT_TICK))
        .asList()
        .containsExactly(10, 20)
        .inOrder();
  }

  @Config(minSdk = S)
  @Test
  public void getPrimitiveDurations_multipleNonDefaultsSupplied_returnsOnlyRequestedValues() {
    shadowOf(vibrator).setPrimitiveDurations(EFFECT_CLICK, 10);
    shadowOf(vibrator).setPrimitiveDurations(EFFECT_TICK, 20);

    assertThat(vibrator.getPrimitiveDurations(EFFECT_CLICK)).asList().containsExactly(10).inOrder();
  }

  @Config(minSdk = S)
  @Test
  public void getPrimitiveDurations_nonDefaultSuppliedTwice_returnsLatestNonDefaultValue() {
    shadowOf(vibrator).setPrimitiveDurations(EFFECT_CLICK, 10);
    shadowOf(vibrator).setPrimitiveDurations(EFFECT_CLICK, 20);

    assertThat(vibrator.getPrimitiveDurations(EFFECT_CLICK)).asList().containsExactly(20);
  }

  @Config(minSdk = S)
  @Test
  public void getPrimitiveDurations_nonDefaultSuppliedForSome_returnsNonDefaultValueForOverrides() {
    shadowOf(vibrator).setPrimitiveDurations(EFFECT_CLICK, 10);

    assertThat(vibrator.getPrimitiveDurations(EFFECT_CLICK, EFFECT_TICK))
        .asList()
        .containsExactly(10, 0)
        .inOrder();
  }
}
