package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import org.robolectric.annotation.Implements;

/** Shadow for {@link android.database.sqlite.SQLiteRawStatement}. */
@SuppressWarnings(
    "robolectric.internal.IgnoreMissingClass") // Remove when Robolectric compiles against V
@Implements(
    className = "android.database.sqlite.SQLiteRawStatement",
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true,
    minSdk = VANILLA_ICE_CREAM)
public class ShadowNativeSQLiteRawStatement {}
