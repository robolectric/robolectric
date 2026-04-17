package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static java.util.stream.Collectors.toCollection;
import static org.robolectric.util.reflector.Reflector.reflector;
import static org.robolectric.versioning.VersionCalculator.CINNAMON_BUN;

import android.annotation.CallbackExecutor;
import android.annotation.NonNull;
import android.security.advancedprotection.AdvancedProtectionFeature;
import android.security.advancedprotection.AdvancedProtectionManager;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for the AdvancedProtectionManager framework class. */
@Implements(value = AdvancedProtectionManager.class, minSdk = BAKLAVA, isInAndroidSdk = false)
public class ShadowAdvancedProtectionManager {
  protected final Object lock = new Object();

  @GuardedBy("lock")
  private boolean isAdvancedProtectionEnabled = false;

  @GuardedBy("lock")
  private final Map<AdvancedProtectionManager.Callback, Executor> callbackToExecutorMap =
      new HashMap<>();

  @GuardedBy("lock")
  protected final List<AdvancedProtectionFeature> features = new ArrayList<>();

  @Implementation
  protected void registerAdvancedProtectionCallback(
      @NonNull @CallbackExecutor Executor executor,
      @NonNull AdvancedProtectionManager.Callback callback) {
    final boolean wasAdvancedProtectionEnabled;
    synchronized (lock) {
      callbackToExecutorMap.put(callback, executor);
      wasAdvancedProtectionEnabled = isAdvancedProtectionEnabled;
    }
    triggerListener(callback, executor, wasAdvancedProtectionEnabled);
  }

  @Implementation
  protected void unregisterAdvancedProtectionCallback(
      @NonNull AdvancedProtectionManager.Callback callback) {
    synchronized (lock) {
      callbackToExecutorMap.remove(callback);
    }
  }

  @Implementation
  protected boolean isAdvancedProtectionEnabled() {
    synchronized (lock) {
      return isAdvancedProtectionEnabled;
    }
  }

  @Implementation
  protected void setAdvancedProtectionEnabled(boolean enabled) {
    final Set<Map.Entry<AdvancedProtectionManager.Callback, Executor>> callbacksToTrigger;
    synchronized (lock) {
      // platform service is a no-op if the state is the same
      if (isAdvancedProtectionEnabled == enabled) {
        return;
      }
      isAdvancedProtectionEnabled = enabled;
      callbacksToTrigger = new HashSet<>(callbackToExecutorMap.entrySet());
    }
    triggerListeners(callbacksToTrigger, enabled);
  }

  // Return true if any callbacks have been registered using registerAdvancedProtectionCallback
  public boolean hasListeners() {
    synchronized (lock) {
      return !callbackToExecutorMap.isEmpty();
    }
  }

  @Implementation
  protected List<AdvancedProtectionFeature> getAdvancedProtectionFeatures() {
    synchronized (lock) {
      return new ArrayList<>(features);
    }
  }

  /**
   * Sets available advanced protection features for testing {@link getAdvancedProtectionFeatures}.
   */
  public void setAdvancedProtectionFeatures(List<AdvancedProtectionFeature> availableFeatures) {
    synchronized (lock) {
      features.clear();
      features.addAll(availableFeatures);
    }
  }


  // Invoke all the callbacks that were registered, using the executors they registered with.
  public void triggerListeners(
      Set<Map.Entry<AdvancedProtectionManager.Callback, Executor>> callbacksToTrigger,
      boolean advancedProtectionState) {
    for (Map.Entry<AdvancedProtectionManager.Callback, Executor> entry : callbacksToTrigger) {
      triggerListener(entry.getKey(), entry.getValue(), advancedProtectionState);
    }
  }

  private void triggerListener(
      AdvancedProtectionManager.Callback callback,
      Executor executor,
      boolean advancedProtectionState) {
    executor.execute(() -> callback.onAdvancedProtectionChanged(advancedProtectionState));
  }

