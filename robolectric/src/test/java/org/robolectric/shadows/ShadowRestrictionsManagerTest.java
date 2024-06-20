package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

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
}
