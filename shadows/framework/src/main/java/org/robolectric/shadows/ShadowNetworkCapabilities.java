package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.NetworkCapabilities;
import android.net.NetworkSpecifier;
import android.net.TransportInfo;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Robolectic provides overrides for fetching and updating transport. */
@Implements(value = NetworkCapabilities.class, minSdk = LOLLIPOP, looseSignatures = true)
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

  /**
   * Changes {@link NetworkSpecifier} for this network capabilities. Works only on Android O and
   * higher. For lower versions use {@link #setNetworkSpecifier(String)}
   */
  @Implementation(minSdk = O)
  public NetworkCapabilities setNetworkSpecifier(NetworkSpecifier networkSpecifier) {
    return reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .setNetworkSpecifier(networkSpecifier);
  }

  /**
   * Changes {@link NetworkSpecifier} for this network capabilities. Works only on Android N_MR1 and
   * lower. For higher versions use {@link #setNetworkSpecifier(NetworkSpecifier)}
   */
  @Implementation(minSdk = N, maxSdk = N_MR1)
  public NetworkCapabilities setNetworkSpecifier(String networkSpecifier) {
    return reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .setNetworkSpecifier(networkSpecifier);
  }

  /** Sets the {@code transportInfo} of the NetworkCapabilities. */
  @HiddenApi
  @Implementation(minSdk = Q)
  public NetworkCapabilities setTransportInfo(TransportInfo transportInfo) {
    return reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .setTransportInfo(transportInfo);
  }

  /** Sets the LinkDownstreamBandwidthKbps of the NetworkCapabilities. */
  @HiddenApi
  @Implementation
  public Object setLinkDownstreamBandwidthKbps(Object kbps) {
    // Loose signatures is necessary because the return type of setLinkDownstreamBandwidthKbps
    // changed from void to NetworkCapabilities starting from API 28 (Pie)
    return reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .setLinkDownstreamBandwidthKbps((int) kbps);
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

    @Direct
    NetworkCapabilities setNetworkSpecifier(NetworkSpecifier networkSpecifier);

    @Direct
    NetworkCapabilities setNetworkSpecifier(String networkSpecifier);

    @Direct
    NetworkCapabilities setTransportInfo(TransportInfo transportInfo);

    @Direct
    NetworkCapabilities setLinkDownstreamBandwidthKbps(int kbps);
  }
}
