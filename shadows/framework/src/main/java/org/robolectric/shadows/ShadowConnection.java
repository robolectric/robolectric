package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;

import android.os.Bundle;
import android.telecom.Connection;
import java.util.Optional;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link Connection} that represents a phone call or connection to a remote endpoint
 * that carries voice and/or video traffic.
 */
@Implements(value = Connection.class, minSdk = N_MR1)
public class ShadowConnection {

  private String mostRecentEvent;
  private boolean destroyed = false;

  /** Records the event sent through sendConnectionEvent to be accessed later by tests. */
  @Filter
  protected void sendConnectionEvent(String event, Bundle extras) {
    this.mostRecentEvent = event;
  }

  @Filter
  protected void destroy() {
    this.destroyed = true;
  }

  public Optional<String> getLastConnectionEvent() {
    return Optional.ofNullable(mostRecentEvent);
  }

  public boolean isDestroyed() {
    return destroyed;
  }
}
