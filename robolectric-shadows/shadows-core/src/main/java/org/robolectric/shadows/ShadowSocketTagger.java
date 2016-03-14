package org.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.net.Socket;
import java.net.SocketException;

import dalvik.system.SocketTagger;

/**
 * Shadow for {@link dalvik.system.SocketTagger}.
 */
@Implements(value = SocketTagger.class, isInAndroidSdk = false)
public class ShadowSocketTagger {

  @Implementation
  public final void tag(Socket socket) throws SocketException {
  }

  @Implementation
  public final void untag(Socket socket) throws SocketException {
  }
}
