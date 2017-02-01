package org.robolectric.shadows;

import org.robolectric.annotation.Implements;

@Implements(className = "android.app.SystemServiceRegistry", isInAndroidSdk = false)
public class ShadowSystemServiceRegistry {
}
