package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.net.Uri;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ContentResolverTest {

    private ContentResolver contentResolver;


    @Before
    public void setUp() throws Exception {
        contentResolver = Robolectric.application.getContentResolver();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void whenNullUri_readThrowsException() throws Exception {
        contentResolver.openInputStream(null).read();
    }

    @Test
    public void whenBytesSet_readWorks() throws Exception {
        String streamData = "Hello, I am a stream";
        byte[] sourceBytes = streamData.getBytes();
        shadowOf(contentResolver).setStreamData(sourceBytes);
        byte[] bytes = new byte[sourceBytes.length];
        contentResolver.openInputStream(null).read(bytes);
        assertArrayEquals(sourceBytes, bytes);
    }

    @Test
    public void androidResources_canBeLoaded() throws Exception {
        Uri url = Uri.parse("android.resource://com.xtremelabs.robolectric/" + R.raw.raw_resource);
        assertEquals("raw txt file contents", TestUtil.readString(contentResolver.openInputStream(url)));
    }
}
