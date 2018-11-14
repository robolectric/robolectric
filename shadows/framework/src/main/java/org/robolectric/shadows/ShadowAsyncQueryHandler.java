package org.robolectric.shadows;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow of {@link android.content.AsyncQueryHandler}, which calls methods synchronously. */
@Implements(AsyncQueryHandler.class)
public class ShadowAsyncQueryHandler {

  @RealObject private AsyncQueryHandler asyncQueryHandler;

  private ContentResolver contentResolver;

  @Implementation
  protected void __constructor__(ContentResolver contentResolver) {
    this.contentResolver = contentResolver;
  }

  @Implementation
  protected void startDelete(
      int token, Object cookie, Uri uri, String selection, String[] selectionArgs) {
    int rows = contentResolver.delete(uri, selection, selectionArgs);
    ReflectionHelpers.callInstanceMethod(
        asyncQueryHandler,
        "onDeleteComplete",
        new ClassParameter<>(int.class, token),
        new ClassParameter<>(Object.class, cookie),
        new ClassParameter<>(int.class, rows));
  }

  @Implementation
  protected void startInsert(int token, Object cookie, Uri uri, ContentValues initialValues) {
    Uri resultUri = contentResolver.insert(uri, initialValues);
    ReflectionHelpers.callInstanceMethod(
        asyncQueryHandler,
        "onInsertComplete",
        new ClassParameter<>(int.class, token),
        new ClassParameter<>(Object.class, cookie),
        new ClassParameter<>(Uri.class, resultUri));
  }

  @Implementation
  protected void startQuery(
      int token,
      Object cookie,
      Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String orderBy) {
    Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, orderBy);
    ReflectionHelpers.callInstanceMethod(
        asyncQueryHandler,
        "onQueryComplete",
        new ClassParameter<>(int.class, token),
        new ClassParameter<>(Object.class, cookie),
        new ClassParameter<>(Cursor.class, cursor));
  }

  @Implementation
  protected void startUpdate(
      int token,
      Object cookie,
      Uri uri,
      ContentValues values,
      String selection,
      String[] selectionArgs) {
    int rows = contentResolver.update(uri, values, selection, selectionArgs);
    ReflectionHelpers.callInstanceMethod(
        asyncQueryHandler,
        "onUpdateComplete",
        new ClassParameter<>(int.class, token),
        new ClassParameter<>(Object.class, cookie),
        new ClassParameter<>(int.class, rows));
  }

  @Implementation
  protected final void cancelOperation(int token) {}
}
