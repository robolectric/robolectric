package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.media.session.MediaSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(MediaSession.class)
public class ShadowMediaSession {

  // Map underlying IPC binder objects to their host package name
  private static final Map<Object, String> binderToPackage = new ConcurrentHashMap<>();
  @RealObject private MediaSession realMediaSession;

  @Implementation
  protected void __constructor__(Context context, String tag) {
    reflector(MediaSessionReflector.class, realMediaSession).__constructor__(context, tag);
    if (context != null && realMediaSession.getSessionToken() != null) {
      Object binder =
          reflector(MediaSessionTokenReflector.class, realMediaSession.getSessionToken())
              .getBinder();
      if (binder != null) {
        binderToPackage.put(binder, context.getPackageName());
      }
    }
  }

  /**
   * Returns the host package name for the given {@link MediaSession.Token}, as tracked across IPC
   * boundaries during session initialization. Returns {@code null} if the token is null or was not
   * registered.
   */
  public static String getPackageNameForToken(MediaSession.Token token) {
    if (token == null) {
      return null;
    }
    Object binder = reflector(MediaSessionTokenReflector.class, token).getBinder();
    return binder == null ? null : binderToPackage.get(binder);
  }

  @Resetter
  public static void reset() {
    binderToPackage.clear();
  }

  @ForType(MediaSession.class)
  interface MediaSessionReflector {
    @Direct
    void __constructor__(Context context, String tag);
  }

  @ForType(MediaSession.Token.class)
  interface MediaSessionTokenReflector {
    @Accessor("mBinder")
    Object getBinder();
  }
}
