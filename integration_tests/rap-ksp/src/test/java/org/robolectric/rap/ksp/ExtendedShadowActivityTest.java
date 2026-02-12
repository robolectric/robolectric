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
 * <p>The KSP processor intentionally ignores the {@code isInAndroidSdk} attribute since it never
 * generates {@code shadowOf()} helper methods. Shadows with {@code isInAndroidSdk = false} must
 * still appear in the {@code SHADOWS} list and have their package instrumented.
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
