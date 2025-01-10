package org.robolectric;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import javax.annotation.Nonnull;

public class CustomConstructorContentProvider extends ContentProvider {
  private final int intValue;

  public CustomConstructorContentProvider(int intValue) {
    this.intValue = intValue;
  }

  public int getIntValue() {
    return intValue;
  }

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(@Nonnull Uri uri, String[] strings, String s, String[] strings1, String s1) {
    return null;
  }

  @Override
  public String getType(@Nonnull Uri uri) {
    return null;
  }

  @Override
  public Uri insert(@Nonnull Uri uri, ContentValues contentValues) {
    return null;
  }

  @Override
  public int delete(@Nonnull Uri uri, String s, String[] strings) {
    return 0;
  }

  @Override
  public int update(@Nonnull Uri uri, ContentValues contentValues, String s, String[] strings) {
    return 0;
  }
}
