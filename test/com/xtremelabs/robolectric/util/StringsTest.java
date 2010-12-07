package com.xtremelabs.robolectric.util;

import org.junit.Test;

import java.io.InputStream;
import java.io.StringBufferInputStream;

import static junit.framework.Assert.assertEquals;

public class StringsTest {
    @Test
    public void shouldGetStringFromStream() throws Exception {
        InputStream stream = new StringBufferInputStream("some random string");
        assertEquals("some random string", Strings.fromStream(stream));
    }
}
