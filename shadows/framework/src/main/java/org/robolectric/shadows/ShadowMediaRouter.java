package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

import android.media.AudioRoutesInfo;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.os.Parcel;
import android.text.TextUtils;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow class for {@link android.media.MediaRouter}. */
@Implements(MediaRouter.class)
public class ShadowMediaRouter {
  public static final String BLUETOOTH_DEVICE_NAME = "TestBluetoothDevice";

  private @RealObject MediaRouter realObject;

  /**
   * Adds the Bluetooth A2DP route and ensures it's the selected route, simulating connecting a
   * Bluetooth device.
   */
  public void addBluetoothRoute() {
    updateBluetoothAudioRoute(BLUETOOTH_DEVICE_NAME);

    if (RuntimeEnvironment.getApiLevel() <= JELLY_BEAN_MR1) {
      ReflectionHelpers.callInstanceMethod(
          MediaRouter.class,
          realObject,
          "selectRouteInt",
          ClassParameter.from(int.class, MediaRouter.ROUTE_TYPE_LIVE_AUDIO),
          ClassParameter.from(RouteInfo.class, getBluetoothA2dpRoute()));
    } else {
      realObject.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO, getBluetoothA2dpRoute());
    }
  }

  /** Removes the Bluetooth A2DP route, simulating disconnecting the Bluetooth device. */
  public void removeBluetoothRoute() {
    // Android's AudioService passes a null Bluetooth device name to MediaRouter to signal that the
    // A2DP route should be removed.
    updateBluetoothAudioRoute(null);
  }

  /** Returns whether the Bluetooth A2DP route is the currently selected route. */
  public boolean isBluetoothRouteSelected(int type) {
    return realObject.getSelectedRoute(type).equals(getBluetoothA2dpRoute());
  }

  private static RouteInfo getBluetoothA2dpRoute() {
    return ReflectionHelpers.getField(
        ReflectionHelpers.getStaticField(MediaRouter.class, "sStatic"), "mBluetoothA2dpRoute");
  }

  /**
   * Updates the MediaRouter's Bluetooth audio route.
   *
   * @param bluetoothDeviceName the name of the Bluetooth device or null to indicate that the
   *     already-existing Bluetooth A2DP device should be removed
   */
  private void updateBluetoothAudioRoute(@Nullable String bluetoothDeviceName) {
    callUpdateAudioRoutes(newAudioRouteInfo(bluetoothDeviceName));
  }

  /**
   * Creates a new {@link AudioRoutesInfo} to be used for updating the Bluetooth audio route.
   *
   * @param bluetoothDeviceName the name of the Bluetooth device or null to indicate that the
   *     already-existing Bluetooth A2DP device should be removed
   */
  private static AudioRoutesInfo newAudioRouteInfo(@Nullable String bluetoothDeviceName) {
    Parcel p = Parcel.obtain();
    TextUtils.writeToParcel(bluetoothDeviceName, p, /* parcelableFlags= */ 0);
    p.setDataPosition(0);
    return AudioRoutesInfo.CREATOR.createFromParcel(p);
  }

  private void callUpdateAudioRoutes(AudioRoutesInfo routesInfo) {
    ReflectionHelpers.callInstanceMethod(
        ReflectionHelpers.getStaticField(MediaRouter.class, "sStatic"),
        RuntimeEnvironment.getApiLevel() <= JELLY_BEAN
            ? "updateRoutes"
            : "updateAudioRoutes",
        ClassParameter.from(AudioRoutesInfo.class, routesInfo));
  }

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(MediaRouter.class, "sStatic", null);
  }
}
