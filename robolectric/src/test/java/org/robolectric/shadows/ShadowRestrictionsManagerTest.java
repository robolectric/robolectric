package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.content.RestrictionEntry;
import android.content.RestrictionsManager;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public final class ShadowRestrictionsManagerTest {

  private RestrictionsManager restrictionsManager;
  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    restrictionsManager =
        (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
  }

  @Test
  public void getApplicationRestrictions() {
    assertThat(restrictionsManager.getApplicationRestrictions()).isNull();

    Bundle bundle = new Bundle();
    bundle.putCharSequence("test_key", "test_value");
    shadowOf(restrictionsManager).setApplicationRestrictions(bundle);

    assertThat(
            restrictionsManager.getApplicationRestrictions().getCharSequence("test_key").toString())
        .isEqualTo("test_value");
  }

  @Test
  public void getManifestRestrictions() {
    RestrictionEntry restrictionEntry =
        Iterables.getOnlyElement(
            restrictionsManager.getManifestRestrictions(context.getPackageName()));

    assertThat(restrictionEntry.getKey()).isEqualTo("restrictionKey");
  }

  @Test
  @Config(minSdk = O)
  public void restrictionsManager_activityContextEnabled_hasConsistentRestrictionsProvider() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      RestrictionsManager applicationRestrictionsManager =
          ApplicationProvider.getApplicationContext().getSystemService(RestrictionsManager.class);
      Activity activity = controller.get();
      RestrictionsManager activityRestrictionsManager =
          activity.getSystemService(RestrictionsManager.class);

      assertThat(applicationRestrictionsManager).isNotSameInstanceAs(activityRestrictionsManager);

      boolean applicationHasProvider = applicationRestrictionsManager.hasRestrictionsProvider();
      boolean activityHasProvider = activityRestrictionsManager.hasRestrictionsProvider();

      assertThat(activityHasProvider).isEqualTo(applicationHasProvider);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
