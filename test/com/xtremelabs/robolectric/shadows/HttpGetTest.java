package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(WithTestDefaultsRunner.class)
public class HttpGetTest {
    @Test
    public void shouldReturnMethod() throws Exception {
        assertEquals(HttpGet.METHOD_NAME, new HttpGet().getMethod());
    }

    @Test
    public void shouldConstructWithURI() throws Exception {
        URI uri = URI.create("www.example.com");
        assertSame(uri, new HttpGet(uri).getURI());
    }
    
    @Test
    public void shouldConstructWithString() throws Exception {
        assertEquals(URI.create("www.example.com"), new HttpGet("www.example.com").getURI());
    }
}
