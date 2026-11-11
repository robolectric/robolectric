package org.robolectric.rap.ksp;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadow.api.Shadow;

/**
 * Tests that a KSP-processed shadow with {@code isInAndroidSdk = false} gets no {@code shadowOf()}
 * helper but is still registered and applied at runtime.
 */
@RunWith(RobolectricTestRunner.class)
public final class ExtendedShadowActivityTest {

  @Test
  public void isInAndroidSdkFalse_shadowIsExtracted() {
    try (ActivityController<Activity> activityController =
        Robolectric.buildActivity(Activity.class)) {
      Activity activity = activityController.setup().get();
      ExtendedShadowActivity shadow = Shadow.extract(activity);
      assertThat(shadow).isNotNull();
    }
  }

  @Test
  public void isInAndroidSdkFalse_shadowIsCorrectType() {
    try (ActivityController<Activity> activityController =
        Robolectric.buildActivity(Activity.class)) {
      Activity activity = activityController.setup().get();
      Object shadow = Shadow.extract(activity);
      assertThat(shadow).isInstanceOf(ExtendedShadowActivity.class);
    }
  }

  @Test
  public void builtInShadowOf_returnsCustomShadow() {
    try (ActivityController<Activity> activityController =
        Robolectric.buildActivity(Activity.class)) {
      Activity activity = activityController.setup().get();
      assertThat(shadowOf(activity)).isInstanceOf(ExtendedShadowActivity.class);
    }
  }

  @Test
  public void isInAndroidSdkFalse_shadowRegisteredInShadowsProvider() {
    // The KSP-generated Shadows provider in this package, not org.robolectric.Shadows.
    Shadows provider = new Shadows();
    boolean found =
        StreamSupport.stream(provider.getShadows().spliterator(), false)
            .map(Map.Entry::getKey)
            .anyMatch("android.app.Activity"::equals);
    assertThat(found).isTrue();
  }
}
