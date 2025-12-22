package org.robolectric.shadows;

import android.media.MediaRoute2Info;
import android.media.RoutingSessionInfo;
import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Delegate for {@link android.media.IMediaRouterService} that provides a default implementation for
 * {@link android.media.IMediaRouterService#getSystemRoutes(String, boolean)} and {@link
 * android.media.IMediaRouterService#getSystemSessionInfo()}.
 */
public class MediaRouterServiceDelegate {

  private static final String SYSTEM_SESSION_ID = "SYSTEM_SESSION";
  private static final String DEFAULT_ROUTE_ID = "DEFAULT_ROUTE";
  private static final String SYSTEM_ROUTE_PROVIDER_ID =
      "com.android.server.media/.SystemMediaRoute2Provider";

  public List<MediaRoute2Info> getSystemRoutes(String callerPackageName, boolean isProxyRouter) {
    return getSystemRoutes();
  }

  public List<MediaRoute2Info> getSystemRoutes() {
    MediaRoute2Info defaultRoute =
        new MediaRoute2Info.Builder(DEFAULT_ROUTE_ID, "Built-in Speaker")
            .setDescription("Default Route")
            .setConnectionState(MediaRoute2Info.CONNECTION_STATE_CONNECTED)
            .setType(MediaRoute2Info.TYPE_BUILTIN_SPEAKER)
            .addFeature(MediaRoute2Info.FEATURE_LIVE_AUDIO)
            .setProviderId(SYSTEM_ROUTE_PROVIDER_ID)
            .setVolumeHandling(MediaRoute2Info.PLAYBACK_VOLUME_VARIABLE)
            .build();

    return ImmutableList.of(defaultRoute);
  }

  public RoutingSessionInfo getSystemSessionInfo() {
    return new RoutingSessionInfo.Builder(SYSTEM_SESSION_ID, "" /* clientPackageName */)
        .addSelectedRoute(DEFAULT_ROUTE_ID)
        .setProviderId(SYSTEM_ROUTE_PROVIDER_ID)
        .setVolumeHandling(MediaRoute2Info.PLAYBACK_VOLUME_VARIABLE)
        .setSystemSession(true)
        .build();
  }
}
