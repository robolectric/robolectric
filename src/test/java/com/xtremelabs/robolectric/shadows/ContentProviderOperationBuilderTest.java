package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import android.content.ContentValues;
import android.net.Uri;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.ContentProviderOperation;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class ContentProviderOperationBuilderTest {
    @Test
    public void withValue() {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Uri.EMPTY)
            .withValue("stringTest", "bar")
            .withValue("intTest", 5)
            .withValue("longTest", 10L);

        ContentValues values = Robolectric.shadowOf(builder).getValues();
        assertThat(values.size(), is(3));
        assertThat(values.get("stringTest").toString(), equalTo("bar"));
        assertThat(Integer.parseInt(values.get("intTest").toString()), equalTo(5));
        assertThat(Long.parseLong(values.get("longTest").toString()), equalTo(10L));
    }
    
    @Test
    public void withSelection() {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(Uri.EMPTY)
            .withSelection("a=? AND b=?", new String[]{"a", "b"});


        ShadowContentProviderOperationBuilder shadowBuilder = Robolectric.shadowOf(builder);
        assertEquals("a=? AND b=?", shadowBuilder.getSelection());
        assertEquals(2, shadowBuilder.getSelectionArgs().length);
        assertEquals("a", shadowBuilder.getSelectionArgs()[0]);
        assertEquals("b", shadowBuilder.getSelectionArgs()[1]);
    }
    
    @Test
    public void withValueBackReference() {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(Uri.EMPTY)
                .withSelection("a=? AND b=?", new String[]{"a", "b"});
        builder.withValueBackReference("foo", 5);

        ShadowContentProviderOperationBuilder shadowBuilder = Robolectric.shadowOf(builder);
        ContentValues values = shadowBuilder.getValuesBackReferences();
        assertEquals(5, values.get("foo"));
    }
    
    @Test
    public void build() {
        ContentProviderOperation operation = ContentProviderOperation.newUpdate(Uri.EMPTY)
                .withValue("foo", "bar")
                .build();
        assertThat(operation, notNullValue());
    }
}
