package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.media.MediaRoute2Info;
import android.media.MediaRouter2;
import android.media.MediaRouter2.RouteCallback;
import android.media.MediaRouter2.TransferCallback;
import android.media.RouteDiscoveryPreference;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowMediaRouter2}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.R)
public final class ShadowMediaRouter2Test {

  private static final String DEFAULT_ROUTE_ID = "DEFAULT_ROUTE";
  private static final String SYSTEM_SESSION_ID = "SYSTEM_SESSION";
  private static final String SYSTEM_ROUTE_PROVIDER_ID =
      "com.android.server.media/.SystemMediaRoute2Provider";

  @Test
  public void getInstance_doesNotThrowException() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());

    assertThat(mediaRouter2).isNotNull();
  }

  @Test
  public void getInstance_forSameContext_returnsSameInstance() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());
    MediaRouter2 mediaRouter2Second =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());

    assertThat(mediaRouter2Second).isSameInstanceAs(mediaRouter2);
  }

  @Test
  public void getSystemController_returnsSystemRoutingController() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());

    MediaRouter2.RoutingController systemRoutingController = mediaRouter2.getSystemController();

    assertThat(systemRoutingController).isNotNull();
    assertThat(systemRoutingController.getId()).contains(SYSTEM_SESSION_ID);
    assertThat(systemRoutingController.getId()).contains(SYSTEM_ROUTE_PROVIDER_ID);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      assertThat(systemRoutingController.getRoutingSessionInfo().isSystemSession()).isTrue();
    }
  }

  @Test
  public void getControllers_returnsSystemRoutingController() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());

    List<MediaRouter2.RoutingController> controllers = mediaRouter2.getControllers();

    assertThat(controllers).isNotEmpty();
    // The first controller is the system routing controller.
    assertThat(controllers.get(0).getId()).contains(SYSTEM_SESSION_ID);
    assertThat(controllers.get(0).getId()).contains(SYSTEM_ROUTE_PROVIDER_ID);
  }

  @Test
  public void getRoutes_withoutRouteCallback_returnsEmptyList() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());

    List<MediaRoute2Info> routes = mediaRouter2.getRoutes();

    assertThat(routes).isEmpty();
  }

  @Test
  public void getRoutes_withRouteCallbackWithoutPreferredFeatures_returnsEmptyList() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());

    mediaRouter2.registerRouteCallback(
        new Handler()::post,
        new RouteCallback() {},
        new RouteDiscoveryPreference.Builder(
                /* preferredFeatures= */ Collections.emptyList(), /* activeScan= */ false)
            .build());
    List<MediaRoute2Info> routes = mediaRouter2.getRoutes();

    assertThat(routes).isEmpty();
  }

  @Test
  public void getRoutes_withRouteCallbackWithPreferredFeaturesRegistered_returnsRoutes() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());

    mediaRouter2.registerRouteCallback(
        new Handler()::post,
        new RouteCallback() {},
        new RouteDiscoveryPreference.Builder(
                /* preferredFeatures= */ Collections.singletonList(
                    MediaRoute2Info.FEATURE_LIVE_AUDIO),
                /* activeScan= */ false)
            .build());
    List<MediaRoute2Info> routes = mediaRouter2.getRoutes();

    assertThat(
            routes.stream()
                .anyMatch(
                    route ->
                        (route.getId().contains(DEFAULT_ROUTE_ID)
                            && route.getId().contains(SYSTEM_ROUTE_PROVIDER_ID))))
        .isTrue();
  }

  @Test
  // onRoutesAdded does not get called for versions below T.
  // onRoutesAdded is deprecated in favor of onRoutesUpdated for versions above T.
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void routeCallback_withPreferredFeaturesRegistered_isCalledWithRoutesAdded() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());
    RouteCallback routeCallback = mock(RouteCallback.class);

    mediaRouter2.registerRouteCallback(
        new Handler()::post,
        routeCallback,
        new RouteDiscoveryPreference.Builder(
                /* preferredFeatures= */ Collections.singletonList(
                    MediaRoute2Info.FEATURE_LIVE_AUDIO),
                /* activeScan= */ false)
            .build());
    shadowOf(Looper.getMainLooper()).idle();

    @SuppressWarnings("unchecked")
    // onRoutesAdded has a List<MediaRoute2Info> parameter.
    ArgumentCaptor<List<MediaRoute2Info>> routesCaptor = ArgumentCaptor.forClass(List.class);
    verify(routeCallback).onRoutesAdded(routesCaptor.capture());
    assertThat(routesCaptor.getValue()).isNotEmpty();
    assertThat(
            routesCaptor.getValue().stream()
                .anyMatch(
                    route ->
                        (route.getId().contains(DEFAULT_ROUTE_ID)
                            && route.getId().contains(SYSTEM_ROUTE_PROVIDER_ID))))
        .isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void routeCallback_withPreferredFeaturesRegistered_isCalledWithRoutesUpdated() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());
    RouteCallback routeCallback = mock(RouteCallback.class);

    mediaRouter2.registerRouteCallback(
        new Handler()::post,
        routeCallback,
        new RouteDiscoveryPreference.Builder(
                /* preferredFeatures= */ Collections.singletonList(
                    MediaRoute2Info.FEATURE_LIVE_AUDIO),
                /* activeScan= */ false)
            .build());
    shadowOf(Looper.getMainLooper()).idle();

    @SuppressWarnings("unchecked")
    // onRoutesUpdated has a List<MediaRoute2Info> parameter.
    ArgumentCaptor<List<MediaRoute2Info>> routesCaptor = ArgumentCaptor.forClass(List.class);
    verify(routeCallback).onRoutesUpdated(routesCaptor.capture());
    assertThat(routesCaptor.getValue()).isNotEmpty();
    assertThat(
            routesCaptor.getValue().stream()
                .anyMatch(
                    route ->
                        (route.getId().contains(DEFAULT_ROUTE_ID)
                            && route.getId().contains(SYSTEM_ROUTE_PROVIDER_ID))))
        .isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void
      routeCallback_registeredAgainWithAppropriatePreferredFeatures_isCalledWithRoutesUpdated() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());
    RouteCallback routeCallback = mock(RouteCallback.class);

    mediaRouter2.registerRouteCallback(
        new Handler()::post,
        routeCallback,
        new RouteDiscoveryPreference.Builder(
                /* preferredFeatures= */ Collections.emptyList(), /* activeScan= */ false)
            .build());
    shadowOf(Looper.getMainLooper()).idle();

    verify(routeCallback, never()).onRoutesUpdated(any());

    mediaRouter2.registerRouteCallback(
        new Handler()::post,
        routeCallback,
        new RouteDiscoveryPreference.Builder(
                /* preferredFeatures= */ Collections.singletonList(
                    MediaRoute2Info.FEATURE_LIVE_AUDIO),
                /* activeScan= */ false)
            .build());
    shadowOf(Looper.getMainLooper()).idle();

    @SuppressWarnings("unchecked")
    // onRoutesUpdated has a List<MediaRoute2Info> parameter.
    ArgumentCaptor<List<MediaRoute2Info>> routesCaptor = ArgumentCaptor.forClass(List.class);
    verify(routeCallback).onRoutesUpdated(routesCaptor.capture());
    assertThat(routesCaptor.getValue()).isNotEmpty();
    assertThat(
            routesCaptor.getValue().stream()
                .anyMatch(
                    route ->
                        (route.getId().contains(DEFAULT_ROUTE_ID)
                            && route.getId().contains(SYSTEM_ROUTE_PROVIDER_ID))))
        .isTrue();
  }

  @Test
  public void getRoutes_withRouteCallbackWithPreferenceUnregistered_returnsEmptyList() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());
    RouteCallback routeCallback = new RouteCallback() {};

    mediaRouter2.registerRouteCallback(
        new Handler()::post,
        routeCallback,
        new RouteDiscoveryPreference.Builder(
                /* preferredFeatures= */ Collections.singletonList(
                    MediaRoute2Info.FEATURE_LIVE_AUDIO),
                /* activeScan= */ false)
            .build());
    mediaRouter2.unregisterRouteCallback(routeCallback);
    List<MediaRoute2Info> routes = mediaRouter2.getRoutes();

    assertThat(routes).isEmpty();
  }

  @Test
  public void registerTransferCallback_doesNotThrowException() {
    MediaRouter2 mediaRouter2 =
        MediaRouter2.getInstance(ApplicationProvider.getApplicationContext());

    mediaRouter2.registerTransferCallback(new Handler()::post, new TransferCallback() {});
  }
}
