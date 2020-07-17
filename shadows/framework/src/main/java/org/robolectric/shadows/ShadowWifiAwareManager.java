package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.net.wifi.aware.AttachCallback;
import android.net.wifi.aware.DiscoverySessionCallback;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.PublishDiscoverySession;
import android.net.wifi.aware.SubscribeConfig;
import android.net.wifi.aware.SubscribeDiscoverySession;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Handler;
import android.os.Looper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow Implementation of {@link android.net.wifi.aware.WifiAwareManager} */
@Implements(value = WifiAwareManager.class, minSdk = O)
public class ShadowWifiAwareManager {
  private boolean available;
  private WifiAwareSession session;
  private boolean sessionDetached = true;
  private PublishDiscoverySession discoverySessionToPublish;
  private SubscribeDiscoverySession discoverySessionToSubscribe;

  @Implementation
  protected boolean isAvailable() {
    return available;
  }

  @Implementation
  protected void attach(AttachCallback callback, Handler handler) {
    if (available && sessionDetached) {
      sessionDetached = true;
      callback.onAttached(session);
    } else if (available && !sessionDetached) {
      return;
    } else {
      callback.onAttachFailed();
    }
  }

  @Implementation
  protected void publish(
      int clientId, Looper looper, PublishConfig publishConfig, DiscoverySessionCallback callback) {
    if (available) {
      callback.onPublishStarted(discoverySessionToPublish);
    }
  }

  @Implementation
  protected void subscribe(
      int clientId,
      Looper looper,
      SubscribeConfig subscribeConfig,
      DiscoverySessionCallback callback) {
    if (available) {
      callback.onSubscribeStarted(discoverySessionToSubscribe);
    }
  }

  /* Sets the availability of the wifiAwareManager. */
  public void setAvailable(boolean available) {
    this.available = available;
  }

  /* Sets parameter to pass to AttachCallback#onAttach(WifiAwareSession session) */
  public void setWifiAwareSession(WifiAwareSession session) {
    this.session = session;
  }

  /*Sets the boolean value indicating if a wifiAwareSession has been detached. */
  public void setSessionDetached(boolean sessionDetached) {
    this.sessionDetached = sessionDetached;
  }

  /* Sets parameter to pass to DiscoverySessionCallback#onPublishStarted(PublishDiscoverySession) */
  public void setDiscoverySessionToPublish(PublishDiscoverySession publishDiscoverySession) {
    this.discoverySessionToPublish = publishDiscoverySession;
  }

  /* Sets param to pass to DiscoverySessionCallback#onSubscribeStarted(SubscribeDiscoverySession) */
  public void setDiscoverySessionToSubscribe(SubscribeDiscoverySession subscribeDiscoverySession) {
    this.discoverySessionToSubscribe = subscribeDiscoverySession;
  }
}
