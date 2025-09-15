package org.robolectric.shadows;

import android.net.Network;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build.VERSION_CODES;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.Logger;

/**
 * Shadow for {@link NsdManager}.
 *
 * <p>Note that not all functionality is implemented.
 */
@Implements(NsdManager.class)
public class ShadowNsdManager {
  private static final Map<NsdServiceKey, NsdManager.RegistrationListener> registeredServices =
      new HashMap<>();
  private static final Map<NsdManager.RegistrationListener, NsdServiceInfo> registeredListeners =
      new HashMap<>();
  private static final Map<NsdManager.DiscoveryListener, String> discoveryListeners =
      new HashMap<>();
  private static final Map<String, ArrayList<NsdManager.DiscoveryListener>> discoveryServiceTypes =
      new HashMap<>();
  private static final Map<NsdManager.ResolveListener, NsdServiceInfo> resolveListeners =
      new HashMap<>();
  private static final Map<NsdManager.ServiceInfoCallback, NsdServiceInfo> serviceInfoCallbacks =
      new HashMap<>();
  private static final Map<NsdServiceKey, ArrayList<NsdManager.ResolveListener>> resolveServices =
      new HashMap<>();
  private static final Map<NsdServiceKey, ArrayList<NsdManager.ServiceInfoCallback>>
      serviceInfoServices = new HashMap<>();

  @AutoValue
  abstract static class NsdServiceKey {
    public static NsdServiceKey create(String serviceName, String serviceType) {
      return new AutoValue_ShadowNsdManager_NsdServiceKey(serviceName, serviceType);
    }

    abstract String serviceName();

    abstract String serviceType();
  }

  @Implementation(maxSdk = VERSION_CODES.S_V2)
  protected void init() {
    // do not blow up.
  }

