package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import org.robolectric.annotation.Implements;

@Implements(
    className = "android.content.res.StringBlock",
    isInAndroidSdk = false,
    minSdk = VANILLA_ICE_CREAM,
    callNativeMethodsByDefault = true,
    shadowPicker = ShadowBaseStringBlock.Picker.class)
public class ShadowNativeStringBlock extends ShadowBaseStringBlock {}
