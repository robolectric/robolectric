package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(CursorWrapper.class)
public class ShadowCursorWrapper {
  private Cursor wrappedCursor;

  @Implementation
  protected void __constructor__(Cursor c) {
    wrappedCursor = c;
  }

  @Implementation
  protected int getCount() {
    return wrappedCursor.getCount();
  }

  @Implementation
  protected int getPosition() {
    return wrappedCursor.getPosition();
  }

  @Implementation
  protected boolean move(int i) {
    return wrappedCursor.move(i);
  }

  @Implementation
  protected boolean moveToPosition(int i) {
    return wrappedCursor.moveToPosition(i);
  }

  @Implementation
  protected boolean moveToFirst() {
    return wrappedCursor.moveToFirst();
  }

  @Implementation
  protected boolean moveToLast() {
    return wrappedCursor.moveToLast();
  }

  @Implementation
  protected boolean moveToNext() {
    return wrappedCursor.moveToNext();
  }

  @Implementation
  protected boolean moveToPrevious() {
    return wrappedCursor.moveToPrevious();
  }

  @Implementation
  protected boolean isFirst() {
    return wrappedCursor.isFirst();
  }

  @Implementation
  protected boolean isLast() {
    return wrappedCursor.isLast();
  }

  @Implementation
  protected boolean isBeforeFirst() {
    return wrappedCursor.isBeforeFirst();
  }

  @Implementation
  protected boolean isAfterLast() {
    return wrappedCursor.isAfterLast();
  }

  @Implementation
  protected int getColumnIndex(String s) {
    return wrappedCursor.getColumnIndex(s);
  }

  @Implementation
  protected int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
    return wrappedCursor.getColumnIndexOrThrow(s);
  }

  @Implementation
  protected String getColumnName(int i) {
    return wrappedCursor.getColumnName(i);
  }

  @Implementation
  protected String[] getColumnNames() {
    return wrappedCursor.getColumnNames();
  }

  @Implementation
  protected int getColumnCount() {
    return wrappedCursor.getColumnCount();
  }

  @Implementation
  protected byte[] getBlob(int i) {
    return wrappedCursor.getBlob(i);
  }

  @Implementation
  protected String getString(int i) {
    return wrappedCursor.getString(i);
  }

  @Implementation
  protected void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {
    wrappedCursor.copyStringToBuffer(i, charArrayBuffer);
  }

  @Implementation
  protected short getShort(int i) {
    return wrappedCursor.getShort(i);
  }

  @Implementation
  protected int getInt(int i) {
    return wrappedCursor.getInt(i);
  }

  @Implementation
  protected long getLong(int i) {
    return wrappedCursor.getLong(i);
  }

  @Implementation
  protected float getFloat(int i) {
    return wrappedCursor.getFloat(i);
  }

  @Implementation
  protected double getDouble(int i) {
    return wrappedCursor.getDouble(i);
  }

  @Implementation
  protected boolean isNull(int i) {
    return wrappedCursor.isNull(i);
  }

  @Implementation
  protected void deactivate() {
    wrappedCursor.deactivate();
  }

  @Implementation
  protected boolean requery() {
    return wrappedCursor.requery();
  }

  @Implementation
  protected void close() {
    wrappedCursor.close();
  }

  @Implementation
  protected boolean isClosed() {
    return wrappedCursor.isClosed();
  }

  @Implementation
  protected void registerContentObserver(ContentObserver contentObserver) {
    wrappedCursor.registerContentObserver(contentObserver);
  }

  @Implementation
  protected void unregisterContentObserver(ContentObserver contentObserver) {
    wrappedCursor.unregisterContentObserver(contentObserver);
  }

  @Implementation
  protected void registerDataSetObserver(DataSetObserver dataSetObserver) {
    wrappedCursor.registerDataSetObserver(dataSetObserver);
  }

  @Implementation
  protected void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    wrappedCursor.unregisterDataSetObserver(dataSetObserver);
  }

  @Implementation
  protected void setNotificationUri(ContentResolver contentResolver, Uri uri) {
    wrappedCursor.setNotificationUri(contentResolver, uri);
  }

  @Implementation(minSdk = KITKAT)
  protected Uri getNotificationUri() {
    return wrappedCursor.getNotificationUri();
  }

  @Implementation
  protected boolean getWantsAllOnMoveCalls() {
    return wrappedCursor.getWantsAllOnMoveCalls();
  }

  @Implementation(minSdk = M)
  protected void setExtras(Bundle extras) {
    wrappedCursor.setExtras(extras);
  }

  @Implementation
  protected Bundle getExtras() {
    return wrappedCursor.getExtras();
  }

  @Implementation
  protected Bundle respond(Bundle bundle) {
    return wrappedCursor.respond(bundle);
  }

  @Implementation
  protected int getType(int columnIndex) {
    return wrappedCursor.getType(columnIndex);
  }

  @Implementation
  protected Cursor getWrappedCursor() {
    return wrappedCursor;
  }
}
