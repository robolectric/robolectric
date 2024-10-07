package org.robolectric.shadows;

import static android.media.MediaRouter.ROUTE_TYPE_LIVE_AUDIO;
import static android.media.MediaRouter.ROUTE_TYPE_LIVE_VIDEO;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowMediaRouter}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowMediaRouterTest {
  private MediaRouter mediaRouter;

  @Before
  public void setUp() {
    mediaRouter =
        (MediaRouter)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.MEDIA_ROUTER_SERVICE);
  }

  @Test
  public void testAddBluetoothRoute_additionalRouteAvailable() {
    shadowOf(mediaRouter).addBluetoothRoute();
    assertThat(mediaRouter.getRouteCount()).isEqualTo(2);
  }

  @Test
  public void testAddBluetoothRoute_bluetoothRouteSelected() {
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);
    assertThat(mediaRouter.getSelectedRoute(ROUTE_TYPE_LIVE_AUDIO)).isEqualTo(bluetoothRoute);
  }

  @Test
  public void testAddBluetoothRoute_checkBluetoothRouteProperties() {
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);
    assertThat(bluetoothRoute.getName().toString())
        .isEqualTo(ShadowMediaRouter.BLUETOOTH_DEVICE_NAME);
    assertThat(bluetoothRoute.getDescription().toString()).isEqualTo("Bluetooth audio");
  }

  @Test
  @Config(minSdk = N)
  public void testAddBluetoothRoute_checkBluetoothRouteProperties_apiN() {
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);
    assertThat(bluetoothRoute.getDeviceType()).isEqualTo(RouteInfo.DEVICE_TYPE_BLUETOOTH);
  }

  @Test
  public void testSelectBluetoothRoute_getsSetAsSelectedRoute() {
    // Although this isn't something faked out by the shadow we should ensure that the Bluetooth
    // route can be selected after it's been added.
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);

    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_AUDIO, bluetoothRoute);

    assertThat(mediaRouter.getSelectedRoute(ROUTE_TYPE_LIVE_AUDIO)).isEqualTo(bluetoothRoute);
  }

  @Test
  public void testRemoveBluetoothRoute_whenBluetoothSelected_defaultRouteAvailableAndSelected() {
    shadowOf(mediaRouter).addBluetoothRoute();

    shadowOf(mediaRouter).removeBluetoothRoute();

    assertThat(mediaRouter.getRouteCount()).isEqualTo(1);
    assertThat(mediaRouter.getSelectedRoute(ROUTE_TYPE_LIVE_AUDIO))
        .isEqualTo(mediaRouter.getDefaultRoute());
  }

  @Test
  public void testRemoveBluetoothRoute_whenDefaultSelected_defaultRouteAvailableAndSelected() {
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);
    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_AUDIO, bluetoothRoute);

    shadowOf(mediaRouter).removeBluetoothRoute();

    assertThat(mediaRouter.getRouteCount()).isEqualTo(1);
    assertThat(mediaRouter.getSelectedRoute(ROUTE_TYPE_LIVE_AUDIO))
        .isEqualTo(mediaRouter.getDefaultRoute());
  }

  @Test
  public void testIsBluetoothRouteSelected_bluetoothRouteNotAdded_returnsFalse() {
    assertThat(shadowOf(mediaRouter).isBluetoothRouteSelected(ROUTE_TYPE_LIVE_AUDIO)).isFalse();
  }

  @Test
  public void testIsBluetoothRouteSelected_bluetoothRouteAddedButNotSelected_returnsFalse() {
    shadowOf(mediaRouter).addBluetoothRoute();
    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_AUDIO, mediaRouter.getDefaultRoute());
    assertThat(shadowOf(mediaRouter).isBluetoothRouteSelected(ROUTE_TYPE_LIVE_AUDIO)).isFalse();
  }

  @Test
  public void testIsBluetoothRouteSelected_bluetoothRouteSelectedForDifferentType_returnsFalse() {
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);

    // Select the Bluetooth route for AUDIO and the default route for AUDIO.
    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_AUDIO, bluetoothRoute);
    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_VIDEO, mediaRouter.getDefaultRoute());

    assertThat(shadowOf(mediaRouter).isBluetoothRouteSelected(ROUTE_TYPE_LIVE_VIDEO)).isFalse();
  }

  @Test
  public void testIsBluetoothRouteSelected_bluetoothRouteSelected_returnsTrue() {
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);
    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_AUDIO, bluetoothRoute);
    assertThat(shadowOf(mediaRouter).isBluetoothRouteSelected(ROUTE_TYPE_LIVE_AUDIO)).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void mediaRouter_activityContextEnabled_differentInstancesRetrieveDefaultRoute() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      MediaRouter applicationMediaRouter =
          (MediaRouter)
              ApplicationProvider.getApplicationContext()
                  .getSystemService(Context.MEDIA_ROUTER_SERVICE);

      Activity activity = controller.get();
      MediaRouter activityMediaRouter =
          (MediaRouter) activity.getSystemService(Context.MEDIA_ROUTER_SERVICE);

      assertThat(applicationMediaRouter).isNotSameInstanceAs(activityMediaRouter);

      MediaRouter.RouteInfo applicationDefaultRoute = applicationMediaRouter.getDefaultRoute();
      MediaRouter.RouteInfo activityDefaultRoute = activityMediaRouter.getDefaultRoute();

      assertThat(activityDefaultRoute).isEqualTo(applicationDefaultRoute);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
