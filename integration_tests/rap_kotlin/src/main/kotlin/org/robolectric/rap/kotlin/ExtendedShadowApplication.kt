package org.robolectric.rap.kotlin

import android.app.Application
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.S
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowApplication

@Implements(value = Application::class, minSdk = P, maxSdk = S)
class ExtendedShadowApplication : ShadowApplication()
