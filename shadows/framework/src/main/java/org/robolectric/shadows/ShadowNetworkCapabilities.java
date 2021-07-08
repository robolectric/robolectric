package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.NetworkCapabilities;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

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
    return reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .addTransportType(transportType);
  }

  /** Updates the transport types for this network capablities to remove {@code transportType}. */
  @HiddenApi
  @Implementation
  public NetworkCapabilities removeTransportType(int transportType) {
    return reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .removeTransportType(transportType);
  }

  /** Adds {@code capability} to the NetworkCapabilities. */
  @HiddenApi
  @Implementation
  public NetworkCapabilities addCapability(int capability) {
    return reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .addCapability(capability);
  }

  /** Removes {@code capability} from the NetworkCapabilities. */
  @HiddenApi
  @Implementation
  public NetworkCapabilities removeCapability(int capability) {
    return reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .removeCapability(capability);
  }

  @ForType(NetworkCapabilities.class)
  interface NetworkCapabilitiesReflector {

    @Direct
    NetworkCapabilities addTransportType(int transportType);

    @Direct
    NetworkCapabilities removeTransportType(int transportType);

    @Direct
    NetworkCapabilities addCapability(int capability);

    @Direct
    NetworkCapabilities removeCapability(int capability);
  }
}
