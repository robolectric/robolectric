package com.xtremelabs.robolectric.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;

public class StringsTest {
    @Test
    public void shouldGetStringFromStream() throws Exception {
        InputStream stream = new ByteArrayInputStream("some random string".getBytes());
        assertEquals("some random string", Strings.fromStream(stream));
    }
}
