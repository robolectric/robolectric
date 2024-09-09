package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Bundle;
import android.telecom.Connection;
import java.util.Optional;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for {@link Connection} that represents a phone call or connection to a remote endpoint
 * that carries voice and/or video traffic.
 */
@Implements(value = Connection.class, minSdk = N_MR1)
public class ShadowConnection {

  @RealObject private Connection connection;
  private String mostRecentEvent;
  private boolean destroyed = false;

  /** Records the event sent through sendConnectionEvent to be accessed later by tests. */
  @Implementation
  protected void sendConnectionEvent(String event, Bundle extras) {
    this.mostRecentEvent = event;
  }

  @Implementation
  protected void destroy() {
    this.destroyed = true;

    reflector(ConnectionReflector.class, connection).destroy();
  }

  public Optional<String> getLastConnectionEvent() {
    return Optional.ofNullable(mostRecentEvent);
  }

  public boolean isDestroyed() {
    return destroyed;
  }

  @ForType(Connection.class)
  interface ConnectionReflector {
    @Direct
    void destroy();
  }
}
