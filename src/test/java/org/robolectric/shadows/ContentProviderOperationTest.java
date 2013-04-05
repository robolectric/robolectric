package org.robolectric.shadows;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.net.Uri;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ContentProviderOperationTest {

    @Test
    public void newInsert() {
        final Uri URI = Uri.parse("content://org.robolectric");
        Builder builder = ContentProviderOperation.newInsert(URI);
        builder.withValue("stringValue", "bar");
        builder.withValue("intValue", 5);
        ContentProviderOperation operation = builder.build();
        ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
        assertThat(operation.getUri()).isEqualTo(URI);
        assertThat(shadowOperation.isInsert()).isTrue();
        assertThat(shadowOperation.isUpdate()).isFalse();
        assertThat(shadowOperation.isDelete()).isFalse();
        assertThat(shadowOperation.getValues().size()).isEqualTo(2);
        assertThat(shadowOperation.getValues().get("stringValue").toString()).isEqualTo("bar");
        assertThat(Integer.parseInt(shadowOperation.getValues().get("intValue").toString())).isEqualTo(5);
    }
    
    @Test
    public void newInsertWithValueBackReference() {
        final Uri URI = Uri.parse("content://org.robolectric");
        Builder builder = ContentProviderOperation.newInsert(URI);
        builder.withValueBackReference("my_id", 0);
        ContentProviderOperation operation = builder.build();
        ShadowContentProviderOperation.ShadowBuilder shadowBuilder = Robolectric.shadowOf(builder);
        ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
        assertThat(shadowBuilder.getWithValueBackReference("my_id")).isEqualTo(0);
        assertThat(shadowOperation.getWithValueBackReference("my_id")).isEqualTo(0);
    }

    @Test
    public void newUpdate() {
        final Uri URI = Uri.parse("content://org.robolectric");
        Builder builder = ContentProviderOperation.newUpdate(URI);
        builder.withSelection("id_column", new String[] { "5" });
        ContentProviderOperation operation = builder.build();
        ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
        assertThat(operation.getUri()).isEqualTo(URI);
        assertThat(shadowOperation.isInsert()).isFalse();
        assertThat(shadowOperation.isUpdate()).isTrue();
        assertThat(shadowOperation.isDelete()).isFalse();
        assertThat(shadowOperation.getSelections().get("id_column")).isEqualTo(new String[]{"5"});
    }

    @Test
    public void newDelete() {
        final Uri URI = Uri.parse("content://org.robolectric");
        Builder builder = ContentProviderOperation.newDelete(URI);
        builder.withSelection("id_column", new String[] { "5" });
        ContentProviderOperation operation = builder.build();
        ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
        assertThat(operation.getUri()).isEqualTo(URI);
        assertThat(shadowOperation.isInsert()).isFalse();
        assertThat(shadowOperation.isUpdate()).isFalse();
        assertThat(shadowOperation.isDelete()).isTrue();
        assertThat(shadowOperation.getSelections().get("id_column")).isEqualTo(new String[]{"5"});
    }
}