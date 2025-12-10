package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.security.advancedprotection.AdvancedProtectionFeature;
import android.security.advancedprotection.AdvancedProtectionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** A placeholder starting test used to verify AdvancedProtectionManager support in Robolectric. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = BAKLAVA)
public class ShadowAdvancedProtectionManagerTest {
  @Test
  public void advancedProtectionManager() {
    AdvancedProtectionManager advancedProtectionManager = getManager();

    assertThat(advancedProtectionManager).isNotNull();
  }

  @Test
  public void getSetAdvancedProtectionEnabled_success() {
    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    AdvancedProtectionManager.Callback cb = mock(AdvancedProtectionManager.Callback.class);
    advancedProtectionManager.registerAdvancedProtectionCallback(Runnable::run, cb);

    assertThat(advancedProtectionManager.isAdvancedProtectionEnabled()).isFalse();
    assertThat(shadow.isAdvancedProtectionEnabled()).isFalse();
    verify(cb).onAdvancedProtectionChanged(false);
    advancedProtectionManager.setAdvancedProtectionEnabled(true);

    assertThat(advancedProtectionManager.isAdvancedProtectionEnabled()).isTrue();
    assertThat(shadow.isAdvancedProtectionEnabled()).isTrue();
    verify(cb).onAdvancedProtectionChanged(true);
  }

  @Test
  public void setAdvancedProtectionEnabled_callbackOnlyOnChanged() {
    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    AdvancedProtectionManager.Callback cb = mock(AdvancedProtectionManager.Callback.class);
    advancedProtectionManager.registerAdvancedProtectionCallback(Runnable::run, cb);

    assertThat(advancedProtectionManager.isAdvancedProtectionEnabled()).isFalse();
    assertThat(shadow.isAdvancedProtectionEnabled()).isFalse();
    verify(cb).onAdvancedProtectionChanged(false);
    clearInvocations(cb);

    advancedProtectionManager.setAdvancedProtectionEnabled(false);

    verify(cb, never()).onAdvancedProtectionChanged(false);
  }

  @Test
  public void registerListener_success() {
    AdvancedProtectionManager.Callback cb = mock(AdvancedProtectionManager.Callback.class);
    AdvancedProtectionManager advancedProtectionManager = getManager();

    advancedProtectionManager.registerAdvancedProtectionCallback(Runnable::run, cb);
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    assertThat(shadow.hasListeners()).isTrue();
  }

  @Test
  public void unregisterListener_success() {
    AdvancedProtectionManager.Callback cb = mock(AdvancedProtectionManager.Callback.class);
    AdvancedProtectionManager advancedProtectionManager = getManager();

    advancedProtectionManager.registerAdvancedProtectionCallback(Runnable::run, cb);
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    assertThat(shadow.hasListeners()).isTrue();
    advancedProtectionManager.unregisterAdvancedProtectionCallback(cb);
    assertThat(shadow.hasListeners()).isFalse();
  }

  @Test
  public void getAdvancedProtectionFeatures_returnsEmptyListIfNotSet() {
    AdvancedProtectionManager advancedProtectionManager = getManager();

    List<AdvancedProtectionFeature> features =
        advancedProtectionManager.getAdvancedProtectionFeatures();
    assertThat(features).isEmpty();
  }

  @Test
  public void getAdvancedProtectionFeatures_returnsFeaturesFromSetAdvancedProtectionFeatures() {
    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);

    List<AdvancedProtectionFeature> setFeatures1 =
        Arrays.asList(
            new AdvancedProtectionFeature(
                AdvancedProtectionManager.FEATURE_ID_DISALLOW_CELLULAR_2G));
    shadow.setAdvancedProtectionFeatures(setFeatures1);
    List<AdvancedProtectionFeature> features1 =
        advancedProtectionManager.getAdvancedProtectionFeatures();
    assertThat(features1).hasSize(setFeatures1.size());
    assertThat(features1.containsAll(setFeatures1)).isTrue();
    assertThat(setFeatures1.containsAll(features1)).isTrue();

    List<AdvancedProtectionFeature> setFeatures2 =
        Arrays.asList(
            new AdvancedProtectionFeature(
                AdvancedProtectionManager.FEATURE_ID_DISALLOW_CELLULAR_2G),
            new AdvancedProtectionFeature(
                AdvancedProtectionManager.FEATURE_ID_DISALLOW_INSTALL_UNKNOWN_SOURCES));
    shadow.setAdvancedProtectionFeatures(setFeatures2);
    List<AdvancedProtectionFeature> features2 =
        advancedProtectionManager.getAdvancedProtectionFeatures();
    assertThat(features2).hasSize(setFeatures2.size());
    assertThat(features2.containsAll(setFeatures2)).isTrue();
    assertThat(setFeatures2.containsAll(features2)).isTrue();

    // Test empty list.
    shadow.setAdvancedProtectionFeatures(new ArrayList<AdvancedProtectionFeature>());
    List<AdvancedProtectionFeature> features3 =
        advancedProtectionManager.getAdvancedProtectionFeatures();
    assertThat(features3).isEmpty();
  }

  private AdvancedProtectionManager getManager() {
    return (AdvancedProtectionManager)
        getApplicationContext().getSystemService(Context.ADVANCED_PROTECTION_SERVICE);
  }
}
