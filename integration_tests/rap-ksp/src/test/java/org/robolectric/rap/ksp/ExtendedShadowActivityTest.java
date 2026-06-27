package org.robolectric.rap.ksp;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadow.api.Shadow;

/**
 * Integration tests verifying that a KSP-processed shadow using {@code className} attribute and
 * {@code isInAndroidSdk = false} is correctly registered and functional at runtime.
 *
 * <p>For an {@code isInAndroidSdk = false} shadow the KSP processor emits no {@code shadowOf()}
 * helper (matching the javac processor), but the shadow must still appear in the {@code SHADOWS}
 * list and have its package instrumented so it applies at runtime.
 */
@RunWith(RobolectricTestRunner.class)
public final class ExtendedShadowActivityTest {

  @Test
  public void isInAndroidSdkFalse_shadowIsExtracted() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    ExtendedShadowActivity shadow = Shadow.extract(activity);
    assertThat(shadow).isNotNull();
  }

  @Test
  public void isInAndroidSdkFalse_shadowIsCorrectType() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    Object shadow = Shadow.extract(activity);
    assertThat(shadow).isInstanceOf(ExtendedShadowActivity.class);
  }

  @Test
  public void isInAndroidSdkFalse_shadowRegisteredInShadowsProvider() {
    Shadows provider = new Shadows();
    boolean found =
        StreamSupport.stream(provider.getShadows().spliterator(), false)
            .map(Map.Entry::getKey)
            .anyMatch("android.app.Activity"::equals);
    assertThat(found).isTrue();
  }
}
