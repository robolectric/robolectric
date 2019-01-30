package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.content.Context;
import android.media.session.MediaSession;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = MediaSession.class, minSdk = LOLLIPOP)
public class ShadowMediaSession {

  @Implementation
  protected void __constructor__(Context context, String tag) {}
}
