package org.robolectric.shadows;

import android.util.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.io.UnsupportedEncodingException;

import static android.util.Base64.DEFAULT;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class Base64Test {

    @Test
    public void testEncode() throws UnsupportedEncodingException {
        String inputString = "Some nice String";
        String encodedString = Base64.encodeToString(inputString.getBytes("UTF-8"), DEFAULT);
        assertThat(encodedString).isEqualTo("U29tZSBuaWNlIFN0cmluZw==\n");
    }

    @Test
    public void testDecode() throws Exception {
        byte[] decodedBytes = Base64.decode("U29tZSBuaWNlIFN0cmluZw==", DEFAULT);
        assertThat(new String(decodedBytes)).isEqualTo("Some nice String");
    }
}
