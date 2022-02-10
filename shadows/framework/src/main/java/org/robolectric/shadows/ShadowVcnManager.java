package org.robolectric.shadows;

import android.net.vcn.VcnConfig;
import android.net.vcn.VcnManager;
import android.net.vcn.VcnManager.VcnStatusCallback;
import android.os.Build.VERSION_CODES;
import android.os.ParcelUuid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** A Shadow for android.net.vcn.VcnManager added in Android S. */
@Implements(value = VcnManager.class, minSdk = VERSION_CODES.S, isInAndroidSdk = false)
public class ShadowVcnManager {

  private final Map<VcnStatusCallback, VcnStatusCallbackInfo> callbacks = new HashMap<>();
  private final Map<ParcelUuid, VcnConfig> configs = new HashMap<>();

  private int currentVcnStatus = VcnManager.VCN_STATUS_CODE_NOT_CONFIGURED;

  @Implementation
  protected void registerVcnStatusCallback(
      ParcelUuid subGroup, Executor executor, VcnStatusCallback callback) {

    callbacks.put(callback, new VcnStatusCallbackInfo(executor, subGroup));
  }

  @Implementation
  protected void unregisterVcnStatusCallback(VcnStatusCallback callback) {
    if (callback == null) {
      throw new IllegalArgumentException("VcnStatusCallback == null");
    } else if (!callbacks.containsKey(callback)) {
      throw new IllegalArgumentException("VcnStatusCallback not registered");
    }
    callbacks.remove(callback);
  }

  @Implementation
  protected void setVcnConfig(ParcelUuid subGroup, VcnConfig config) {
    configs.put(subGroup, config);
  }

  @Implementation
  protected void clearVcnConfig(ParcelUuid subGroup) {
    if (subGroup == null) {
      throw new IllegalArgumentException("subGroup == null");
    }
    configs.remove(subGroup);
  }

  @Implementation
  protected List<ParcelUuid> getConfiguredSubscriptionGroups() {
    return new ArrayList<>(configs.keySet());
  }

  /** Gets a list of all registered VcnStatusCallbacks. */
  public Set<VcnStatusCallback> getRegisteredVcnStatusCallbacks() {
    return Collections.unmodifiableSet(callbacks.keySet());
  }

  /**
   * Set the vcn status code (see {@link #currentVcnStatus}). Triggers {@link
   * VcnStatusCallback#onStatusChanged} of all registered {@link #callbacks}
   */
  public void setStatus(int statusCode) {
    currentVcnStatus = statusCode;
    for (VcnStatusCallback callback : callbacks.keySet()) {
      callbacks.get(callback).executor.execute(() -> callback.onStatusChanged(currentVcnStatus));
    }
  }

  /**
   * Triggers onGatewayConnectionError of VcnStatusCallback {@link
   * VcnStatusCallback#onGatewayConnectionError}).
   */
  public void setGatewayConnectionError(
      String gatewayConnectionName, int errorCode, Throwable detail) {
    for (VcnStatusCallback callback : callbacks.keySet()) {
      callbacks
          .get(callback)
          .executor
          .execute(
              () -> callback.onGatewayConnectionError(gatewayConnectionName, errorCode, detail));
    }
  }

  /** Gets the subscription group of given VcnStatusCallback in {@link #callbacks}. */
  public ParcelUuid getRegisteredSubscriptionGroup(VcnStatusCallback callback) {
    return callbacks.get(callback).subGroup;
  }

  private static final class VcnStatusCallbackInfo {
    private final Executor executor;
    private final ParcelUuid subGroup;

    private VcnStatusCallbackInfo(Executor executor, ParcelUuid subGroup) {
      this.executor = executor;
      this.subGroup = subGroup;
    }
  }
}
