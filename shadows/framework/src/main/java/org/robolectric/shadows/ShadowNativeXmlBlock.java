package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import org.robolectric.annotation.Implements;

@Implements(
    className = "android.content.res.XmlBlock",
    isInAndroidSdk = false,
    minSdk = VANILLA_ICE_CREAM,
    callNativeMethodsByDefault = true,
    shadowPicker = ShadowBaseXmlBlock.Picker.class)
public class ShadowNativeXmlBlock extends ShadowBaseXmlBlock {}
