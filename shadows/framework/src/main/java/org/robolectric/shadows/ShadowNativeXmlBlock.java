package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;

import org.robolectric.annotation.Implements;

@Implements(
    className = "android.content.res.XmlBlock",
    isInAndroidSdk = false,
    minSdk = BAKLAVA,
    callNativeMethodsByDefault = true,
    shadowPicker = ShadowBaseXmlBlock.Picker.class)
public class ShadowNativeXmlBlock extends ShadowBaseXmlBlock {}
