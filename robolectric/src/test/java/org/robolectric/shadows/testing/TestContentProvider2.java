package org.robolectric.shadows.testing;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import javax.annotation.Nonnull;

public class TestContentProvider2 extends ContentProvider {

  @Override
  public int delete(@Nonnull Uri arg0, String arg1, String[] arg2) {
    return 0;
  }

  @Override
  public String getType(@Nonnull Uri arg0) {
    return null;
  }

  @Override
  public Uri insert(@Nonnull Uri arg0, ContentValues arg1) {
    return null;
  }

  @Override
  public boolean onCreate() {
    return false;
  }

  @Override
  public Cursor query(@Nonnull Uri arg0, String[] arg1, String arg2, String[] arg3, String arg4) {
    return null;
  }

  @Override
  public int update(@Nonnull Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
    return 0;
  }
}
