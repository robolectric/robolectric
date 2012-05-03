package com.xtremelabs.robolectric.shadows;

import android.util.Base64;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import javax.xml.bind.DatatypeConverter;

@Implements(Base64.class)
public class ShadowBase64 {

    @Implementation
    public static String encodeToString(byte[] bytes, int flags) {
        return DatatypeConverter.printBase64Binary(bytes);
    }

    @Implementation
    public static byte[] decode(String str, int flags) {
        return DatatypeConverter.parseBase64Binary(str);
    }
}
