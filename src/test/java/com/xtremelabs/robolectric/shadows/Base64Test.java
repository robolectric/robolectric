package com.xtremelabs.robolectric.shadows;

import android.util.Base64;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

@RunWith(WithTestDefaultsRunner.class)
public class Base64Test {

    private static final String base64Encoded = "YmFzZTY0IGVuY29kZWQgc3RyaW5n";
    private static final String base64Decoded = "base64 encoded string";

    @Test
    public void testEncodeToString() {
        String result = Base64.encodeToString(base64Decoded.getBytes(), Base64.DEFAULT);

        assertEquals(result, base64Encoded);
    }

    @Test
    public void testDecode() {
        byte[] result = Base64.decode(base64Encoded, Base64.DEFAULT);

        assertArrayEquals(result, base64Decoded.getBytes());
    }
}
