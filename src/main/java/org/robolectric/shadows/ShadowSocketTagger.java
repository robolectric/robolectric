package org.robolectric.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.net.Socket;
import java.net.SocketException;

@Implements(value = Robolectric.Anything.class, className = ShadowSocketTagger.REAL_CLASS_NAME)
public class ShadowSocketTagger {
  public static final String REAL_CLASS_NAME = "dalvik.system.SocketTagger";

  @Implementation
  public final void untag(Socket socket) throws SocketException { }
}
