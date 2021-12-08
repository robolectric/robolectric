package org.robolectric.shadows;

import android.net.vcn.VcnManager;
import android.net.vcn.VcnManager.VcnStatusCallback;
import android.os.Build.VERSION_CODES;
import android.os.ParcelUuid;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** A Shadow for android.content.rollback.RollbackManager added in Android S. */
@Implements(value = VcnManager.class, minSdk = VERSION_CODES.S, isInAndroidSdk = false)
public class ShadowVcnManager {

  private final Map<VcnStatusCallback, Executor> callbacks = new HashMap<>();

  private int currentVcnStatus = VcnManager.VCN_STATUS_CODE_NOT_CONFIGURED;

  @Implementation
  protected void registerVcnStatusCallback(
      ParcelUuid subGroup, Executor executor, VcnStatusCallback callback) {
    callbacks.put(callback, executor);
    // VcnManager fires callback with the current state.
    executor.execute(() -> callback.onStatusChanged(currentVcnStatus));
  }

  @Implementation
  protected void unregisterVcnStatusCallback(VcnStatusCallback callback) {
    if (callback == null) {
      throw new IllegalArgumentException("VcnStatusCallback == null");
    }
    callbacks.remove(callback);
  }

  public Set<VcnStatusCallback> getCallbacks() {
    return Collections.unmodifiableSet(callbacks.keySet());
  }

  public void setStatus(int statusCode) {
    currentVcnStatus = statusCode;
    for (VcnStatusCallback callback : callbacks.keySet()) {
      callbacks.get(callback).execute(() -> callback.onStatusChanged(currentVcnStatus));
    }
  }

  public void setGatewayConnectionError(
      String gatewayConnectionName, int errorCode, Throwable detail) {
    for (VcnStatusCallback callback : callbacks.keySet()) {
      callbacks
          .get(callback)
          .execute(
              () -> callback.onGatewayConnectionError(gatewayConnectionName, errorCode, detail));
    }
  }
}
