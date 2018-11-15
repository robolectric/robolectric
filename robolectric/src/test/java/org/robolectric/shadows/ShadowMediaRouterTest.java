package org.robolectric.shadows;

import static android.media.MediaRouter.ROUTE_TYPE_LIVE_AUDIO;
import static android.media.MediaRouter.ROUTE_TYPE_LIVE_VIDEO;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowMediaRouter}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowMediaRouterTest {
  private MediaRouter mediaRouter;

  @Before
  public void setUp() throws Exception {
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
    assertThat(bluetoothRoute.getName()).isEqualTo(ShadowMediaRouter.BLUETOOTH_DEVICE_NAME);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void testAddBluetoothRoute_checkBluetoothRouteProperties_apiJbMr2() {
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);
    assertThat(bluetoothRoute.getDescription()).isEqualTo("Bluetooth audio");
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
    assertThat(mediaRouter.getSelectedRoute(ROUTE_TYPE_LIVE_AUDIO)).isEqualTo(getDefaultRoute());
  }

  @Test
  public void testRemoveBluetoothRoute_whenDefaultSelected_defaultRouteAvailableAndSelected() {
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);
    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_AUDIO, bluetoothRoute);

    shadowOf(mediaRouter).removeBluetoothRoute();

    assertThat(mediaRouter.getRouteCount()).isEqualTo(1);
    assertThat(mediaRouter.getSelectedRoute(ROUTE_TYPE_LIVE_AUDIO)).isEqualTo(getDefaultRoute());
  }

  @Test
  public void testIsBluetoothRouteSelected_bluetoothRouteNotAdded_returnsFalse() {
    assertThat(shadowOf(mediaRouter).isBluetoothRouteSelected(ROUTE_TYPE_LIVE_AUDIO)).isFalse();
  }

  // Pre-API 18, non-user routes weren't able to be selected.
  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void testIsBluetoothRouteSelected_bluetoothRouteAddedButNotSelected_returnsFalse() {
    shadowOf(mediaRouter).addBluetoothRoute();
    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_AUDIO, getDefaultRoute());
    assertThat(shadowOf(mediaRouter).isBluetoothRouteSelected(ROUTE_TYPE_LIVE_AUDIO)).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testIsBluetoothRouteSelected_bluetoothRouteSelectedForDifferentType_returnsFalse() {
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);

    // Select the Bluetooth route for AUDIO and the default route for AUDIO.
    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_AUDIO, bluetoothRoute);
    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_VIDEO, getDefaultRoute());

    assertThat(shadowOf(mediaRouter).isBluetoothRouteSelected(ROUTE_TYPE_LIVE_VIDEO)).isFalse();
  }

  @Test
  public void testIsBluetoothRouteSelected_bluetoothRouteSelected_returnsTrue() {
    shadowOf(mediaRouter).addBluetoothRoute();
    RouteInfo bluetoothRoute = mediaRouter.getRouteAt(1);
    mediaRouter.selectRoute(ROUTE_TYPE_LIVE_AUDIO, bluetoothRoute);
    assertThat(shadowOf(mediaRouter).isBluetoothRouteSelected(ROUTE_TYPE_LIVE_AUDIO)).isTrue();
  }

  private RouteInfo getDefaultRoute() {
    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR2) {
      return mediaRouter.getDefaultRoute();
    }
    return mediaRouter.getRouteAt(0);
  }
}
