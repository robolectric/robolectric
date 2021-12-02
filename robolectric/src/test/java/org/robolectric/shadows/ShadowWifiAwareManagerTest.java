package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.content.Context;
import android.net.wifi.aware.AttachCallback;
import android.net.wifi.aware.DiscoverySessionCallback;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.PublishDiscoverySession;
import android.net.wifi.aware.SubscribeConfig;
import android.net.wifi.aware.SubscribeDiscoverySession;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link ShadowWifiAwareManager} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public final class ShadowWifiAwareManagerTest {
  private WifiAwareManager wifiAwareManager;
  private Binder binder;
  private Handler handler;
  private Looper looper;
  private WifiAwareSession session;
  private static final int CLIENT_ID = 1;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    wifiAwareManager = (WifiAwareManager) context.getSystemService(Context.WIFI_AWARE_SERVICE);
    binder = new Binder();
    handler = new Handler();
    looper = handler.getLooper();
    session = ShadowWifiAwareManager.newWifiAwareSession(wifiAwareManager, binder, CLIENT_ID);
    shadowOf(wifiAwareManager).setWifiAwareSession(session);
  }

  @After
  public void tearDown() {
    session.close();
  }

  @Test
  public void setAvailable_shouldUpdateWithAvailableStatus() {
    boolean available = false;
    shadowOf(wifiAwareManager).setAvailable(available);
    assertThat(wifiAwareManager.isAvailable()).isEqualTo(available);
  }

  @Test
  public void attach_shouldAttachIfSessionDetachedAndWifiAwareManagerAvailable() {
    shadowOf(wifiAwareManager).setAvailable(true);
    shadowOf(wifiAwareManager).setSessionDetached(true);
    TestAttachCallback testAttachCallback = new TestAttachCallback();
    wifiAwareManager.attach(testAttachCallback, handler);
    shadowMainLooper().idle();
    assertThat(testAttachCallback.success).isTrue();
  }

  @Test
  public void attach_shouldNotAttachSessionIfSessionDetachedAndWifiAwareUnavailable()
      throws Exception {
    shadowOf(wifiAwareManager).setAvailable(false);
    shadowOf(wifiAwareManager).setSessionDetached(true);
    TestAttachCallback testAttachCallback = new TestAttachCallback();
    wifiAwareManager.attach(testAttachCallback, handler);
    shadowMainLooper().idle();
    assertThat(testAttachCallback.success).isFalse();
  }

  @Test
  public void publish_shouldPublishServiceIfWifiAwareAvailable() {
    int sessionId = 1;
    PublishConfig config = new PublishConfig.Builder().setServiceName("service").build();
    PublishDiscoverySession publishDiscoverySession =
        ShadowWifiAwareManager.newPublishDiscoverySession(wifiAwareManager, CLIENT_ID, sessionId);
    shadowOf(wifiAwareManager).setAvailable(true);
    shadowOf(wifiAwareManager).setDiscoverySessionToPublish(publishDiscoverySession);
    TestDiscoverySessionCallback testDiscoverySessionCallback = new TestDiscoverySessionCallback();
    shadowOf(wifiAwareManager).publish(CLIENT_ID, looper, config, testDiscoverySessionCallback);
    shadowMainLooper().idle();
    assertThat(testDiscoverySessionCallback.publishSuccess).isTrue();
    publishDiscoverySession.close();
  }

  @Test
  public void publish_shouldPublishServiceIfWifiAwareUnavailable() {
    int sessionId = 2;
    PublishConfig config = new PublishConfig.Builder().setServiceName("service").build();
    PublishDiscoverySession publishDiscoverySession =
        ShadowWifiAwareManager.newPublishDiscoverySession(wifiAwareManager, CLIENT_ID, sessionId);
    shadowOf(wifiAwareManager).setAvailable(false);
    shadowOf(wifiAwareManager).setDiscoverySessionToPublish(publishDiscoverySession);
    TestDiscoverySessionCallback testDiscoverySessionCallback = new TestDiscoverySessionCallback();
    wifiAwareManager.publish(CLIENT_ID, looper, config, testDiscoverySessionCallback);
    shadowMainLooper().idle();
    assertThat(testDiscoverySessionCallback.publishSuccess).isFalse();
    publishDiscoverySession.close();
  }

  @Test
  public void subscribe_shouldSubscribeIfWifiAwareAvailable() {
    int sessionId = 3;
    SubscribeConfig config = new SubscribeConfig.Builder().setServiceName("service").build();
    SubscribeDiscoverySession subscribeDiscoverySession =
        ShadowWifiAwareManager.newSubscribeDiscoverySession(wifiAwareManager, CLIENT_ID, sessionId);
    shadowOf(wifiAwareManager).setAvailable(true);
    shadowOf(wifiAwareManager).setDiscoverySessionToSubscribe(subscribeDiscoverySession);
    TestDiscoverySessionCallback testDiscoverySessionCallback = new TestDiscoverySessionCallback();
    wifiAwareManager.subscribe(CLIENT_ID, looper, config, testDiscoverySessionCallback);
    shadowMainLooper().idle();
    assertThat(testDiscoverySessionCallback.subscribeSuccess).isTrue();
    subscribeDiscoverySession.close();
  }

  @Test
  public void subscribe_shouldNotSubscribeIfWifiAwareUnavailable() {
    int sessionId = 4;
    SubscribeConfig config = new SubscribeConfig.Builder().setServiceName("service").build();
    SubscribeDiscoverySession subscribeDiscoverySession =
        ShadowWifiAwareManager.newSubscribeDiscoverySession(wifiAwareManager, CLIENT_ID, sessionId);
    shadowOf(wifiAwareManager).setAvailable(false);
    shadowOf(wifiAwareManager).setDiscoverySessionToSubscribe(subscribeDiscoverySession);
    TestDiscoverySessionCallback testDiscoverySessionCallback = new TestDiscoverySessionCallback();
    wifiAwareManager.subscribe(CLIENT_ID, looper, config, testDiscoverySessionCallback);
    shadowMainLooper().idle();
    assertThat(testDiscoverySessionCallback.subscribeSuccess).isFalse();
    subscribeDiscoverySession.close();
  }

  @Test
  public void canCreatePublishDiscoverySessionViaNewInstance() {
    int sessionId = 1;
    try (PublishDiscoverySession publishDiscoverySession =
        ShadowWifiAwareManager.newPublishDiscoverySession(wifiAwareManager, CLIENT_ID, sessionId)) {
      assertThat(publishDiscoverySession).isNotNull();
    }
  }

  @Test
  public void canCreateSubscribeDiscoverySessionViaNewInstance() {
    int sessionId = 1;
    SubscribeDiscoverySession subscribeDiscoverySession =
        ShadowWifiAwareManager.newSubscribeDiscoverySession(wifiAwareManager, CLIENT_ID, sessionId);
    assertThat(subscribeDiscoverySession).isNotNull();
    subscribeDiscoverySession.close();
  }

  @Test
  public void canCreateWifiAwareSessionViaNewInstance() {
    WifiAwareSession wifiAwareSession =
        ShadowWifiAwareManager.newWifiAwareSession(wifiAwareManager, binder, CLIENT_ID);
    assertThat(wifiAwareSession).isNotNull();
    wifiAwareSession.close();
  }

  private static class TestAttachCallback extends AttachCallback {
    private boolean success;

    @Override
    public void onAttached(WifiAwareSession session) {
      success = true;
    }

    @Override
    public void onAttachFailed() {
      success = false;
    }
  }

  private static class TestDiscoverySessionCallback extends DiscoverySessionCallback {
    private boolean publishSuccess;
    private boolean subscribeSuccess;

    @Override
    public void onPublishStarted(PublishDiscoverySession publishDiscoverySession) {
      publishSuccess = true;
    }

    @Override
    public void onSubscribeStarted(SubscribeDiscoverySession subscribeDiscoverySession) {
      subscribeSuccess = true;
    }
  }
}
