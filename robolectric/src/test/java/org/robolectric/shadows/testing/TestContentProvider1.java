package org.robolectric.shadows.testing;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class TestContentProvider1 extends ContentProvider {

  public final List<String> transcript = new ArrayList<>();

  @Override
  public boolean onCreate() {
    transcript.add("onCreate");
    return false;
  }

  @Override
  public void shutdown() {
    super.shutdown();
    transcript.add("shutdown");
  }

  @Override
  public Cursor query(
      @Nonnull Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder) {
    transcript.add("query for " + uri);
    return null;
  }

  @Override
  public String getType(@Nonnull Uri uri) {
    return null;
  }

  @Override
  public Uri insert(@Nonnull Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(@Nonnull Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(
      @Nonnull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }
}
