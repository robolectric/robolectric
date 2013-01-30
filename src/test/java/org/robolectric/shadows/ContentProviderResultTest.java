package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.ContentProviderResult;
import android.net.Uri;

@RunWith(TestRunners.WithDefaults.class)
public class ContentProviderResultTest {
    @Test
    public void count() {
        ContentProviderResult result = new ContentProviderResult(5);
        assertThat(result.count, is(5));
    }
    
    @Test
    public void uri() {
        Uri uri = Uri.parse("content://com.xtremelabs.robolectric");
        ContentProviderResult result = new ContentProviderResult(uri);
        assertThat(result.uri, equalTo(uri));
    }
}