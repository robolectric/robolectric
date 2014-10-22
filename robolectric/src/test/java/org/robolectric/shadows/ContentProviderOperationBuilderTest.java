package org.robolectric.shadows;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

@RunWith(TestRunners.WithDefaults.class)
public class ContentProviderOperationBuilderTest {
  private Builder builder;

  @Test
  public void build() throws Exception {
    Uri uri = Uri.parse("content://authority/path");

    builder = ContentProviderOperation.newUpdate(uri);
    builder.withSelection("a=?", new String[] {"a"});
    builder.withValue("k1", "v1");
    ContentValues cv = new ContentValues();
    cv.put("k2", "v2");
    builder.withValues(cv);
    ContentProviderOperation op = builder.build();

    assertThat(op).isNotNull();
    assertThat(op.getUri()).isEqualTo(uri);

    final ContentRequest request = new ContentRequest();
    ContentProvider provider = new ContentProvider() {
      @Override
      public boolean onCreate() {
        return true;
      }

      @Override
      public Cursor query(Uri uri, String[] projection, String selection,
          String[] selectionArgs, String sortOrder) {
        return null;
      }

      @Override
      public String getType(Uri uri) {
        return null;
      }

      @Override
      public Uri insert(Uri uri, ContentValues values) {
        return null;
      }

      @Override
      public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
      }

      @Override
      public int update(Uri uri, ContentValues values, String selection,
          String[] selectionArgs) {
        request.uri = uri;
        request.values = values;
        request.selection = selection;
        request.selectionArgs = selectionArgs;
        return 0;
      }

    };

    op.apply(provider, null, 0);

    assertThat(request.uri).isEqualTo(uri);
    assertThat(request.selection).isEqualTo("a=?");
    assertThat(request.selectionArgs).isEqualTo(new String[] {"a"});

    assertThat(request.values.containsKey("k1")).isTrue();
    assertThat(request.values.containsKey("k2")).isTrue();

  }

  static class ContentRequest {
    Uri uri;
    String selection;
    String[] selectionArgs;
    ContentValues values;
  }

}