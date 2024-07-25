package org.robolectric.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.V;

@Implements(
    className = "android.content.res.StringBlock",
    isInAndroidSdk = false,
    minSdk = V.SDK_INT,
    callNativeMethodsByDefault = true,
    shadowPicker = ShadowBaseStringBlock.Picker.class)
public class ShadowNativeStringBlock extends ShadowBaseStringBlock {}
