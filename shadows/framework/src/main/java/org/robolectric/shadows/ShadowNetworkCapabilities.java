package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.NetworkCapabilities;
import android.net.NetworkSpecifier;
import android.net.TransportInfo;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Robolectric provides overrides for fetching and updating transport. */
@Implements(value = NetworkCapabilities.class)
public class ShadowNetworkCapabilities {

  @RealObject protected NetworkCapabilities realNetworkCapabilities;

  public static final int NET_CAPABILITY_NOT_BANDWIDTH_CONSTRAINED = 37;

  public static NetworkCapabilities newInstance() {
    return Shadow.newInstanceOf(NetworkCapabilities.class);
  }

  /** Updates the transport types for this network capabilities to include {@code transportType}. */
  @HiddenApi
  @Implementation
  public NetworkCapabilities addTransportType(int transportType) {
    return reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .addTransportType(transportType);
  }

  /** Updates the transport types for this network capabilities to remove {@code transportType}. */
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
  @Implementation(maxSdk = O_MR1, methodName = "setLinkDownstreamBandwidthKbps")
  protected void setLinkDownstreamBandwidthKbpsPrePie(int kbps) {
    reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .setLinkDownstreamBandwidthKbps(kbps);
  }

  /**
   * Sets the LinkDownstreamBandwidthKbps of the NetworkCapabilities.
   *
   * <p>Return type changed to {@code NetworkCapabilities} starting from Pie.
   */
  @HiddenApi
  @Implementation(minSdk = P)
  public NetworkCapabilities setLinkDownstreamBandwidthKbps(int kbps) {
    return reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
        .setLinkDownstreamBandwidthKbps(kbps);
  }

  /** Clears capabilities. */
  public void clearCapabilities() {
    if (RuntimeEnvironment.getApiLevel() < M) {
      reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
          .setMNetworkCapabilities(0L);

      if (RuntimeEnvironment.getApiLevel() >= S) {
        reflector(NetworkCapabilitiesReflector.class, realNetworkCapabilities)
            .setMForbiddenNetworkCapabilities(0L);
      }
    } else {
      realNetworkCapabilities.clearAll();
    }
  }

  @ForType(NetworkCapabilities.class)
  interface NetworkCapabilitiesReflector {

    @Accessor("mNetworkCapabilities")
    void setMNetworkCapabilities(long capabilities);

    @Accessor("mForbiddenNetworkCapabilities")
    void setMForbiddenNetworkCapabilities(long capabilities);

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
