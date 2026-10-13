package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;

import org.robolectric.annotation.Implements;

@Implements(
    className = "android.content.res.StringBlock",
    isInAndroidSdk = false,
    minSdk = BAKLAVA,
    callNativeMethodsByDefault = true,
    shadowPicker = ShadowBaseStringBlock.Picker.class)
public class ShadowNativeStringBlock extends ShadowBaseStringBlock {}
