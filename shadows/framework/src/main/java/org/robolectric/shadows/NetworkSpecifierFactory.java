package org.robolectric.shadows;

import android.annotation.RequiresApi;
import android.net.NetworkSpecifier;
import android.net.StringNetworkSpecifier;
import android.os.Build;

/** Factory to create {@link NetworkSpecifier} types that are hidden on certain SDK levels. */
@RequiresApi(Build.VERSION_CODES.O)
public final class NetworkSpecifierFactory {

  /**
   * Constructs a new {@link StringNetworkSpecifier} instance, which has remained hidden on all SDKs
   * (as of U), but has existed since the {@link NetworkSpecifier} hierarchy was created in O to
   * represent a few more niche specifier types without defining a full-blown subclass of {@link
   * NetworkSpecifier} for each of their particular use cases. These meanings typically stabilize
   * over time and then gain a concrete {@link NetworkSpecifier} subtype in the public SDK, which
   * tests should prefer when available.
   *
   * <p>Depending on the {@code specifier} string's content, the returned instance will have one of
   * several different meanings. See {@link
   * android.net.NetworkRequest.Builder#setNetworkSpecifier(String)} documentation for more detail.
   */
  public static NetworkSpecifier newStringNetworkSpecifier(String specifier) {
    return new StringNetworkSpecifier(specifier);
  }

  private NetworkSpecifierFactory() {}
}
