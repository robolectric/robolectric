package org.robolectric.shadows;

import android.content.Context;
import android.media.session.MediaSession;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = MediaSession.class)
public class ShadowMediaSession {

  @Implementation
  protected void __constructor__(Context context, String tag) {}
}
