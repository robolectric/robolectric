package org.robolectric.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.V;

@Implements(
    className = "android.content.res.XmlBlock",
    isInAndroidSdk = false,
    minSdk = V.SDK_INT,
    callNativeMethodsByDefault = true,
    shadowPicker = ShadowBaseXmlBlock.Picker.class)
public class ShadowNativeXmlBlock extends ShadowBaseXmlBlock {}
