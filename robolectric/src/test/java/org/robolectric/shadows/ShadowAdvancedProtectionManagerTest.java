package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.util.reflector.Reflector.reflector;
import static org.robolectric.versioning.VersionCalculator.CINNAMON_BUN;

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
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

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

  @Test
  public void getAdvancedProtectionFeatures_returnsCopy() {
    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);

    List<AdvancedProtectionFeature> features =
        Arrays.asList(
            new AdvancedProtectionFeature(
                AdvancedProtectionManager.FEATURE_ID_DISALLOW_CELLULAR_2G));
    shadow.setAdvancedProtectionFeatures(features);

    List<AdvancedProtectionFeature> features1 =
        advancedProtectionManager.getAdvancedProtectionFeatures();
    List<AdvancedProtectionFeature> features2 =
        advancedProtectionManager.getAdvancedProtectionFeatures();

    assertThat(features1).isNotSameInstanceAs(features2);
    assertThat(features1).isEqualTo(features2);
  }

  @Test
  @Config(minSdk = CINNAMON_BUN)
  public void
      updateAdvancedProtectionFeaturesProvisioning_provisionListContainsFeature_returnsProvisionedByAdmin()
          throws Exception {
    int featureIdCellular2g = getFeatureId("FEATURE_ID_DISALLOW_CELLULAR_2G");
    int featureIdUnknownSources = getFeatureId("FEATURE_ID_DISALLOW_INSTALL_UNKNOWN_SOURCES");
    int modeDeprovisionedByDefault =
        getProvisioningMode("PROVISIONING_MODE_DEPROVISIONED_BY_DEFAULT");
    int modeProvisionedByFeatureAdmin =
        getProvisioningMode("PROVISIONING_MODE_PROVISIONED_BY_FEATURE_ADMIN");

    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    shadow.setAdvancedProtectionFeatures(
        (List)
            Arrays.asList(
                createFeature(featureIdCellular2g, true, modeDeprovisionedByDefault),
                createFeature(featureIdUnknownSources, true, modeDeprovisionedByDefault)));

    int[] featuresToProvision = {featureIdCellular2g};
    List<?> returnedFeatures = updateFeatures(advancedProtectionManager, featuresToProvision, null);

    assertThat(returnedFeatures).hasSize(1);

    Object provisionedFeature = returnedFeatures.get(0);
    assertThat((int) provisionedFeature.getClass().getMethod("getId").invoke(provisionedFeature))
        .isEqualTo(featureIdCellular2g);
    assertThat(isFeatureEnabled(provisionedFeature)).isTrue();
    assertThat(getProvisioningModeFromObject(provisionedFeature))
        .isEqualTo(modeProvisionedByFeatureAdmin);

    List<AdvancedProtectionFeature> allFeatures =
        advancedProtectionManager.getAdvancedProtectionFeatures();
    Object unmodifiedFeature = allFeatures.get(1);
    assertThat((int) unmodifiedFeature.getClass().getMethod("getId").invoke(unmodifiedFeature))
        .isEqualTo(featureIdUnknownSources);
    assertThat(isFeatureEnabled(unmodifiedFeature)).isTrue();
    assertThat(getProvisioningModeFromObject(unmodifiedFeature))
        .isEqualTo(modeDeprovisionedByDefault);
  }

  @Test
  @Config(minSdk = CINNAMON_BUN)
  public void
      updateAdvancedProtectionFeaturesProvisioning_deprovisionListContainsFeature_returnsDeprovisionedByAdmin()
          throws Exception {
    int featureIdUnknownSources = getFeatureId("FEATURE_ID_DISALLOW_INSTALL_UNKNOWN_SOURCES");
    int modeProvisionedByFeatureAdmin =
        getProvisioningMode("PROVISIONING_MODE_PROVISIONED_BY_FEATURE_ADMIN");
    int modeDeprovisionedByFeatureAdmin =
        getProvisioningMode("PROVISIONING_MODE_DEPROVISIONED_BY_FEATURE_ADMIN");

    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    Object feature = createFeature(featureIdUnknownSources, true, modeProvisionedByFeatureAdmin);
    shadow.setAdvancedProtectionFeatures((List) Arrays.asList(feature));

    int[] featuresToDeprovision = {featureIdUnknownSources};
    List<?> features = updateFeatures(advancedProtectionManager, null, featuresToDeprovision);

    assertThat(features).hasSize(1);
    Object updatedFeature = features.get(0);
    assertThat(getProvisioningModeFromObject(updatedFeature))
        .isEqualTo(modeDeprovisionedByFeatureAdmin);
  }

  @Test
  @Config(minSdk = CINNAMON_BUN)
  public void
      updateAdvancedProtectionFeaturesProvisioning_neitherListContainsFeature_returnsEmptyList_andDidNotModify()
          throws Exception {
    int featureIdUnknownSources = getFeatureId("FEATURE_ID_DISALLOW_INSTALL_UNKNOWN_SOURCES");
    int modeProvisionedByAdb = getProvisioningMode("PROVISIONING_MODE_PROVISIONED_BY_ADB");

    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    Object feature = createFeature(featureIdUnknownSources, true, modeProvisionedByAdb);
    shadow.setAdvancedProtectionFeatures((List) Arrays.asList(feature));

    List<?> features = updateFeatures(advancedProtectionManager, null, null);

    assertThat(features).isEmpty();

    List<AdvancedProtectionFeature> allFeatures =
        advancedProtectionManager.getAdvancedProtectionFeatures();
    Object storedFeature = allFeatures.get(0);
    assertThat(getProvisioningModeFromObject(storedFeature)).isEqualTo(modeProvisionedByAdb);
  }

  @Test
  @Config(minSdk = CINNAMON_BUN)
  public void updateAdvancedProtectionFeaturesProvisioning_returnsCopyOfFeatures()
      throws Exception {
    int featureIdUnknownSources = getFeatureId("FEATURE_ID_DISALLOW_INSTALL_UNKNOWN_SOURCES");
    int modeDeprovisionedByDefault =
        getProvisioningMode("PROVISIONING_MODE_DEPROVISIONED_BY_DEFAULT");

    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    Object feature = createFeature(featureIdUnknownSources, true, modeDeprovisionedByDefault);
    shadow.setAdvancedProtectionFeatures((List) Arrays.asList(feature));

    List<?> features1 = updateFeatures(advancedProtectionManager, new int[0], new int[0]);
    List<?> features2 = updateFeatures(advancedProtectionManager, new int[0], new int[0]);

    assertThat(features1).isNotSameInstanceAs(features2);
    assertThat(features1).isEmpty();
    assertThat(features2).isEmpty();
  }

  @Test
  @Config(minSdk = CINNAMON_BUN)
  public void
      updateAdvancedProtectionFeaturesProvisioning_invalidFeatureIdInProvisionList_throwsIllegalArgumentException()
          throws Exception {
    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    shadow.setAdvancedProtectionFeatures(Arrays.asList());

    int[] featuresToProvision = {1234567};
    assertThrows(
        IllegalArgumentException.class,
        () -> updateFeatures(advancedProtectionManager, featuresToProvision, null));
  }

  @Test
  @Config(minSdk = CINNAMON_BUN)
  public void
      updateAdvancedProtectionFeaturesProvisioning_invalidFeatureIdInDeprovisionList_throwsIllegalArgumentException()
          throws Exception {
    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    shadow.setAdvancedProtectionFeatures(Arrays.asList());

    int[] featuresToDeprovision = {1234567};
    assertThrows(
        IllegalArgumentException.class,
        () -> updateFeatures(advancedProtectionManager, null, featuresToDeprovision));
  }

  @Test
  @Config(minSdk = CINNAMON_BUN)
  public void
      updateAdvancedProtectionFeaturesProvisioning_sameIdInBothLists_throwsIllegalArgumentException()
          throws Exception {
    int featureIdCellular2g = getFeatureId("FEATURE_ID_DISALLOW_CELLULAR_2G");

    AdvancedProtectionManager advancedProtectionManager = getManager();
    ShadowAdvancedProtectionManager shadow = Shadow.extract(advancedProtectionManager);
    shadow.setAdvancedProtectionFeatures(Arrays.asList());

    int[] featuresToProvision = {featureIdCellular2g};
    int[] featuresToDeprovision = {featureIdCellular2g};

    assertThrows(
        IllegalArgumentException.class,
        () ->
            updateFeatures(advancedProtectionManager, featuresToProvision, featuresToDeprovision));
  }

  private int getFeatureId(String name) {
    if (name.equals("FEATURE_ID_DISALLOW_CELLULAR_2G")) {
      return reflector(AdvancedProtectionManagerReflector.class).getFeatureIdDisallowCellular2g();
    } else {
      return reflector(AdvancedProtectionManagerReflector.class)
          .getFeatureIdDisallowInstallUnknownSources();
    }
  }

  private int getProvisioningMode(String name) {
    switch (name) {
      case "PROVISIONING_MODE_PROVISIONED_BY_FEATURE_ADMIN":
        return reflector(AdvancedProtectionFeatureReflector.class)
            .getModeProvisionedByFeatureAdmin();
      case "PROVISIONING_MODE_DEPROVISIONED_BY_FEATURE_ADMIN":
        return reflector(AdvancedProtectionFeatureReflector.class)
            .getModeDeprovisionedByFeatureAdmin();
      case "PROVISIONING_MODE_DEPROVISIONED_BY_DEFAULT":
        return reflector(AdvancedProtectionFeatureReflector.class).getModeDeprovisionedByDefault();
      case "PROVISIONING_MODE_PROVISIONED_BY_ADB":
        return reflector(AdvancedProtectionFeatureReflector.class).getModeProvisionedByAdb();
      default:
        throw new IllegalArgumentException();
    }
  }

  private Object createFeature(int id, boolean enabled, int provisioningMode) {
    return reflector(AdvancedProtectionFeatureReflector.class)
        .newInstance(id, enabled, provisioningMode);
  }

  private List<?> updateFeatures(Object manager, int[] toProvision, int[] toDeprovision) {
    try {
      return reflector(AdvancedProtectionManagerReflector.class, manager)
          .updateAdvancedProtectionFeaturesProvisioning(toProvision, toDeprovision);
    } catch (RuntimeException e) {
      if (e.getCause() instanceof IllegalArgumentException) {
        throw (IllegalArgumentException) e.getCause();
      }
      throw e;
    }
  }

  private int getProvisioningModeFromObject(Object feature) {
    return reflector(AdvancedProtectionFeatureReflector.class, feature).getProvisioningMode();
  }

  private boolean isFeatureEnabled(Object feature) {
    return reflector(AdvancedProtectionFeatureReflector.class, feature).isEnabled();
  }

  private AdvancedProtectionManager getManager() {
    return (AdvancedProtectionManager)
        getApplicationContext().getSystemService(Context.ADVANCED_PROTECTION_SERVICE);
  }

  @ForType(AdvancedProtectionManager.class)
  private interface AdvancedProtectionManagerReflector {
    @Accessor("FEATURE_ID_DISALLOW_CELLULAR_2G")
    @Static
    int getFeatureIdDisallowCellular2g();

    @Accessor("FEATURE_ID_DISALLOW_INSTALL_UNKNOWN_SOURCES")
    @Static
    int getFeatureIdDisallowInstallUnknownSources();

    List<AdvancedProtectionFeature> updateAdvancedProtectionFeaturesProvisioning(
        int[] featuresToProvision, int[] featuresToDeprovision);
  }

  @ForType(AdvancedProtectionFeature.class)
  private interface AdvancedProtectionFeatureReflector {
    @Accessor("PROVISIONING_MODE_PROVISIONED_BY_FEATURE_ADMIN")
    @Static
    int getModeProvisionedByFeatureAdmin();

    @Accessor("PROVISIONING_MODE_DEPROVISIONED_BY_FEATURE_ADMIN")
    @Static
    int getModeDeprovisionedByFeatureAdmin();

    @Accessor("PROVISIONING_MODE_DEPROVISIONED_BY_DEFAULT")
    @Static
    int getModeDeprovisionedByDefault();

    @Accessor("PROVISIONING_MODE_PROVISIONED_BY_ADB")
    @Static
    int getModeProvisionedByAdb();

    int getProvisioningMode();

    boolean isEnabled();

    @Constructor
    AdvancedProtectionFeature newInstance(int id, boolean isEnabled, int provisioningMode);
  }
}
