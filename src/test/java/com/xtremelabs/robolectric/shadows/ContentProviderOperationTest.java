package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
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
        builder.withValue("stringValue", "bar");
        builder.withValue("intValue", 5);
        ContentProviderOperation operation = builder.build();
        ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
        assertThat(operation.getUri(), equalTo(URI));
        assertThat(shadowOperation.isInsert(), is(true));
        assertThat(shadowOperation.isUpdate(), is(false));
        assertThat(shadowOperation.isDelete(), is(false));
        assertThat(shadowOperation.getValues().size(), is(2));
        assertThat(shadowOperation.getValues().get("stringValue").toString(), equalTo("bar"));
        assertThat(Integer.parseInt(shadowOperation.getValues().get("intValue").toString()), is(5));
    }
    
    @Test
    public void newUpdate() {
        Builder builder = ContentProviderOperation.newUpdate(URI);
        builder.withSelection("id_column", new String[] { "5" });
        ContentProviderOperation operation = builder.build();
        ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
        assertThat(operation.getUri(), equalTo(URI));
        assertThat(shadowOperation.isInsert(), is(false));
        assertThat(shadowOperation.isUpdate(), is(true));
        assertThat(shadowOperation.isDelete(), is(false));
        assertThat(shadowOperation.getSelections().get("id_column"), equalTo(new String[] { "5" }));
    }
    
    @Test
    public void newDelete() {
        Builder builder = ContentProviderOperation.newDelete(URI);
        builder.withSelection("id_column", new String[] { "5" });
        ContentProviderOperation operation = builder.build();
        ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
        assertThat(operation.getUri(), equalTo(URI));
        assertThat(shadowOperation.isInsert(), is(false));
        assertThat(shadowOperation.isUpdate(), is(false));
        assertThat(shadowOperation.isDelete(), is(true));
        assertThat(shadowOperation.getSelections().get("id_column"), equalTo(new String[] { "5" }));
    }
}