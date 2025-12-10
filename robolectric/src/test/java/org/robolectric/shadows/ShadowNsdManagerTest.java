package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Application;
import android.content.Context;
import android.net.Network;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdManager.ServiceInfoCallback;
import android.net.nsd.NsdServiceInfo;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public final class ShadowNsdManagerTest {

  private Application context;
  private NsdManager nsdManager;
  private ShadowNsdManager shadowNsdManager;
  private FakeExecutor fakeExecutor;
  @Mock private RegistrationListener registrationListener;
  @Mock private DiscoveryListener discoveryListener;
  @Mock private ResolveListener resolveListener;

  @Rule(order = 0)
  public final MockitoRule mockito = MockitoJUnit.rule();

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    shadowNsdManager = Shadow.extract(nsdManager);
    fakeExecutor = new FakeExecutor();
  }

  @Config(minSdk = VERSION_CODES.TIRAMISU)
  @Test
  public void registerService_withExecutor_invokesListener() {
    // First register a service.
    NsdServiceInfo serviceInfo = new NsdServiceInfo();
    serviceInfo.setServiceName("test_service");
    serviceInfo.setServiceType("foo.bar");
    serviceInfo.setAttribute("abc", "def");
    serviceInfo.setPort(1234);

    nsdManager.registerService(
        serviceInfo, NsdManager.PROTOCOL_DNS_SD, fakeExecutor, registrationListener);

    // See if executor was used for callback.
    assertThat(fakeExecutor.command).isNotNull();
    // Check that the shadow provides the registered listener.
    RegistrationListener registeredListener = shadowNsdManager.getRegistrationListener(serviceInfo);
    assertThat(registeredListener).isNotNull();
    assertThat(registeredListener).isEqualTo(registrationListener);

    // Check that the shadow provides the registered service info by listener.
    assertThat(shadowNsdManager.getRegisteredServiceInfo(registrationListener))
        .isEqualTo(serviceInfo);
    // Now we can run the callback.
    fakeExecutor.command.run();
    // Check that the listener was properly notified of the (successful) registration.
    verify(registrationListener).onServiceRegistered(serviceInfo);
  }

  @Config(minSdk = VERSION_CODES.TIRAMISU)
  @Test
  public void registerService_withExecutor_failsOnBadProtocol() {
    // Try to register a service.
    NsdServiceInfo serviceInfo = new NsdServiceInfo();
    serviceInfo.setServiceName("test_service");
    serviceInfo.setServiceType("foo.bar");
    serviceInfo.setAttribute("abc", "def");
    serviceInfo.setPort(1234);

    nsdManager.registerService(serviceInfo, -1, fakeExecutor, registrationListener);

    // See if executor was used for callback.
    assertThat(fakeExecutor.command).isNotNull();
    // Now we can run the callback.
    fakeExecutor.command.run();
    // Check that the listener was properly notified of the registration failure.
    verify(registrationListener)
        .onRegistrationFailed(serviceInfo, NsdManager.FAILURE_BAD_PARAMETERS);
  }

  @Test
  public void registerService_invokesListener() {
    // First register a service.
    NsdServiceInfo serviceInfo = new NsdServiceInfo();
    serviceInfo.setServiceName("test_service");
    serviceInfo.setServiceType("foo.bar");
    serviceInfo.setAttribute("abc", "def");
    serviceInfo.setPort(1234);

    nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

    // Check that the shadow provides the registered listener.
    RegistrationListener registeredListener = shadowNsdManager.getRegistrationListener(serviceInfo);
    assertThat(registeredListener).isNotNull();
    assertThat(registeredListener).isEqualTo(registrationListener);

    // Check that the shadow provides the registered service info by listener.
    assertThat(shadowNsdManager.getRegisteredServiceInfo(registrationListener))
        .isEqualTo(serviceInfo);
    // Check that the listener was properly notified of the (successful) registration.
    verify(registrationListener).onServiceRegistered(serviceInfo);
  }

  @Test
  public void registerService_withBadProtocolType_invokesFailureCallback() {
    // Create a test registration listener.
    // First register a service.
    NsdServiceInfo serviceInfo = new NsdServiceInfo();
    serviceInfo.setServiceName("test_service");
    serviceInfo.setServiceType("foo.bar");
    serviceInfo.setAttribute("abc", "def");
    serviceInfo.setPort(1234);

    nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD - 1, registrationListener);

    // Check that the listener was properly notified of the failed registration.
    verify(registrationListener)
        .onRegistrationFailed(serviceInfo, NsdManager.FAILURE_BAD_PARAMETERS);
  }

  @Test
  public void registerService_withRegisteredListener_throwsException() {
    // Register a service.
    NsdServiceInfo serviceInfo = createTestServiceInfo();

    nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

    verify(registrationListener).onServiceRegistered(serviceInfo);

    // Try to register the same listener for a different service.
    NsdServiceInfo serviceInfo2 = createTestServiceInfo();
    serviceInfo2.setServiceName("test_service2");
    serviceInfo2.setServiceType("foo.baz");
    assertThrows(
        IllegalArgumentException.class,
        () ->
            nsdManager.registerService(
                serviceInfo2, NsdManager.PROTOCOL_DNS_SD, registrationListener));
  }

  @Test
  public void unregisterService_withUnregisteredListener_throwsException() {
    // Try to unregister without ever registering.
    assertThrows(
        IllegalArgumentException.class, () -> nsdManager.unregisterService(registrationListener));
    // No callbacks should happen.
    verify(registrationListener, never()).onServiceRegistered(any());
    verify(registrationListener, never()).onServiceUnregistered(any());
    verify(registrationListener, never()).onUnregistrationFailed(any(), anyInt());
  }

  @Test
  public void unregisterService_withRegisteredListener_invokesListener() {
    // Create a test registration listener.
    NsdServiceInfo serviceInfo = createTestServiceInfo();

    nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

    verify(registrationListener).onServiceRegistered(serviceInfo);
    verify(registrationListener, never()).onServiceUnregistered(any());

    nsdManager.unregisterService(registrationListener);

    verify(registrationListener).onServiceUnregistered(serviceInfo);
  }

  @Config(minSdk = VERSION_CODES.TIRAMISU)
  @Test
  public void discoverServices_withExecutor_invokesListener() {
    // First register for service discovery.
    String serviceType = "foo.bar";

    nsdManager.discoverServices(
        serviceType, NsdManager.PROTOCOL_DNS_SD, (Network) null, fakeExecutor, discoveryListener);

    // See if executor was used for callback.
    assertThat(fakeExecutor.command).isNotNull();
    // Now we can run the callback.
    fakeExecutor.command.run();

    // Check that the listener was notified of discovery start.
    verify(discoveryListener).onDiscoveryStarted(serviceType);
  }

  @Test
  public void discoverServices_invokesListener() {
    // First register for service discovery.
    String serviceType = "foo.bar";

    nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

    // Check that the shadow provides the discovery listener.
    List<DiscoveryListener> discoveryListeners =
        shadowNsdManager.getDiscoveryListeners(serviceType);
    assertThat(discoveryListeners).isNotNull();
    assertThat(discoveryListeners).containsExactly(discoveryListener);

    // Check that the shadow provides the service info by discovery listener.
    assertThat(shadowNsdManager.getDiscoveryListenerServiceType(discoveryListener))
        .isEqualTo(serviceType);
    // Check that the listener was notified of discovery start.
    verify(discoveryListener).onDiscoveryStarted(serviceType);
  }

  @Test
  public void discoverServices_sameListener_throwsException() {
    // First register for service discovery.
    String serviceType = "foo.bar";

    nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

    // Now try to register same listener again.
    assertThrows(
        IllegalArgumentException.class,
        () ->
            nsdManager.discoverServices(
                serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener));
  }

  @Test
  public void getDiscoveryListeners_getsCorrectListener() {
    // First register for service discovery.
    String serviceType = "foo.bar";

    nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

    // Discovery started but nothing discovered yet.
    verify(discoveryListener).onDiscoveryStarted(serviceType);
    verify(discoveryListener, never()).onServiceFound(any());

    // Now try to fetch the same listener.
    DiscoveryListener listener = shadowNsdManager.getDiscoveryListeners(serviceType).get(0);
    // Perform fake discovery callback.
    listener.onServiceFound(createTestServiceInfo());
    verify(discoveryListener).onServiceFound(any());
  }

  @Test
  public void getDiscoveryListenerServiceType_getsCorrectServiceType() {
    // First register for service discovery.
    String serviceType = "foo.bar";

    nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    assertThat(shadowNsdManager.getDiscoveryListenerServiceType(discoveryListener))
        .isEqualTo(serviceType);
  }

  @Test
  public void stopServiceDiscovery_invokesListener() {
    // First register for service discovery.
    String serviceType = "foo.bar";

    nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

    // Stop discovery.
    nsdManager.stopServiceDiscovery(discoveryListener);

    // Check that the shadow made callbacks.
    verify(discoveryListener).onDiscoveryStopped(serviceType);
    // Check that listener is no longer registered.
    assertThat(shadowNsdManager.getDiscoveryListeners(serviceType)).isNull();
  }

  @Test
  public void stopServiceDiscovery_withUnregisteredListener_throwsException() {
    assertThrows(
        IllegalArgumentException.class, () -> nsdManager.stopServiceDiscovery(discoveryListener));

    // Check that the shadow made no callbacks.
    verify(discoveryListener, never()).onDiscoveryStopped(any());
    verify(discoveryListener, never()).onDiscoveryStarted(any());
    verify(discoveryListener, never()).onServiceFound(any());
  }

  @Test
  public void resolveService_savesListener() {
    // First register for service resolution.
    NsdServiceInfo serviceInfo = createTestServiceInfo();
    nsdManager.resolveService(serviceInfo, resolveListener);
    assertThat(shadowNsdManager.getResolveListeners(serviceInfo)).containsExactly(resolveListener);
    assertThat(shadowNsdManager.getResolveListenerServiceInfo(resolveListener))
        .isEqualTo(serviceInfo);
  }

  @Test
  public void resolveService_withRegisteredListener_throwsException() {
    // First register for service resolution.
    NsdServiceInfo serviceInfo = createTestServiceInfo();
    nsdManager.resolveService(serviceInfo, resolveListener);
    assertThat(shadowNsdManager.getResolveListeners(serviceInfo)).containsExactly(resolveListener);
    assertThat(shadowNsdManager.getResolveListenerServiceInfo(resolveListener))
        .isEqualTo(serviceInfo);
  }

  @Test
  public void removeResolveListener_removesListener() {
    // First register for service resolution.
    NsdServiceInfo serviceInfo = createTestServiceInfo();
    nsdManager.resolveService(serviceInfo, resolveListener);
    assertThat(shadowNsdManager.getResolveListeners(serviceInfo)).containsExactly(resolveListener);
    assertThat(shadowNsdManager.getResolveListenerServiceInfo(resolveListener))
        .isEqualTo(serviceInfo);
    // Now remove the listener.
    shadowNsdManager.removeResolveListener(resolveListener);
    assertThat(shadowNsdManager.getResolveListeners(serviceInfo)).isEmpty();
    assertThat(shadowNsdManager.getResolveListenerServiceInfo(resolveListener)).isNull();
  }

  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @Test
  public void registerServiceInfoCallback_savesCallback() {
    ServiceInfoCallback serviceInfoCallback = mock(ServiceInfoCallback.class);
    // First register for service resolution.
    NsdServiceInfo serviceInfo = createTestServiceInfo();
    nsdManager.registerServiceInfoCallback(serviceInfo, fakeExecutor, serviceInfoCallback);
    assertThat(shadowNsdManager.getServiceInfoCallbacks(serviceInfo))
        .containsExactly(serviceInfoCallback);
    assertThat(shadowNsdManager.getServiceInfoCallbackServiceInfo(serviceInfoCallback))
        .isEqualTo(serviceInfo);
  }

  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @Test
  public void registerServiceInfoCallback_withRegisteredCallback_throwsException() {
    ServiceInfoCallback serviceInfoCallback = mock(ServiceInfoCallback.class);
    // First register for service resolution.
    NsdServiceInfo serviceInfo = createTestServiceInfo();
    nsdManager.registerServiceInfoCallback(serviceInfo, fakeExecutor, serviceInfoCallback);
    assertThat(shadowNsdManager.getServiceInfoCallbacks(serviceInfo))
        .containsExactly(serviceInfoCallback);
    assertThat(shadowNsdManager.getServiceInfoCallbackServiceInfo(serviceInfoCallback))
        .isEqualTo(serviceInfo);
  }

  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @Test
  public void removeServiceInfoCallback_removesCallback() {
    ServiceInfoCallback serviceInfoCallback = mock(ServiceInfoCallback.class);
    // First register for service resolution.
    NsdServiceInfo serviceInfo = createTestServiceInfo();
    nsdManager.registerServiceInfoCallback(serviceInfo, fakeExecutor, serviceInfoCallback);
    assertThat(shadowNsdManager.getServiceInfoCallbacks(serviceInfo))
        .containsExactly(serviceInfoCallback);
    assertThat(shadowNsdManager.getServiceInfoCallbackServiceInfo(serviceInfoCallback))
        .isEqualTo(serviceInfo);
    // Now remove the listener.
    shadowNsdManager.removeServiceInfoCallback(serviceInfoCallback);
    assertThat(shadowNsdManager.getServiceInfoCallbacks(serviceInfo)).isEmpty();
    assertThat(shadowNsdManager.getServiceInfoCallbackServiceInfo(serviceInfoCallback)).isNull();
  }

  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @Test
  public void reset_clearsAll() {
    NsdServiceInfo serviceInfo = createTestServiceInfo();
    nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    assertThat(shadowNsdManager.getRegisteredServiceInfo(registrationListener))
        .isEqualTo(serviceInfo);
    assertThat(shadowNsdManager.getRegistrationListener(serviceInfo))
        .isEqualTo(registrationListener);

    nsdManager.discoverServices(
        serviceInfo.getServiceType(), NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    assertThat(shadowNsdManager.getDiscoveryListenerServiceType(discoveryListener))
        .isEqualTo(serviceInfo.getServiceType());
    assertThat(shadowNsdManager.getDiscoveryListeners(serviceInfo.getServiceType()))
        .containsExactly(discoveryListener);

    nsdManager.resolveService(serviceInfo, resolveListener);
    assertThat(shadowNsdManager.getResolveListenerServiceInfo(resolveListener))
        .isEqualTo(serviceInfo);
    assertThat(shadowNsdManager.getResolveListeners(serviceInfo)).containsExactly(resolveListener);

    ServiceInfoCallback serviceInfoCallback = mock(ServiceInfoCallback.class);
    nsdManager.registerServiceInfoCallback(serviceInfo, fakeExecutor, serviceInfoCallback);
    assertThat(shadowNsdManager.getServiceInfoCallbackServiceInfo(serviceInfoCallback))
        .isEqualTo(serviceInfo);

    ShadowNsdManager.reset();

    assertThat(shadowNsdManager.getRegisteredServiceInfo(registrationListener)).isNull();
    assertThat(shadowNsdManager.getDiscoveryListenerServiceType(discoveryListener)).isNull();
    assertThat(shadowNsdManager.getDiscoveryListeners(serviceInfo.getServiceType())).isNull();
    assertThat(shadowNsdManager.getResolveListenerServiceInfo(resolveListener)).isNull();
    assertThat(shadowNsdManager.getResolveListeners(serviceInfo)).isNull();
    assertThat(shadowNsdManager.getServiceInfoCallbacks(serviceInfo)).isNull();
    assertThat(shadowNsdManager.getServiceInfoCallbackServiceInfo(serviceInfoCallback)).isNull();
  }

  public static class FakeExecutor implements Executor {
    @Nullable public Runnable command = null;

    @Override
    public void execute(Runnable command) {
      this.command = command;
    }
  }

  private NsdServiceInfo createTestServiceInfo() {
    NsdServiceInfo serviceInfo = new NsdServiceInfo();
    serviceInfo.setServiceName("test_service");
    serviceInfo.setServiceType("foo.bar");
    serviceInfo.setAttribute("abc", "def");
    serviceInfo.setPort(1234);
    return serviceInfo;
  }
}
