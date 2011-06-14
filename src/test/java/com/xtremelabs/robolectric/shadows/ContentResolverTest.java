package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ContentResolverTest {

    private ShadowContentResolver contentResolver;

    @Before
    public void setUp() throws Exception {
        contentResolver = new ShadowContentResolver();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWhenNoBytesSet_readThrowsException() throws Exception {
        contentResolver.openInputStream(null).read();
    }

    public void testWhenBytesSet_readWorks() throws Exception {
        String streamData = "Hello, I am a stream";
        byte[] sourceBytes = streamData.getBytes();
        contentResolver.setStreamData(sourceBytes);
        byte[] bytes = new byte[sourceBytes.length];
        contentResolver.openInputStream(null).read(bytes);
        assertEquals(sourceBytes, bytes);
    }
}