  /**
   * Shadows the original {@link #registerService} method available since SDK 16. The recommended
   * method, available since SDK 33, takes an additional {@link Executor} parameter.
   */
  @Implementation(maxSdk = VERSION_CODES.S_V2)
  protected void registerService(
      NsdServiceInfo serviceInfo, int protocolType, NsdManager.RegistrationListener listener) {
    registerService(serviceInfo, protocolType, Runnable::run, listener);
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected void registerService(
      NsdServiceInfo serviceInfo,
      int protocolType,
      Executor executor,
      NsdManager.RegistrationListener listener) {
    NsdServiceKey serviceKey =
        NsdServiceKey.create(serviceInfo.getServiceName(), serviceInfo.getServiceType());
    Preconditions.checkArgument(
        !registeredListeners.containsKey(listener), "listener already registered");
    if (protocolType != NsdManager.PROTOCOL_DNS_SD) {
      Logger.warn("registerService: invalid protocol type");
      executor.execute(
          () -> listener.onRegistrationFailed(serviceInfo, NsdManager.FAILURE_BAD_PARAMETERS));
      return;
    }
    // Registration ok - on success, "register" the service and save the listener.
    registeredServices.put(serviceKey, listener);
    registeredListeners.put(listener, serviceInfo);
    executor.execute(() -> listener.onServiceRegistered(serviceInfo));
  }

  @Implementation
  protected void unregisterService(NsdManager.RegistrationListener listener) {
    Preconditions.checkArgument(
        registeredListeners.containsKey(listener), "listener not registered");
    NsdServiceInfo serviceInfo = registeredListeners.get(listener);
    registeredServices.remove(getServiceKey(serviceInfo));
    registeredListeners.remove(listener);
    listener.onServiceUnregistered(serviceInfo);
  }

  @Implementation(maxSdk = VERSION_CODES.S_V2)
  protected void discoverServices(
      String serviceType, int protocolType, NsdManager.DiscoveryListener listener) {
    discoverServices(serviceType, protocolType, (Network) null, Runnable::run, listener);
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected void discoverServices(
      String serviceType,
      int protocolType,
      Network network,
      Executor executor,
      NsdManager.DiscoveryListener listener) {
    // Check for existing discovery listeners.
    Preconditions.checkArgument(
        !discoveryListeners.containsKey(listener), "listener already registered");
    // Register the listener.
    discoveryServiceTypes.putIfAbsent(serviceType, new ArrayList<>());
    discoveryServiceTypes.get(serviceType).add(listener);
    // Add new listener to listener tracking.
    discoveryListeners.put(listener, serviceType);
    // Notify the listener of the successful start of discovery.
    executor.execute(() -> listener.onDiscoveryStarted(serviceType));
  }

  @Implementation
  protected void stopServiceDiscovery(NsdManager.DiscoveryListener listener) {
    // Check for existing discovery listener.
    Preconditions.checkArgument(
        discoveryListeners.containsKey(listener), "listener not registered");
    // Unregister the listener.
    String serviceType = discoveryListeners.get(listener);
    discoveryServiceTypes.remove(serviceType);
    discoveryListeners.remove(listener);
    listener.onDiscoveryStopped(serviceType);
  }

  @Implementation(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  protected void registerServiceInfoCallback(
      NsdServiceInfo serviceInfo, Executor executor, NsdManager.ServiceInfoCallback callback) {
    Preconditions.checkArgument(
        !serviceInfoCallbacks.containsKey(callback), "callback already registered");
    // Register the callback.
    NsdServiceKey serviceKey = getServiceKey(serviceInfo);
    serviceInfoServices.putIfAbsent(serviceKey, new ArrayList<>());
    serviceInfoServices.get(serviceKey).add(callback);
    serviceInfoCallbacks.put(callback, serviceInfo);
  }

  @Implementation
  protected void resolveService(NsdServiceInfo serviceInfo, NsdManager.ResolveListener listener) {
    Preconditions.checkArgument(
        !resolveListeners.containsKey(listener), "listener already registered");
    // Register the resolver.
    NsdServiceKey serviceKey = getServiceKey(serviceInfo);
    resolveServices.putIfAbsent(serviceKey, new ArrayList<>());
    resolveServices.get(serviceKey).add(listener);
    // Add new listener to listener tracking.
    resolveListeners.put(listener, serviceInfo);
  }

  private static NsdServiceKey getServiceKey(NsdServiceInfo serviceInfo) {
    return NsdServiceKey.create(serviceInfo.getServiceName(), serviceInfo.getServiceType());
  }

  /** Resets shadow to its initial state with no registered services and no discovery listeners. */
  @Resetter
  public static void reset() {
    registeredServices.clear();
    registeredListeners.clear();
    discoveryListeners.clear();
    discoveryServiceTypes.clear();
    resolveListeners.clear();
    serviceInfoCallbacks.clear();
    resolveServices.clear();
    serviceInfoServices.clear();
  }

  /**
   * Returns the listener for a registered service, or null if no matching service is registered.
   *
   * @param serviceInfo The registered service info for the listener to find.
   */
  @Nullable
  public NsdManager.RegistrationListener getRegistrationListener(NsdServiceInfo serviceInfo) {
    NsdServiceKey serviceKey = getServiceKey(serviceInfo);
    if (registeredServices.containsKey(serviceKey)) {
      return registeredServices.get(serviceKey);
    }
    return null;
  }

  /**
   * Returns the service info for a registered service, or null if no matching service is
   * registered.
   *
   * @param listener The listener for the service to find.
   */
  @Nullable
  public NsdServiceInfo getRegisteredServiceInfo(NsdManager.RegistrationListener listener) {
    if (registeredListeners.containsKey(listener)) {
      return registeredListeners.get(listener);
    }
    return null;
  }

  @Nullable
  public ImmutableList<NsdManager.DiscoveryListener> getDiscoveryListeners(String serviceType) {
    if (discoveryServiceTypes.containsKey(serviceType)) {
      return ImmutableList.copyOf(discoveryServiceTypes.get(serviceType));
    }
    return null;
  }

  /**
   * Returns the service type that a discovery listener is waiting for, or null if listener is not
   * discovering.
   *
   * @param listener The listener for the service type.
   */
  @Nullable
  public String getDiscoveryListenerServiceType(NsdManager.DiscoveryListener listener) {
    if (discoveryListeners.containsKey(listener)) {
      return discoveryListeners.get(listener);
    }
    return null;
  }

  @Nullable
  public ImmutableList<NsdManager.ResolveListener> getResolveListeners(NsdServiceInfo serviceInfo) {
    NsdServiceKey serviceKey = getServiceKey(serviceInfo);
    if (resolveServices.containsKey(serviceKey)) {
      return ImmutableList.copyOf(resolveServices.get(serviceKey));
    }
    return null;
  }

  @Nullable
  public ImmutableList<NsdManager.ServiceInfoCallback> getServiceInfoCallbacks(
      NsdServiceInfo serviceInfo) {
    NsdServiceKey serviceKey = getServiceKey(serviceInfo);
    if (serviceInfoServices.containsKey(serviceKey)) {
      return ImmutableList.copyOf(serviceInfoServices.get(serviceKey));
    }
    return null;
  }

  @Nullable
  public NsdServiceInfo getResolveListenerServiceInfo(NsdManager.ResolveListener listener) {
    return resolveListeners.getOrDefault(listener, null);
  }

  @Nullable
  public NsdServiceInfo getServiceInfoCallbackServiceInfo(NsdManager.ServiceInfoCallback callback) {
    if (serviceInfoCallbacks.containsKey(callback)) {
      return serviceInfoCallbacks.get(callback);
    }
    return null;
  }

  protected void removeResolveListener(NsdManager.ResolveListener listener) {
    if (resolveListeners.containsKey(listener)) {
      NsdServiceInfo serviceInfo = resolveListeners.get(listener);
      NsdServiceKey serviceKey = getServiceKey(serviceInfo);
      resolveServices.get(serviceKey).remove(listener);
      resolveListeners.remove(listener);
    }
  }

  protected void removeServiceInfoCallback(NsdManager.ServiceInfoCallback callback) {
    if (serviceInfoCallbacks.containsKey(callback)) {
      NsdServiceInfo serviceInfo = serviceInfoCallbacks.get(callback);
      NsdServiceKey serviceKey = getServiceKey(serviceInfo);
      serviceInfoServices.get(serviceKey).remove(callback);
      serviceInfoCallbacks.remove(callback);
    }
  }
}