  @Implementation(minSdk = CINNAMON_BUN)
  protected List<AdvancedProtectionFeature> updateAdvancedProtectionFeaturesProvisioning(
      int[] featuresToProvision, int[] featuresToDeprovision) {
    synchronized (lock) {
      Set<Integer> provisionedFeatures = convertToSet(featuresToProvision);
      Set<Integer> deprovisionedFeatures = convertToSet(featuresToDeprovision);
      Set<Integer> featureIdsToUpdate = new HashSet<>();

      Set<Integer> allFeatureIds =
          reflector(AdvancedProtectionManagerReflector.class).getAllFeatureIds();

      for (int featureId : provisionedFeatures) {
        if (!allFeatureIds.contains(featureId)) {
          throw new IllegalArgumentException(
              "Feature " + featureId + " is not a valid feature ID.");
        }
        featureIdsToUpdate.add(featureId);
      }
      for (int featureId : deprovisionedFeatures) {
        if (!allFeatureIds.contains(featureId)) {
          throw new IllegalArgumentException(
              "Feature " + featureId + " is not a valid feature ID.");
        }
        if (featureIdsToUpdate.contains(featureId)) {
          throw new IllegalArgumentException(
              "Feature " + featureId + " cannot be both provisioned and deprovisioned");
        }
        featureIdsToUpdate.add(featureId);
      }

      List<AdvancedProtectionFeature> updatedFeatures = new ArrayList<>();
      for (AdvancedProtectionFeature feature : features) {
        updatedFeatures.add(
            updateProvisioning(feature, provisionedFeatures, deprovisionedFeatures));
      }

      features.clear();
      features.addAll(updatedFeatures);

      return features.stream()
          .filter(feature -> featureIdsToUpdate.contains(feature.getId()))
          .collect(toCollection(ArrayList::new));
    }
  }

  private AdvancedProtectionFeature updateProvisioning(
      AdvancedProtectionFeature feature,
      Set<Integer> featuresToProvision,
      Set<Integer> featuresToDeprovision) {
    int provisioningMode =
        determineProvisioningMode(feature, featuresToProvision, featuresToDeprovision);

    int currentMode =
        reflector(AdvancedProtectionFeatureReflector.class, feature).getProvisioningMode();
    if (provisioningMode == currentMode) {
      return feature;
    }

    boolean isEnabled = reflector(AdvancedProtectionFeatureReflector.class, feature).isEnabled();

    return reflector(AdvancedProtectionFeatureReflector.class)
        .newInstance(feature.getId(), isEnabled, provisioningMode);
  }

  private int determineProvisioningMode(
      AdvancedProtectionFeature feature,
      Set<Integer> featuresToProvision,
      Set<Integer> featuresToDeprovision) {
    if (featuresToProvision.contains(feature.getId())) {
      return reflector(AdvancedProtectionFeatureReflector.class).getModeProvisionedByFeatureAdmin();
    } else if (featuresToDeprovision.contains(feature.getId())) {
      return reflector(AdvancedProtectionFeatureReflector.class)
          .getModeDeprovisionedByFeatureAdmin();
    } else {
      return reflector(AdvancedProtectionFeatureReflector.class, feature).getProvisioningMode();
    }
  }

  private Set<Integer> convertToSet(int[] features) {
    return features == null
        ? new HashSet<>()
        : Arrays.stream(features).boxed().collect(toCollection(HashSet::new));
  }

  @ForType(AdvancedProtectionManager.class)
  private interface AdvancedProtectionManagerReflector {
    @Accessor("ALL_FEATURE_IDS")
    @Static
    Set<Integer> getAllFeatureIds();
  }

  @ForType(AdvancedProtectionFeature.class)
  private interface AdvancedProtectionFeatureReflector {
    @Accessor("PROVISIONING_MODE_PROVISIONED_BY_FEATURE_ADMIN")
    @Static
    int getModeProvisionedByFeatureAdmin();

    @Accessor("PROVISIONING_MODE_DEPROVISIONED_BY_FEATURE_ADMIN")
    @Static
    int getModeDeprovisionedByFeatureAdmin();

    int getProvisioningMode();

    boolean isEnabled();

    @Constructor
    AdvancedProtectionFeature newInstance(int id, boolean isEnabled, int provisioningMode);
  }
}
