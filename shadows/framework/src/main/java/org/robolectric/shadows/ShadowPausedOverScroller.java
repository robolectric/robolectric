package org.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.OverScroller;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * The OverScroller shadow for {@link org.robolectric.annotation.LooperMode.Mode#PAUSED}.
 */
@Implements(value = OverScroller.class, isInAndroidSdk = false)
public class ShadowPausedOverScroller extends ShadowOverScroller {
}

