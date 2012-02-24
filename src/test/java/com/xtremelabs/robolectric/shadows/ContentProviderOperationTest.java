package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.net.Uri;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class ContentProviderOperationTest {
    private static final Uri URI = Uri.parse("content://com.xtremelabs.robolectric");
    
    @Test
    public void newInsert() {
        Builder builder = ContentProviderOperation.newInsert(URI);
        assertThat(builder, notNullValue());
        // Verify setUri() call
        ShadowContentProviderOperationBuilder shadowBuilder = Robolectric.shadowOf(builder);
        assertThat(shadowBuilder.getUri(), equalTo(URI));
    }
    
    @Test
    public void newUpdate() {
        Builder builder = ContentProviderOperation.newUpdate(URI);
        assertThat(builder, notNullValue());
       // Verify setUri() call
        ShadowContentProviderOperationBuilder shadowBuilder = Robolectric.shadowOf(builder);
        assertThat(shadowBuilder.getUri(), equalTo(URI));
    }
    
    @Test
    public void newDelete() {
        Builder builder = ContentProviderOperation.newDelete(URI);
        assertThat(builder, notNullValue());
       // Verify setUri() call
        ShadowContentProviderOperationBuilder shadowBuilder = Robolectric.shadowOf(builder);
        assertThat(shadowBuilder.getUri(), equalTo(URI));
    }
}