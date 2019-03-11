package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.net.NetworkCapabilities;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Robolectic provides overrides for fetching and updating transport. */
@Implements(value = NetworkCapabilities.class, minSdk = LOLLIPOP)
public class ShadowNetworkCapabilities {

  @RealObject protected NetworkCapabilities realNetworkCapabilities;

  public static NetworkCapabilities newInstance() {
    return Shadow.newInstanceOf(NetworkCapabilities.class);
  }

  /** Updates the transport types for this network capablities to include {@code transportType}. */
  @HiddenApi
  @Implementation
  public NetworkCapabilities addTransportType(int transportType) {
    return directlyOn(
        realNetworkCapabilities,
        NetworkCapabilities.class,
        "addTransportType",
        ClassParameter.from(int.class, transportType));
  }

  /** Updates the transport types for this network capablities to remove {@code transportType}. */
  @HiddenApi
  @Implementation
  public NetworkCapabilities removeTransportType(int transportType) {
    return directlyOn(
        realNetworkCapabilities,
        NetworkCapabilities.class,
        "removeTransportType",
        ClassParameter.from(int.class, transportType));
  }
}
