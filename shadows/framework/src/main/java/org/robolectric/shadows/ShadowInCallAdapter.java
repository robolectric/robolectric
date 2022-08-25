package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION_CODES;
import android.telecom.CallAudioState;
import android.telecom.InCallAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.telecom.InCallAdapter}. */
@Implements(value = InCallAdapter.class, isInAndroidSdk = false)
public class ShadowInCallAdapter {

  @RealObject private InCallAdapter inCallAdapter;

  private int audioRoute = CallAudioState.ROUTE_EARPIECE;

  @Implementation(minSdk = VERSION_CODES.LOLLIPOP)
  protected void setAudioRoute(int route) {
    audioRoute = route;
    if (isInternalInCallAdapterSet()) {
      reflector(ReflectorInCallAdapter.class, inCallAdapter).setAudioRoute(route);
    }
  }

  /** Returns audioRoute set by setAudioRoute. Defaults to CallAudioState.ROUTE_EARPIECE. */
  public int getAudioRoute() {
    return audioRoute;
  }

  /**
   * Checks if the InCallService was bound using {@link
   * com.android.internal.telecom.IInCallService#setInCallAdapter(com.android.internal.telecom.IInCallAdapter)}.
   * ;
   *
   * <p>If it was bound using this interface, the internal InCallAdapter will be set and it will
   * forward invocations to FakeTelecomServer.
   *
   * <p>Otherwise, invoking these methods will yield NullPointerExceptions, so we will avoid
   * forwarding the calls to the real objects.
   */
  private boolean isInternalInCallAdapterSet() {
    Object internalAdapter =
        reflector(ReflectorInCallAdapter.class, inCallAdapter).getInternalInCallAdapter();
    return internalAdapter != null;
  }

  @ForType(InCallAdapter.class)
  interface ReflectorInCallAdapter {
    @Direct
    void setAudioRoute(int route);

    @Accessor("mAdapter")
    Object getInternalInCallAdapter();
  }
}
