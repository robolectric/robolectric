package com.xtremelabs.droidsugar.fakes;

import android.util.Base64;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;


@SuppressWarnings({"UnusedDeclaration"})
@Implements(Base64.class)
public class FakeBase64 {

    @Implementation
    public static String encodeToString(byte[] input, int flags) {
        return new String(input) + "__fake_Base64_encode_string__" + flags;
    }
}
