package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;

import android.os.Bundle;
import android.telecom.Connection;
import java.util.Optional;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link Connection} that represents a phone call or connection to a remote endpoint
 * that carries voice and/or video traffic.
 */
@Implements(value = Connection.class, minSdk = N_MR1)
public class ShadowConnection {
  private String mostRecentEvent;

  /** Records the event sent through sendConnectionEvent to be accessed later by tests. */
  @Implementation
  protected void sendConnectionEvent(String event, Bundle extras) {
    this.mostRecentEvent = event;
  }

  public Optional<String> getLastConnectionEvent() {
    return Optional.ofNullable(mostRecentEvent);
  }
}
